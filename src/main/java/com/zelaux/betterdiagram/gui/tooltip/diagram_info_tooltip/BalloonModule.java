package com.zelaux.betterdiagram.gui.tooltip.diagram_info_tooltip;

import com.zelaux.betterdiagram.extend.accessors.DiagramScreenAccessors;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.BalloonVolumeCalculatorScreen;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.gui.tooltip.AbstractTooltipHandler;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import lombok.AllArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public record BalloonModule(DiagramScreenAccessors diagramScreenAcc,
                            DiagramScreen diagramScreen) implements DiagramInfoModule {
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

        OPEN_BALLOON_SCREEN_TOOLTIP_HANDLER.addTooltip(this, list.size()-1, list);
    }
}
