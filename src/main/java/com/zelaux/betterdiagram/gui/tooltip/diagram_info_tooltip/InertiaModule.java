package com.zelaux.betterdiagram.gui.tooltip.diagram_info_tooltip;

import com.zelaux.betterdiagram.DebugRuntime;
import com.zelaux.betterdiagram.extend.accessors.DiagramScreenAccessors;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.network.extradata.DiagramExtraDataPacket;
import com.zelaux.betterdiagram.util.MatrixFormat;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.network.chat.Component;

import java.util.List;

public record InertiaModule(DiagramScreenAccessors accessors) implements DiagramInfoModule {
    @Override
    public void appendInto(List<Component> list, int tooltipColor) {
        var dependenciesData = accessors.bcd$subLevelDependencies();

        if(tryToDisplay(list, tooltipColor, dependenciesData)) return;

        list.add(Component
            .literal("")
            .append(Component.translatable("bcd.extra-info.inertia").withColor(tooltipColor))
            .append(" --- ")
        );
    }

    private boolean tryToDisplay(List<Component> list, int tooltipColor, SubLevelDependenciesData dependenciesData) {
        if(dependenciesData == null) return false;
        SubLevel mainSublevel = dependenciesData.constraintTree.subLevelGroup.first();
        DiagramExtraDataPacket extraData = dependenciesData.get(mainSublevel).extraData;
        if(extraData == null) return false;

        list.add(Component.translatable("bcd.extra-info.inertia").withColor(tooltipColor));

        MatrixFormat.Presets.inertia(
            extraData.inertiaTensor(),
            it -> list.add(Component.literal("   ").append(it))
        );
        return true;
    }
}
