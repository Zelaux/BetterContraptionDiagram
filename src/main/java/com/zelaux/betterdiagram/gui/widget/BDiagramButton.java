package com.zelaux.betterdiagram.gui.widget;

import com.zelaux.betterdiagram.index.BCDTextures;
import com.zelaux.betterdiagram.struct.BCDTexture;
import com.zelaux.betterdiagram.util.UIUtil;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.index.SimSoundEvents;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BDiagramButton extends AbstractWidget {

    private BCDTexture texture;
    private final Runnable onClick;
    private TooltipListProvider diagramTooltip;
    @Getter
    @Setter
    public BCDTexture background= BCDTextures.Diagram.DIAGRAM_ICON_BTN_BACKGROUND;


    private BooleanSupplier iconSwitch;

    public BDiagramButton(final BCDTexture texture, final int x, final int y, final Component message, final Runnable onClick) {
        super(x, y, texture.width, texture.height, message);
        this.texture = texture;
        this.onClick = onClick;
        this.iconSwitch = this::isHovered;
    }

    @Override
    public void onClick(final double mouseX, final double mouseY) {
        super.onClick(mouseX, mouseY);
        this.onClick.run();
    }

    public void setTexture(final BCDTexture texture) {
        this.texture = texture;
    }

    public BCDTexture getTexture() {
        return this.texture;
    }

    @Override
    protected void renderWidget(final @NotNull GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTicks) {
        if(background!=null) background.render(guiGraphics, this.getX() - 1, this.getY() - 1);

        this.texture.render(guiGraphics, this.getX() - 1, this.getY() - 1, this.isHovered() || this.iconSwitch.getAsBoolean() ? DiagramScreen.BUTTON_COLOR : DiagramScreen.DULL_BUTTON_COLOR);

        if(this.diagramTooltip != null && this.isHovered()) {
            final var lines = this.diagramTooltip.get();
            DiagramScreen.renderTooltip(guiGraphics, mouseX, mouseY, UIUtil.formattedText(lines));
        }
    }

    @Override
    public void playDownSound(final SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SimSoundEvents.DIAGRAM_TAP.event(), 1.0F));
    }

    @Override
    protected void updateWidgetNarration(final @NotNull NarrationElementOutput narrationElementOutput) {

    }

    public BDiagramButton withTooltip(final TooltipListProvider diagramTooltip) {
        this.diagramTooltip = diagramTooltip;
        return this;
    }
    public BDiagramButton withTooltip(final Supplier<Component> diagramTooltip) {
        return withTooltip(()->List.of(diagramTooltip.get()));
    }

    public BDiagramButton setIconSwitch(final BooleanSupplier switcher) {
        this.iconSwitch = switcher;
        return this;
    }
    public interface TooltipListProvider{
        List<Component> get();
    }
}
