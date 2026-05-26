package com.zelaux.betterdiagram.gui.user;

public enum ButtonKind {
    LEFT,
    RIGHT,
    MIDDLE,
    BUTTON_4,
    BUTTON_5,
    BUTTON_6,
    BUTTON_7,
    BUTTON_8,
    ;
    public final static ButtonKind[] all = values();

    public static ButtonKind fromButton(int button) {
        if(button < 0 || button >= all.length) return null;
        return all[button];
    }
}
