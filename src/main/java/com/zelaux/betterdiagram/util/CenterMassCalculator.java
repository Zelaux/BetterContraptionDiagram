package com.zelaux.betterdiagram.util;

import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.data.OffCenterBlocksShowState;
import com.zelaux.betterdiagram.data.OffCenteredBlock;
import com.zelaux.betterdiagram.extend.ClientData;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.extend.accessors.DiagramScreenAccessors;
import com.zelaux.betterdiagram.extend.accessors.DiagramStickyNoteAccessors;
import com.zelaux.betterdiagram.feature.balance.BalanceForce;
import com.zelaux.betterdiagram.feature.balance.BalanceModel;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import com.zelaux.betterdiagram.index.BCDForceGroups;
import com.zelaux.betterdiagram.struct.Weights;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.strategy.IdentityStrategy;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Runtime;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CenterMassCalculator {
    public static  float DELTA = 0.0001f;
    public static final Set<ForceGroup> FORCE_GROUP_BLACKLIST;

    static {
        var forceGroupBlacklist = new ObjectOpenCustomHashSet<ForceGroup>(IdentityStrategy.IDENTITY);
        //forceGroupBlacklist.add(ForceGroups.GRAVITY.get());
        forceGroupBlacklist.add(BCDForceGroups.MERGED_FORCE_GROUP.group);
        forceGroupBlacklist.add(BCDForceGroups.CONTRAPTION_GRAVITY.group);

        FORCE_GROUP_BLACKLIST = forceGroupBlacklist;
    }

    public static @NotNull Vector3dc centerOfMass(SubLevel subLevel) {
        return subLevel.logicalPose().rotationPoint();
    }


    public static @NotNull BCDData checkCache(BCDData data, BalanceForce balanceForce, boolean[] shouldUpdate) {
        Cache cache = data.cache;
        if(cache == null || !cache.isApproxEquals(data.eCOM(), balanceForce.displacement(), balanceForce.mass())) {
            data = data.withCache(Cache.create(data.eCOM(), balanceForce.displacement(), balanceForce.mass()));
            shouldUpdate[0] = true;
        } else {
            shouldUpdate[0] = false;
        }
        return data;
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

    public static boolean equals3(double v1, double v2) {
        return Runtime.equals(v1, v2, 0.001f);
    }

    public static boolean equals2(double v1, double v2) {
        return Runtime.equals(v1, v2, 0.01f);
    }

    public static boolean equals(Vector3dc COM1, Vector3dc COM2) {
        if(COM1 == null) return COM2 == null;
        return COM1.equals(COM2, DELTA);
    }

    //TODO: candidate for removal
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

    public static @Nullable ArrayList<OffCenteredBlock> getOrCreateOffCenteredBlocks(@NotNull DiagramScreen self) {
        WithClientData clientData = accessors(self).betterContraptionDiagram$clientData();
        var data = clientData.bcdDataOrTryDefault();

        DiagramDataPacket serverData = accessors(self).betterContraptionDiagram$serverData();
        if(serverData == null || data == null) return null;
        if(data.offCenterBlocksShowState == OffCenterBlocksShowState.none) {
            clientData.bcdiagram$updateData(data.withOffCenteredBlocks(null));
            return null;
        }
        boolean[] shouldUpdate = {false};
        data = checkCache(data, BalanceModel.getMergedBalancePointOrCOM(self), shouldUpdate);
        if(data.offCenteredBlocks != null && !shouldUpdate[0]) return data.offCenteredBlocks;
        ArrayList<OffCenteredBlock> offCenteredBlocks = findOffCenteredBlocks(
            clientData,
            self.subLevel,
            data.axisStatesAsArray(null),
            new ArrayList<>(),
            data.offCenterBlocksShowState == OffCenterBlocksShowState.show ?
                BalanceModel.recalculateStacks(self) :
                null
        );
        if(offCenteredBlocks.isEmpty()) return null;
        clientData.bcdiagram$updateData(
            data.withOffCenteredBlocks(offCenteredBlocks)
        );


        return offCenteredBlocks;
    }


    public static Vector3d locateMergedCenterOfMass(Vector3d COM, SubLevelDependenciesData dependenciesData, @NotNull BCDData data) {
        if(dependenciesData == null || !data.shouldMergedForcesWithConnectedSublevels) return COM;
        SubLevelGroup levelGroup = dependenciesData.constraintTree.subLevelGroup;
        Forces forces = dependenciesData.collectFixedForces(levelGroup);
        if(forces == null) return COM;
        List<QueuedForceGroup.PointForce> pointForces = forces.get(ForceGroups.GRAVITY.get());
        if(pointForces == null) return COM;

        return COM.set(pointForces.getFirst().point());
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
