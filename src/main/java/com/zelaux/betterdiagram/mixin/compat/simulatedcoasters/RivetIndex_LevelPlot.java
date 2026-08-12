package com.zelaux.betterdiagram.mixin.compat.simulatedcoasters;


import com.zelaux.betterdiagram.compat.simulatedcoasters.RivetClientIndex;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.silvergold.simulatedcoasters.rivet.RivetAnchorKey;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.*;

import java.lang.ref.WeakReference;

@Mixin(LevelPlot.class)
public  class RivetIndex_LevelPlot implements RivetClientIndex.ILevelPlot {

    @Unique
    private final Object2ObjectMap<RivetAnchorKey, WeakReference<BlockEntity>> bcd$connectionRivets = new Object2ObjectOpenHashMap<>();
    @Unique
    private final Object2ObjectMap<RivetAnchorKey, WeakReference<BlockEntity>> bcd$ownRivets=new Object2ObjectOpenHashMap<>();

    @Override
    public Object2ObjectMap<RivetAnchorKey, WeakReference<BlockEntity>> bcd$connectedRivets() {
        return bcd$connectionRivets;
    }

    @Override
    public Object2ObjectMap<RivetAnchorKey, WeakReference<BlockEntity>> bcd$locatedInRivets() {
        return bcd$ownRivets;
    }
}
