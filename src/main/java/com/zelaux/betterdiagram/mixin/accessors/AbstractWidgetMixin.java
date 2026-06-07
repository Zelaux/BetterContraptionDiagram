package com.zelaux.betterdiagram.mixin.accessors;

import com.zelaux.betterdiagram.extend.accessors.AbstractWidgetAccessors;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.*;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin implements AbstractWidgetAccessors {
    @Shadow
    protected abstract boolean isValidClickButton(int button);

    @Override
    public boolean bcd$isValidClickButton(int button){
        return isValidClickButton(button);
    }


}
