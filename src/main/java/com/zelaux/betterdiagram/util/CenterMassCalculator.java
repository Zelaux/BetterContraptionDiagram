package com.zelaux.betterdiagram.util;

import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.extend.ClientData;
import com.zelaux.betterdiagram.extend.DiagramScreenAccessors;
import com.zelaux.betterdiagram.extend.DiagramStickyNoteAccessors;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.struct.MassStack;
import com.zelaux.betterdiagram.struct.Weights;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Runtime;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Objects;

public class CenterMassCalculator {
    public static final float DELTA = 0.00001f;

    public static Weights @NotNull [] makeMassStacks() {
        return new Weights[]{new Weights(), new Weights(), new Weights()};
    }

    public static @NotNull Vector3d centerOfMass(ClientSubLevel subLevel) {
        return subLevel.logicalPose().rotationPoint();
    }

    public static Weights[] recalculateStacks(DiagramScreen screen) {
        var accessors = accessors(screen);
        DiagramDataPacket diagramDataPacket = accessors.betterContraptionDiagram$serverData();
        if(diagramDataPacket==null)return null;
        return recalculateStacks((accessors.betterContraptionDiagram$clientData()), screen.subLevel, diagramDataPacket.mass());
    }

    public static Weights[] recalculateStacks(WithClientData clientData, ClientSubLevel subLevel, double mass) {
        BCDData data = clientData.bcdiagram$dataOrNull();
        if(data == null) return null;

        var expectedCOM = expectedCenterOfMass(clientData, subLevel);
        Vector3d currentCOM = centerOfMass(subLevel);
        if(expectedCOM.equals(currentCOM, DELTA) || mass == 0) {

            clientData.bcdiagram$updateData(
                data.withNoAxisData()
            );
            return null;
        }
        Cache cache = data.cache;
        var stacks = data.axisWeightStacks;

        if(cache != null && cache.isApproxEquals(expectedCOM, currentCOM, mass)) {
            return stacks;
        }
        if(stacks == null) stacks = makeMassStacks();

        data = data.withCache(Cache.update(cache, expectedCOM, currentCOM, mass));


        stacks[0].clear();
        stacks[1].clear();
        stacks[2].clear();

        Vector3d diff = expectedCOM.sub(currentCOM, new Vector3d());
        diff.mul(mass);

        diff.mul(4);

        addStacks(stacks[0], VecUtil.X_V, expectedCOM, diff.x);
        addStacks(stacks[1], VecUtil.Y_V, expectedCOM, diff.y);
        addStacks(stacks[2], VecUtil.Z_V, expectedCOM, diff.z);

        clientData.bcdiagram$updateData(
            data = data.withAxisWeightStacks(stacks)
        );
        return stacks;
    }

    private static void addStacks(Weights weights, Vector3d uni, Vector3dc expectedCOM, double value0) {
        if(equals(value0, 0)) value0 = 0;
        weights.totalWeight = value0 / 4f;
        long value = (long) Math.floor(value0);
        final int maxIterations = Config.MAX_ITERATION.getAsInt();
        final int maxFixDistance = Config.MAX_FIX_DISTANCE.getAsInt();

        long sig = value0 < 0 ? -1 : 1;
        if(equals(Math.abs(value0 - value), 1)) value += sig;
        else if(!Runtime.equals(value0, value, 0.0001f)) {
            double v1 = (value0 - value) / 4f;

            long sigV1 = v1 < 0 ? -1 : 1;
            v1 = Math.abs(v1);
            double dst0 = v1 / 0.25;

            var unit = uni.mul(sigV1, new Vector3d());
            for(double dst = dst0; dst < maxFixDistance && v1 < Math.abs(value0); dst += 4, v1 += 1) {
                weights.smallStacks.add(new MassStack(new Vector3d(unit).mul(dst).add(expectedCOM), 0.25));
            }
        }
        if(value == 0) return;
        long dst = 1;

        value *= sig;
        long v = value;
        int iteration = 0;
        while(v > 0) {
            iteration++;
            if(iteration > maxIterations) break;
            weights.stacks.add(
                new MassStack(uni.mul(dst * sig, new Vector3d()).add(expectedCOM), v / 4f)
            );
            do {
                dst++;
                if(dst > maxFixDistance) return;
            } while(value % dst != 0 && dst < value);

            v = value / dst;
        }
    }

    public static Vector3dc expectedCenterOfMass(DiagramScreen screen) {
        return expectedCenterOfMass((accessors(screen).betterContraptionDiagram$clientData()), screen.subLevel);
    }

    public static DiagramScreenAccessors accessors(DiagramScreen screen) {return (DiagramScreenAccessors) screen;}

    public static DiagramStickyNoteAccessors accessors(DiagramStickyNote screen) {return (DiagramStickyNoteAccessors) screen;}

    @NotNull
    public static Vector3dc expectedCenterOfMass(WithClientData clientData, ClientSubLevel subLevel) {
        BCDData data = clientData.bcdiagram$dataOrNull();
        if(data == null || data.eCOM() == null) return centerOfMass(subLevel);
        return data.eCOM();
    }

    public static void expectedCenterOfMass(ClientData clientData, Vector3d newValue) {
        if(newValue == null) {
            clientData.bcdiagram$updateData(null);
            return;
        }
        clientData.bcdiagram$updateData(
            clientData.bcdiagram$dataOrCreate().withECOM(new Vector3d(newValue))
        );
    }

    public static boolean equals(double v1, double v2) {
        return Runtime.equals(v1, v2, DELTA);
    }

    public static boolean equals(Vector3dc COM1, Vector3dc COM2) {
        if(COM1 == null) return COM2 == null;
        return COM1.equals(COM2, DELTA);
    }

    public static Vector3d calculateGravityDirection(ClientSubLevel subLevel, DiagramDataPacket serverData, @Nullable Vector3d output) {
        //DimensionPhysicsData.getGravity(level)
        QueuedForceGroup.PointForce first = serverData.forces().get(ForceGroups.GRAVITY.get()).getFirst();
        Vector3d force = output == null ? new Vector3d(first.force()) : output.set(first.force());

        force.div(serverData.mass());

        subLevel.logicalPose().transformNormal(force);

        return force;
    }


    public static class Cache {
        public final Vector3d expectedCOM = new Vector3d();
        public final Vector3d actualCOM = new Vector3d();
        public double mass;

        public Cache() {}

        public Vector3d expectedCOM() {return expectedCOM;}

        public Vector3d actualCOM() {return actualCOM;}

        public double mass() {return mass;}

        public Cache set(Vector3dc expectedCOM, Vector3d actualCOM, double mass) {
            this.expectedCOM.set(expectedCOM);
            this.actualCOM.set(actualCOM);
            this.mass = mass;
            return this;
        }

        public static Cache update(Cache cache, Vector3dc expectedCOM, Vector3d currentCOM, double mass) {
            return Objects.requireNonNullElseGet(cache, Cache::new).set(expectedCOM, currentCOM, mass);
        }

        private boolean isApproxEquals(Vector3dc expectedCOM, Vector3d currentCOM, double mass) {
            return this.expectedCOM.equals(expectedCOM, DELTA) && actualCOM.equals(currentCOM, DELTA) && Runtime.equals(mass, this.mass, DELTA);
        }
    }
}
