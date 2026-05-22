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
                         .forGetter(k -> k.axisWeightStacks == null ? null : List.of(k.axisWeightStacks)),
            Codec.<CenterMassCalculator.Cache>unit(null).fieldOf("cache").forGetter(x -> x.cache)
        ).apply(i, BCDData::make)
    );
    public static final Codec<BCDData> SHORT_CODEC = RecordCodecBuilder.create(i -> i.group(
            SableCompanionUtil.VECTOR_3D_CODEC.optionalFieldOf("eCOM", null).forGetter(k1 -> k1.eCOM_m())
        ).apply(i, ecom -> BCDData.NULL().withECOM(ecom))

    );

    private static BCDData make(Vector3d x, List<Weights> y, CenterMassCalculator.Cache z) {return new BCDData(x, y == null ? null : y.toArray(new Weights[3]), z);}

    private Vector3d eCOM_m() {return ((Vector3d) eCOM);}

    @Nullable
    protected Vector3dc eCOM;
    @Nullable
    public Weights[] axisWeightStacks;
    @Nullable
    public CenterMassCalculator.Cache cache;

    @NotNull
    public static BCDData NULL() {
        return new BCDData(null, null, null);
    }

    public BCDData withNoAxisData() {
        return withAxisWeightStacks(null)
            .withCache(null);
    }
}
