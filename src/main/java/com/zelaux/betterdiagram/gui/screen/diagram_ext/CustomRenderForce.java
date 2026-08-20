package com.zelaux.betterdiagram.gui.screen.diagram_ext;

import com.zelaux.betterdiagram.extend.accessors.ProjectionAccessor;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.simulated_team.simulated.content.entities.diagram.screen.ForceClusterFinder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;

import java.util.List;

public interface CustomRenderForce {
    void onHover(GuiGraphics graphics, ProjectionAccessor projectionAccessor, int mouseX, int mouseY, List<FormattedText> tooltipLines, ForceClusterFinder.Cluster pointForce, ForceGroup forceGroup);
    void onRender(GuiGraphics graphics, ForceClusterFinder.Cluster pointForce, ForceGroup forceGroup, int mouseX, int mouseY, double maxArrowLength, ProjectionAccessor projectionAccessor, boolean isDot);
}
