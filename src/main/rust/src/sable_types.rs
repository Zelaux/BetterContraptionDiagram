#![allow(unused)]

use dashmap::DashMap;
use fern::colors::{Color, ColoredLevelConfig};
use log::info;
use rayon::iter::ParallelIterator;
use rayon::prelude::{IntoParallelRefIterator, ParallelExtend};
use std::cell::RefCell;
use std::collections::HashMap;
use std::f32::consts::PI;
use std::sync::atomic::{AtomicUsize, Ordering};
use std::sync::{Arc, OnceLock, RwLock};

// --- ОСНОВНЫЕ ТИПЫ И КОНСТАНТЫ ---

pub type Real = f32;

/// log_2 of the size of a chunk
pub const CHUNK_SHIFT: u8 = 4;
/// The size of a chunk in blocks
pub const CHUNK_SIZE: u8 = 1 << CHUNK_SHIFT;
/// Bitwise mask to apply to a block coordinate to get the chunk-relative coordinate of the block
pub const CHUNK_MASK: i32 = (CHUNK_SIZE - 1) as i32;

pub const OCTREE_CHUNK_SHIFT: i32 = 6;
pub const OCTREE_CHUNK_SIZE: i32 = 1 << OCTREE_CHUNK_SHIFT;

pub const NEEDS_HOOKS_USER_DATA: u32 = 1;
pub const NO_HOOKS_USER_DATA: u32 = 0;

pub const DEFAULT_COLLISION_PARALLEL_CUTOFF: usize = 256;
pub const JOINT_SPRING_FREQUENCY: Real = 550.0;
pub const JOINT_SPRING_DAMPING_RATIO: Real = 4.0;
const WORLD_SIZE: Real = 30_000_000.0;

use rapier3d::dynamics::{
    CCDSolver, ImpulseJointHandle, ImpulseJointSet, IslandManager, MultibodyJointSet,
    RigidBodyHandle, RigidBodySet, RigidBodyVelocity, RigidBodyBuilder, RigidBody,
    MassProperties, IntegrationParameters, JointAxis, JointAxesMask, SpringCoefficients,
};
use rapier3d::geometry::{
    ColliderHandle, ColliderSet, DefaultBroadPhase, NarrowPhase, Shape, ShapeType,
    SharedShape, TypedShape, InteractionGroups, InteractionTestMode, Group,
    ContactManifold, ContactManifoldData, ColliderBuilder,
};
use rapier3d::glamx::{DVec3, IVec3, Vec3, Quat, Mat3, Pose3};
use rapier3d::parry::bounding_volume::{Aabb, BoundingSphere};
use rapier3d::parry::query::{
    Contact, ContactManifoldsWorkspace, QueryDispatcher, DefaultQueryDispatcher,
    PersistentQueryDispatcher, RayCast, PointQuery, ClosestPoints, ShapeCastHit,
    ShapeCastOptions, NonlinearRigidMotion, Unsupported,
};
use rapier3d::parry::query::details::{NormalConstraints, contact_manifold_cuboid_cuboid_shapes};
use rapier3d::pipeline::{PhysicsPipeline, EventHandler, PhysicsHooks, ContactModificationContext};
use rapier3d::pipeline::{ActiveEvents, ActiveHooks};
use rapier3d::parry::query::ContactManifold as GenericContactManifold;

// --- VOXEL / LEVEL STRUCTURES ---

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum VoxelPhysicsState {
    Empty,
    Face,
    Edge,
    Corner,
    Interior,
}

pub const ALL_VOXEL_PHYSICS_STATES: [VoxelPhysicsState; 5] = [
    VoxelPhysicsState::Empty,
    VoxelPhysicsState::Face,
    VoxelPhysicsState::Edge,
    VoxelPhysicsState::Corner,
    VoxelPhysicsState::Interior,
];

pub type BlockState = (u32, VoxelPhysicsState);

#[derive(Debug)]
pub struct ChunkSection {
    blocks: Vec<BlockState>,
}

impl ChunkSection {
    pub fn new(blocks: Vec<BlockState>) -> Self {
        if blocks.len() != (CHUNK_SIZE as usize).pow(3) {
            panic!("Invalid block count: {}", blocks.len());
        }
        Self { blocks }
    }

    #[inline(always)]
    fn get_index(&self, x: i32, y: i32, z: i32) -> usize {
        (x + (z << 4) + (y << 8)) as usize
    }

