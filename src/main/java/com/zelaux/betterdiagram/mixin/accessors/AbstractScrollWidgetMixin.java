package com.zelaux.betterdiagram.mixin.accessors;

import com.zelaux.betterdiagram.extend.accessors.AbstractScrollWidgetAccessors;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import org.spongepowered.asm.mixin.*;

@Mixin(AbstractScrollWidget.class)
public class AbstractScrollWidgetMixin implements AbstractScrollWidgetAccessors {
    @Shadow
    private boolean scrolling;

    @Override
    public boolean bcd$scrolling() {return scrolling;}

    @Override
    public void bcd$scrolling(boolean scrolling) {this.scrolling = scrolling;}

}
