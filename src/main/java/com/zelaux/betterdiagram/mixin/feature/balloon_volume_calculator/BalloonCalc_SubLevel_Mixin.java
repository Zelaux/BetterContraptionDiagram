package com.zelaux.betterdiagram.mixin.feature.balloon_volume_calculator;

import com.zelaux.betterdiagram.feature.balloon_volume_calculator.api.IBalloonCalc_SubLevel;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.*;

@Mixin(SubLevel.class)
public class BalloonCalc_SubLevel_Mixin implements IBalloonCalc_SubLevel {
    @Unique
    public final ObjectArrayList<Balloon> bcd$locatedBalloons=new ObjectArrayList<>();

    @Override
    public ObjectArrayList<Balloon> bcd$locatedBalloons() {
        return bcd$locatedBalloons;
    }

    @Override
    public boolean bcd$addBalloon(Balloon balloon) {
        return bcd$locatedBalloons.add(balloon);
    }

    @Override
    public boolean bcd$removeBalloon(Balloon balloon) {
        return bcd$locatedBalloons.remove(balloon);
    }
}
