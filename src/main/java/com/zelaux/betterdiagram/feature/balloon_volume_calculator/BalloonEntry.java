package com.zelaux.betterdiagram.feature.balloon_volume_calculator;

import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import dev.ryanhcode.sable.sublevel.SubLevel;
import lombok.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

@ToString
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public final class BalloonEntry {
    public final Balloon balloon;
    @Nullable
    public final BalloonEntry.GasCenterInfo gasCenterInfo;
    @NonNull
    public final SubLevel subLevel;


    public interface GasCenterInfo {
        Vector3d getGasCenterInto(Vector3d dest);
    }


}
