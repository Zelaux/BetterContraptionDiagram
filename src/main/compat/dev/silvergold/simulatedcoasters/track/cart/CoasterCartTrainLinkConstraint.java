package dev.silvergold.simulatedcoasters.track.cart;

import com.zelaux.betterdiagram.annotations.SilentDelete;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintHandle;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
@SilentDelete
public final class CoasterCartTrainLinkConstraint {

    public static GenericConstraintConfiguration bcd$updateConstraint(Object physics, ActiveLink link, SubLevel cartA, SubLevel cartB) {
        return null;
    }

    public static final class ActiveLink {
        public final ResourceKey<Level> dimension;
        public final UUID cartA;
        public final UUID cartB;
        public final double centerDistance;
        @Nullable
        public GenericConstraintHandle handle;

        public ActiveLink(ResourceKey<Level> dimension, UUID cartA, UUID cartB, double centerDistance) {
            this.dimension = dimension;
            this.cartA = cartA;
            this.cartB = cartB;
            this.centerDistance = centerDistance;
        }
    }
}