    pub fn set_block(&mut self, x: i32, y: i32, z: i32, state: BlockState) {
        let index = self.get_index(x, y, z);
        self.blocks[index] = state;
    }

    pub fn get_block(&self, x: i32, y: i32, z: i32) -> BlockState {
        let index = self.get_index(x, y, z);
        unsafe { *self.blocks.get_unchecked(index) }
    }
}

pub struct VoxelColliderData {
    pub collision_boxes: Vec<(f32, f32, f32, f32, f32, f32)>,
    pub is_fluid: bool,
    pub friction: f32,
    pub volume: f32,
    pub restitution: f32,
    pub dynamic: bool,
    // JNI Hooks removed
}

impl VoxelColliderData {
    pub fn get_user_data(&self) -> u32 {
        if self.friction != 1.0 || self.restitution != 0.0 {
            NEEDS_HOOKS_USER_DATA
        } else {
            NO_HOOKS_USER_DATA
        }
    }

    pub fn needs_hooks(data: u32) -> bool {
        data & NEEDS_HOOKS_USER_DATA > 0
    }
}

pub struct OctreeChunkSection {
    pub octree: SubLevelOctree,
    pub liquid_octree: SubLevelOctree,
}

impl OctreeChunkSection {
    pub fn new() -> Self {
        Self {
            octree: SubLevelOctree::new(OCTREE_CHUNK_SHIFT),
            liquid_octree: SubLevelOctree::new(OCTREE_CHUNK_SHIFT),
        }
    }
}

// --- OCTREE ---

#[derive(Debug)]
pub struct SubLevelOctree {
    pub log_size: i32,
    pub buffer: Vec<i32>,
    size: i32,
    free_space_index_head: i32,
}

impl SubLevelOctree {
    pub fn new(log_size: i32) -> Self {
        Self {
            log_size,
            buffer: vec![0; 256],
            size: 1,
            free_space_index_head: -1,
        }
    }

    #[inline(always)]
    fn get_octant_index(x: i32, y: i32, z: i32) -> i32 {
        (x & 1) | ((y & 1) << 1) | ((z & 1) << 2)
    }

    fn allocate_branch(&mut self) -> i32 {
        if self.free_space_index_head != -1 {
            let index = self.free_space_index_head - 1;
            self.free_space_index_head = self.buffer[index as usize];
            return index;
        }
        let index = self.size;
        if self.size + 8 > self.buffer.len() as i32 {
            self.buffer.resize((self.buffer.len() as f32 * 1.5) as usize, 0);
        }
        self.size += 8;
        index
    }

    fn split(&mut self, index: i32) {
        let node = self.buffer[index as usize];
        let branch_start_index = self.allocate_branch();
        self.buffer[index as usize] = branch_start_index;
        for i in 0..8 {
            self.buffer[(branch_start_index + i) as usize] = node;
        }
    }

    pub fn merge(&mut self, index: i32) {
        let branch_start_index = self.buffer[index as usize];
        if branch_start_index <= 0 { return; }
        let node = self.buffer[branch_start_index as usize];
        for i in 1..8 {
            if self.buffer[(branch_start_index + i) as usize] != node { return; }
        }
        if node == 0 {
            self.buffer[index as usize] = node;
            self.delete_children(branch_start_index);
            self.free_space_index_head = branch_start_index + 1;
        }
    }

    fn delete_children(&mut self, branch_start_index: i32) {
        self.buffer[branch_start_index as usize] = self.free_space_index_head;
        self.free_space_index_head = branch_start_index;
        for i in 1..8 {
            self.buffer[(branch_start_index + i) as usize] = 0;
        }
    }

    pub fn insert(&mut self, x: i32, y: i32, z: i32, block: i32) -> bool {
        let mut shift = self.log_size - 1;
        let mut index = 0;
        let mut node = self.buffer[index as usize];
        let mut branch_index = 0;
        let mut branches_visited = vec![0; self.log_size as usize];

        while shift >= 0 {
            if node == self.block_id_to_node(block) { return false; }
            let octant_index = Self::get_octant_index(x >> shift, y >> shift, z >> shift);
            if node > 0 {
                branches_visited[branch_index as usize] = index;
                branch_index += 1;
            } else {
                self.split(index);
            }
            let branch_start_index = self.buffer[index as usize];
            index = branch_start_index + octant_index;
            node = self.buffer[index as usize];
            shift -= 1;
        }
        self.buffer[index as usize] = self.block_id_to_node(block);
        for i in (0..self.log_size).rev() {
            self.merge(branches_visited[i as usize]);
        }
        true
    }

