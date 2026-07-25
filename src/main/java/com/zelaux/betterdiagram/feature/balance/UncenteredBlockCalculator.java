package com.zelaux.betterdiagram.feature.balance;

import com.zelaux.betterdiagram.data.OffCenterBlocksShowState;
import com.zelaux.betterdiagram.data.OffCenteredBlock;
import com.zelaux.betterdiagram.extend.BAccessors;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.struct.Weights;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.ArrayList;

public class UncenteredBlockCalculator {
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
                            if(enabled[i] && !CenterMassCalculator.equals(VecUtil.GETTERS_3d[i].get(blockCenterOfMass), 0.5))
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
        WithClientData clientData = BAccessors.accessors(self).bcd$clientData();
        var data = clientData.bcdDataOrTryDefault();

        DiagramDataPacket serverData = BAccessors.accessors(self).bcd$serverData();
        if(serverData == null || data == null) return null;
        if(data.offCenterBlocksShowState == OffCenterBlocksShowState.none) {
            clientData.bcd$updateData(data.withOffCenteredBlocks(null));
            return null;
        }
        boolean[] shouldUpdate = {false};
        data = CenterMassCalculator.checkCache(data, BalanceModel.mergedBalancePointOrCOM(self), shouldUpdate);
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
        clientData.bcd$updateData(
            data.withOffCenteredBlocks(offCenteredBlocks)
        );


        return offCenteredBlocks;
    }
}
