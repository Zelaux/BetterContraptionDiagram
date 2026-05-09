package com.zelaux.betterdiagram;

import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import net.minecraft.network.chat.Component;

public class Content {
    public static final ForceGroup AXIS_X_GROUP = new ForceGroup(Component.translatable("better_contraption_diagram.axis_group.x"), null, 0xE65751, false);
    public static final ForceGroup AXIS_Y_GROUP = new ForceGroup(Component.translatable("better_contraption_diagram.axis_group.y"), null, 0x88E661, false);
    public static final ForceGroup AXIS_Z_GROUP = new ForceGroup(Component.translatable("better_contraption_diagram.axis_group.z"), null, 0x8E88E6, false);
    public static final ForceGroup[] AXIS_GROUPS = {AXIS_X_GROUP,AXIS_Y_GROUP,AXIS_Z_GROUP};
}
