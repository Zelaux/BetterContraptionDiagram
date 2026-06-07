package com.zelaux.betterdiagram.gui;
import com.zelaux.betterdiagram.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class GridModeWarningScreen extends WarningScreen {
    private final Runnable onConfirm; // Обратный вызов (true если чекбокс нажат)
    public static final Component TITLE= Component.translatable("better_contraption_diagram.calculator.grid-warning.title");
    public static final Component CONTENT= Component.translatable("better_contraption_diagram.calculator.grid-warning.content");
    public static final Component CHECK= Component.translatable("better_contraption_diagram.calculator.grid-warning.check");

    public GridModeWarningScreen(Runnable onConfirm) {
        super(TITLE, CONTENT, CHECK, TITLE); // Используем title как narration по умолчанию
        this.onConfirm = onConfirm;
    }

    @Override
    protected @NotNull Layout addFooterButtons() {
        // Создаем горизонтальный ряд кнопок
        LinearLayout footerLayout = LinearLayout.horizontal().spacing(8);

        footerLayout.addChild(Button.builder(CommonComponents.GUI_OK, (button) -> {
            boolean shouldStopShowing = this.stopShowing != null && this.stopShowing.selected();
            if(shouldStopShowing){
                Config.SHOW_WARGING_WHEN_GRID.set(false);
            }
            Minecraft.getInstance().popGuiLayer();
            this.onConfirm.run();

        }).build());

        return footerLayout;
    }
    public static void tryOpen(Runnable action){
        if(!Config.SHOW_WARGING_WHEN_GRID.get()){
            action.run();
            return;
        }
        Minecraft.getInstance().pushGuiLayer(new GridModeWarningScreen(action));
    }
}