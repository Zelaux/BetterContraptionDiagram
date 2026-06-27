package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @see BetterContraptionDiagram#BetterContraptionDiagram(IEventBus, ModContainer)
 *
 */
public interface ActorHandler {
    ActorHandler DUMMY = (a, subLevel2Group) -> null;
    Object2ObjectOpenHashMap<Class<? extends BlockEntitySubLevelActor>, Function<BlockEntitySubLevelActor, @NotNull ActorHandler>> actorMake = new Object2ObjectOpenHashMap<>();
    ObjectArrayList<Function<BlockEntitySubLevelActor, @Nullable ActorHandler>> actorTryMake = new ObjectArrayList<>();

    static ActorHandler findFor(BlockEntitySubLevelActor actor) {
        if(actor == null) return DUMMY;
        var function = actorMake.getOrDefault(actor.getClass(), null);
        if(function != null) return function.apply(actor);
        for(Function<BlockEntitySubLevelActor, @Nullable ActorHandler> mapperFunction : actorTryMake) {
            ActorHandler actorHandler = mapperFunction.apply(actor);
            if(actorHandler != null) return actorHandler;
        }
        return DUMMY;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T extends BlockEntitySubLevelActor> void register(Class<? extends T> entityClass, Function<T, ActorHandler> mapper) {
        actorMake.put(entityClass, (Function) mapper);
    }


    @Nullable
    Iterable<? extends AbstractForceMapper> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group);

    @Nullable
    default Iterable<SubLevel> collectWithFixedConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        Iterable<? extends AbstractForceMapper> constraints = mapToConstraint(a, subLevel2Group);
        if(constraints == null) return null;
        List<SubLevel> result = null;
        for(Object obj : constraints) {
            if(!(obj instanceof LocalConstraint<?> constraint) || constraint.kind() != ConstaintKind.Fixed) {
                continue;
            }
            var list = result == null ? result = new ObjectArrayList<>() : result;
            list.add(constraint.toAttachSubLevel());
        }
        return result;
    }
}
