package com.zelaux.betterdiagram.util;

import net.createmod.catnip.animation.LerpedFloat;

public class MathUtil {

    public interface Interpolations {

        static float easyOutBounce(float time, float from, float to) {
            float x = time;
            float n1 = 7.5625f;
            float d1 = 2.75f;
            float bounce;

            if(x < 1 / d1) {
                bounce = n1 * x * x;
            } else if(x < 2 / d1) {
                bounce = n1 * (x -= 1.5f / d1) * x + 0.75f;
            } else if(x < 2.5f / d1) {
                bounce = n1 * (x -= 2.25f / d1) * x + 0.9375f;
            } else {
                bounce = n1 * (x -= 2.625f / d1) * x + 0.984375f;
            }

            return bounce * (to - from) + from;
        }
    }
}
