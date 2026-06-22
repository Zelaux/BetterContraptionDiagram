package com.zelaux.betterdiagram.feature.include_subsublevel;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ConstraintTree;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.MergedSubLevel;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class SubLevelDependenciesData extends Object2ObjectOpenHashMap<UUID, SubLevelDependenciesData.Entry> {


    public static final Vector3d TMP1 = new Vector3d();
    public final ConstraintTree constraintTree;
    public boolean updated;
    private DiagramDataPacket mergedForces;

    public SubLevelDependenciesData(ConstraintTree constraintTree) {
        this.constraintTree = constraintTree;

        var visited = new ObjectOpenHashSet<SubLevel>();
        var currentLayer = new ObjectArrayList<ConstraintTree>();
        currentLayer.add(constraintTree);
        var nextLayer = new ObjectArrayList<ConstraintTree>();

        while(!currentLayer.isEmpty()) {
            for(ConstraintTree tree : currentLayer) {
                tree.mergedSubLevel.forEach(visited::add);
                tree.children.forEach(nextLayer::add);
            }
            currentLayer.clear();
            var tmp = currentLayer;
            currentLayer = nextLayer;
            nextLayer = tmp;
        }

        for(SubLevel subLevel : visited) {
            put(subLevel.getUniqueId(), new Entry(subLevel));
        }
    }

    public DiagramDataPacket mergedForces(){
        if(mergedForces!=null && !updated)return mergedForces;
        mergedForces=null;
        updated=false;

        MergedSubLevel level = constraintTree.mergedSubLevel;

        SubLevel mainSubLevel = level.all().getFirst();

        Vector3d mergedDisplacement = new Vector3d();
        Vector3d sumOfForces = new Vector3d();
        CenterMassCalculator.calculateSumOfForcesMomentum(mergedDisplacement, sumOfForces, TMP1, CenterMassCalculator.centerOfMass(mainSubLevel), consumer->{
            for(SubLevel subLevel : level) {
                Entry entry=get0(subLevel);
                if(entry.data != null) {
                    QueuedForceGroup.PointForce first = entry.data.forces().get(ForceGroups.GRAVITY.get()).getFirst();
                    consumer.accept(VecUtil.projectForce(level.all().getFirst().logicalPose(), subLevel.logicalPose(), first));
                }
            }
        });
        mergedDisplacement.add(CenterMassCalculator.centerOfMass(mainSubLevel));
        double mass=0;
        var forces =new Object2ObjectOpenHashMap<ForceGroup, List<QueuedForceGroup.PointForce>>();
        boolean wasAny=false;
        for(SubLevel subLevel : level) {
            Entry entry = get0(subLevel);
            if(entry.data == null) continue;
            wasAny=true;
            mass+=entry.data.mass();
            for(var listEntry : entry.data.forces().entrySet()) {
                if(listEntry.getKey() == ForceGroups.GRAVITY.get()) continue;
                List<QueuedForceGroup.PointForce> pointForces = forces.computeIfAbsent(listEntry.getKey(), it -> new ObjectArrayList<>());
                for(QueuedForceGroup.PointForce pointForce : listEntry.getValue()) {

                    var projected = VecUtil.projectForce(mainSubLevel.logicalPose(), subLevel.logicalPose(), pointForce);
                    pointForces.add(projected);
                }
            }
        }
        if(!wasAny) {
            return null;
        }

        forces.put(ForceGroups.GRAVITY.get(),List.of(new QueuedForceGroup.PointForce(mergedDisplacement,sumOfForces)));
        return mergedForces=new DiagramDataPacket(forces,mass);
    }

    private Entry get0(SubLevel subLevel) {
        return get(subLevel.getUniqueId());
    }


    public boolean updateData(SubLevel subLevel, DiagramDataPacket packet) {
        Entry entry = get(subLevel.getUniqueId());
        if(entry == null) return false;
        if(entry.data != packet && (packet == null || entry.data == null || packet.forces().equals(entry.data.forces()))) {
            entry.data = packet;
            updated = true;
        }
        return true;
        //bySubLevel.computeIfAbsent(subLevel, () -> new SubLevel(subLevel))
    }

    public Stream<SubLevel> allSublevels() {
        return values().stream().map(Entry::subLevel);
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
