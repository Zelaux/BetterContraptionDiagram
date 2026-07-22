package com.zelaux.betterdiagram.gui.tooltip.diagram_info_tooltip;

import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.data.BalloonCalculatorSettings;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.extend.accessors.DiagramScreenAccessors;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.BalloonVolumeCalculator;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.BalloonVolumeCalculatorScreen;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.gui.tooltip.AbstractTooltipHandler;
import com.zelaux.betterdiagram.index.Colors;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.OptionalInt;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;


public record BalloonModule(
    DiagramScreenAccessors diagramScreenAcc,
    DiagramScreen diagramScreen,
    BalloonVolumeCalculator calculator
) implements DiagramInfoModule {

    public BalloonModule(DiagramScreenAccessors diagramScreenAcc, DiagramScreen diagramScreen) {
        this(diagramScreenAcc, diagramScreen, new BalloonVolumeCalculator(diagramScreenAcc.bcd$subLevelDependencies(), diagramScreenAcc.betterContraptionDiagram$diagram()));
    }

    public static final AbstractTooltipHandler<BalloonModule> OPEN_BALLOON_SCREEN_TOOLTIP_HANDLER = AbstractTooltipHandler.create(
        translatable("bcd.tooltip-handler.balloon.message"),
        (a, b) -> true,
        lastPayload -> {
            var accessors = lastPayload.diagramScreenAcc;
            SubLevelDependenciesData dependenciesData = accessors.bcd$subLevelDependencies();
            if(dependenciesData == null) return;
            BalloonVolumeCalculatorScreen.show(dependenciesData, accessors.betterContraptionDiagram$clientData(), lastPayload.diagramScreen);
        }
    );

    @Override
    public void appendInto(List<Component> list, int tooltipColor) {
        WithClientData clientData = diagramScreenAcc.betterContraptionDiagram$clientData();
        BCDData data = clientData.bcdDataOrTryDefault();
        DiagramDataPacket originalPacket = diagramScreenAcc.betterContraptionDiagram$serverData();
        if(data == null || originalPacket == null) {
            list.add(translatable("bcd.extra-info.balloon-volume.server-waiting").withColor(tooltipColor));
            return;
        }
        SubLevelDependenciesData dependenciesData = diagramScreenAcc.bcd$subLevelDependencies();
        if(dependenciesData == null) return;
        DiagramDataPacket packet = dependenciesData.mergedForces();
        if(packet == null) {
            packet = originalPacket;
        }
        SubLevel mainSubLevel = dependenciesData.mainSubLevel();

        BalloonCalculatorSettings balloonCalc = clientData.bcdDataNotNull_readOnly().balloonCalc;
        OptionalInt selectedBalloonY = balloonCalc.selectedBalloonY();

        if(selectedBalloonY.isEmpty()) {
            list.add(translatable("bcd.extra-info.balloon-volume.not-selected").withColor(tooltipColor));
            list.add(literal("   ").append(
                translatable("bcd.extra-info.balloon-volume.not-selected.tip").withStyle(ChatFormatting.GRAY)
            ));
        } else {
            int targetY = selectedBalloonY.getAsInt();
            list.add(translatable("bcd.extra-info.balloon-volume.title",
                balloonCalc.calculationMode().displayName().copy().withColor(Colors.WHITE.getRGB()),
                literal("" + targetY).withColor(Colors.WHITE.getRGB())).withColor(tooltipColor));
            Level level = mainSubLevel.getLevel();
            BalloonVolumeCalculatorScreen
                .getLiftingGasTypeStream(level)
                .map(gasType -> {
                    float volume = calculator.calculateVolume(level, gasType, targetY);
                    var gasTypeName = gasType.getName().copy().withColor(BalloonVolumeCalculatorScreen.colorForGasType(gasType).getRGB());
                    return literal("   ").append(translatable("bcd.extra-info.balloon-volume.entry",
                        gasTypeName,
                        BalloonVolumeCalculatorScreen.formatVolume(volume).withColor(Colors.WHITE.getRGB())
                    ).withColor(tooltipColor));
                })
                .forEach(list::add);
        }
        OPEN_BALLOON_SCREEN_TOOLTIP_HANDLER.addTooltip(this, list.size() - 1, list);
    }
}
