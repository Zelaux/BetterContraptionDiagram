package com.zelaux.betterdiagram.feature.balloon_volume_calculator;

import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.extend.accessors.DiagramScreenAccessors;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.widget.GraphBackground;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.widget.HorizontalLineSelector;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.widget.ModeSelector;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.widget.Plotter;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.index.BTextures;
import com.zelaux.betterdiagram.index.SomeConstants;
import com.zelaux.betterdiagram.struct.BTexture;
import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import com.zelaux.betterdiagram.util.StringUtil;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasType;
import dev.eriksonn.aeronautics.index.AeroRegistries;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;
import java.util.stream.Stream;

public class BalloonVolumeCalculatorScreen extends AbstractSimiScreen {
    public static final Color gridColor = new Color(0x91918f | 0xff000000);
    public static final BoundingBox2i DEFAULT_BACKGROUND_PADDING = new BoundingBox2i(5, 5, BTextures.BalloonScreen.BACKGROUND.width - 3, BTextures.BalloonScreen.BACKGROUND.height - 3);
    public final ObjectArrayList<FormattedText> tooltipList = new ObjectArrayList<>();
    private final BalloonVolumeCalculator calculator;
    private final WithClientData clientData;

    public ClientSubLevel subLevel;
    SubLevelDependenciesData dependenciesData;

    BTexture background = BTextures.BalloonScreen.BACKGROUND;
    GraphBackground graphBackground;
    private static RandomSource random = RandomSource.create(0);
    private Plotter[] plotters;
    private HorizontalLineSelector lineSelector;
    private BoundingBox2i originalGridDimention;

    public BalloonVolumeCalculatorScreen(ClientSubLevel subLevel, SubLevelDependenciesData dependenciesData, DiagramScreen diagramScreen, WithClientData clientData) {

        this.subLevel = subLevel;
        this.dependenciesData = dependenciesData;
        this.clientData = clientData;
        this.calculator = new BalloonVolumeCalculator(
            dependenciesData,
            new Balloons(dependenciesData),
            ((DiagramScreenAccessors) diagramScreen).bcd$diagram()
        );
    }

    public static void show(SubLevelDependenciesData dependenciesData, WithClientData withClientData, @NotNull DiagramScreen diagramScreen) {
        ClientSubLevel mainSublevel = (ClientSubLevel) dependenciesData.mainSubLevel();
        BalloonVolumeCalculatorScreen screen = new BalloonVolumeCalculatorScreen(
            mainSublevel,
            dependenciesData,
            diagramScreen,
            withClientData
        );
        Minecraft.getInstance().pushGuiLayer(screen);

    }

    @Override
    protected void init() {
        super.init();

        ClientLevel level = subLevel.getLevel();
        int minBuildHeight = level.getMinBuildHeight();
        int maxBuildHeight = level.getMaxBuildHeight();
        ;
        originalGridDimention = new BoundingBox2i(0, minBuildHeight, 64, maxBuildHeight);
        addRenderableOnly(graphBackground =
            new GraphBackground(background, DEFAULT_BACKGROUND_PADDING, new BoundingBox2i(originalGridDimention))
                .gridSpacing(200, 40));
        graphBackground.setPosition(
            width / 2 - graphBackground.getWidth() / 2,
            height / 2 - graphBackground.getHeight() / 2
        );
        addRenderableWidget(
            new ModeSelector(graphBackground.getRight(), graphBackground.getY(), clientData.bcdDataNotNull_readOnly().balloonCalc.calculationMode())
                .calling(mode -> {
                    graphBackground.gridDimension.set(originalGridDimention);
                    clientData.bcd$updateData(data -> data.withBalloonCalc(data.balloonCalc.withCalculationMode(mode)));
                })

        );


        var entries = getLiftingGasTypeStream(level).toList();
        plotters = new Plotter[entries.size()];
        var colors = new Color[plotters.length];
        double mass = dependenciesData.mergedForces().mass();
        double gravityScalar = SomeConstants.gravityScalar(level);
        //double force = mass * gravityScalar;
        for(int i = 0; i < plotters.length; i++) {


            LiftingGasType gasType = entries.get(i);
            Color color = colorForGasType(gasType);
            colors[i] = color;
            //random.setSeed();
            addRenderableWidget(plotters[i] = new Plotter(graphBackground, y -> {
                    float volume = calculator.calculateVolume(level, gasType, y);
                    BoundingBox2i gridDimension = graphBackground.gridDimension;
                    if(volume < 0) {
                        if(y < gridDimension.centerY()) {
                            gridDimension.minY = (int) Math.max(y + 0.5, gridDimension.minY);
                        } else if(y > gridDimension.centerY()) {
                            gridDimension.maxY = (int) Math.min(y + 0.5, gridDimension.maxY);
                        }
                    } else if(Float.isFinite(volume)) {
                        gridDimension.minX = Math.min(gridDimension.minX, (int) volume);
                        gridDimension.maxX = Math.max(gridDimension.maxX, (int) volume);
                        graphBackground.gridSpacing.x=gridDimension.width()/16;
                    }

                    return volume;
                }, color)
                    .resolution(200)
                    .flipAxis(true)
                    .doHover(false)
                    .formatter((input, output) ->
                        Component.translatable("bcd.balloon-calculator.plot",
                            gasType.getName().copy().withColor(color.getRGB()),
                            formatVolume(output), String.format("%,.2f", input))
                    )
                    .tooltipSink(tooltipList::add)
            );


        }

        addRenderableWidget(lineSelector = new HorizontalLineSelector(graphBackground, Component.literal("selection line")));
        HorizontalLineSelector line = lineSelector;
        line.plotters(plotters);


        var selectedBalloonY = clientData.bcdDataNotNull_readOnly().balloonCalc.selectedBalloonY();


        int currentY;
        if(selectedBalloonY.isPresent()) {
            currentY = selectedBalloonY.getAsInt();
        } else else_:{
            double commonY = calculator.balloonsY();
            if(!Double.isNaN(commonY)) {
                currentY = (int) commonY;
                break else_;
            }
            LocalPlayer player = Minecraft.getInstance().player;
            currentY = player.getBlockY();
        }
        line.currentY(currentY);
        line.onYChanges(value -> clientData.bcd$updateData(data ->
            data.withBalloonCalc(data.balloonCalc().withSelectedBalloonY(OptionalInt.of(value)))
        ));
    }

    public static @NotNull MutableComponent formatVolume(float output) {
        return Component.translatable("aeronautics.unit.meter_cubed", String.format("%,.2f", output));
    }

    /**
     * @see MutableComponent#toString()
     *
     */
    public static @NotNull Color colorForGasType(LiftingGasType gasType) {
        String string = gasType.getName().toString();
        long longHashCode = StringUtil.calculateLongHashCode(string);
        random.setSeed(longHashCode);

        return new Color(java.awt.Color.HSBtoRGB(random.nextFloat(), 0.5f, 0.85f) | 0xff000000);
    }

    public static @NotNull Stream<LiftingGasType> getLiftingGasTypeStream(Level level) {
        return level.holderLookup(AeroRegistries.Keys.LIFTING_GAS_TYPE).listElements().map(Holder.Reference::value);
    }

    @Override
    protected void renderMenuBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.flush();
        super.renderMenuBackground(guiGraphics, x, y, width, height);

    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {


    }

    @Override
    public void tick() {
        super.tick();

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
