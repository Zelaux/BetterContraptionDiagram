package com.zelaux.betterdiagram.feature.balloon_volume_calculator.api;

import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public interface IBalloonCalc_SubLevel {
    ObjectArrayList<Balloon> bcd$locatedBalloons();
    boolean bcd$addBalloon(Balloon balloon);
    boolean bcd$removeBalloon(Balloon balloon);

}
