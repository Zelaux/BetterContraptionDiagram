package com.zelaux.betterdiagram.feature.balance;

import com.zelaux.betterdiagram.util.animation.Animation;
import com.zelaux.betterdiagram.util.animation.AnimationAction;
import com.zelaux.betterdiagram.util.animation.AnimationList;
import net.createmod.catnip.animation.AnimationTickHolder;

public class BalanceListSwitchAnimation {
    private static final AnimationAction
        fillAction = Animation.action(null, null, 2),
        newIconAction = Animation.action(null, null, 1),
        hiding = Animation.action(null, null, 0.5f);
    private enum State{
        Wait,
        Appearing,
        Hiding
    }
    private static State state=State.Wait;

    private static final AnimationList appearingList = Animation.list(
        Animation.wait(() -> state==State.Appearing),
        fillAction,
        newIconAction
    );
    private static final AnimationList hidingList = Animation.list(
        Animation.wait(() -> state==State.Hiding),
        hiding
    );
    public static void show(){
        switch(state) {
            case Appearing -> {
                return;
            }
            case Hiding -> hiding.reverse();
            case Wait -> {}
        }
        state=State.Appearing;
    }
    public static void hide(){
        switch(state) {
            case Hiding,Wait -> {
                return;
            }
            case Appearing -> hiding.resetReverse();
        }
        state=State.Hiding;

    }

    /**
     * @return -1, [0-1]
     * */
    public static float textureProgress(){
        if(appearingList.currentAnim()<1) return -1;
        return fillAction.progress();
    }

    /**
     * @return -1, [0-1]
     * */
    public static float newIconAppearProgress(){
        if(appearingList.currentAnim()<2) return -1;
        return newIconAction.progress();
    }


    /**
     * @return [0-1]
     * */
    public static float hidingProgress(){
        return hiding.progress();
    }

    public static void tick() {
        float partialTicks = AnimationTickHolder.getPartialTicks();
        appearingList.tick(partialTicks);
        if(hidingList.tick(partialTicks) && state==State.Hiding) {
            state=State.Wait;
            reset();
        }
    }

    public static void reset() {
        appearingList.reset();
        hidingList.reset();
    }
}
