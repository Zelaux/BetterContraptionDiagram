package com.zelaux.betterdiagram.feature.balloon_volume_calculator;

import com.zelaux.betterdiagram.feature.balloon_volume_calculator.api.IBalloonCalc_SubLevel;
import com.zelaux.betterdiagram.mixin.accessors.BalloonAccessors;
import com.zelaux.betterdiagram.mixin.accessors.LevelAcceleratorAccessors;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.ClientBalloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.ServerBalloon;
import dev.ryanhcode.sable.Sable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class BalloonIndexUpdate {
    static final Object2ObjectOpenHashMap<ClientBalloon, BlockPos> clientBalloons = new Object2ObjectOpenHashMap<>();
    static final Object2ObjectOpenHashMap<ServerBalloon, BlockPos> serverBalloons = new Object2ObjectOpenHashMap<>();

    public static void ticket(Balloon balloon, BlockPos oldPosition) {
        if(balloon instanceof ClientBalloon clientBalloon) {
            clientBalloons.putIfAbsent(clientBalloon, oldPosition);
        } else if(balloon instanceof ServerBalloon serverBalloon) {
            serverBalloons.putIfAbsent(serverBalloon, oldPosition);
        }
    }

    public static void serverTick() {
        updateBalloons(serverBalloons);
    }

    public static void clientTick() {
        updateBalloons(clientBalloons);
    }

    private static void updateBalloons(Object2ObjectOpenHashMap<? extends Balloon, BlockPos> balloons) {
        balloons.forEach((clientBalloon, blockPos) -> {
            var levelAccelerator = (LevelAcceleratorAccessors) ((BalloonAccessors) clientBalloon).bcd$accelerator();
            Level level = levelAccelerator.bcd$level();
            if(blockPos != null) containing(level, blockPos, it -> it.bcd$removeBalloon(clientBalloon));

            containing(level, clientBalloon.getControllerPos(), it -> it.bcd$addBalloon(clientBalloon));
        });
        balloons.clear();
    }

    private static void containing(Level level, BlockPos blockPos, Consumer<@NotNull IBalloonCalc_SubLevel> consumer) {
        if(Sable.HELPER.getContaining(level, blockPos) instanceof IBalloonCalc_SubLevel calculatorSubLevel) {
            consumer.accept(calculatorSubLevel);
        }
    }
}
