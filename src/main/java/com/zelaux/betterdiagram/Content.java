package com.zelaux.betterdiagram;

import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.simulated_team.simulated.Simulated;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Content {
    public static final BCDForceGroup AXIS_X_GROUP = new BCDForceGroup(Component.translatable("better_contraption_diagram.axis_group.x"), null, 0xE65751, false, Component.literal("X"));
    public static final BCDForceGroup AXIS_Y_GROUP = new BCDForceGroup(Component.translatable("better_contraption_diagram.axis_group.y"), null, 0x88E661, false, Component.literal("Y"));
    public static final BCDForceGroup AXIS_Z_GROUP = new BCDForceGroup(Component.translatable("better_contraption_diagram.axis_group.z"), null, 0x8E88E6, false, Component.literal("Z"));
    public static final BCDForceGroup[] AXIS_GROUPS = {AXIS_X_GROUP, AXIS_Y_GROUP, AXIS_Z_GROUP};
    public static final ResourceLocation simulated$bufferLocation = Simulated.path("diagram_buffer");

    @Getter
    public static class BCDForceGroup {
        public final ForceGroup group;
        public final Component axisName;
        public final int color;
        public final @NotNull Component name;
        public final @Nullable Component description;

        public BCDForceGroup(@NotNull Component name, @Nullable Component description, int color, boolean defaultDisplayed, Component axisName) {
            this.axisName = axisName;
            this.color = color;
            this.name = name;
            this.description = description;
            group = new ForceGroup(name, description, color, defaultDisplayed);
        }
    }
}
