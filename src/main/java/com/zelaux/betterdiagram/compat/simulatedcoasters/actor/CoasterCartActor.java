package com.zelaux.betterdiagram.compat.simulatedcoasters.actor;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.LocalConstraint;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.client.cart.CoasterCartTrainLinkClientState;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkSync;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class CoasterCartActor implements ActorHandler {

    @Override
    public @Nullable Iterable<? extends AbstractForceMapper> mapToConstraint(SubLevel a, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        Level level = a.getLevel();
        SubLevelContainer container = ServerSubLevelContainer.getContainer(level);
        if(container == null) return null;
        ResourceKey<Level> dimension = level.dimension();
        List<CoasterCartTrainLinkSync.LinkEntry> entries = CoasterCartTrainLinkClientState.snapshot(dimension).get(a.getUniqueId());
        if(entries==null || entries.isEmpty()) return null;
        ObjectArrayList<AbstractForceMapper> list = new ObjectArrayList<>();
        for(CoasterCartTrainLinkSync.LinkEntry entry : entries) {
            SubLevel b = container.getSubLevel(entry.partnerId());
            if(b == null) continue;
            CoasterCartTrainLinkConstraint.ActiveLink link = new CoasterCartTrainLinkConstraint.ActiveLink(dimension, a.getUniqueId(), b.getUniqueId(), entry.centerDistance());
            GenericConstraintConfiguration genericConstraintConfiguration = CoasterCartTrainLinkConstraint.bcd$updateConstraint(null, link, a, b);
            if(genericConstraintConfiguration == null) continue;
            list.add(LocalConstraint.from(genericConstraintConfiguration, a, b, subLevel2Group));
        }
        //CoasterCartResolve.isCartSubLevel()
        return list.isEmpty() ? null : list;
    }
}
