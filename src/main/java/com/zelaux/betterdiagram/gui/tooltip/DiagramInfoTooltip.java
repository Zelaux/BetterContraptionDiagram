package com.zelaux.betterdiagram.gui.tooltip;

import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.extend.accessors.DiagramScreenAccessors;
import com.zelaux.betterdiagram.feature.include_subsublevel.IDiagramScreenWithDependencies;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.gui.widget.BDiagramButton;
import com.zelaux.betterdiagram.index.BCDForceGroups;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.VecFormat;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.List;
import java.util.Set;


@Slf4j
public class DiagramInfoTooltip implements BDiagramButton.TooltipListProvider {
    private static final Vector3d tmp = new Vector3d();
    private static final Vector3d sumOfForces = new Vector3d();
    private static final Vector3d sumOfMoments = new Vector3d();
    private final ClientSubLevel subLevel;
    private final @NotNull DiagramScreen diagramScreeen;
    private final @NotNull DiagramScreenAccessors diagramScreeenAcc;
    private final @NotNull IDiagramScreenWithDependencies diagramScreenWithDependencies;
    private final int tooltipColor;

    public DiagramInfoTooltip(@NotNull DiagramScreen self, ClientSubLevel subLevel, DiagramDataPacket serverData) {
        this.diagramScreeen = self;
        diagramScreeenAcc = DiagramScreenAccessors.of(self);
        diagramScreenWithDependencies = (IDiagramScreenWithDependencies)self;
        tooltipColor = diagramScreeenAcc.TOOLTIP_LABEL_COLOR();
        this.subLevel = subLevel;
    }

    @Override
    public List<Component> get() {
        DiagramDataPacket serverData = diagramScreeenAcc.betterContraptionDiagram$serverData();
        if(serverData == null) return List.of();
        final var gravityDirection = CenterMassCalculator.calculateGravityDirection(subLevel, serverData, tmp);
        final var gravityComponent = VecFormat.Presets.lightGray(gravityDirection);
        Vector3d mergedDisplacement = tmp;
        CenterMassCalculator.calculateSumOfMomentum(
            mergedDisplacement,
            sumOfForces,
            sumOfMoments,
            serverData,
            subLevel,
            Set.of(ForceGroups.DRAG.get(), BCDForceGroups.MERGED_FORCE_GROUP.group)
        );
        Vector2d calculateSpeed=calculateSpeed(mergedDisplacement,sumOfForces);
        //VecUtil.subMinVec3d(mergedDisplacement, subLevel.getPlot().getBoundingBox());



        final var displacementComponent = VecFormat.Presets.lightGray(mergedDisplacement);
        final var directionComponent = VecFormat.Presets.lightGray(sumOfForces);




        return List.of(
            Component.translatable("better_contraption_diagram.extra-info.diagram-button").withColor(tooltipColor),
            Component.translatable("better_contraption_diagram.extra-info.gravity", gravityComponent),
            Component.translatable("better_contraption_diagram.extra-info.sum-of-forces.title")
            //Component.literal("  ").append(Component.translatable("better_contraption_diagram.extra-info.sum-of-forces.displacement", displacementComponent)),
            //Component.literal("  ").append(Component.translatable("better_contraption_diagram.extra-info.sum-of-forces.direction", directionComponent))
        );
    }

    private Vector2d calculateSpeed(Vector3d displacement, Vector3d force) {
        SubLevelDependenciesData dependenciesData = diagramScreenWithDependencies.bcd$subLevelDependencies();
        if(dependenciesData==null)return null;
        BCDData data = ((WithClientData) diagramScreeenAcc.betterContraptionDiagram$diagram()).bcdDataNotNull_readOnly();

        Vector3d COM = CenterMassCalculator.calculateMergedBalancePoint(dependenciesData, data);



        return null;
    }

}
