package com.zelaux.betterdiagram.mixin.feature.client_mass_tracker;

import com.zelaux.betterdiagram.feature.client_mass_tracker.MassTrackerLevelPlotI;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;
import org.spongepowered.asm.mixin.*;

@Mixin(ServerLevelPlot.class)
public abstract class MassTracker_ServerLevelPlot implements MassTrackerLevelPlotI {
    @Shadow
    public abstract ServerSubLevel getSubLevel();

    @Override
    public MassTracker bcd$massTracker() {
        return getSubLevel().getSelfMassTracker();
    }
}
