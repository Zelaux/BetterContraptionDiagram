package com.zelaux.betterdiagram.compat.simulatedcoasters.actor;

import com.zelaux.betterdiagram.compat.simulatedcoasters.RivetClientIndex;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.FixedForceMapper;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class RivetsActor implements ActorHandler {
    @Override
    public @Nullable Iterable<? extends AbstractForceMapper> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        ObjectArrayList<AbstractForceMapper> mappers = new ObjectArrayList<>();
        SubLevelContainer container = SubLevelContainer.getContainer(a.getLevel());
        if(container == null) return null;
        RivetClientIndex.locatedRivets(a, rivet -> {
            UUID uuid = rivet.anchorKey().host().hostSubLevelId();
            if(uuid == null) return;
            SubLevel b = container.getSubLevel(uuid);
            if(b == null) return;
            mappers.add(new FixedForceMapper(AbstractForceMapper.sublevelInfo(a, b, subLevel2Group)));
        });
        RivetClientIndex.connectedRivets(a, rivet -> {
            SubLevel b = Sable.HELPER.getContaining((BlockEntity) rivet);
            if(b == null) return;
            mappers.add(new FixedForceMapper(AbstractForceMapper.sublevelInfo(a, b, subLevel2Group)));
        });

        return mappers.isEmpty() ? null : mappers;
    }
}
