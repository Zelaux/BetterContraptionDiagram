package com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler;

import com.zelaux.betterdiagram.extend.IRopeStrandHolderBlockEntity;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.RopeForceMapper;
import com.zelaux.betterdiagram.mixin.accessors.RopeStrandHolderBehaviorAccessors;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientLevelRopeManager;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopeStrand;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
public class RopeActor implements ActorHandler {
    RopeStrandHolderBlockEntity ropeStrandHolderBlockEntity;

    @Override
    public @Nullable List<AbstractForceMapper> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        ObjectArrayList<SubLevel> list = getConnectionDependencies();

        RopeStrandHolderBehavior behavior = ropeStrandHolderBlockEntity.getBehavior();
        ClientRopeStrand clientStrand = behavior.getClientStrand();
        if(clientStrand == null && list == null) {
            RopeStrandHolderBehaviorAccessors accessors = (RopeStrandHolderBehaviorAccessors) behavior;

            Level level = behavior.blockEntity.getLevel();
            ClientLevelRopeManager manager = ClientLevelRopeManager.getOrCreate(level);
            UUID uuid = accessors.bcd$attachedRopeID();
            if(uuid != null) {
                ClientRopeStrand strand = manager.getStrand(uuid);
                if(strand != null) {
                    clientStrand = strand;
                    list = collectDependencies(level, clientStrand);
                }
            }
        }

        if(list == null || list.size() == 1) return null;
        boolean startIsOrigin = list.get(0) == a;
        int originI = startIsOrigin ? 0 : 1;

        SubLevel origin = list.get(originI);
        SubLevel toAttach = list.get(1 - originI);
        return List.of(new RopeForceMapper(
            AbstractForceMapper.sublevelInfo(origin, toAttach, subLevel2Group),
            clientStrand,
            startIsOrigin
        ));
    }

    protected @Nullable ObjectArrayList<@NotNull SubLevel> getConnectionDependencies() {
        return getConnectionDependencies(ropeStrandHolderBlockEntity);
    }

    protected static @Nullable ObjectArrayList<@NotNull SubLevel> getConnectionDependencies(RopeStrandHolderBlockEntity entity) {
        if(entity instanceof IRopeStrandHolderBlockEntity iEntity) {
            Iterable<@NotNull SubLevel> levels = iEntity.bcd$sable$getConnectionDependencies();
            return levels == null ? null : new ObjectArrayList<>(levels.iterator());
        }
        return null;
    }

    protected @NotNull ObjectArrayList<SubLevel> collectDependencies(Level level, ClientRopeStrand clientStrand) {
        final var dependencies = new ObjectArrayList<SubLevel>();

        final SubLevel subLevelA = Sable.HELPER.getContaining(level, clientStrand.startAttachment);
        final SubLevel subLevelB = Sable.HELPER.getContaining(level, clientStrand.endAttachment);


        if(subLevelA != null) dependencies.add(subLevelA);
        if(subLevelB != null) dependencies.add(subLevelB);
        return dependencies;
    }

}
