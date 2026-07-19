package com.zelaux.betterdiagram.feature.balloon_volume_calculator.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.zelaux.betterdiagram.index.Colors;
import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.data.SimLang;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2d;
import org.joml.Vector2d;

import java.util.List;
import java.util.function.Consumer;

public class HorizontalLineSelector extends OnGraphWidget {
    final Vector2d tmpVec = new Vector2d();
    public int currentY = 0;
    public Plotter[] plotters;
    ScrollInput scrollInput;
    private IntConsumer onChangeListener;
    private boolean isDragging;

    public HorizontalLineSelector(GraphBackground background, Component message) {
        super(background, message);
        ScrollInput input = new ScrollInput(0, 0, 0, 0) {
            {
                toolTip = new ObjectArrayList<>();
            }

            @Override
            protected void updateTooltip() {
                toolTip.clear();
                toolTip.add(this.title.plainCopy()
                                      .withStyle(s -> s.withColor(HEADER_RGB.getRGB())));
                toolTip.clear();
                if(this.hint != null)
                    toolTip.add(this.hint.plainCopy()
                                         .withStyle(s -> s.withColor(HINT_RGB.getRGB())));
                toolTip.add(formatter.apply(state));
                toolTip.add(this.scrollToModify.plainCopy()
                                               .withStyle(ChatFormatting.ITALIC, ChatFormatting.WHITE));
                toolTip.add(this.shiftScrollsFaster.plainCopy()
                                                   .withStyle(ChatFormatting.ITALIC, ChatFormatting.WHITE));
            }
        };
        scrollInput = input
            .format(this::formatHeight)
            .calling(this::updateY)
            .withRange(background.gridDimension.minY, background.gridDimension.maxY)
            .withShiftStep(10)
        ;
    }

    private @NotNull MutableComponent formatHeight(double height) {
        return SimLang.translate("altitude_sensor.height", SimLang.text(String.format("%.2f", height)).style(ChatFormatting.AQUA))
                      .style(ChatFormatting.GRAY).component();
    }

    private void updateY(int i) {
        currentY = i;
        if(onChangeListener != null) {
            onChangeListener.accept(i);
        }

    }

    public HorizontalLineSelector onYChanges(IntConsumer consumer) {
        this.onChangeListener = consumer;
        return this;
    }

    public HorizontalLineSelector plotters(Plotter[] plotters) {
        this.plotters = plotters;
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        setPosition(background.drawableRegion.minX, background.drawableRegion.minY);
        setSize(background.drawableRegion.width(), background.drawableRegion.height());
        scrollInput.tick();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        BoundingBox2i drawableRegion = background.drawableRegion;

        Font font = Minecraft.getInstance().font;
        int gridMouse = (int) (projectToGrid(tmpVec.set(0, mouseY)).y + 0.5);
        boolean isAbove =isDragging &&  gridMouse > currentY;
        if(!isDragging && drawableRegion.contains(drawableRegion.centerX(), mouseY)) {
            guiGraphics.hLine(drawableRegion.minX, drawableRegion.maxX, mouseY, Colors.DARK_GRAY.getRGB());
            displayTooltipAlligned(guiGraphics, mouseY, font, drawableRegion, List.of(formatHeight(gridMouse)), isAbove, true);
        }

        int selectedScreenY = (int) (projectToScreen(tmpVec.set(0, currentY)).y + 0.5);
        guiGraphics.hLine(drawableRegion.minX, drawableRegion.maxX, selectedScreenY, Colors.BLACK.getRGB());

        displayTooltipAlligned(guiGraphics, selectedScreenY, font, drawableRegion,scrollInput.getToolTip(), !isAbove, true);

        if(plotters == null || plotters.length == 0) return;
        Plotter.PointEntry entry = new Plotter.PointEntry();
        entry.dst2 = 0;
        var tooltips = new ObjectArrayList<FormattedText>();
        Matrix3x2d matrix = new Matrix3x2d();
        Vector2d tmpVec = this.tmpVec;


        final int input = currentY;
        Vector2d placementPosition = new Vector2d();
        for(Plotter plotter : plotters) {
            Consumer<Component> prev = plotter.tooltipSink;
            plotter.tooltipSink = tooltips::add;
            float output = plotter.plotFunction.get(input);
            tmpVec.set(input, output).mulPosition(plotter.inputOutputToScreenMatrix(matrix));
            placementPosition.add(tmpVec);
            plotter.hovered(guiGraphics, entry.set(
                input, output, -1, 0, tmpVec
            ));
            plotter.tooltipSink = prev;
        }
        placementPosition.div(plotters.length);

        displayTooltipAlligned(guiGraphics, selectedScreenY, font,drawableRegion, tooltips, !isAbove, false);
    }

