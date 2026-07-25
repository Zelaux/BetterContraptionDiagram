package com.zelaux.betterdiagram.util.animation;

import lombok.AllArgsConstructor;

import java.util.function.BooleanSupplier;

@AllArgsConstructor
public class WaitExternalAnimation extends Animation{
    public BooleanSupplier function;
    @Override
    public boolean tick(float partialTicks, AnimationUnusedTimeLeft unusedTime) {
        boolean finish = function.getAsBoolean();
        if(finish){
            unusedTime.value=partialTicks;
        }
        return finish;
    }

    @Override
    public void reset() {

    }

    @Override
    public void reverse() {

    }

    @Override
    public Animation invokeAfterAll(Animation... animations) {
        return list(this).invokeAfterAll(animations);
    }
}