    pub fn query(&self, x: i32, y: i32, z: i32, log_size_of_target: i32) -> i32 {
        let size = 1 << self.log_size;
        if x < 0 || y < 0 || z < 0 || x >= size || y >= size || z >= size { return -2; }
        let mut shift = self.log_size - 1;
        let mut index = 0;
        let mut node: i32 = self.buffer[index as usize];
        while shift >= log_size_of_target {
            if node < 0 { return self.node_to_block_id(node); }
            else if node == 0 { return -2; }
            let octant_index = Self::get_octant_index(x >> shift, y >> shift, z >> shift);
            index = node + octant_index;
            node = self.buffer[index as usize];
            shift -= 1;
        }
        match node {
            ..0 => self.node_to_block_id(node),
            0 => -2,
            _ => -1,
        }
    }

    fn block_id_to_node(&self, block: i32) -> i32 { -block - 1 }
    #[inline(always)]
    fn node_to_block_id(&self, node: i32) -> i32 { -node - 1 }
    pub fn is_empty(&self) -> bool { self.buffer[0] == 0 }
}

// --- SCENE DATA & CORE ---

pub type LevelColliderID = usize;
pub type ChunkMap = HashMap<i64, ChunkSection>;

pub trait ChunkAccess {
    fn get_chunk_mut(&mut self, x: i32, y: i32, z: i32) -> Option<&mut ChunkSection>;
    fn get_chunk(&self, x: i32, y: i32, z: i32) -> Option<&ChunkSection>;
}

#[inline(always)]
pub fn pack_section_pos(i: i32, j: i32, k: i32) -> i64 {
    let mut l: i64 = 0;
    l |= (i as i64 & 4194303i64) << 42;
    l |= (j as i64 & 1048575i64);
    l | (k as i64 & 4194303i64) << 20
}

#[derive(Debug)]
pub struct ActiveLevelColliderInfo {
    pub collider: ColliderHandle,
    pub static_mount: Option<RigidBodyHandle>,
    pub fake_velocities: Option<RigidBodyVelocity<Real>>,
    pub local_bounds_min: Option<IVec3>,
    pub local_bounds_max: Option<IVec3>,
    pub center_of_mass: Option<DVec3>,
    pub octree: Option<SubLevelOctree>,
    pub chunk_map: Option<ChunkMap>,
}

impl ChunkAccess for ActiveLevelColliderInfo {
    fn get_chunk_mut(&mut self, x: i32, y: i32, z: i32) -> Option<&mut ChunkSection> {
        self.chunk_map.as_mut()?.get_mut(&pack_section_pos(x, y, z))
    }
    fn get_chunk(&self, x: i32, y: i32, z: i32) -> Option<&ChunkSection> {
        self.chunk_map.as_ref()?.get(&pack_section_pos(x, y, z))
    }
}

pub struct SimulationSceneData {
    pub pipeline: PhysicsPipeline,
    pub rigid_body_set: RigidBodySet,
    pub collider_set: ColliderSet,
    pub island_manager: IslandManager,
    pub broad_phase: DefaultBroadPhase,
    pub narrow_phase: NarrowPhase,
    pub impulse_joint_set: ImpulseJointSet,
    pub multibody_joint_set: MultibodyJointSet,
    pub ccd_solver: CCDSolver,
    pub physics_hooks: SablePhysicsHooks,
    pub event_handler: SableEventHandler,
}

pub struct SableSceneData {
    pub main_level_chunks: ChunkMap,
    pub octree_chunks: HashMap<i64, OctreeChunkSection>,
    pub joint_set: SableJointSet,
    pub rope_map: RopeMap,
    pub level_colliders: HashMap<LevelColliderID, ActiveLevelColliderInfo>,
    pub rigid_bodies: HashMap<LevelColliderID, RigidBodyHandle>,
}

#[repr(C)]
pub struct PhysicsScene {
    pub sim_data: RwLock<SimulationSceneData>,
    pub sable_data: Arc<RwLock<SableSceneData>>,
    pub reported_collisions: Arc<ReportedCollisionBuffer>,
    pub manifold_info_map: Arc<SableManifoldInfoMap>,
    pub ground_handle: Option<RigidBodyHandle>,
    pub gravity: Vec3,
    pub universal_drag: Real,
}

