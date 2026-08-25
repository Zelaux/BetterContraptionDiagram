package com.zelaux.betterdiagram.compat.gadgetsandgizmos;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.FixedForceMapper;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class PhysicsGantryShaftActor implements ActorHandler {
    BlockEntitySubLevelActor actor;

    @Override
    public @Nullable Iterable<? extends AbstractForceMapper> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        if(!(actor instanceof IPhysicsGantryShaft shaft)) return null;
        List<? extends BlockEntitySubLevelActor> carriages = shaft.getConnectedCarriages();
        if(carriages.isEmpty()) return null;
        var mappers = new ObjectArrayList<AbstractForceMapper>();
        for(BlockEntitySubLevelActor has : carriages) {


            BlockEntity blockEntity = (BlockEntity) has;
            SubLevel b = Sable.HELPER.getContaining(blockEntity);
            if(b == null) continue;
            mappers.add(new FixedForceMapper(AbstractForceMapper.sublevelInfo(
                a, b, subLevel2Group
            )));
        }
        if(mappers.isEmpty()) return null;
        return mappers;

    }
}
