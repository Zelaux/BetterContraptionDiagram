package com.zelaux.betterdiagram.feature.balloon_volume_calculator;

import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.widget.GraphBackground;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.widget.HorizontalLineSelector;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.widget.Plotter;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.index.BCDTextures;
import com.zelaux.betterdiagram.index.SomeConstants;
import com.zelaux.betterdiagram.mixin.feature.easy_balloon_access.IStoreBlockEntityLiftingGasProvider;
import com.zelaux.betterdiagram.struct.BCDTexture;
import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.eriksonn.aeronautics.content.blocks.hot_air.BlockEntityLiftingGasProvider;
import dev.eriksonn.aeronautics.content.blocks.hot_air.BlockEntityLiftingGasProvider.ClientBalloonInfo;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasType;
import dev.eriksonn.aeronautics.index.AeroRegistries;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
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
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3d;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

public class BalloonVolumeCalculatorScreen extends AbstractSimiScreen {
    public static final Color gridColor = new Color(0x91918f | 0xff000000);
    public static final BoundingBox2i DEFAULT_BACKGROUND_PADDING = new BoundingBox2i(5, 5, BCDTextures.BalloonScreen.BACKGROUND.width - 3, BCDTextures.BalloonScreen.BACKGROUND.height - 3);
    public final Vector2i backgroundOffset = new Vector2i();
    public final BoundingBox2i backgroundRegion = new BoundingBox2i(DEFAULT_BACKGROUND_PADDING);
    public final ObjectArrayList<FormattedText> tooltipList = new ObjectArrayList<>();
    private final DiagramScreen diagramScreen;
    private final WithClientData withClientData;

    public ClientSubLevel subLevel;
    SubLevelDependenciesData dependenciesData;
    List<BalloonEntry> balloons;

    BCDTexture background = BCDTextures.BalloonScreen.BACKGROUND;
    GraphBackground graphBackground;
    private static RandomSource random = RandomSource.create(0);
    private Plotter[] plotters;
    private HorizontalLineSelector lineSelector;

    public BalloonVolumeCalculatorScreen(ClientSubLevel subLevel, SubLevelDependenciesData dependenciesData, List<BalloonEntry> balloons, DiagramScreen diagramScreen, WithClientData withClientData) {
        this.subLevel = subLevel;
        this.dependenciesData = dependenciesData;
        this.balloons = balloons;
        this.diagramScreen = diagramScreen;
        this.withClientData = withClientData;
    }

    public static void show(SubLevelDependenciesData dependenciesData, WithClientData withClientData, @NotNull DiagramScreen diagramScreen) {

        //list.add(Component.translatable("bcd.extra-info.balloon-volume",
        //    Component.translatable("aeronautics.unit.meter_cubed", "---")
        //).withColor(tooltipColor));

        ClientSubLevel mainSublevel = (ClientSubLevel) dependenciesData.constraintTree.subLevelGroup.first();
        ClientLevel level = mainSublevel.getLevel();
        var balloons = dependenciesData
            .allSublevels()
            .map(it -> (IStoreBlockEntityLiftingGasProvider) it.getPlot())
            .map(IStoreBlockEntityLiftingGasProvider::bcd$blockEntityLiftingGasProviders)
            .map(Long2ObjectMap::values)
            .flatMap(Collection::stream)
            .map(BlockEntityLiftingGasProvider::getBalloon)
            .filter(Objects::nonNull)
            .distinct()
            .map(balloon -> {
                for(BlockEntityLiftingGasProvider heater : balloon.getHeaters()) {
                    for(Field field : heater.getClass().getDeclaredFields()) {
                        if(field.getType() == ClientBalloonInfo.class) {
                            try {
                                field.setAccessible(true);
                                var info = (ClientBalloonInfo) field.get(heater);
                                if(info != null) {
                                    return new BalloonEntry(balloon, info, Sable.HELPER.getContaining(level, info.gasCenter()));
                                }
                            } catch(IllegalAccessException e) {
                            }
                        }
                    }
                }

                return new BalloonEntry(balloon, null, Sable.HELPER.getContaining(
                    level, VecUtil.center(balloon.getBounds(), new Vector3d())
                ));
            })
            .toList();

        BalloonVolumeCalculatorScreen screen = new BalloonVolumeCalculatorScreen(
            mainSublevel,
            dependenciesData,
            balloons,
            diagramScreen,
            withClientData
        );
        Minecraft.getInstance().pushGuiLayer(screen);

    }

