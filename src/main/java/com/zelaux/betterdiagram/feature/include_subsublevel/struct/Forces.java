package com.zelaux.betterdiagram.feature.include_subsublevel.struct;

import com.zelaux.betterdiagram.struct.BCDForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Forces extends Object2ObjectOpenHashMap<ForceGroup, List<QueuedForceGroup.PointForce>> {
    public double mass;

    public Forces(Map<? extends ForceGroup, ? extends List<QueuedForceGroup.PointForce>> m, double mass) {
        super(m);
        this.mass = mass;
    }

    public Forces(double mass) {
        this.mass = mass;
    }

    public List<QueuedForceGroup.PointForce> forcesForGroup(ForceGroup key) {
        return computeIfAbsent(key, it -> new ObjectArrayList<>());
    }

    public void putSingle(ForceGroup forceGroup, QueuedForceGroup.PointForce pointForce) {
        put(forceGroup, single(pointForce));
    }

    public void putSingle(BCDForceGroup forceGroup, QueuedForceGroup.PointForce pointForce) {
        put(forceGroup.group(), single(pointForce));
    }

    private List<QueuedForceGroup.PointForce> single(QueuedForceGroup.PointForce pointForce) {
        return new ObjectArrayList<>(List.of(pointForce));
    }

    public void addForce(BCDForceGroup forceGroup, @NotNull QueuedForceGroup.PointForce pointForce) {
        addForce(forceGroup.group(), pointForce);
    }

    public void addForce(ForceGroup forceGroup,@NotNull QueuedForceGroup.PointForce pointForce) {
        forcesForGroup(forceGroup).add(pointForce);
    }

    public void addProjectedForce(ForceGroup forceGroup, Pose3dc from, Pose3dc to,@NotNull QueuedForceGroup.PointForce pointForce) {
        forcesForGroup(forceGroup).add(pointForce);
    }

    public void forEachForce(Consumer<QueuedForceGroup.PointForce> forceConsumer) {
        for(List<QueuedForceGroup.PointForce> list : values()) {
            list.forEach(forceConsumer);
        }
    }
}
