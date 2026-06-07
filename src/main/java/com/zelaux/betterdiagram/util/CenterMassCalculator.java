package com.zelaux.betterdiagram.util;

import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.data.OffCenteredBlock;
import com.zelaux.betterdiagram.extend.ClientData;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.extend.accessors.DiagramScreenAccessors;
import com.zelaux.betterdiagram.extend.accessors.DiagramStickyNoteAccessors;
import com.zelaux.betterdiagram.struct.MassStack;
import com.zelaux.betterdiagram.struct.Weights;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Runtime;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.ArrayList;

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
        if(diagramDataPacket == null) return null;
        return recalculateStacks((accessors.betterContraptionDiagram$clientData()), screen.subLevel, diagramDataPacket.mass());
    }

    public static Weights[] recalculateStacks(WithClientData clientData, ClientSubLevel subLevel, double mass) {
        BCDData data = clientData.bcdDataOrTryDefault();
        if(data == null || data.eCOM() == null) return null;

        var expectedCOM = expectedCenterOfMass(clientData, subLevel);
        Vector3d currentCOM = centerOfMass(subLevel);
        if(expectedCOM.equals(currentCOM, DELTA) || mass == 0) {

            clientData.bcdiagram$updateData(data
                .withWeightStacksByAxis(null)
            );
            return null;
        }
        var stacks = data.weightStacksByAxis;
        boolean[] shouldUpdate = {false};
        data = checkCache(mass, data, currentCOM, shouldUpdate);
        if(!shouldUpdate[0]) return stacks;
        if(stacks == null) stacks = makeMassStacks();

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
            data = data.withWeightStacksByAxis(stacks)
        );
        return stacks;
    }

    private static @NotNull BCDData checkCache(double mass, BCDData data, Vector3d currentCOM, boolean[] shouldUpdate) {
        Cache cache = data.cache;
        if(cache == null || !cache.isApproxEquals(data.eCOM(), currentCOM, mass)) {
            data = data.withCache(Cache.create(data.eCOM(), currentCOM, mass));
            shouldUpdate[0] = true;
        } else {
            shouldUpdate[0] = false;
        }
        return data;
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
        BCDData data = clientData.bcdDataOrTryDefault();
        if(data == null || data.eCOM() == null) return centerOfMass(subLevel);
        return data.eCOM();
    }

    public static void expectedCenterOfMass(ClientData clientData, Vector3d newValue) {
        BCDData data = clientData.bcdDataOrTryDefault();
        if(data == null) return;
        clientData.bcdiagram$updateData(
            newValue == null ? data.withECOM(null) : data.withECOM(new Vector3d(newValue))
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
        var gravity = serverData.forces().get(ForceGroups.GRAVITY.get());
        if(gravity == null) return new Vector3d(0, 0, 0);
        QueuedForceGroup.PointForce first = gravity.getFirst();
        Vector3d force = output == null ? new Vector3d(first.force()) : output.set(first.force());

        force.div(serverData.mass());

        subLevel.logicalPose().transformNormal(force);

        return force;
    }

    @NotNull
    @Contract(mutates = "param2")
    public static ArrayList<OffCenteredBlock> findOffCenteredBlocks(
        WithClientData clientData, ClientSubLevel subLevel, boolean[] enabled, ArrayList<OffCenteredBlock> list, Weights[] weights) {
        if(list == null) list = new ArrayList<>();
        if(weights != null) {
            for(int i = 0; i < weights.length; i++) {
                Weights weight = weights[i];
                if(weight.smallStacks.isEmpty()) enabled[i] = false;
            }
        }

        checkIsEmpty:
        {
            for(boolean b : enabled) {
                if(b) break checkIsEmpty;
            }
            return list;
        }
        final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        BoundingBox3ic bounds = subLevel.getPlot().getBoundingBox();
        Level level = subLevel.getLevel();


        for(int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for(int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for(int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    final BlockState state = level.getBlockState(blockPos.set(x, y, z));
                    double mass = PhysicsBlockPropertyHelper.getMass(level, BlockPos.ZERO, state);
                    if(CenterMassCalculator.equals(mass, 0)) continue;

                    Vector3dc blockCenterOfMass = MassTracker.BLOCK_CENTER_OF_MASS.apply(level, state);
                    checkHasOffset:
                    {
                        for(int i = 0; i < enabled.length; i++) {
                            if(enabled[i] && !equals(VecUtil.GETTERS_3d[i].get(blockCenterOfMass), 0.5))
                                break checkHasOffset;
                        }
                        continue;
                    }
                    //if(blockCenterOfMass.equals(JOMLConversion.HALF)) continue;
                    list.add(OffCenteredBlock.create(subLevel, new BlockPos(blockPos), state, mass, new Vector3d(blockCenterOfMass)));
                }
            }
        }

        return list;
    }

    public static @Nullable ArrayList<OffCenteredBlock> getOrCreateOffCenteredBlocks(WithClientData clientData, @NotNull DiagramScreen self) {
        var data = clientData.bcdDataOrTryDefault();

        DiagramDataPacket serverData = accessors(self).betterContraptionDiagram$serverData();
        if(serverData == null || data == null) return null;
        if(data.offCenterBlocksShowState == BCDData.OffCenterBlocksShowState.none) {
            clientData.bcdiagram$updateData(data.withOffCenteredBlocks(null));
            return null;
        }
        boolean[] shouldUpdate = {false};
        data = checkCache(serverData.mass(), data, centerOfMass(self.subLevel), shouldUpdate);
        if(data.offCenteredBlocks != null && !shouldUpdate[0]) return data.offCenteredBlocks;
        ArrayList<OffCenteredBlock> offCenteredBlocks = findOffCenteredBlocks(
            clientData,
            self.subLevel,
            data.axisStatesAsArray(null),
            new ArrayList<>(),
            data.offCenterBlocksShowState == BCDData.OffCenterBlocksShowState.show ?
                recalculateStacks(clientData, self.subLevel, serverData.mass()) :
                null
        );
        if(offCenteredBlocks.isEmpty()) return null;
        clientData.bcdiagram$updateData(
            data.withOffCenteredBlocks(offCenteredBlocks)
        );


        return offCenteredBlocks;
    }

    public static void calculateSumOfForcesMomentum(Vector3d mergedDisplacement, Vector3d sumOfForces, Vector3d tmp1, DiagramDataPacket serverData, ClientSubLevel subLevel1) {
        mergedDisplacement.zero();
        sumOfForces.zero();
        tmp1.zero();
        Vector3d sumOfMoments = tmp1;
        Vector3d tmp2 = mergedDisplacement;
        sumOfMoments.zero();
        Vector3d COM = new Vector3d(centerOfMass(subLevel1));
        serverData.forces().forEach((_i, pointForces) -> {
            if(_i == ForceGroups.GRAVITY.get()) {
                return;
            }
            for(var force : pointForces) {

                sumOfForces.add(force.force());
                Vector3d momentOfForce = tmp2.set(force.point()).sub(COM).cross(force.force());
                sumOfMoments.add(momentOfForce);
                tmp2.add(force.force());
            }
        });
        mergedDisplacement.set(sumOfForces).cross(sumOfMoments).div(sumOfForces.lengthSquared());
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Cache {
        @Nullable
        public final Vector3dc expectedCOM;
        @NotNull
        public final Vector3dc actualCOM;
        public double mass;


        @Contract(value = "_,_,_->new")
        public static Cache create(Vector3dc expectedCOM, Vector3dc currentCOM, double mass) {
            return new Cache(expectedCOM, new Vector3d(currentCOM), mass);
        }

        private boolean isApproxEquals(Vector3dc expectedCOM, Vector3dc currentCOM, double mass) {
            return (expectedCOM == this.expectedCOM || this.expectedCOM != null && this.expectedCOM.equals(expectedCOM, DELTA)) && actualCOM.equals(currentCOM, DELTA) && Runtime.equals(mass, this.mass, DELTA);
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof Cache cache)) return false;
            return isApproxEquals(cache.expectedCOM, cache.actualCOM, cache.mass);
        }
    }
}
