package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import com.google.common.collect.Iterables;
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

    static Iterable<ActorHandler> handlersFor(SubLevel current) {

        Iterable<ActorHandler> transform = Iterables.transform(current.getPlot().getBlockEntityActors(), ActorHandler::findFor);
        Iterable<ActorHandler> extra = ActorHandlerPollution.withoutBlockEntities(current);
        if(extra!=null)transform=Iterables.concat(transform,extra);
        return transform;
    }


    @Nullable
    Iterable<? extends AbstractForceMapper> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group);

}
