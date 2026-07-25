package com.zelaux.betterdiagram.util.animation;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;

public class AnimationList extends Animation {
    public ObjectArrayList<Animation> animations = new ObjectArrayList<>();
    protected int current = 0;
    protected final AnimationUnusedTimeLeft ownTimeLeft = new AnimationUnusedTimeLeft();

    @Override
    public boolean tick(float partialTicks, AnimationUnusedTimeLeft timeLeft) {
        if(current >= animations.size()) {
            timeLeft.value = partialTicks;
            return true;
        }
        timeLeft.value = 0;

        var ownTimeLeft = this.ownTimeLeft;
        ownTimeLeft.value = partialTicks;
        do {
            if(!animations.get(current).tick(ownTimeLeft.value, ownTimeLeft)) break;

            current++;
            if(current >= animations.size()) {
                timeLeft.value = ownTimeLeft.value;
                return true;
            }

        } while(ownTimeLeft.value > 0);
        return false;
    }

    @Override
    public void reset() {
        int len = Math.min(current,animations.size());
        for(int i = 0; i < len; i++) {
            animations.get(i).reset();
        }
        current=0;
    }

    @Override
    public void reverse() {
        var list = animations;
        int size = list.size(),lastI=size-1;
        for(int i = 0; i < size / 2; i++) {
            var a = list.get(i);
            var b = list.get(lastI-i);
            list.set(lastI-i,a);
            list.set(i,b);
        }
        for(Animation animation : list) {
            animation.reverse();
        }
        current=lastI-current;
    }

    @Override
    public AnimationList invokeAfterAll(Animation... animations) {
        if(animations.length==0)return this;
        ObjectArrayList<Animation> myList = this.animations;
        myList.ensureCapacity(myList.size()+animations.length);
        Collections.addAll(myList, animations);
        return this;
    }

    public int currentAnim() {
        return current;
    }
}
