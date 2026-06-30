package com.zelaux.betterdiagram.feature.include_subsublevel;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ConstraintTree;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import net.createmod.catnip.animation.AnimationTickHolder;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class SubLevelDependenciesData {

    public final Object2ObjectOpenHashMap<UUID, SubLevelDependenciesData.Entry> sublevelDataMap = new Object2ObjectOpenHashMap<>();
    public static final Vector3d TMP1 = new Vector3d();
    public final ConstraintTree constraintTree;
    public boolean updated;
    private DiagramDataPacket mergedForces;

    @Nullable
    public static SubLevelDependenciesData tryConstruct(SubLevel origin, int depth) {
        ConstraintTree tree = ConstraintTree.build(origin, depth);
        if(tree.children.isEmpty() && tree.subLevelGroup.single()) return null;
        return new SubLevelDependenciesData(tree);
    }

    public SubLevelDependenciesData(ConstraintTree constraintTree) {
        this.constraintTree = constraintTree;

        var visited = new ObjectOpenHashSet<SubLevel>();
        var currentLayer = new ObjectArrayList<ConstraintTree>();
        currentLayer.add(constraintTree);
        var nextLayer = new ObjectArrayList<ConstraintTree>();

        while(!currentLayer.isEmpty()) {
            for(ConstraintTree tree : currentLayer) {
                tree.subLevelGroup.forEach(visited::add);
                tree.children.forEach(nextLayer::add);
            }
            currentLayer.clear();
            var tmp = currentLayer;
            currentLayer = nextLayer;
            nextLayer = tmp;
        }

        for(SubLevel subLevel : visited) {
            sublevelDataMap.put(subLevel.getUniqueId(), new Entry(subLevel));
        }
    }

    int updateTick = Integer.MIN_VALUE;

    public DiagramDataPacket mergedForces() {
        if(mergedForces != null && !updated) {
            int ticks = AnimationTickHolder.getTicks(true);
            if(ticks > updateTick) {
                updateTick = ticks + 20;
                mergedForces = null;
            } else {
                return mergedForces;
            }
        }
        mergedForces = null;

        updated = false;

        ConstraintTree constraintTree1 = constraintTree;
        var forces = collectAllForces(constraintTree1);
        if(forces == null) return null;

        return mergedForces = new DiagramDataPacket(forces, forces.mass);
    }

    private @Nullable Forces collectAllForces(ConstraintTree tree) {
        SubLevelGroup subLevelGroup = tree.subLevelGroup;
        var forces = collectForces(subLevelGroup);
        if(forces == null) return null;
        for(ConstraintTree child : tree.children) {
            Forces childForces = collectAllForces(child);
            if(childForces == null) continue;
            child.transitionConstraint.mergeForces(forces, childForces);
        }
        return forces;
    }

    private @Nullable Forces collectForces(SubLevelGroup subLevelGroup) {
        SubLevel mainSubLevel = subLevelGroup.all().getFirst();

        Vector3d mergedDisplacement = new Vector3d();
        Vector3d sumOfForces = new Vector3d();
        CenterMassCalculator.calculateSumOfForcesMomentum(mergedDisplacement, sumOfForces, TMP1, CenterMassCalculator.centerOfMass(mainSubLevel), consumer -> {
            for(SubLevel subLevel : subLevelGroup) {
                Entry entry = get(subLevel);
                if(entry.data == null) continue;
                List<QueuedForceGroup.PointForce> pointForces = entry.data.forces().get(ForceGroups.GRAVITY.get());
                if(pointForces == null) {
                    updated = true;
                    continue;
                }
                QueuedForceGroup.PointForce first = pointForces.getFirst();
                consumer.accept(VecUtil.projectForce(mainSubLevel.logicalPose(), subLevel.logicalPose(), first));
            }
        });
        mergedDisplacement.add(CenterMassCalculator.centerOfMass(mainSubLevel));

        var forces = new Forces(0);
        boolean wasAny = false;
        for(SubLevel subLevel : subLevelGroup) {
            Entry entry = get(subLevel);
            if(entry.data == null) continue;
            wasAny = true;
            forces.mass += entry.data.mass();
            for(var listEntry : entry.data.forces().entrySet()) {
                if(listEntry.getKey() == ForceGroups.GRAVITY.get()) continue;

                List<QueuedForceGroup.PointForce> pointForces = forces.forcesForGroup(listEntry.getKey());
                for(QueuedForceGroup.PointForce pointForce : listEntry.getValue()) {

                    var projected = VecUtil.projectForce(mainSubLevel.logicalPose(), subLevel.logicalPose(), pointForce);
                    pointForces.add(projected);
                }
            }
        }
        if(!wasAny) {
            return null;
        }

        forces.putSingle(ForceGroups.GRAVITY.get(), new QueuedForceGroup.PointForce(mergedDisplacement, sumOfForces));
        return forces;
    }

    public Entry get(SubLevel subLevel) {
        return sublevelDataMap.get(subLevel.getUniqueId());
    }


    public boolean updateData(SubLevel subLevel, DiagramDataPacket packet) {
        Entry entry = get(subLevel);
        if(entry == null) return false;
        if(entry.data != packet) {
            entry.data = packet;
            updated = true;
        }
        return true;
        //bySubLevel.computeIfAbsent(subLevel, () -> new SubLevel(subLevel))
    }

    public Stream<SubLevel> allSublevels() {
        return sublevelDataMap.values().stream().map(Entry::subLevel);
    }

    @ToString
    @FieldDefaults(level = AccessLevel.PUBLIC)
    @Getter
    public static class Entry {
        SubLevel subLevel;
        @Nullable
        DiagramDataPacket data;

        public Entry(SubLevel subLevel) {
            this.subLevel = subLevel;
        }
    }
}
