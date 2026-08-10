package com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.LocalConstraint;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.FixedForceMapper;
import com.zelaux.betterdiagram.mixin.accessors.ISwivelBearingBlockEntity;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.constraint.FixedConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintConfiguration;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;


@AllArgsConstructor
public class FixedActor implements ActorHandler {
    BlockEntitySubLevelActor entity;

    @Override
    public @Nullable List<? extends AbstractForceMapper> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        Iterable<@NotNull SubLevel> subLevels = entity.sable$getConnectionDependencies();
        if(subLevels == null) return null;
        ObjectArrayList<FixedForceMapper> mappers = new ObjectArrayList<>();
        var info = new AbstractForceMapper.SubLevelInfo(a, subLevel2Group.get(a), null, null);
        for(SubLevel subLevel : subLevels) {
            if(subLevel == a) continue;
            mappers.add(new FixedForceMapper(info.withTarget(subLevel,subLevel2Group.get(subLevel))));
        }
        return mappers.isEmpty() ? null : mappers;
    }

}
