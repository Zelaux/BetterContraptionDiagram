package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import com.zelaux.betterdiagram.BetterContraptionDiagram;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @see BetterContraptionDiagram#BetterContraptionDiagram(IEventBus, ModContainer)
 * */
public interface ActorMapper {
    ActorMapper DUMMY = a -> null;
    Object2ObjectOpenHashMap<Class<? extends BlockEntitySubLevelActor>, Function<BlockEntitySubLevelActor, @NotNull ActorMapper>> actorMake = new Object2ObjectOpenHashMap<>();
    ObjectArrayList<Function<BlockEntitySubLevelActor, @Nullable ActorMapper>> actorTryMake = new ObjectArrayList<>();

    static ActorMapper findFor(BlockEntitySubLevelActor actor) {
        if(actor == null) return DUMMY;
        var function = actorMake.getOrDefault(actor.getClass(), null);
        if(function != null) return function.apply(actor);
        for(Function<BlockEntitySubLevelActor, @Nullable ActorMapper> mapperFunction : actorTryMake) {
            ActorMapper actorMapper = mapperFunction.apply(actor);
            if(actorMapper != null) return actorMapper;
        }
        return DUMMY;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T extends BlockEntitySubLevelActor> void register(Class<?extends T> entityClass, Function<T, ActorMapper> mapper) {
        actorMake.put(entityClass,(Function)mapper);
    }


    @Nullable
    Iterable<LocalConstraint> mapToConstraint(SubLevel a);
}
