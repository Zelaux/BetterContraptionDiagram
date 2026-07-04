package com.zelaux.betterdiagram.gui.tooltip;

import com.zelaux.betterdiagram.extend.accessors.DiagramScreenAccessors;
import com.zelaux.betterdiagram.feature.create_contraption_mass.ClientLevelPlotExt;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.gui.widget.BDiagramButton;
import com.zelaux.betterdiagram.index.BCDForceGroups;
import com.zelaux.betterdiagram.mixin.accessors.FloatingBlockDataAccessors;
import com.zelaux.betterdiagram.mixin.feature.extra_info_deliver.FloatingBlockControllerExt;
import com.zelaux.betterdiagram.network.DiagramExtraDataPacket;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.MergedPointForceCalculator;
import com.zelaux.betterdiagram.util.VecFormat;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.sublevel.KinematicContraption;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.physics.floating_block.*;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3d;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.Set;


@Slf4j
public class DiagramInfoTooltip implements BDiagramButton.TooltipListProvider {

    private static final MergedPointForceCalculator mergedForce = new MergedPointForceCalculator();
    public static final FloatingBlockControllerExt DUMMY_CONTROLLER = (FloatingBlockControllerExt)new FloatingBlockController(null);
    private final ClientSubLevel mySubLevel;
    private final @NotNull DiagramScreen diagramScreeen;
    private final @NotNull DiagramScreenAccessors diagramScreeenAcc;
    private final int tooltipColor;

    public DiagramInfoTooltip(@NotNull DiagramScreen self, ClientSubLevel subLevel, DiagramDataPacket serverData) {
        this.diagramScreeen = self;
        diagramScreeenAcc = DiagramScreenAccessors.of(self);
        tooltipColor = diagramScreeenAcc.TOOLTIP_LABEL_COLOR();
        this.mySubLevel = subLevel;
    }

    @Override
    public List<Component> get() {
        DiagramDataPacket serverData = diagramScreeenAcc.betterContraptionDiagram$serverData();
        SubLevelDependenciesData dependenciesData = diagramScreeenAcc.bcd$subLevelDependencies();
        if(serverData == null || dependenciesData==null) return List.of();
        final var gravityDirection = CenterMassCalculator.calculateGravityDirection(mySubLevel, serverData, new Vector3d());
        final var gravityComponent = VecFormat.Presets.lightGray(gravityDirection);
        mergedForce.begin(CenterMassCalculator.getMergedBalancePointOrCOM(diagramScreeen));

        Set<ForceGroup> blackList = Set.of(
            ForceGroups.DRAG.get(),
            BCDForceGroups.MERGED_FORCE_GROUP.group,
            BCDForceGroups.CONTRAPTION_GRAVITY.group,
            BCDForceGroups.ROPE_FORCE.group,
            BCDForceGroups.ROPE_MASS_FORCE.group
        );
        for(var entry : serverData.forces().entrySet()) {
            if(blackList.contains(entry.getKey())) continue;

            mergedForce.addAll(entry.getValue());
        }
        mergedForce.end();


        double length = mergedForce.totalForce.length();
        SubLevelGroup group = dependenciesData.constraintTree.subLevelGroup;
        ClientSubLevel mainSubLevel = mySubLevel;
        ClientLevel level = mainSubLevel.getLevel();
        Matrix3d resultFrictionMatrxi=new Matrix3d().zero();
        Matrix3d tmpMat=new Matrix3d();
        for(SubLevel subLevel : group) {
            Vector2d totalScaleHV=new Vector2d();
            SubLevelDependenciesData.Entry entry = dependenciesData.get(subLevel);
            if(entry.extraData == null) continue;
            Vector3dc localGravity = DimensionPhysicsData.getGravity(level, subLevel.logicalPose().position());
            double gravityMagnitude = localGravity.length();
            for(DiagramExtraDataPacket.ClusterInfo cluster : entry.extraData.clusters()) {

                double scale = cluster.pressureTimesTotalScale();
                FloatingBlockMaterial material = cluster.material();
                if(material.scaleWithGravity()) {
                    scale*= gravityMagnitude;
                }
                addFriction(totalScaleHV, scale, material);
            }
            if(false) for(KinematicContraption contraption : ((ClientLevelPlotExt) subLevel.getPlot()).bcd$getContraptions()) {
                FloatingClusterContainer clusterContainer = contraption.sable$getFloatingClusterContainer();
                for(FloatingBlockCluster cluster : clusterContainer.clusters) {
                    FloatingBlockMaterial material = cluster.getMaterial();
                    double scale=((FloatingBlockDataAccessors) cluster.getBlockData()).bcd$totalScale();
                    if(material.scaleWithPressure()){
                        FloatingBlockData blockData = cluster.getBlockData();
                        blockData.computePressureScale(subLevel);
                        scale*= blockData.getPressureScale();
                    }
                    if(material.scaleWithGravity()){
                        scale*= gravityMagnitude;
                    }
                    addFriction(totalScaleHV, scale, material);
                }
            }


            DUMMY_CONTROLLER.bcd$getGravityMatrix(localGravity, totalScaleHV.y, totalScaleHV.x, tmpMat);
            resultFrictionMatrxi.add(tmpMat);
        }
        //DimensionPhysicsData.getUniversalDrag()
        double universalDrag=0.09f;
        double timeStep=1/20f/2f;

        double universalDragCoeff=-(serverData.mass()*universalDrag)/((1-universalDrag));
        tmpMat.identity().scale(universalDragCoeff);
        resultFrictionMatrxi.add(tmpMat);

        ObjectArrayList<Component> list = new ObjectArrayList<>();
        list.add(Component.translatable("bcd.extra-info.diagram-button").withColor(tooltipColor));



        if(Math.abs(resultFrictionMatrxi.determinant()) > 1e-9) {
            Vector3d maxSpeed = new Vector3d();
            //resultFrictionMatrxi.scale(20);
            resultFrictionMatrxi.invert();
            resultFrictionMatrxi.transform(mergedForce.totalForce.negate(), maxSpeed);
            list.add(Component.translatable("bcd.extra-info.max-speed",VecFormat.Presets.lightGray(maxSpeed)).withColor(tooltipColor));
        }else{
            list.add(Component.translatable("bcd.extra-info.max-speed","...").withColor(tooltipColor));
        }

        list.add(Component.translatable("better_contraption_diagram.extra-info.gravity", gravityComponent));
        return list;
        //Component.literal("  ").append(Component.translatable("better_contraption_diagram.extra-info.sum-of-forces.displacement", displacementComponent)),
        //Component.literal("  ").append(Component.translatable("better_contraption_diagram.extra-info.sum-of-forces.direction", directionComponent))
    }

    private void addFriction(Vector2d totalScaleHV, double scale, FloatingBlockMaterial material) {
        totalScaleHV.x+= scale * material.fastHorizontalFriction();
        totalScaleHV.y+= scale * material.fastVerticalFriction();
    }


}
