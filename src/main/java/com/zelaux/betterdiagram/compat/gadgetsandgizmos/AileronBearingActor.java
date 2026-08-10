package com.zelaux.betterdiagram.compat.gadgetsandgizmos;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.FixedForceMapper;
import com.zelaux.betterdiagram.util.ReflectionHelper;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import lombok.Lombok;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@AllArgsConstructor
public class AileronBearingActor implements ActorHandler {
    static final Lazy<Object[]> Head_values = Lazy.of(() -> {
        try {
            Class<?> name = Class.forName("com.rieno.gadgetsandgizmos.content.AileronBearingBlockEntity$Head");
            return name.getEnumConstants();
        } catch(ClassNotFoundException e) {
            throw Lombok.sneakyThrow(e);
        }
    });
    static final Lazy<Function<BlockEntitySubLevelActor, EnumMap<?, ?>>> stateGetter = Lazy.of(() ->
        ReflectionHelper.locateGetter("com.rieno.gadgetsandgizmos.content.AileronBearingBlockEntity", "heads")
    );

    static final Lazy<Function<Object, UUID>> mountedSubLevelId_Getter = Lazy.of(() ->
        ReflectionHelper.locateGetter("com.rieno.gadgetsandgizmos.content.AileronBearingBlockEntity$HeadState", "mountedSubLevelId")
    );

    BlockEntitySubLevelActor actor;

    @Override
    public @Nullable Iterable<? extends AbstractForceMapper> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        Level level = ((BlockEntity) actor).getLevel();
        if(level == null) return null;
        SubLevelContainer container = SubLevelContainer.getContainer(level);
        if(container == null) return null;
        ObjectArrayList<AbstractForceMapper> mappers = new ObjectArrayList<>();
        Function<Object, UUID> subLevelIdGetter = mountedSubLevelId_Getter.get();
        for(Object value : stateGetter.get().apply(actor).values()) {
            SubLevel subLevel = container.getSubLevel(subLevelIdGetter.apply(value));
            if(subLevel == null) continue;
            mappers.add(new FixedForceMapper(
                AbstractForceMapper.sublevelInfo(
                    a, subLevel, subLevel2Group
                )
            ));
        }

        return mappers.isEmpty() ? null : mappers;
    }
}
