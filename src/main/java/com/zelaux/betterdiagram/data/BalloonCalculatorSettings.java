package com.zelaux.betterdiagram.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.BalloonCalculationMode;
import lombok.NonNull;
import lombok.With;

import java.util.Optional;
import java.util.OptionalInt;

@With
public record BalloonCalculatorSettings(
    @NonNull
    OptionalInt selectedBalloonY,
    BalloonCalculationMode calculationMode
) {

    public static BalloonCalculatorSettings DEFAULT = new BalloonCalculatorSettings(OptionalInt.empty(), BalloonCalculationMode.playerPosition);

    public static Codec<BalloonCalculatorSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.optionalFieldOf("selectedBalloonY")
                 .xmap(x -> x.map(OptionalInt::of).orElseGet(OptionalInt::empty),
                     x -> x.isEmpty() ? Optional.empty() : Optional.of(x.getAsInt())
                 )
                 .forGetter(BalloonCalculatorSettings::selectedBalloonY),
        BalloonCalculationMode.CODEC.optionalFieldOf("calculationMode", Optional.empty())
                                    .xmap(x -> x.orElse(DEFAULT.calculationMode), Optional::of)
                                    .forGetter(BalloonCalculatorSettings::calculationMode)
    ).apply(i, BalloonCalculatorSettings::new));
}

