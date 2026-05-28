package com.zelaux.betterdiagram.mixin.accessors;

import com.zelaux.betterdiagram.extend.accessors.EditBoxAccessors;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.*;

@Mixin(EditBox.class)
public class EditBoxMixin implements EditBoxAccessors {
    @Shadow
    private int displayPos;

    @Shadow
    private int highlightPos;

    @Override
    public int bcd$displayPos() {return displayPos;}

    @Override
    public int bcd$highlightPos() {return highlightPos;}

    @Override
    public void bcd$displayPos(int displayPos) {
        this.displayPos = displayPos;
    }

    @Override
    public void bcd$highlightPos(int highlightPos) {
        this.highlightPos = highlightPos;
    }
}
