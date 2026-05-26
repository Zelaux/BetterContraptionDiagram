package com.zelaux.betterdiagram.gui.tooltip;

import com.zelaux.betterdiagram.extend.DiagramScreenAccessors;
import com.zelaux.betterdiagram.gui.widget.BDiagramButton;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.VecFormat;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.List;


public class DiagramInfoTooltip implements BDiagramButton.TooltipListProvider {
    private static final Vector3d tmp = new Vector3d();
    private static final Vector3d sumOfForces = new Vector3d();
    private static final Vector3d sumOfMoments = new Vector3d();
    private final ClientSubLevel subLevel;
    private final DiagramDataPacket serverData;
    private final @NotNull DiagramScreen diagramScreeen;
    private final @NotNull DiagramScreenAccessors diagramScreeenAcc;
    private final int tooltipColor;

    public DiagramInfoTooltip(@NotNull DiagramScreen self, ClientSubLevel subLevel, DiagramDataPacket serverData) {
        this.diagramScreeen = self;
        diagramScreeenAcc = DiagramScreenAccessors.of(self);
        tooltipColor = diagramScreeenAcc.TOOLTIP_LABEL_COLOR();
        this.subLevel = subLevel;
        this.serverData = serverData;
    }

    @Override
    public List<Component> get() {
        DiagramDataPacket serverData = diagramScreeenAcc.betterContraptionDiagram$serverData();
        if(serverData == null) return List.of();
        final var gravityDirection = CenterMassCalculator.calculateGravityDirection(subLevel, serverData, tmp);
        final var gravityComponent = VecFormat.Presets.lightGray(gravityDirection);
        tmp.zero();
        sumOfForces.zero();
        sumOfMoments.zero();
        Vector3d COM = CenterMassCalculator.centerOfMass(subLevel);
        serverData.forces().forEach((_i, pointForces) -> {
            for(var force : pointForces) {

                sumOfForces.add(force.force());
                Vector3d momentOfForce = tmp.set(force.point()).sub(COM).cross(force.point());
                sumOfMoments.add(momentOfForce);
                tmp.add(force.force());
            }
        });
        Vector3d mergedDisplacement = tmp.set(sumOfForces).cross(sumOfMoments).div(sumOfForces.lengthSquared()).add(COM);
        //VecUtil.subMinVec3d(mergedDisplacement, subLevel.getPlot().getBoundingBox());


        final var displacementComponent = VecFormat.Presets.lightGray(mergedDisplacement);
        final var directionComponent = VecFormat.Presets.lightGray(sumOfForces);
        return List.of(
            Component.translatable("better_contraption_diagram.extra-info.diagram-button").withColor(tooltipColor),
            Component.translatable("better_contraption_diagram.extra-info.gravity", gravityComponent),
            Component.translatable("better_contraption_diagram.extra-info.sum-of-forces.title"),
            Component.literal("  ").append(Component.translatable("better_contraption_diagram.extra-info.sum-of-forces.displacement", displacementComponent)),
            Component.literal("  ").append(Component.translatable("better_contraption_diagram.extra-info.sum-of-forces.direction", directionComponent))
        );
    }
}
