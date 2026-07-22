package com.zelaux.betterdiagram.gui.screen.pressure;

import com.zelaux.betterdiagram.feature.balloon_volume_calculator.BalloonVolumeCalculatorScreen;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.widget.GraphBackground;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.widget.Plotter;
import com.zelaux.betterdiagram.index.BCDTextures;
import com.zelaux.betterdiagram.index.Colors;
import com.zelaux.betterdiagram.index.SomeConstants;
import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import dev.ryanhcode.sable.physics.config.dimension_physics.BezierResourceFunction;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.data.SimLang;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.Collection;
import java.util.List;

public class PressureScreen extends AbstractSimiScreen {
    public final Level level;
    private final ObjectArrayList<FormattedText> tooltipList = new ObjectArrayList<>();

    public PressureScreen(Level level) {this.level = level;}

    public static void show(Level level) {
        PressureScreen pressureScreen = new PressureScreen(level);
        Minecraft.getInstance().pushGuiLayer(pressureScreen);
    }

    Vector3d tmp = new Vector3d();

    @Override
    protected void init() {
        super.init();
        int minBuildHeight = level.getMinBuildHeight();
        int maxBuildHeight = level.getMaxBuildHeight();
        ;
        ;

        SomeConstants.PressureCurve pressureCurve = SomeConstants.getPressureCurve(level);
        BoundingBox2i gridDimension = new BoundingBox2i(0, minBuildHeight, 64, maxBuildHeight);
        BezierResourceFunction curve = pressureCurve.curve();
        List<BezierResourceFunction.BezierPoint> points = curve.getPoints();
        if(!points.isEmpty()) {
            gridDimension.minY = gridDimension.minX = Integer.MAX_VALUE;
            gridDimension.maxY = gridDimension.maxX = Integer.MIN_VALUE;
            for(BezierResourceFunction.BezierPoint point : points) {
                gridDimension.minY = (int) Math.min(gridDimension.minY, point.altitude());
                gridDimension.maxY = (int) Math.max(gridDimension.maxY, point.altitude());
                gridDimension.minX = (int) Math.min(gridDimension.minX, point.value()* pressureCurve.pressure());
                gridDimension.maxX = (int) Math.max(gridDimension.maxX, point.value()* pressureCurve.pressure());

            }
        }
        GraphBackground graphBackground = new GraphBackground(BCDTextures.BalloonScreen.BACKGROUND, BalloonVolumeCalculatorScreen.DEFAULT_BACKGROUND_PADDING, gridDimension);
        addRenderableOnly(graphBackground);
        graphBackground.setPosition(
            width/2- graphBackground.getWidth()/2,
            height/2- graphBackground.getHeight()/2
        );

        addRenderableWidget(new Plotter(
                graphBackground, pressureCurve::floatPressureAt, Colors.BLUE
            )
                .flipAxis(true)
                .formatter(this::formatPressure)
                .resolution(200)
                .tooltipSink(tooltipList::add)
        );
    }


    private @NotNull Collection<Component> formatPressure(float height, float airPressure) {
        ObjectArrayList<Component> list = new ObjectArrayList<>();
        SimLang.translate("altitude_sensor.height", SimLang.text(String.format("%.2f", height)).style(ChatFormatting.AQUA))
               .style(ChatFormatting.GRAY)
               .forGoggles(list, 0);

        SimLang.translate("altitude_sensor.air_pressure", SimLang.text(String.format("%.2f%%", airPressure)).style(ChatFormatting.AQUA))
               .style(ChatFormatting.GRAY)
               .forGoggles(list, 0);
        return list;
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    protected void renderWindowForeground(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        if(!this.tooltipList.isEmpty()) {
            DiagramScreen.renderTooltip(graphics, mouseX, mouseY, this.tooltipList);
        }

        this.tooltipList.clear();

        super.renderWindowForeground(graphics, mouseX, mouseY, partialTicks);
    }
}
