package com.zelaux.betterdiagram.gui.widget;

import com.zelaux.betterdiagram.gui.user.ButtonKind;
import com.zelaux.betterdiagram.gui.user.ClickHandler;
import com.zelaux.betterdiagram.index.BCDTextures;
import com.zelaux.betterdiagram.struct.BCDTexture;
import com.zelaux.betterdiagram.util.UIUtil;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.index.SimSoundEvents;
import lombok.Getter;
import lombok.Setter;
import net.createmod.catnip.theme.Color;
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
import java.util.function.Supplier;

public class BDiagramButton extends AbstractWidget {


    public static final Color[] DEFAULT_COLORS = {DiagramScreen.DULL_BUTTON_COLOR, DiagramScreen.BUTTON_COLOR};
    public static final int[] DEFAULT_INDEX_MAP = {0, 1, 2, 3, 4, 5, 6, 7};
    private BCDTexture[] textures;
    private int[] textureIndexMap = DEFAULT_INDEX_MAP;
    private final ClickHandler onClick;
    private TooltipListProvider diagramTooltip;
    @Getter
    @Setter
    public BCDTexture background = BCDTextures.Diagram.DIAGRAM_ICON_BTN_BACKGROUND;

    private IndexSelector iconSwitch;
    private ColorSelector colorSwitch;
    public Color[] colors = DEFAULT_COLORS;
    public ButtonKind[] supportedButtons={ButtonKind.LEFT};

    public BDiagramButton supportedButtons(ButtonKind... supportedButtons) {
        this.supportedButtons = supportedButtons;
        return this;
    }

    public BDiagramButton colors(Color normal, Color hovered) {
        this.colors = new Color[]{normal, hovered};
        return this;
    }

    public BDiagramButton textureIndexMap(int... textureIndexMap) {
        this.textureIndexMap = textureIndexMap;
        return this;
    }

    public BDiagramButton colors(Color normal, Color hovered, Color activeNormal, Color activeHovered) {
        this.colors = new Color[]{normal, hovered, activeNormal, activeHovered};
        return this;
    }

    public BDiagramButton colors(Color normal, Color hovered, Color activeNormal, Color activeHovered,
                                 Color iconSwitchedNormal, Color iconSwitchedHovered, Color iconSwitchedActiveNormal, Color iconSwitchedActiveHovered) {
        this.colors = new Color[]{
            normal, hovered, activeNormal, activeHovered,
            iconSwitchedNormal, iconSwitchedHovered, iconSwitchedActiveNormal, iconSwitchedActiveHovered
        };
        return this;
    }

    public BDiagramButton colors(Color[] colors) {
        if(colors.length == 0) {
            throw new IllegalArgumentException("colors is empty");
        }
        this.colors = colors;
        return this;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        for(ButtonKind kind : supportedButtons) {
            if(kind.ordinal() == button) return true;
        }
        return false;
    }

    public BDiagramButton(final BCDTexture textures, final int x, final int y, final Component message, final ClickHandler onClick) {
        this(new BCDTexture[]{textures, textures}, x, y, message, onClick);
    }
    public BDiagramButton(final BCDTexture textures, final int x, final int y, final Component message, final Runnable onClick) {
        this(new BCDTexture[]{textures, textures}, x, y, message, ClickHandler.run(onClick));
    }

    public BDiagramButton(final BCDTexture[] textures, final int x, final int y, final Component message, final Runnable onClick) {
        this(textures,x,y,message,ClickHandler.run(onClick));
    }
    public BDiagramButton(final BCDTexture[] textures, final int x, final int y, final Component message, final ClickHandler onClick) {
        super(x, y, textures[0].width, textures[0].height, message);
        this.textures = textures;
        this.onClick = onClick;
        this.iconSwitch = null;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        super.onClick(mouseX, mouseY, button);
        this.onClick.handle(this,mouseX,mouseY,button);
    }

    public void setTextures(final BCDTexture textures, boolean active) {
        this.textures[active ? 1 : 0] = textures;
    }

    public BCDTexture getTexture(boolean active) {
        return this.textures[active ? 1 : 0];
    }

    @Override
    protected void renderWidget(final @NotNull GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTicks) {
        if(background != null) background.render(guiGraphics, this.getX() - 1, this.getY() - 1);

        int textureIndex = iconSwitch != null ? iconSwitch.selectIndex(active, isHovered()) : boolToBin(isHovered(), this.active, false);
        int colorIndex = colorSwitch != null ?
            colorSwitch.selectIndex(active, isHovered(), textureIndex) :
            boolToBin(isHovered(), this.active, false);
        this.textures[textureIndexMap[textureIndex % textureIndexMap.length] % textures.length].render(guiGraphics, this.getX() - 1, this.getY() - 1, colors[colorIndex % colors.length]);

        if(this.diagramTooltip != null && this.isHovered()) {
            final var lines = this.diagramTooltip.get();
            DiagramScreen.renderTooltip(guiGraphics, mouseX, mouseY, UIUtil.formattedText(lines));
        }
    }

    private static int boolToBin(boolean b1, boolean b2, boolean b4) {
        return (b1 ? 1 : 0) | (b2 ? 2 : 0) | (b4 ? 4 : 0);
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
        return withTooltip(() -> List.of(diagramTooltip.get()));
    }

    public BDiagramButton setIconSwitch(final BooleanSupplier switcher) {
        this.iconSwitch = ((active, hovered) -> boolToBin(hovered, active, switcher.getAsBoolean()));
        return this;
    }

    public BDiagramButton setIconSwitch(final IndexSelector switcher) {
        this.iconSwitch = switcher;
        return this;
    }

    public BDiagramButton setColorSelector(final ColorSelector switcher) {
        this.colorSwitch = switcher;
        return this;
    }

    @FunctionalInterface
    public interface TooltipListProvider {
        List<? extends FormattedText> get();
    }

    @FunctionalInterface
    public interface IndexSelector {
        int selectIndex(boolean active, boolean hovered);
    }

    @FunctionalInterface
    public interface ColorSelector {
        int selectIndex(boolean active, boolean hovered, int textureIndex);
    }

    @FunctionalInterface
    public interface SimpleClickHandler extends ClickHandler {
        default void handle(BDiagramButton self, double mouseX, double mouseY, int button) {onButtonClick(ButtonKind.fromButton(button));}

        void onButtonClick(ButtonKind button);
    }

}
