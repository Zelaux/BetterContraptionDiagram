package com.zelaux.betterdiagram.feature.balloon_volume_calculator;

import com.zelaux.betterdiagram.feature.balloon_volume_calculator.api.IBalloonCalc_ClientBalloon;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.api.IBalloonCalc_SubLevel;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import dev.eriksonn.aeronautics.content.blocks.hot_air.BlockEntityLiftingGasProvider;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.ServerBalloon;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


public class Balloons implements Iterable<BalloonEntry> {
    SubLevelDependenciesData dependenciesData;

    public Balloons(SubLevelDependenciesData dependenciesData) {
        this.dependenciesData = dependenciesData;
    }

    List<BalloonEntry> cache;

    private @NotNull List<BalloonEntry> findBalloons() {
        ClientSubLevel mainSublevel = (ClientSubLevel) dependenciesData.mainSubLevel();

        ClientLevel level = mainSublevel.getLevel();
        var balloons = dependenciesData
            .allSublevels()
            .map(it -> (IBalloonCalc_SubLevel) it)
            .map(IBalloonCalc_SubLevel::bcd$locatedBalloons)
            .flatMap(Collection::stream)
            .distinct()
            .map(balloon -> {
                if(balloon instanceof IBalloonCalc_ClientBalloon clientBalloon) {
                    return balloonEntryOrNull(level, balloon, clientBalloon::bcd$getCenter);
                } else if(balloon instanceof ServerBalloon serverBalloon) {
                    return balloonEntryOrNull(level, balloon, it -> JOMLConversion.toJOML(serverBalloon.getCenter(), it));
                }

                //So, I dont think this is realy useful
                //TODO: removal candidate
                BlockEntityLiftingGasProvider.ClientBalloonInfo info = findClientBalloonInfo(balloon);
                if(info!=null){
                    return balloonEntryOrNull(level, balloon, it -> JOMLConversion.toJOML(info.gasCenter(), it));
                }

                return balloonEntryOrNull(level, balloon, null);
            })
            .filter(Objects::nonNull)
            .toList();
        return balloons;
    }

    private BlockEntityLiftingGasProvider.@Nullable ClientBalloonInfo findClientBalloonInfo(Balloon balloon) {
        for(BlockEntityLiftingGasProvider heater : balloon.getHeaters()) {
            for(Field field : heater.getClass().getDeclaredFields()) {
                if(field.getType() == BlockEntityLiftingGasProvider.ClientBalloonInfo.class) {
                    try {
                        field.setAccessible(true);
                        var info = (BlockEntityLiftingGasProvider.ClientBalloonInfo) field.get(heater);
                        if(info != null) return info;
                    } catch(IllegalAccessException ignored) {
                    }
                }
            }
        }
        return null;
    }

    private @Nullable BalloonEntry balloonEntryOrNull(Level level, Balloon balloon, BalloonEntry.GasCenterInfo info) {
        SubLevel subLevel = Sable.HELPER.getContaining(level, balloon.getControllerPos());
        if(subLevel != null)
            return new BalloonEntry(balloon, info, subLevel);
        return null;
    }

    int lastUpdateTick = Integer.MIN_VALUE;

    public List<BalloonEntry> asList() {
        int ticks = AnimationTickHolder.getTicks(true);
        if(cache == null || ticks - lastUpdateTick > DiagramScreen.UPDATE_REQUEST_INTERVAL) {
            cache = findBalloons();
            lastUpdateTick = ticks;
        }
        return cache;
    }

    @Override
    public @NotNull Iterator<BalloonEntry> iterator() {
        return asList().iterator();
    }
}
