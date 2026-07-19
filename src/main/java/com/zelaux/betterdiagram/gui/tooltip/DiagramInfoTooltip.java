package com.zelaux.betterdiagram.gui.tooltip;

import com.demonwav.mcdev.annotations.Translatable;
import com.zelaux.betterdiagram.DebugRuntime;
import com.zelaux.betterdiagram.extend.accessors.DiagramScreenAccessors;
import com.zelaux.betterdiagram.feature.balance.BalanceModel;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.BalloonVolumeCalculatorScreen;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.max_speed.WithLiftProviders;
import com.zelaux.betterdiagram.gui.widget.BDiagramButton;
import com.zelaux.betterdiagram.index.BCDForceGroups;
import com.zelaux.betterdiagram.index.Colors;
import com.zelaux.betterdiagram.index.SomeConstants;
import com.zelaux.betterdiagram.mixin.feature.extra_info_deliver.FloatingBlockControllerExt;
import com.zelaux.betterdiagram.network.extradata.ClusterInfo;
import com.zelaux.betterdiagram.network.extradata.DiagramExtraDataPacket;
import com.zelaux.betterdiagram.util.*;
import dev.ryanhcode.sable.api.block.BlockSubLevelLiftProvider;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.physics.floating_block.FloatingBlockController;
import dev.ryanhcode.sable.physics.floating_block.FloatingBlockMaterial;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.util.SableMathUtils;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import lombok.extern.slf4j.Slf4j;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.strategy.IdentityStrategy;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.lang.Math;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DiagramInfoTooltip implements BDiagramButton.TooltipListProvider {

    private static final MergedPointForceCalculator mergedForce = new MergedPointForceCalculator();
    public static final FloatingBlockControllerExt DUMMY_CONTROLLER = (FloatingBlockControllerExt) new FloatingBlockController(null);
    private final ClientSubLevel mySubLevel;
    private final @NotNull DiagramScreen diagramScreeen;
    private final @NotNull DiagramScreenAccessors diagramScreeenAcc;
    private final int tooltipColor = Colors.TOOLTIP_COLOR.getRGB();
    private static float debugScale = -1;
    public static final AbstractTooltipHandler<DiagramInfoTooltip> OPEN_BALLOON_SCREEN_TOOLTIP_HANDLER =new AbstractTooltipHandler<>() {
        protected @NotNull MutableComponent getActionName() {
            return Component.translatable("bcd.tooltip-handler.balloon.message");
        }

        @Override
        public boolean isSamePayload(@NotNull DiagramInfoTooltip newHovered, DiagramInfoTooltip oldHover) {
            return true;
        }

        @Override
        public void onActive(DiagramInfoTooltip lastPayload) {
            var accessors = lastPayload.diagramScreeenAcc;
            SubLevelDependenciesData dependenciesData = accessors.bcd$subLevelDependencies();
            if(dependenciesData==null)return;
            BalloonVolumeCalculatorScreen.show(dependenciesData,accessors.betterContraptionDiagram$clientData(),lastPayload.diagramScreeen);
        }
    };

    public DiagramInfoTooltip(@NotNull DiagramScreen self, ClientSubLevel subLevel, DiagramDataPacket serverData) {
        this.diagramScreeen = self;
        diagramScreeenAcc = DiagramScreenAccessors.of(self);
        this.mySubLevel = subLevel;
    }

    int prevUpdateTicks = 0;
    List<Component> calculatedTooltips;

    @Override
    public List<Component> get() {
        int ticks = AnimationTickHolder.getTicks();
        if(prevUpdateTicks - ticks > DiagramScreen.UPDATE_REQUEST_INTERVAL || calculatedTooltips == null) {
            prevUpdateTicks = ticks;
            calculatedTooltips = calculate();
        }
        ObjectArrayList<Component> list = new ObjectArrayList<>(calculatedTooltips);

        OPEN_BALLOON_SCREEN_TOOLTIP_HANDLER.addTooltip(this, list.size()-1,(List)list);
        return list;

    }

    public List<Component> calculate() {
        DiagramDataPacket serverData = diagramScreeenAcc.betterContraptionDiagram$serverData();
        SubLevelDependenciesData dependenciesData = diagramScreeenAcc.bcd$subLevelDependencies();
        if(serverData == null || dependenciesData == null) return List.of(
            Component.translatable("bcd.extra-info.diagram-button").withColor(tooltipColor),
            Component.translatable("bcd.extra-info.max-speed", "---").withColor(tooltipColor),
            Component.translatable("better_contraption_diagram.extra-info.gravity", "---").withColor(tooltipColor)
        );

        mergedForce.begin(BalanceModel.getMergedBalancePointOrCOM(diagramScreeen).displacement());

        Set<ForceGroup> blackList = new ObjectOpenCustomHashSet<>(Set.of(
            ForceGroups.DRAG.get(),
            BCDForceGroups.MERGED_FORCE_GROUP.group,
            BCDForceGroups.CONTRAPTION_GRAVITY.group,
            BCDForceGroups.ROPE_FORCE.group,
            BCDForceGroups.ROPE_MASS_FORCE.group
        ), IdentityStrategy.IDENTITY);

        var list2 = new ObjectArrayList<Map.Entry<ForceGroup, List<QueuedForceGroup.PointForce>>>();
        for(var entry : serverData.forces().entrySet()) {
            if(blackList.contains(entry.getKey())) continue;
            list2.add(entry);
            for(QueuedForceGroup.PointForce force : entry.getValue()) {
                mergedForce.add(force);
            }
        }
        mergedForce.end();
        SubLevelGroup group = dependenciesData.constraintTree.subLevelGroup;

        ClientSubLevel mainSubLevel = mySubLevel;
        ClientLevel level = mainSubLevel.getLevel();
        Matrix3d resultFrictionMatrix = new Matrix3d().zero();
        Matrix3d sailFrictionMatrix = new Matrix3d().zero();
        Matrix3d tmpMat = new Matrix3d();
        for(SubLevel subLevel : group) {
            Vector2d totalScaleHV = new Vector2d();
            SubLevelDependenciesData.Entry entry = dependenciesData.get(subLevel);
            if(entry.extraData == null) continue;
            Vector3dc localGravity = DimensionPhysicsData.getGravity(level, subLevel.logicalPose().position());
            double gravityMagnitude = localGravity.length();
            for(ClusterInfo cluster : entry.extraData.clusters()) {

                double scale = cluster.pressureTimesTotalScale();
                FloatingBlockMaterial material = cluster.material();
                if(material.scaleWithGravity()) {
                    scale *= gravityMagnitude;
                }
                addFriction(totalScaleHV, scale, material);
            }
            var ctxProviders = entry.extraData.liftProviders();
            var liftProviders =
                ctxProviders.isPresent() ?
                    ctxProviders.get() :
                    ((WithLiftProviders) subLevel.getPlot()).bcd$liftProviders().values();
            for(var ctx : liftProviders) {

                BlockSubLevelLiftProvider block = (BlockSubLevelLiftProvider) ctx.state().getBlock();


                Vector3d blockWorldPos = new Vector3d(
                    ctx.pos().getX() + 0.5, ctx.pos().getY() + 0.5, ctx.pos().getZ() + 0.5
                );
                double pressure = DimensionPhysicsData.getAirPressure(level, VecUtil.projectToWorld(subLevel.logicalPose(), blockWorldPos));

                double kd = block.sable$getDirectionlessDragScalar() * pressure;
                double kp = block.sable$getParallelDragScalar() * pressure;
                if(kd > 0) {
                    tmpMat.identity().scale(kd * debugScale);
                    sailFrictionMatrix.add(tmpMat);
                }
                //todo should lift force be accounted for?
                if(kp > 0) {
                    Direction localDir = block.sable$getNormal(ctx.state());
                    Vector3i joml = JOMLConversion.toJOML(localDir.getNormal());
                    Vector3d normal =
                        VecUtil.projectDirection(mainSubLevel.logicalPose(), subLevel.logicalPose(), new Vector3d(joml));


                    SableMathUtils.fmaOuterProduct(normal, normal, kp * debugScale, sailFrictionMatrix);
                }
            }


            DUMMY_CONTROLLER.bcd$getGravityMatrix(localGravity, totalScaleHV.y, totalScaleHV.x, tmpMat);
            resultFrictionMatrix.add(tmpMat);
        }
        //0.09
        double universalDrag = SomeConstants.universalDrag(level);

        //double universalDragCoeff = -(serverData.mass() * universalDrag) * ((1 - universalDrag));

        //double dampingRate =  universalDrag / (1 - universalDrag);
        double dampingRate = universalDrag;
        //double dampingRate = -Math.log(1.0 - universalDrag);
        double universalDragCoeff = -serverData.mass() * dampingRate;
        tmpMat.identity().scale(universalDragCoeff);
        resultFrictionMatrix.add(tmpMat);
        resultFrictionMatrix.add(sailFrictionMatrix);

        ObjectArrayList<Component> list = new ObjectArrayList<>();
        list.add(Component.translatable("bcd.extra-info.diagram-button").withColor(tooltipColor));

        Vector3d predictedSpeed = displaySpeed(list, "bcd.extra-info.max-speed", new Matrix3d(resultFrictionMatrix), mergedForce.totalForce);

        if(DebugRuntime.isDebug()) {
            DecimalFormat format = new DecimalFormat("0.######");
            String message = format.format(predictedSpeed.length()) + " | " + format.format(realSpeed(mainSubLevel).length());
            list.add(Component.literal(message));
            if(Screen.hasAltDown()) {
                DebugRuntime.logFile("data.csv", message + "\n");
                list.add(Component.literal("SAVED"));
            }
        }
        if(DebugRuntime.isDebug() && false) {
            list.add(Component.translatable("bcd.extra-info.inertia").withColor(tooltipColor));

            DiagramExtraDataPacket extraData = dependenciesData.get(mainSubLevel).extraData;

            MatrixFormat.Presets.inertia(
                extraData.inertiaTensor(),
                it -> list.add(Component.literal("   ").append(it))
            );
        }
        Vector3dc gravity = DimensionPhysicsData.getGravity(level, mainSubLevel.logicalPose().position());
        list.add(Component.translatable("better_contraption_diagram.extra-info.gravity",
            VecFormat.Presets.lightGray(gravity)
        ).withColor(tooltipColor));
        return list;
        //Component.literal("  ").append(Component.translatable("better_contraption_diagram.extra-info.sum-of-forces.displacement", displacementComponent)),
        //Component.literal("  ").append(Component.translatable("better_contraption_diagram.extra-info.sum-of-forces.direction", directionComponent))
    }

    private @NotNull Vector3d realSpeed(ClientSubLevel clientSubLevel) {
        Minecraft instance = Minecraft.getInstance();
        SubLevel subLevel = clientSubLevel;
        IntegratedServer singleplayerServer = instance.getSingleplayerServer();
        if(singleplayerServer != null) {
            ServerLevel level = singleplayerServer.getLevel(clientSubLevel.getLevel().dimension());
            ServerSubLevelContainer container = ServerSubLevelContainer.getContainer(level);
            if(container != null) {
                ServerSubLevel serverSubLevel = (ServerSubLevel) (subLevel = container.getSubLevel(clientSubLevel.getUniqueId()));
                Vector3d latestLinearVelocity = serverSubLevel.latestLinearVelocity;
                return new Vector3d(latestLinearVelocity);
            }
        }
        Vector3d jomlPos = new Vector3d(CenterMassCalculator.centerOfMass(subLevel));
        subLevel.logicalPose().transformPosition(jomlPos, new Vector3d()).sub(subLevel.lastPose().transformPosition(jomlPos, new Vector3d()), jomlPos).mul(20.0F);
        return jomlPos;
    }

    private Vector3d displaySpeed(ObjectArrayList<Component> list, @Translatable String key, Matrix3d frictionMatrix, Vector3dc vector3d) {
        if(Math.abs(frictionMatrix.determinant()) > 1e-9) {
            Vector3d maxSpeed = new Vector3d();
            frictionMatrix.invert();
            frictionMatrix.transform(vector3d.negate(maxSpeed), maxSpeed);

            list.add(Component.translatable(key, VecFormat.Presets.lightGray(maxSpeed)).withColor(tooltipColor));
            return maxSpeed;
        } else {
            list.add(Component.translatable(key, "---").withColor(tooltipColor));
            return null;
        }
    }

    private void addFriction(Vector2d totalScaleHV, double scale, FloatingBlockMaterial material) {
        totalScaleHV.x += scale * (material.fastHorizontalFriction());
        totalScaleHV.y += scale * (material.fastVerticalFriction());
    }


}
