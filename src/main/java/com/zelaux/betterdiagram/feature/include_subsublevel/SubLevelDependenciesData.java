package com.zelaux.betterdiagram.feature.include_subsublevel;

import com.zelaux.betterdiagram.feature.create_contraption_mass.ClientLevelPlotExt;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ConstraintTree;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import com.zelaux.betterdiagram.index.BCDForceGroups;
import com.zelaux.betterdiagram.index.SomeConstants;
import com.zelaux.betterdiagram.network.extradata.DiagramExtraDataPacket;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.MergedPointForceCalculator;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.api.sublevel.KinematicContraption;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;
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
import org.joml.Vector3dc;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class SubLevelDependenciesData {

    public final Object2ObjectOpenHashMap<UUID, SubLevelDependenciesData.Entry> sublevelDataMap = new Object2ObjectOpenHashMap<>();
    public final ConstraintTree constraintTree;
    public boolean updated;
    private DiagramDataPacket mergedForces;

    @Nullable
    public static SubLevelDependenciesData tryConstruct(SubLevel origin, int depth) {
        ConstraintTree tree = ConstraintTree.build(origin, depth);
        //if(tree.children.isEmpty() && tree.subLevelGroup.single()) return null;
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

        var forces = collectAllForces(constraintTree);
        if(forces == null) return null;

        return mergedForces = new DiagramDataPacket(forces, forces.mass);
    }

    private @Nullable Forces collectAllForces(ConstraintTree tree) {
        SubLevelGroup subLevelGroup = tree.subLevelGroup;
        var forces = collectFixedForces(subLevelGroup);
        if(forces == null) return null;
        for(ConstraintTree child : tree.children) {
            Forces childForces = collectAllForces(child);
            if(childForces == null) continue;
            child.transitionConstraint.mergeForces(forces, childForces, this);
        }
        return forces;
    }

    public @Nullable Forces collectFixedForces(SubLevelGroup subLevelGroup) {
        SubLevel mainSubLevel = subLevelGroup.all().getFirst();

        var mergedGravity = new MergedPointForceCalculator();
        var gravityVector = SomeConstants.gravityVector(mainSubLevel.getLevel());
        var contraptionGravityForce = new Vector3d();
        var contraptionGravityDisplacement = new Vector3d();


        mergedGravity.begin(CenterMassCalculator.centerOfMass(mainSubLevel));

        var forces = new Forces(0);

        Vector3dc localGravity=VecUtil.unprojectDirectionToLocal(mainSubLevel.logicalPose(),new Vector3d(
            SomeConstants.gravityVector(mainSubLevel.getLevel())
        ));
        Vector3d innerSublevelDirection=new Vector3d();
        Vector3d innerSublevelCOM=new Vector3d();
        double sumOfMass=0;
        for(SubLevel subLevel : subLevelGroup) {
            Entry entry = get(subLevel);

            if(entry.data == null) continue;
            Vector3d com =VecUtil.unprojectPosToLocal(mainSubLevel.logicalPose(), innerSublevelCOM.set(subLevel.logicalPose().position()));
            double mass = entry.data.mass();
            forces.putSingle(BCDForceGroups.CONTRAPTION_GRAVITY, new QueuedForceGroup.PointForce(
                new Vector3d(com),
                localGravity.mul(mass,new Vector3d()))
            );

            mergedGravity.displacement.fma(mass,com);
            sumOfMass+= mass;

            if(true)continue;
            List<QueuedForceGroup.PointForce> pointForces = entry.data.forces().get(ForceGroups.GRAVITY.get());
            if(pointForces == null) {
                updated = true;
                continue;
            }
            QueuedForceGroup.PointForce first = pointForces.getFirst();
            LevelPlot plot = subLevel.getPlot();
            Collection<KinematicContraption> contraptions = plot instanceof ServerLevelPlot serverLevelPlot ? serverLevelPlot.getContraptions() : plot instanceof ClientLevelPlotExt levelPlot ? levelPlot.bcd$getContraptions() : null;
            mergedGravity.add(VecUtil.projectForce(mainSubLevel.logicalPose(), subLevel.logicalPose(), first));
            if(contraptions != null) {
                for(KinematicContraption contraption : contraptions) {
                    MassTracker massTracker = contraption.sable$getMassTracker();
                    contraptionGravityDisplacement.set(contraption.sable$getPosition());

                    gravityVector.mul(massTracker.getMass(), contraptionGravityForce);
                    /*mergedGravity.add(
                        VecUtil.projectPos(mainSubLevel.logicalPose(), subLevel.logicalPose(), contraptionGravityDisplacement),
                        VecUtil.projectDirection(mainSubLevel.logicalPose(), subLevel.logicalPose(), contraptionGravityForce)
                    );*/
                    forces.putSingle(BCDForceGroups.CONTRAPTION_GRAVITY, new QueuedForceGroup.PointForce(new Vector3d(contraptionGravityDisplacement), new Vector3d(contraptionGravityForce)));
                }
            }
        }
mergedGravity.displacement.div(sumOfMass);
        mergedGravity.totalForce.set(localGravity).mul(sumOfMass);
        //mergedGravity.endPlusCOM();

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

        forces.putSingle(ForceGroups.GRAVITY.get(), new QueuedForceGroup.PointForce(mergedGravity.displacement, mergedGravity.totalForce));
        return forces;
    }

    public Entry get(SubLevel subLevel) {
        return sublevelDataMap.get(subLevel.getUniqueId());
    }

    public Entry get(UUID subLevel) {
        return sublevelDataMap.get(subLevel);
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

    public boolean updateData(UUID sublevel, DiagramExtraDataPacket extraDataPacket) {
        Entry entry = get(sublevel);
        if(entry == null) return false;
        entry.extraData = extraDataPacket;
        updated = true;
        return true;
    }

    @ToString
    @FieldDefaults(level = AccessLevel.PUBLIC)
    @Getter
    public static class Entry {
        SubLevel subLevel;
        @Nullable
        DiagramDataPacket data;
        @Nullable
        DiagramExtraDataPacket extraData;

        public Entry(SubLevel subLevel) {
            this.subLevel = subLevel;
        }
    }
}
