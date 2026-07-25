package com.zelaux.betterdiagram.util.animation;

import com.zelaux.betterdiagram.func.FloatConsumer;
import com.zelaux.betterdiagram.struct.BAnimatedTexture;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public abstract class Animation {

    public static final @Nullable Runnable DUMMY_RUNNABLE = () -> {};

    /**
     * @return isFinished or not
     *
     */
    public boolean tick(float partialTicks) {
        return tick(partialTicks, AnimationUnusedTimeLeft.NIL);
    }

    public abstract boolean tick(float partialTicks, AnimationUnusedTimeLeft unusedTime);

    public abstract void reset();

    public abstract void reverse();
    public abstract Animation invokeAfterAll(Animation... animations);


    public static AnimationList list(Animation... animations){
        return new AnimationList().invokeAfterAll(animations);
    }
    public static AnimationAction action(@Nullable FloatConsumer consumer, @Nullable Runnable afterDone, float totalTicks){
        if(consumer==null)consumer=FloatConsumer.DUMMY;
        if(afterDone==null)afterDone= DUMMY_RUNNABLE;
        return new AnimationAction(
            consumer,afterDone,totalTicks
        );
    }
    public static WaitExternalAnimation wait(BooleanSupplier supplier){
        return new WaitExternalAnimation(supplier);
    }
}
