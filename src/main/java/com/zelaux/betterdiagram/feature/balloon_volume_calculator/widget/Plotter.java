package com.zelaux.betterdiagram.feature.balloon_volume_calculator.widget;

import com.zelaux.betterdiagram.index.BTextures;
import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import com.zelaux.betterdiagram.util.ui.GuiGraphicsUtil;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2d;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class Plotter extends OnGraphWidget {
    public static final int HOVER_RADIUS = 8;
    public static final int HOVER_RADIUS2 = HOVER_RADIUS * HOVER_RADIUS;
    final Float2FloatFunction plotFunction;
    public int resolution = 200;
    public boolean flipAxis = false;
    public boolean doHover = true;

    public Consumer<Component> tooltipSink;
    Color plotColor;
    Formatter tooltipFormatter;

    public Plotter(GraphBackground background, Float2FloatFunction plotFunction, Color plotColor) {
        super(0, 0, 0, 0, Component.literal("TODO"), background);
        this.plotFunction = plotFunction;
        this.plotColor = plotColor;
    }

    public Plotter tooltipSink(Consumer<Component> tooltipSink) {
        this.tooltipSink = tooltipSink;
        return this;
    }

    public Plotter formatter(Formatter formatter) {
        this.tooltipFormatter = formatter;
        return this;
    }
    public Plotter formatter(SingleFormatter formatter) {
        this.tooltipFormatter = formatter;
        return this;
    }

    public Plotter flipAxis(boolean flipAxis) {
        this.flipAxis = flipAxis;
        return this;
    }

    public Plotter doHover(boolean doHover) {
        this.doHover = doHover;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        BoundingBox2i gridDimension = background.gridDimension;

        float inputOffset = gridDimension.minX, inputScale = gridDimension.width();
        if(flipAxis) {
            inputOffset = gridDimension.minY;
            inputScale = gridDimension.height();
        }
        Matrix3x2d matrix = inputOutputToScreenMatrix(new Matrix3x2d());


        Vector2d tmp = new Vector2d();

        PointEntry nearest0 = new PointEntry();
        nearest0.dst2 = Double.MAX_VALUE;
        PointEntry nearest1 = new PointEntry();
        PointEntry previousPoint = new PointEntry();
        for(int i = 0; i < resolution; i++) {
            float input = i / (float) resolution * inputScale + inputOffset;
            float output = plotFunction.get(input);
            tmp.set(input, output).mulPosition(matrix);
            double distanceSquared = tmp.distanceSquared(mouseX, mouseY);
            if(distanceSquared < nearest0.dst2) {
                nearest0.set(input, output, i, distanceSquared, tmp);
                nearest1.set(previousPoint);
            } else if(nearest0.index == i - 1 && distanceSquared < nearest1.dst2) {
                nearest1.set(input, output, i, distanceSquared, tmp);
            }
            if(i != 0) {
                GuiGraphicsUtil.renderLine(guiGraphics,
                    previousPoint.screenPoint.x, previousPoint.screenPoint.y,
                    tmp.x, tmp.y,
                    plotColor.getRGB()
                );
            }
            previousPoint.set(
                input, output, i, distanceSquared, tmp
            );
        }

        if(tooltipFormatter == null || tooltipSink == null || !doHover) return;
        if(nearest1.index == -1) {
            if(nearest0.index == -1) return;

            hovered(guiGraphics, nearest0);
            return;
        }


        Vector2d a = new Vector2d(nearest0.screenPoint);
        Vector2d b = new Vector2d(nearest1.screenPoint);
        Vector2d p = new Vector2d(mouseX, mouseY);
        b.sub(a);
        p.sub(a);
        double t = Math.clamp(p.dot(b) / b.lengthSquared(), 0, 1);
        Vector2d point = b.mul(t).add(a);


        double input = nearest0.input + t * (nearest1.input - nearest0.input);
        hovered(guiGraphics, previousPoint.set(
            (float) input, plotFunction.get((float) input), 0, point.distanceSquared(p.add(a)), point
        ));
        //(fx(t) - x0)*(fx(t) - x0)+(fx(t) - x0)*(fx(t) - x0)
    }

    public Matrix3x2d inputOutputToScreenMatrix(Matrix3x2d dest) {
        Matrix3x2d matrix = dest.identity();
        if(flipAxis) {
            matrix.set(
                0, 1,
                1, 0,
                0, 0
            );
        }

        projectMatrixGrid2Draw(matrix);
        return matrix;
    }

    public void hovered(GuiGraphics guiGraphics, PointEntry nearest0) {
        if(nearest0.dst2 >= HOVER_RADIUS2) return;
        tooltipFormatter.format(nearest0.input, nearest0.output).forEach(tooltipSink);

        BTextures.BalloonScreen.ICON_CIRCLE.render(guiGraphics, (int) nearest0.screenPoint.x - 8, (int) nearest0.screenPoint.y - 8, plotColor);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public Plotter resolution(int resolution) {
        this.resolution = resolution;
        return this;
    }

    public interface Formatter {
        Collection<Component> format(float input, float output);
    }
    public interface SingleFormatter extends Formatter {
        Component formatSingle(float input, float output);

        @Override
        default Collection<Component> format(float input, float output){
            return List.of(formatSingle(input,output));
        }
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PUBLIC)
    static final class PointEntry {
        final Vector2d screenPoint = new Vector2d();
        float input;
        float output;
        int index = -1;
        double dst2 = Double.MAX_VALUE;

        public void set(PointEntry previousPoint) {
            set(previousPoint.input, previousPoint.output, previousPoint.index, previousPoint.dst2, previousPoint.screenPoint);
        }

        public PointEntry set(float input, float output, int index, double dst2, Vector2dc screenPoint) {
            this.input = input;
            this.output = output;
            this.index = index;
            this.dst2 = dst2;
            this.screenPoint.set(screenPoint);
            return this;
        }
    }
}
