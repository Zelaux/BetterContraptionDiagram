package com.zelaux.betterdiagram.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zelaux.betterdiagram.struct.Weights;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import dev.ryanhcode.sable.companion.impl.SableCompanionUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;

/**
 * Better Contraption Diagram Data
 *
 */
@AllArgsConstructor
@With
@Getter
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class BCDData {
    public static final Codec<BCDData> FULL_CODEC = RecordCodecBuilder.create(i -> i.group(
            SableCompanionUtil.VECTOR_3D_CODEC.optionalFieldOf("eCOM", null).forGetter(k1 -> k1.eCOM_m()),
            Weights.CODEC.listOf().optionalFieldOf("axisWeightStacks", null)
                         .forGetter(k -> arrToList(k.axisWeightStacks)),
            Codec.<CenterMassCalculator.Cache>unit(null).fieldOf("cache").forGetter(x -> x.cache),
        Codec.BOOL.listOf().optionalFieldOf("axisStates", null).forGetter(k1 -> arrToList(k1.axisStates))
        ).apply(i, BCDData::make)
    );

    private static <T> @Nullable List<T> arrToList(@Nullable T[] axisWeightStacks1) {
        return axisWeightStacks1 == null ? null : List.of(axisWeightStacks1);
    }

    private static @Nullable List<Boolean> arrToList(@Nullable boolean[] values) {
        return values == null ? null : List.of(values[0], values[1], values[2]);
    }

    public static final Codec<BCDData> SHORT_CODEC = RecordCodecBuilder.create(i -> i.group(
            SableCompanionUtil.VECTOR_3D_CODEC.optionalFieldOf("eCOM", null).forGetter(BCDData::eCOM_m),
            Codec.BOOL.listOf().optionalFieldOf("axisStates", null).forGetter(k1 -> arrToList(k1.axisStates))
        ).apply(i, (ecom, axisStates) -> make(ecom, null, null, axisStates))

    );


    private static BCDData make(Vector3d x, List<Weights> y, CenterMassCalculator.Cache z, List<Boolean> axisStates) {
        return new BCDData(x,
            y == null ? null : y.toArray(new Weights[3]),
            z,
            axisStates == null ? null : new boolean[]{axisStates.get(0), axisStates.get(1), axisStates.get(2)}
        );
    }

    private Vector3d eCOM_m() {return ((Vector3d) eCOM);}

    @Nullable
    protected Vector3dc eCOM;

    public Weights @Nullable [] axisWeightStacks;
    @Nullable
    public CenterMassCalculator.Cache cache;

    public boolean @Nullable [] axisStates;

    @NotNull
    public static BCDData NULL() {
        return new BCDData(null, null, null,null);
    }

    public BCDData withNoAxisData() {
        return withAxisWeightStacks(null)
            .withCache(null);
    }
}
