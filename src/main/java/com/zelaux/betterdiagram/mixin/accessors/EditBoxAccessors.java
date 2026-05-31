package com.zelaux.betterdiagram.mixin.accessors;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

import java.util.function.Consumer;

@Mixin(EditBox.class)
public interface EditBoxAccessors {

    @Accessor("textColor")
    int bcd$textColor();
    @Accessor("textColor")
    void bcd$textColor(int value);

    @Accessor("textColorUneditable")
    int bcd$textColorUneditable();
    @Accessor("textColorUneditable")
    void bcd$textColorUneditable(int value);


    @Accessor("displayPos")
    int bcd$displayPos();
    @Accessor("displayPos")
    void bcd$displayPos(int value);

    @Accessor("highlightPos")
    int bcd$highlightPos();
    @Accessor("highlightPos")
    void bcd$highlightPos(int value);

    @Accessor("maxLength")
    int bcd$maxLength();
    @Accessor("maxLength")
    void bcd$maxLength(int value);

    @Accessor("isEditable")
    boolean bcd$isEditable();
    @Accessor("isEditable")
    void bcd$isEditable(boolean value);

    @Accessor("canLoseFocus")
    boolean bcd$canLoseFocus();
    @Accessor("canLoseFocus")
    void bcd$canLoseFocus(boolean value);

    @Accessor("bordered")
    boolean bcd$bordered();
    @Accessor("bordered")
    void bcd$bordered(boolean value);

    @Accessor("suggestion")
    String bcd$suggestion();
    @Accessor("suggestion")
    void bcd$suggestion(String value);

    @Accessor("responder")
    Consumer<String> bcd$responder();
    @Accessor("responder")
    void bcd$responder(Consumer<String> value);

    @Accessor("hint")
    Component bcd$hint();
    @Accessor("hint")
    void bcd$hint(Component value);



}