    private void displayTooltipAlligned(GuiGraphics guiGraphics, int screenY, Font font, BoundingBox2i bounds, List<? extends FormattedText> texts, boolean isAbove, boolean isRight) {
        BoundingBox2i tmp = new BoundingBox2i();
        BoundingBox2i textBounds = new BoundingBox2i();
        final int tooltipPadding = 0;
        for(FormattedText text : texts) {
            tmp.setSized(0, 0, font.width(text), 10);
            textBounds.move(0, tmp.height());
            textBounds.expandTo(tmp.width(), 0);
        }
        textBounds.move(0, 2);
        boolean canBeBellow = textBounds.height() + screenY <= bounds.maxY;
        boolean canBeAbove = screenY - textBounds.height() >= bounds.minY;
        textBounds.maxX += tooltipPadding * 2;
        if(isRight){
            textBounds.move(bounds.maxX - textBounds.width(),0);
        }else{
            textBounds.move(bounds.minX,0);
        }
        if(!canBeAbove && isAbove || canBeBellow && !isAbove) {
            //bellow
            textBounds.move(0, screenY);
            textBounds.maxY += tooltipPadding * 2;
        } else {
            //above
            textBounds.move(0, screenY - textBounds.height());
            textBounds.minY -= tooltipPadding * 2;
        }

        //guiGraphics.drawString(font, formatHeight, textBounds.left(), textBounds.top(), 0xffffffff);


        final Color colorBackground = new Color(0xff3d322a);
        final Color colorBorderTop = new Color(0xff5d483a);
        final Color colorBorderBot = new Color(0xff5d483a);
        RemovedGuiUtils.drawHoveringText(
            guiGraphics,
            texts,
            textBounds.minX-12,
            textBounds.minY+12,
            guiGraphics.guiWidth(),
            guiGraphics.guiHeight(),
            -1,
            colorBackground.getRGB(),
            colorBorderTop.getRGB(),
            colorBorderBot.getRGB(),
            font
        );
        if(true)return;
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0,0,401);
        guiGraphics.hLine(textBounds.minX,textBounds.maxX,textBounds.minY,0xff00ff00);
        guiGraphics.hLine(textBounds.minX,textBounds.maxX,textBounds.maxY,0xff00ff00);
        guiGraphics.vLine(textBounds.minX,textBounds.minY,textBounds.maxY,0xff00ff00);
        guiGraphics.vLine(textBounds.maxX,textBounds.minY,textBounds.maxY,0xff00ff00);
        pose.popPose();
    }

    private @NotNull BoundingBox2i positionTextBounds(Font font, FormattedText formatHeight, int x, int y, int sign, int padding, BoundingBox2i drawableRegion, BoundingBox2i dest) {
        int offset = sign * (font.lineHeight + padding);

        BoundingBox2i bounds = dest.set(x - font.width(formatHeight), Math.min(y, y + offset), x, Math.max(y, y + offset));
        if(!drawableRegion.contains(bounds)) {
            bounds.move(0, -2 * offset);
        }
        bounds.move(-padding, 0);
        return bounds;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(button==0){
            isDragging=false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!active || !visible || button != 0) return false;
        isDragging=true;
        return handleClick(mouseX, mouseY);
    }

    private boolean handleClick(double mouseX, double mouseY) {
        double y = projectToGrid(tmpVec.set(mouseX, mouseY)).y;
        BoundingBox2i gridDimension = background.gridDimension;
        if(!gridDimension.contains(gridDimension.centerX(), y)) return false;

        int newY = (int) y;
        currentY(newY);
        if(onChangeListener != null) onChangeListener.accept(newY);
        return true;
    }

    public HorizontalLineSelector currentY(int currentY) {
        this.currentY = currentY;
        scrollInput.setState(currentY);
        return this;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(!active || !visible || button != 0) return false;
        return handleClick(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return scrollInput.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void setX(int x) {
        super.setX(x);
        scrollInput.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        scrollInput.setY(y);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        scrollInput.setWidth(width);
    }

    @Override
    public void setHeight(int value) {
        super.setHeight(value);
        scrollInput.setHeight(value);
    }
}
