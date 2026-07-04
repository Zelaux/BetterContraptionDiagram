mod sable_types;

use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::{jfloatArray, jlong, jint, jstring};
use jni::objects::JString;

use rapier3d::prelude::*;
use rapier3d::geometry::Aabb;
// Используем математические типы строго из rapier3d
use rapier3d::math::Vector;
use rapier3d::glamx::{Vec3, DVec3, IVec3}; // <--- ВАЖНО: берем отсюда

use crate::sable_types::*;

fn mc_log(env: &mut JNIEnv, msg: &str) {
    let system = env.find_class("java/lang/System").unwrap();
    let out = env.get_static_field(system, "out", "Ljava/io/PrintStream;").unwrap().l().unwrap();
    let msg_jstr = env.new_string(msg).unwrap();
    env.call_method(out, "println", "(Ljava/lang/String;)V", &[(&msg_jstr).into()]).unwrap();
}

#[no_mangle]
pub extern "system" fn Java_com_zelaux_betterdiagram_feature_buoyancy_BuoyancyCalculator_calculateForces0(
    mut env: JNIEnv,
    _class: JClass,
    scene_ptr: jlong,
    target_body_id: jint,
) -> jfloatArray {
    if scene_ptr == 0 {
        return create_empty_array(&env);
    }

    let scene = unsafe { &*(scene_ptr as *const PhysicsScene) };
    let mut out_data = [1.0f32; 12];

    println!("init");
    println!("universal_drag: {:?}",scene.universal_drag);
    let sim_data = match scene.sim_data.read() {
        Ok(d) => d,
        Err(err) => {println!("sim_data {err:?}"); return create_empty_array(&env)},
    };
    println!("sim");
    let sable_data = match scene.sable_data.read() {
        Ok(d) => d,
        Err(err) => {println!("sable_data {err:?}"); return create_empty_array(&env)},
    };
    println!("sable");

    let body_id = target_body_id as usize;

    if let Some(body_handle) = sable_data.rigid_bodies.get(&body_id) {
    println!("body_handle");
        if let Some(info) = sable_data.level_colliders.get(&body_id) {
        println!("info");
            if let Some(body) = sim_data.rigid_body_set.get(*body_handle) {
            println!("body");

                // Используем константы из правильной версии glam
                let com = info.center_of_mass.unwrap_or(DVec3::ZERO);
                let b_min = info.local_bounds_min.unwrap_or(IVec3::ZERO);
                let b_max = info.local_bounds_max.unwrap_or(IVec3::ZERO);

                let linvel = body.linvel();
                let angvel = body.angvel();
                let world_com = body.mass_properties().world_com;

                let diff = b_max - b_min;
                let complex = (diff.x + diff.y + diff.z) < 10;

                let pos = body.position();

                let body_aabb_mins = pos.transform_point(Vector::new(b_min.x as f32, b_min.y as f32, b_min.z as f32));
                let body_aabb_maxs = pos.transform_point(Vector::new(b_max.x as f32, b_max.y as f32, b_max.z as f32));

                let min_v = IVec3::new(
                    body_aabb_mins.x.min(body_aabb_maxs.x).floor() as i32,
                    body_aabb_mins.y.min(body_aabb_maxs.y).floor() as i32,
                    body_aabb_mins.z.min(body_aabb_maxs.z).floor() as i32,
                );
                let max_v = IVec3::new(
                    body_aabb_mins.x.max(body_aabb_maxs.x).ceil() as i32,
                    body_aabb_mins.y.max(body_aabb_maxs.y).ceil() as i32,
                    body_aabb_mins.z.max(body_aabb_maxs.z).ceil() as i32,
                );

    println!("minmax: {min_v:?} | {max_v:?}");
                let mut d_force_acc = Vector::ZERO;
                let mut d_disp_acc = Vector::ZERO;
                let mut d_w = 0.0f32;

                let mut f_force_acc = Vector::ZERO;
                let mut f_disp_acc = Vector::ZERO;
                let mut f_w = 0.0f32;

                for x in min_v.x..=max_v.x {
                    for y in min_v.y..=max_v.y {
                        for z in min_v.z..=max_v.z {
                            let static_pos = IVec3::new(x, y, z);
                            // Явно указываем типы, чтобы избежать ошибок вывода
                            let pos_f64: DVec3 = static_pos.as_dvec3();
                            let local_pos_base: Vec3 = (pos_f64 + DVec3::splat(0.5) - com).as_vec3();

                            let (step, iters) = if complex { (0.25f32, 8) } else { (0.5f32, 1) };

                            for i in 0..iters {
                                let mut lp = local_pos_base;
                                if complex {
                                    let ox = (i & 1) * 2 - 1;
                                    let oy = ((i >> 1) & 1) * 2 - 1;
                                    let oz = ((i >> 2) & 1) * 2 - 1;
                                    lp += Vec3::new(ox as f32 * 0.25, oy as f32 * 0.25, oz as f32 * 0.25);
                                }

                                let (df, dat) = calc_drag_data(body, &linvel, &angvel, &world_com, &static_pos, lp, step);
                                if df.length_squared() > 0.0 {
                                    d_force_acc += df;
                                    let m = df.length();
                                    d_disp_acc += dat * m;
                                    d_w += m;
                                }

                                let chunk_key = pack_section_pos(x >> 4, y >> 4, z >> 4);
                                if sable_data.main_level_chunks.contains_key(&chunk_key) {
                                    let (ff, fat) = calc_float_data(body, &static_pos, lp, step, 1.0);
                                    if ff.length_squared() > 0.0 {
                                        f_force_acc += ff;
                                        let m = ff.length();
                                        f_disp_acc += fat * m;
                                        f_w += m;
                                    }
                                }
                            }
                        }
                    }
                }

                if d_w > 0.0 {
                    let ad = d_disp_acc / d_w;
                    out_data[0] = ad.x; out_data[1] = ad.y; out_data[2] = ad.z;
                    out_data[3] = d_force_acc.x; out_data[4] = d_force_acc.y; out_data[5] = d_force_acc.z;
                }
                if f_w > 0.0 {
                    let af = f_disp_acc / f_w;
                    out_data[6] = af.x; out_data[7] = af.y; out_data[8] = af.z;
                    out_data[9] = f_force_acc.x; out_data[10] = f_force_acc.y; out_data[11] = f_force_acc.z;
                }
            }
        }
    }

    let j_array = env.new_float_array(12).unwrap();
    env.set_float_array_region(&j_array, 0, &out_data).unwrap();
    j_array.into_raw()
}

