package com.zelaux.betterdiagram.gui.tooltip.diagram_info_tooltip;

import com.zelaux.betterdiagram.util.format.VecFormat;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.network.chat.Component;
import org.joml.Vector3dc;

import java.util.List;

public record GravityModule(SubLevel subLevel) implements DiagramInfoModule {

    @Override
    public void appendInto(List<Component> list, int tooltipColor) {
        Vector3dc gravity = DimensionPhysicsData.getGravity(subLevel.getLevel(), subLevel.logicalPose().position());
        list.add(Component.translatable("better_contraption_diagram.extra-info.gravity",
            VecFormat.Presets.lightGray(gravity)
        ).withColor(tooltipColor));
    }
}
