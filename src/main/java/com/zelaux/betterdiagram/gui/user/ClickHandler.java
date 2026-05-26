package com.zelaux.betterdiagram.gui.user;

import com.zelaux.betterdiagram.gui.widget.BDiagramButton;

@FunctionalInterface
public interface ClickHandler {
    static ClickHandler run(Runnable onClick) {
        return (self, mouseX, mouseY, button) -> onClick.run();
    }

    void handle(BDiagramButton self, double mouseX, double mouseY, int button);

    static BDiagramButton.SimpleClickHandler click(BDiagramButton.SimpleClickHandler handler) {return handler;}

}