fn create_empty_array(env: &JNIEnv) -> jfloatArray {
    env.new_float_array(0).unwrap().into_raw()
}

fn get_velocity_at_point(linvel: &Vector, angvel: &Vector, world_com: &Vector, point: &Vector) -> Vector {
    let dr = *point - *world_com;
    *linvel + angvel.cross(dr)
}

fn calc_drag_data(body: &RigidBody, linvel: &Vector, angvel: &Vector, world_com: &Vector, static_pos: &IVec3, lp: Vec3, size: f32) -> (Vector, Vector) {
    let lp_vec = Vector::new(lp.x, lp.y, lp.z);
    let world_point = body.position().transform_point(lp_vec);

    let aabb_dynamic = Aabb::new(world_point - Vector::splat(size), world_point + Vector::splat(size));
    let aabb_static = Aabb::new(
        Vector::new(static_pos.x as f32, static_pos.y as f32, static_pos.z as f32),
        Vector::new(static_pos.x as f32 + 1.0, static_pos.y as f32 + 1.0, static_pos.z as f32 + 1.0)
    );

    if let Some(overlap) = aabb_dynamic.intersection(&aabb_static) {
        let velo = get_velocity_at_point(linvel, angvel, world_com, &world_point);
        let speed = velo.length();
        if speed > 0.001 {
            let force_mag = speed * 1.7 * overlap.volume();
            let force_dir = -velo / speed;
            return (force_dir * force_mag, lp_vec);
        }
    }
    (Vector::ZERO, Vector::ZERO)
}

fn calc_float_data(body: &RigidBody, static_pos: &IVec3, lp: Vec3, size: f32, strength: f32) -> (Vector, Vector) {
    let lp_vec = Vector::new(lp.x, lp.y, lp.z);
    let world_point = body.position().transform_point(lp_vec);

    let aabb_dynamic = Aabb::new(world_point - Vector::splat(size), world_point + Vector::splat(size));
    let aabb_static = Aabb::new(
        Vector::new(static_pos.x as f32, static_pos.y as f32, static_pos.z as f32),
        Vector::new(static_pos.x as f32 + 1.0, static_pos.y as f32 + 1.0, static_pos.z as f32 + 1.0)
    );

    if let Some(overlap) = aabb_dynamic.intersection(&aabb_static) {
        return (Vector::new(0.0, 10.5 * overlap.volume() * strength, 0.0), lp_vec);
    }
    (Vector::ZERO, Vector::ZERO)
}
#[no_mangle]
pub extern "system" fn Java_com_zelaux_betterdiagram_feature_buoyancy_BuoyancyCalculator_helloWorld(
    mut env: JNIEnv,
    _class: JClass,
    input: JString,
) -> jstring {
    let name: String = env.get_string(&input).expect("Couldn't get java string!").into();
    let response = format!("Hello, {}! From Rust.", name);
    mc_log(&mut env,&format!("minmax: {response:?} | {response:?}"));
    println!("minmax: {response:?} | {response:?}");
    let output = env.new_string(response).expect("Couldn't create java string!");
    output.into_raw()
}
