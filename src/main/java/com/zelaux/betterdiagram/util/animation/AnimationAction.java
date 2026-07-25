package com.zelaux.betterdiagram.util.animation;

import com.zelaux.betterdiagram.func.FloatConsumer;


public class AnimationAction extends Animation {
    public FloatConsumer action;
    public Runnable onFinish;

    public float totalTicks;
    public float ticksPassed = 0;

    public AnimationAction(FloatConsumer action, Runnable onFinish, float totalTicks) {
        this.action = action;
        this.onFinish = onFinish;
        this.totalTicks = totalTicks;
    }

    public float progress() {
        float progress = ticksPassed / totalTicks;
        return progress * scl + (1 - scl) * (1-progress);
    }

    private float scl = 1;

    @Override
    public boolean tick(float partialTicks, AnimationUnusedTimeLeft unusedTime) {
        ticksPassed = ticksPassed + partialTicks;
        float extra = Math.max(0, ticksPassed - totalTicks);
        ticksPassed-=extra;
        action.consume(progress());
        if(extra > 0) {
            unusedTime.value = extra;
            onFinish.run();
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        ticksPassed = 0;
    }

    @Override
    public void reverse() {
        scl = 1 - scl;
    }

    @Override
    public Animation invokeAfterAll(Animation... animations) {
        AnimationList list = new AnimationList();
        list.animations.ensureCapacity(1 + animations.length);
        list.animations.add(this);
        return list.invokeAfterAll(animations);
    }

    public void resetReverse() {
        scl = 1;
    }
}
