package com.zelaux.betterdiagram.struct;

import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;

@AllArgsConstructor
public enum ComViewType {
    NEVER(false, false, false) {
        @Override
        public boolean shouldShow(boolean isShifting) {return false;}
    },
    ALWAYS(false, false, false),
    SHIFT(true, false, false),
    F3(false, true, false),
    F3_OR_HITBOX(false, true, true) {
        @Override
        public boolean shouldShow(boolean isShifting) {
            var minecraft = Minecraft.getInstance();
            return (!requireF3 || minecraft.getDebugOverlay().showDebugScreen()) ||
                   (!requireHitbox || minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes());
        }
    },
    F3_SHIFT(true, true, false);
    public final boolean requireShift, requireF3, requireHitbox;

    public boolean shouldShow(boolean isShifting) {
        var minecraft = Minecraft.getInstance();
        return
            (!requireShift || isShifting) &&
            (!requireF3 || minecraft.getDebugOverlay().showDebugScreen()) &&
            (!requireHitbox || minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes());
    }
}