pub struct PhysicsState {
    pub integration_parameters: IntegrationParameters,
    pub voxel_collider_map: VoxelColliderMap,
}

pub static PHYSICS_STATE: OnceLock<RwLock<PhysicsState>> = OnceLock::new();

pub fn get_physics_state() -> std::sync::RwLockReadGuard<'static, PhysicsState> {
    PHYSICS_STATE.get().expect("No physics state!").read().unwrap()
}

// --- COLLIDER IMPLEMENTATIONS ---

#[derive(Debug, Clone, Copy)]
pub struct LevelCollider {
    pub id: Option<LevelColliderID>,
    pub is_static: bool,
    pub cached_aabb: Option<Aabb>,
}

impl LevelCollider {
    pub fn new(id: Option<LevelColliderID>, is_static: bool) -> Self {
        Self { id, is_static, cached_aabb: None }
    }
}

impl Shape for LevelCollider {
    fn compute_local_aabb(&self) -> Aabb {
        if self.is_static {
            Aabb::new(Vec3::splat(-WORLD_SIZE), Vec3::splat(WORLD_SIZE))
        } else {
            self.cached_aabb.expect("AABB not set")
        }
    }
    fn compute_local_bounding_sphere(&self) -> BoundingSphere {
        BoundingSphere::new(Vec3::ZERO, if self.is_static { WORLD_SIZE } else { 1.0 })
    }
    fn clone_dyn(&self) -> Box<dyn Shape> { Box::new(*self) }
    fn mass_properties(&self, _density: Real) -> MassProperties { MassProperties::default() }
    fn shape_type(&self) -> ShapeType { ShapeType::Custom }
    fn as_typed_shape(&self) -> TypedShape<'_> { TypedShape::Custom(self) }
    fn ccd_thickness(&self) -> Real { 0.25 }
    fn ccd_angular_thickness(&self) -> Real { PI / 8.0 }
     fn scale_dyn(&self, _scale: Vec3, _num_subdivisions: u32) -> Option<Box<dyn Shape>> {
            // Если масштабирование не нужно, возвращаем None или копию с обновленным AABB
            Some(Box::new(self.clone()))
        }
}

impl RayCast for LevelCollider {
    fn cast_local_ray_and_get_normal(&self, _: &rapier3d::parry::query::Ray, _: Real, _: bool) -> Option<rapier3d::parry::query::RayIntersection> { None }
}
impl PointQuery for LevelCollider {
    fn project_local_point(&self, _: Vec3, _: bool) -> rapier3d::parry::query::PointProjection { todo!() }
    fn project_local_point_and_get_feature(&self, _: Vec3) -> (rapier3d::parry::query::PointProjection, rapier3d::geometry::FeatureId) { todo!() }
}

// --- COLLISION ALGORITHMS ---

pub fn find_collision_pairs(
    sable_body: &ActiveLevelColliderInfo,
    other_sable_body: Option<&ActiveLevelColliderInfo>,
    isometry: &Pose3,
    prediction: Real,
    cutoff: usize,
    liquid: bool,
    sable_data: &SableSceneData,
) -> Vec<(IVec3, IVec3)> {
    struct StackObject { index: u32, depth: u32, min: IVec3 }
    let octree = sable_body.octree.as_ref().expect("No octree!");
    let local_bounds_min = sable_body.local_bounds_min.unwrap();
    let center_of_mass = sable_body.center_of_mass.unwrap();

    let offset = (local_bounds_min.as_dvec3() - center_of_mass).as_vec3();
    let translation = isometry.translation + isometry.rotation.mul_vec3(offset);

    let mut current_level = vec![StackObject { index: 0, depth: 0, min: IVec3::ZERO }];
    let mut pairs = Vec::with_capacity(16);
    let com_offset = other_sable_body.map_or(DVec3::ZERO, |h| h.center_of_mass.unwrap());

    while !current_level.is_empty() {
        let do_level_parallel = current_level.len() >= cutoff;
        let mut next_level = Vec::new();

        for entry in &current_level {
            let node = octree.buffer[entry.index as usize];
            let node_size = 1 << (octree.log_size as u32 - entry.depth);
            let node_center = entry.min.as_vec3() + node_size as f32 / 2.0;
            let transformed_center = isometry.rotation.mul_vec3(node_center) + translation;
            let radius = node_size as Real / 2.0 * 1.7321 + prediction;

            let (hit, blocks) = get_overlapping_nodes(other_sable_body, com_offset, transformed_center, radius, sable_data, node >= 0, liquid);
            if !hit { continue; }

            if node < 0 {
                for static_block in blocks.unwrap() {
                    pairs.push((static_block, entry.min + local_bounds_min));
                }
            } else if node > 0 {
                for i in 0..8 {
                    next_level.push(StackObject {
                        index: (node + i) as u32,
                        depth: entry.depth + 1,
                        min: entry.min + IVec3::new((i & 1) * node_size / 2, ((i >> 1) & 1) * node_size / 2, ((i >> 2) & 1) * node_size / 2),
                    });
                }
            }
        }
        current_level = next_level;
    }
    pairs
}

