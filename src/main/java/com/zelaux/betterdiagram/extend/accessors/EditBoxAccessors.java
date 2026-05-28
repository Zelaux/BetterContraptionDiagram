package com.zelaux.betterdiagram.extend.accessors;

import net.minecraft.client.gui.components.EditBox;

public interface EditBoxAccessors {
    static EditBoxAccessors of(EditBox editBox) {
        return ((EditBoxAccessors) editBox);
    }

    int bcd$displayPos();

    int bcd$highlightPos();

    void bcd$displayPos(int displayPos);

    void bcd$highlightPos(int highlightPos);
}
