package com.zelaux.betterdiagram.feature.balance;

import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.data.BalanceForceFilter;
import com.zelaux.betterdiagram.data.EnabledForces;
import com.zelaux.betterdiagram.extend.BAccessors;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ConstraintTree;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import com.zelaux.betterdiagram.struct.MassStack;
import com.zelaux.betterdiagram.struct.Weights;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class BalanceModel {

    public static Weights @NotNull [] makeMassStacks() {
        return new Weights[]{new Weights(), new Weights(), new Weights()};
    }

    public static Weights @Nullable [] recalculateStacks(WithClientData clientData, ClientSubLevel subLevel, BalanceForce balanceForce) {
        BCDData data = clientData.bcdDataOrTryDefault();
        if(data == null || data.eCOM() == null) return null;

        var expectedCOM = CenterMassCalculator.expectedCenterOfMass(clientData, subLevel);
        if(balanceForce.mass() == 0 || expectedCOM.equals(balanceForce.displacement(), CenterMassCalculator.DELTA)) {

            clientData.bcd$updateData(data
                .withWeightStacksByAxis(null)
            );
            return null;
        }
        var stacks = data.weightStacksByAxis;
        boolean[] shouldUpdate = {false};
        data = CenterMassCalculator.checkCache(data, balanceForce, shouldUpdate);
        if(!shouldUpdate[0]) return stacks;
        if(stacks == null) stacks = makeMassStacks();

        stacks[0].clear();
        stacks[1].clear();
        stacks[2].clear();

        Vector3d diff = expectedCOM.sub(balanceForce.displacement(), new Vector3d());
        diff.mul(balanceForce.mass());


        addStacks(stacks[0], VecUtil.X_V, expectedCOM, diff.x);
        addStacks(stacks[1], VecUtil.Y_V, expectedCOM, diff.y);
        addStacks(stacks[2], VecUtil.Z_V, expectedCOM, diff.z);

        clientData.bcd$updateData(
            data = data.withWeightStacksByAxis(stacks)
        );
        return stacks;
    }

    public static boolean equals(double v1, double v2) {
        return CenterMassCalculator.equals2(v1, v2);
    }

    private static void addStacks(Weights weights, Vector3dc uni, Vector3dc expectedCOM, double value0) {
        if(CenterMassCalculator.equals2(value0, 0)) return;
        weights.totalWeight = value0;

        value0 *= 4;
        final int maxIterations = Config.MAX_ITERATION.getAsInt();
        final int maxFixDistance = Config.MAX_FIX_DISTANCE.getAsInt();
        long sig = value0 < 0 ? -1 : 1;
        long value;
        addSmallStacks:
        {
            long rounded = (long) (value0 + Math.copySign(0.5, value0));
            if(equals(rounded, value0)) {
                value = rounded;
                break addSmallStacks;
            }
            value = (long) Math.floor(value0);
            if(CenterMassCalculator.equals2(Math.abs(value0 - value), 1)) value += sig;
            else if(!CenterMassCalculator.equals2(value0, value)) {
                double v1 = (value0 - value) / 4f;

                long sigV1 = v1 < 0 ? -1 : 1;
                v1 = Math.abs(v1);
                double dst0 = v1 / 0.25;

                var unit = uni.mul(sigV1, new Vector3d());
                for(double dst = dst0; dst < maxFixDistance && v1 < Math.abs(value0); dst += 4, v1 += 1) {
                    weights.smallStacks.add(new MassStack(new Vector3d(unit).mul(dst).add(expectedCOM), 0.25));
                }
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

    public static @NotNull BalanceForce mergedBalancePointOrCOM(DiagramScreen screen) {
        var accessors = BAccessors.accessors(screen);
        SubLevelDependenciesData dependenciesData = accessors.bcd$subLevelDependencies();
        WithClientData clientData = accessors.bcd$clientData();
        return mergedBalancePointOrCOM(dependenciesData, clientData);
    }

    public static @NotNull BalanceForce mergedBalancePointOrCOM(SubLevelDependenciesData dependenciesData, WithClientData clientData) {
        BCDData readOnly = clientData.bcdDataNotNull_readOnly();
        var balanceForce = calculateMergedBalancePoint(dependenciesData, readOnly);
        DiagramDataPacket diagramDataPacket = dependenciesData.mergedForces(readOnly);
        if(balanceForce != null) return balanceForce;
        return new BalanceForce(
            CenterMassCalculator.centerOfMass(dependenciesData.mainSubLevel()),
            diagramDataPacket != null ? diagramDataPacket.mass() : 0,
            true
        );
    }

    public static BalanceForce calculateMergedBalancePoint(SubLevelDependenciesData dependenciesData, BCDData data) {
        if(dependenciesData == null || !data.shouldMergedForcesWithConnectedSublevels) return null;
        ConstraintTree constraintTree = dependenciesData.constraintTree;
        SubLevelGroup levelGroup = constraintTree.subLevelGroup;
        SubLevel mainSublevel = levelGroup.first();


        EnabledForces enabledForces = data.enabledForces.addBalance(BalanceForceFilter.GRAVITY_FORCE_FILTER);
        //only gravity
        if(enabledForces.tree.onlyGravity()) {
            CenterMassMerger centerMassMerger = new CenterMassMerger();
            centerMassMerger.reset();
            boolean wasError = false;
            for(SubLevel subLevel : levelGroup) {
                DiagramDataPacket sublevelData = dependenciesData.get(subLevel).data;
                if(sublevelData == null) {
                    wasError = true;
                    break;
                }
                Vector3d com = VecUtil.projectPos(mainSublevel.logicalPose(), subLevel.logicalPose(), CenterMassCalculator.centerOfMass(subLevel));
                centerMassMerger.add(com, sublevelData.mass());
            }
            if(wasError) return null;

            centerMassMerger.finish();
            return new BalanceForce(centerMassMerger.centerOfMass, centerMassMerger.mass, true);
        }
        Forces forces = dependenciesData.collectAllForces(constraintTree);
        if(forces == null) return null;
        var merger = new CenterMassMerger();
        merger.reset();

        Vector3d gravity = DimensionPhysicsData.getGravity(mainSublevel.getLevel(), CenterMassCalculator.centerOfMass(mainSublevel));
        double length = gravity.length();
        boolean justGravity = true;
        var filterTree = enabledForces.tree;
        for(var forceEntry : forces.entrySet()) {
            ForceGroup forceGroup = forceEntry.getKey();
            if(!filterTree.couldMatch(forceGroup)) continue;
            boolean isGravity = forceGroup == ForceGroups.GRAVITY.get();
            for(QueuedForceGroup.PointForce point : forceEntry.getValue()) {
                if(filterTree.match(forceGroup, point).matched()) {
                    justGravity &= isGravity;
                    merger.add(point.point(), point.force().length() / length);
                }
            }
        }
        merger.finish();
        return new BalanceForce(merger.centerOfMass, merger.mass, justGravity);
    }

    public static Weights[] recalculateStacks(DiagramScreen screen) {
        var accessors = BAccessors.accessors(screen);
        DiagramDataPacket diagramDataPacket = accessors.bcd$serverData();
        if(diagramDataPacket == null) return null;
        WithClientData clientData = accessors.bcd$clientData();
        return recalculateStacks(clientData, screen.subLevel, mergedBalancePointOrCOM(screen));
    }

}
