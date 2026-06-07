package com.zelaux.betterdiagram.gui.widget;

import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.utility.CreateLang;
import com.zelaux.betterdiagram.func.FloatConsumer;
import com.zelaux.betterdiagram.func.FloatFunction;
import com.zelaux.betterdiagram.func.ToFloatFunction;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;


public class FloatScrollInput extends AbstractSimiWidget {
    protected FloatConsumer onScroll;
    protected float state;
    protected Component title = CreateLang.translateDirect("gui.scrollInput.defaultTitle");
    protected final Component CLICK_TO_SET = Component.translatable("better_contraption_diagram.calculator.click-to-select");
    protected final Component scrollToModify = CreateLang.translateDirect("gui.scrollInput.scrollToModify");
    protected final Component shiftScrollsFaster = CreateLang.translateDirect("gui.scrollInput.shiftScrollsFaster");
    protected final Component shiftScrollsSlower = Component.translatable("better_contraption_diagram.gui.scrollInput.ctrlScrollsSlower");
    protected Component hint = null;
    protected Label displayLabel;
    protected boolean inverted;
    protected boolean soundPlayed;
    protected FloatFunction<Component> formatter;

    protected float min, max;
    protected float shiftStep;
    ToFloatFunction<StepContext> step;

    public FloatScrollInput(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn);
        state = 0;
        min = 0;
        max = 1;
        shiftStep = 5;
        step = standardStep();
        formatter = i -> {
            return Component.literal(String.valueOf(i));
        };
        soundPlayed = false;
    }

    public ToFloatFunction<StepContext> standardStep() {
        return c -> c.shift ? shiftStep : 1;
    }

    public FloatScrollInput inverted() {
        inverted = true;
        return this;
    }

    public FloatScrollInput withRange(float min, float max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public FloatScrollInput calling(FloatConsumer onScroll) {
        this.onScroll = onScroll;
        return this;
    }

    public FloatScrollInput format(FloatFunction<Component> formatter) {
        this.formatter = formatter;
        return this;
    }

    public FloatScrollInput removeCallback() {
        this.onScroll = null;
        return this;
    }

    public FloatScrollInput titled(MutableComponent title) {
        this.title = title;
        updateTooltip();
        return this;
    }

    public FloatScrollInput addHint(MutableComponent hint) {
        this.hint = hint;
        updateTooltip();
        return this;
    }

    public static class StepContext {
        public double currentValue;
        public boolean forward;
        public boolean shift;
        public boolean control;
    }

    public FloatScrollInput withStepFunction(ToFloatFunction<StepContext> step) {
        this.step = step;
        return this;
    }

    public FloatScrollInput writingTo(Label label) {
        this.displayLabel = label;
        if(label != null)
            writeToLabel();
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        soundPlayed = false;
    }

    public float getState() {
        return state;
    }

    public FloatScrollInput setState(float state) {
        this.state = state;
        clampState();
        updateTooltip();
        if(displayLabel != null)
            writeToLabel();
        return this;
    }

    public FloatScrollInput withShiftStep(float step) {
        shiftStep = step;
        return this;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if(inverted)
            pScrollY *= -1;

        StepContext context = new StepContext();
        context.control = AllKeys.ctrlDown();
        context.shift = AllKeys.shiftDown();
        context.currentValue = state;
        context.forward = pScrollY > 0;

        double priorState = state;
        boolean shifted = AllKeys.shiftDown();
        double step = ((int) Math.signum(pScrollY)) * this.step.apply(context);

        state += step;
        //if(shifted)
        //    state -= state % shiftStep;

        clampState();

        if(priorState != state) {
            if(!soundPlayed)
                Minecraft.getInstance()
                         .getSoundManager()
                         .play(SimpleSoundInstance.forUI(AllSoundEvents.SCROLL_VALUE.getMainEvent(),
                             1.5f + 0.1f * (state - min) / (max - min)));
            soundPlayed = true;
            onChanged();
        }

        return priorState != state;
    }

    protected void clampState() {
        if(state >= max)
            state = max;
        if(state < min)
            state = min;
    }

    public void onChanged() {
        if(displayLabel != null)
            writeToLabel();
        if(onScroll != null)
            onScroll.consume(state);
        updateTooltip();
    }

    protected void writeToLabel() {
        displayLabel.text = formatter.apply(state);
    }

    protected void updateTooltip() {
        toolTip.clear();
        if(title == null)
            return;
        toolTip.add(title.plainCopy()
                         .withStyle(s -> s.withColor(HEADER_RGB.getRGB())));
        if(hint != null)
            toolTip.add(hint.plainCopy()
                            .withStyle(s -> s.withColor(HINT_RGB.getRGB())));
        toolTip.add(scrollToModify.plainCopy()
                                  .withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
        toolTip.add(shiftScrollsFaster.plainCopy()
                                      .withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
    }

}