fn get_overlapping_nodes(
    other_handle: Option<&ActiveLevelColliderInfo>,
    com_offset: DVec3,
    pos: Vec3,
    dist: Real,
    sable_data: &SableSceneData,
    cancel_early: bool,
    liquid: bool,
) -> (bool, Option<Vec<IVec3>>) {
    let log2 = (dist * 2.0).ln() / 2.0f32.ln();
    let log2 = (log2.floor() as i32).min(OCTREE_CHUNK_SHIFT);
    let min_block_pos = ((pos - dist).as_dvec3() + com_offset).floor().as_ivec3();
    let max_block_pos = ((pos + dist).as_dvec3() + com_offset).floor().as_ivec3();

    if let Some(other) = other_handle {
        let other_oct = other.octree.as_ref().unwrap();
        let other_min = other.local_bounds_min.unwrap();
        let mut blocks = (!cancel_early).then(Vec::new);

        for x in (min_block_pos.x - other_min.x >> log2)..=(max_block_pos.x - other_min.x >> log2) {
            for y in (min_block_pos.y - other_min.y >> log2)..=(max_block_pos.y - other_min.y >> log2) {
                for z in (min_block_pos.z - other_min.z >> log2)..=(max_block_pos.z - other_min.z >> log2) {
                    if other_oct.query(x << log2, y << log2, z << log2, log2) > -2 {
                        if cancel_early { return (true, None); }
                        blocks.as_mut().unwrap().push(IVec3::new(x << log2, y << log2, z << log2) + other_min);
                    }
                }
            }
        }
        return (!blocks.as_ref().map_or(true, |v| v.is_empty()), blocks);
    }

    let mut blocks = (!cancel_early).then(Vec::new);
    for ox in (min_block_pos.x >> 6)..=(max_block_pos.x >> 6) {
        for oy in (min_block_pos.y >> 6)..=(max_block_pos.y >> 6) {
            for oz in (min_block_pos.z >> 6)..=(max_block_pos.z >> 6) {
                if let Some(chunk) = sable_data.octree_chunks.get(&pack_section_pos(ox, oy, oz)) {
                    let oct = if liquid { &chunk.liquid_octree } else { &chunk.octree };
                    if oct.is_empty() { continue; }
                    for x in (min_block_pos.x >> log2)..=(max_block_pos.x >> log2) {
                        for y in (min_block_pos.y >> log2)..=(max_block_pos.y >> log2) {
                            for z in (min_block_pos.z >> log2)..=(max_block_pos.z >> log2) {
                                if oct.query((x << log2) - (ox << 6), (y << log2) - (oy << 6), (z << log2) - (oz << 6), log2) > -2 {
                                    if cancel_early { return (true, None); }
                                    blocks.as_mut().unwrap().push(IVec3::new(x, y, z) << log2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    (blocks.as_ref().map_or(false, |v| !v.is_empty()), blocks)
}

// --- BUOYANCY ---

pub fn compute_buoyancy(scene: &PhysicsScene) {
    let state = get_physics_state();
    let sable_data = scene.sable_data.read().unwrap();
    let mut sim_data = scene.sim_data.write().unwrap();

    for (id, body_handle) in &sable_data.rigid_bodies {
        let Some(info) = sable_data.level_colliders.get(id) else { continue };
        let body = sim_data.rigid_body_set.get_mut(*body_handle).unwrap();
        let com = info.center_of_mass.unwrap();

        let pairs = find_collision_pairs(info, None, body.position(), 0.0, 256, true, &sable_data);
        let vels = *body.vels();

        for (static_pos, dynamic_pos) in &pairs {
            let local_pos = (dynamic_pos.as_dvec3() + 0.5 - com).as_vec3();
            // Simplified drag/float calls
            do_drag(body, &vels, static_pos, &local_pos, 0.5, 1.0);

            if let Some(chunk) = sable_data.main_level_chunks.get(&pack_section_pos(dynamic_pos.x >> 4, dynamic_pos.y >> 4, dynamic_pos.z >> 4)) {
                let (bid, _) = chunk.get_block(dynamic_pos.x & 15, dynamic_pos.y & 15, dynamic_pos.z & 15);
                if bid > 0 {
                    if let Some(vdata) = state.voxel_collider_map.get(bid as usize - 1, *dynamic_pos) {
                        do_float(body, static_pos, &local_pos, 0.5, vdata.volume);
                    }
                }
            }
        }
    }
}

fn do_drag(body: &mut RigidBody, vels: &RigidBodyVelocity<Real>, static_pos: &IVec3, point: &Vec3, size: Real, strength: Real) {
    let p = body.position().transform_point(*point);
    let aabb_block = Aabb::new(static_pos.as_vec3(), static_pos.as_vec3() + 1.0);
    if let Some(overlap) = Aabb::new(p - size, p + size).intersection(&aabb_block) {
        let velo = vels.velocity_at_point(p, body.mass_properties().world_com);
        body.add_force_at_point(-velo * 1.7 * overlap.volume() * strength, p, false);
    }
}

fn do_float(body: &mut RigidBody, static_pos: &IVec3, point: &Vec3, size: Real, strength: Real) {
    let p = body.position().transform_point(*point);
    let aabb_block = Aabb::new(static_pos.as_vec3(), static_pos.as_vec3() + 1.0);
    if let Some(overlap) = Aabb::new(p - size, p + size).intersection(&aabb_block) {
        body.add_force_at_point(Vec3::new(0.0, 10.5 * overlap.volume() * strength, 0.0), p, false);
    }
}

// --- DISPATCHER & HOOKS ---

pub struct SableManifoldInfo { pub pos_a: IVec3, pub pos_b: IVec3, pub col_a: usize, pub col_b: usize }
#[derive(Default)]
pub struct SableManifoldInfoMap { pub list: DashMap<usize, SableManifoldInfo>, pub counter: AtomicUsize }
impl SableManifoldInfoMap { pub fn clear(&self) { self.list.clear(); self.counter.store(0, Ordering::Relaxed); } }

#[derive(Clone)]
pub struct SableDispatcher {
    pub sable_data: Arc<RwLock<SableSceneData>>,
    pub manifold_info_map: Arc<SableManifoldInfoMap>,
}

impl QueryDispatcher for SableDispatcher {
    fn intersection_test(&self, _: &Pose3, _: &dyn Shape, _: &dyn Shape) -> Result<bool, Unsupported> { Err(Unsupported) }
    fn distance(&self, _: &Pose3, _: &dyn Shape, _: &dyn Shape) -> Result<Real, Unsupported> { Err(Unsupported) }
    fn contact(&self, _: &Pose3, _: &dyn Shape, _: &dyn Shape, _: Real) -> Result<Option<Contact>, Unsupported> { Err(Unsupported) }
    fn closest_points(&self, _: &Pose3, _: &dyn Shape, _: &dyn Shape, _: Real) -> Result<ClosestPoints, Unsupported> { Err(Unsupported) }
    fn cast_shapes(&self, _: &Pose3, _: Vec3, _: &dyn Shape, _: &dyn Shape, _: ShapeCastOptions) -> Result<Option<ShapeCastHit>, Unsupported> { Err(Unsupported) }
    fn cast_shapes_nonlinear(&self, _: &NonlinearRigidMotion, _: &dyn Shape, _: &NonlinearRigidMotion, _: &dyn Shape, _: Real, _: Real, _: bool) -> Result<Option<ShapeCastHit>, Unsupported> { Err(Unsupported) }
}

impl<D: Default + Copy> PersistentQueryDispatcher<ContactManifoldData, D> for SableDispatcher {
    fn contact_manifolds(
        &self,
        pos12: &Pose3,
        g1: &dyn Shape,
        g2: &dyn Shape,
        prediction: Real,
        // Использовать GenericContactManifold вместо ContactManifold
        manifolds: &mut Vec<GenericContactManifold<ContactManifoldData, D>>,
        workspace: &mut Option<ContactManifoldsWorkspace>,
    ) -> Result<(), Unsupported> {
        // ... ваша логика ...
        Ok(())
    }

    fn contact_manifold_convex_convex(
        &self,
        pos12: &Pose3,
        g1: &dyn Shape,
        g2: &dyn Shape,
        normal_constraints1: Option<&dyn NormalConstraints>,
        normal_constraints2: Option<&dyn NormalConstraints>,
        prediction: Real,
        // Здесь тоже меняем на GenericContactManifold
        manifold: &mut GenericContactManifold<ContactManifoldData, D>,
    ) -> Result<(), Unsupported> {
        // ...
        Err(Unsupported)
    }
}

#[derive(Clone)]
pub struct SablePhysicsHooks {
    pub sable_data: Arc<RwLock<SableSceneData>>,
    pub manifold_info_map: Arc<SableManifoldInfoMap>,
}
impl PhysicsHooks for SablePhysicsHooks {
    fn modify_solver_contacts(&self, context: &mut ContactModificationContext) {
        // Logic for friction/restitution based on voxel data
    }
}

// --- JOINTS & ROPES ---

pub struct SableJointSet { pub joints: HashMap<i64, SubLevelJoint> }
impl SableJointSet { pub fn new() -> Self { Self { joints: HashMap::new() } } }

pub struct SubLevelJoint {
    pub id_a: Option<LevelColliderID>, pub id_b: Option<LevelColliderID>,
    pub pos_a: DVec3, pub pos_b: DVec3,
    pub handle: ImpulseJointHandle,
    pub fixed: bool, pub contacts_enabled: bool,
}

pub struct ReportedCollision {
    pub body_a: Option<LevelColliderID>, pub body_b: Option<LevelColliderID>,
    pub local_point_a: DVec3, pub local_point_b: DVec3,
    pub local_normal_a: DVec3, pub local_normal_b: DVec3,
    pub force_amount: f64,
}
pub struct ReportedCollisionBuffer(RefCell<Vec<ReportedCollision>>);
impl ReportedCollisionBuffer {
    pub fn new() -> Self { Self(RefCell::new(Vec::with_capacity(16))) }
    pub fn borrow_mut(&self) -> std::cell::RefMut<'_, Vec<ReportedCollision>> { self.0.borrow_mut() }
}

#[derive(Default)]
pub struct RopeMap { pub counting_id: usize, pub ropes: HashMap<usize, RopeStrand> }
pub struct RopeStrand {
    pub points: Vec<RigidBodyHandle>, pub joints: Vec<(ImpulseJointHandle, ImpulseJointHandle)>,
    pub point_radius: Real, pub first_joint_length: Real,
}

// --- VOXEL COLLIDER MAP ---

pub struct VoxelColliderMap {
    pub voxel_colliders: Vec<Option<VoxelColliderData>>,
    pub dynamic_colliders: HashMap<IVec3, Option<VoxelColliderData>>,
}
impl VoxelColliderMap {
    pub fn new() -> Self { Self { voxel_colliders: Vec::new(), dynamic_colliders: HashMap::new() } }
    pub fn get(&self, index: usize, block_pos: IVec3) -> Option<&VoxelColliderData> {
        if let Some(collider) = self.voxel_colliders.get(index)?.as_ref() {
            if collider.dynamic {
                if let Some(d) = self.dynamic_colliders.get(&block_pos) { return d.as_ref(); }
            }
            return Some(collider);
        }
        None
    }
}

#[derive(Clone)]
pub struct SableEventHandler { pub reported_collisions: Arc<ReportedCollisionBuffer> }
//impl EventHandler for SableEventHandler {
 //   fn handle_collision_event(&self, _: &RigidBodySet, _: &ColliderSet, _: rapier3d::geometry::CollisionEvent, _: Option<&rapier3d::geometry::ContactPair>) {}
 //   fn handle_contact_force_event(&self, _: Real, _: &RigidBodySet, colliders: &ColliderSet, contact_pair: &rapier3d::geometry::ContactPair, total_force_magnitude: Real) {
 //       if total_force_magnitude < 0.1 { return; }
        // Populate reported_collisions
  //  }
//}