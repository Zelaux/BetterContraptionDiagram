package com.zelaux.betterdiagram.compat.gadgetsandgizmos;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.FixedForceMapper;
import com.zelaux.betterdiagram.util.ReflectionHelper;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import lombok.AllArgsConstructor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@AllArgsConstructor
public class VectorBearingActor implements ActorHandler {
    BlockEntitySubLevelActor actor;
    public static final Lazy<Function<BlockEntitySubLevelActor, UUID>> uuidGetter = Lazy.of(() -> ReflectionHelper.locateGetter(
        "com.rieno.gadgetsandgizmos.content.VectorBearingBlockEntity",
        "mountedSubLevelId"
    ));

    @Override
    public @Nullable Iterable<? extends AbstractForceMapper> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        UUID mountedSubLevelId = uuidGetter.get().apply(actor);
        if(mountedSubLevelId == null) return null;
        Level level = ((BlockEntity) actor).getLevel();
        if(level == null) return null;
        var container = SubLevelContainer.getContainer(level);
        if(container == null) return null;
        SubLevel mountedSubLevel = container.getSubLevel(mountedSubLevelId);
        if(mountedSubLevel == null || mountedSubLevel.isRemoved()) return null;
        return List.of(
            new FixedForceMapper(AbstractForceMapper.sublevelInfo(
                a, mountedSubLevel, subLevel2Group
            ))
        );
    }
}