    @Override
    protected void init() {
        super.init();
        backgroundOffset.set(width / 2 - background.width / 2, height / 2 - background.height / 2);
        backgroundRegion.set(DEFAULT_BACKGROUND_PADDING).move(backgroundOffset);

        ClientLevel level = subLevel.getLevel();
        int minBuildHeight = level.getMinBuildHeight();
        int maxBuildHeight = level.getMaxBuildHeight();
        ;
        addRenderableOnly(graphBackground =
            new GraphBackground(background, DEFAULT_BACKGROUND_PADDING, new BoundingBox2i(0, minBuildHeight, 64, maxBuildHeight))
                .gridSpacing(200, 16));
        graphBackground.setPosition(
            width / 2 - graphBackground.getWidth() / 2,
            height / 2 - graphBackground.getHeight() / 2
        );


        var entries = level.holderLookup(AeroRegistries.Keys.LIFTING_GAS_TYPE).listElements().map(Holder.Reference::value).toList();
        plotters = new Plotter[entries.size()];
        var colors = new Color[plotters.length];
        double mass = dependenciesData.mergedForces().mass();
        double gravityScalar = SomeConstants.gravityScalar(level);
        double force = mass * gravityScalar;
        for(int i = 0; i < plotters.length; i++) {


            LiftingGasType gasType = entries.get(i);
            String string = gasType.getName().toString();
            long longHashCode = 0;
            for(char c : string.toCharArray()) {
                longHashCode = longHashCode * 31 + c;
            }
            random.setSeed(longHashCode);

            Color color = new Color(java.awt.Color.HSBtoRGB(random.nextFloat(), 0.5f, 0.5f) | 0xff000000);
            colors[i] = color;
            //random.setSeed();
            Vector3d pos = new Vector3d();
            addRenderableWidget(plotters[i] = new Plotter(graphBackground, y -> {
                    double pressure = DimensionPhysicsData.getAirPressure(level, pos.set(0, y, 0));
                    //force = gravity * totalLift * pressure
                    double totalLift = force / pressure / gravityScalar;
                    //totalLift = volume * getLiftStrength
                    float volume = (float) (totalLift / gasType.getLiftStrength());
                    BoundingBox2i gridDimension = graphBackground.gridDimension;
                    gridDimension.minX = Math.min(gridDimension.minX, (int) volume);
                    gridDimension.maxX = Math.max(gridDimension.maxX, (int) volume);
                    return volume;
                }, color)
                    .resolution(200)
                    .flipAxis(true)
                    .formatter((input, output) ->
                        Component.translatable("bcd.balloon-calculator.plot",
                            gasType.getName().copy().withColor(color.getRGB()),
                            Component.translatable("aeronautics.unit.meter_cubed", String.format("%,.2f", output)), String.format("%,.2f", input))
                    )
                    .tooltipSink(tooltipList::add)
            );

        }

        addRenderableWidget(lineSelector = new HorizontalLineSelector(graphBackground, Component.literal("selection line")));
        HorizontalLineSelector line = lineSelector;
        line.plotters(plotters);


        var selectedBalloonY = withClientData.bcdDataNotNull_readOnly().selectedBalloonY();


        int currentY;
        if(selectedBalloonY.isPresent()) {
            currentY = selectedBalloonY.getAsInt();
        } else else_:{
            if(!balloons.isEmpty()) {
                double commonY = findBalloonY();
                if(!Double.isNaN(commonY)) {
                    currentY = (int) commonY;
                    break else_;
                }
            }
            LocalPlayer player = Minecraft.getInstance().player;
            currentY = player.getBlockY();
        }
        line.currentY(currentY);
        line.onYChanges(value -> withClientData.bcdiagram$updateData(data -> data.withSelectedBalloonY(OptionalInt.of(value))));
    }

    private double findBalloonY() {
        double commonY = 0;
        int amountY = 0;
        for(BalloonEntry balloon : balloons) {
            ClientBalloonInfo info = balloon.info;
            if(info == null) return Double.NaN;
            commonY += info.gasCenter().y;
            amountY++;
        }
        commonY /= amountY;
        return commonY;
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

    record BalloonEntry(Balloon balloon, @Nullable ClientBalloonInfo info, SubLevel subLevel) {

    }

}
