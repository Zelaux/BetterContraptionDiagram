package com.zelaux.betterdiagram.feature.balloon_volume_calculator.widget;

import com.simibubi.create.foundation.gui.widget.CompositeWidget;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.BalloonCalculationMode;
import com.zelaux.betterdiagram.index.BCDTextures;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ModeSelector extends CompositeWidget {
    private final Label label;
    protected Consumer<BalloonCalculationMode> onScroll;
    SelectionScrollInput input;

    public ModeSelector(int x, int y,BalloonCalculationMode current) {

        int width = BCDTextures.BalloonScreen.MODE_BACKGROUND.width;
        int height = BCDTextures.BalloonScreen.MODE_BACKGROUND.height;

        var list = new ObjectArrayList<Component>();
        for(var mode : BalloonCalculationMode.all) {
            list.add(mode.displayName());
        }

        label = new Label(x + 4, y+height/2 - 10/2, CommonComponents.EMPTY).withShadow();
        input = (SelectionScrollInput) new SelectionScrollInput(x, y, width, height)
            .forOptions(list)
            .titled(Component.translatable("bcd.gui.balloon-calculator.mode"))
            .writingTo(label)
            .calling(integer -> {
                if(onScroll == null) return;
                onScroll.accept(BalloonCalculationMode.all[integer]);
            });

        input.setState(current.id);
        addRenderableOnly(label);
        add(input);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        BCDTextures.BalloonScreen.MODE_BACKGROUND.render(graphics,input.getX(),input.getY());
        Font font = Minecraft.getInstance().font;
        int width = font.width(label.text);
        label.setX(input.getX() + (input.getWidth()-8)/2-width/2 + 4);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    public ModeSelector calling(Consumer<BalloonCalculationMode> onScroll) {
        this.onScroll = onScroll;
        return this;
    }
}
