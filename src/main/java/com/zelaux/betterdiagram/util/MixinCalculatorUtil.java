package com.zelaux.betterdiagram.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.zelaux.betterdiagram.Config;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinhelpers.block_outline_render.SubLevelCamera;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.List;

import static com.zelaux.betterdiagram.util.VecUtil.vectorToFormatted;

public class MixinCalculatorUtil {


    public static @Nullable Vector2d screenPositionOfExpectedCOM(DiagramScreen self, Quaternionf localOrientation, Vector3d localCameraPosition, Matrix4f projectionMat, int areaWidth, int areaHeight) {
        final Vector3d centerOfMass = CenterMassCalculator.expectedCenterOfMass(self);
        if(CenterMassCalculator.equals(centerOfMass, CenterMassCalculator.centerOfMass(self.subLevel))) return null;
        return DiagramScreen.getScreenCoords(VecUtil.minVec3d(self.subLevel.getPlot().getBoundingBox()).add(centerOfMass), localOrientation, localCameraPosition, projectionMat, areaWidth, areaHeight);
    }

    public static void renderLevel(DeltaTracker deltaTracker,
                                   boolean bl,
                                   Camera camera,
                                   GameRenderer gameRenderer,
                                   LightTexture lightTexture,
                                   Matrix4f matrix4f,
                                   Matrix4f matrix4f2,
                                   CallbackInfo ci,
                                   ClientLevel level,
                                   SubLevelCamera sable$sublevelCamera, Quaternionf sable$orientationStorage) {
        final Minecraft minecraft = Minecraft.getInstance();
        if(!GogglesItem.isWearingGoggles(minecraft.player))return;
        if (!minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes() && !minecraft.getDebugOverlay().showDebugScreen() || Minecraft.getInstance().showOnlyReducedInfo()) {
            return;
        }
        HitResult hitResult = minecraft.hitResult;
        final PoseStack poseStack = new PoseStack();
        if(!(hitResult instanceof BlockHitResult blockHitResult)) return;


        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockstate = level.getBlockState(blockPos);
        if(blockstate.isAir())return;
        final ClientSubLevel subLevel = (ClientSubLevel) Sable.HELPER.getContaining(level, blockPos);
        if(subLevel==null){

            renderCenterOfMass(camera, matrix4f, level, blockHitResult, minecraft, poseStack, blockPos);
            return;
        }

        poseStack.pushPose();

        final Pose3dc pose = subLevel.renderPose();

        sable$sublevelCamera.setCamera(camera);
        sable$sublevelCamera.setPose(pose);
        final Vec3 cameraPosition = sable$sublevelCamera.getPosition();
        final Vec3 realCameraPosition = camera.getPosition();

        final Vector3dc position = pose.position();
        final Vector3dc rotationPoint = pose.rotationPoint();
        final Quaterniondc orientation = pose.orientation();
        final Vector3dc scale = pose.scale();

        poseStack.translate(
            (float) (position.x() - realCameraPosition.x),
            (float) (position.y() - realCameraPosition.y),
            (float) (position.z() - realCameraPosition.z)
        );
        poseStack.mulPose(sable$orientationStorage.set(orientation));
        poseStack.translate(
            (float) -(rotationPoint.x() - cameraPosition.x),
            (float) -(rotationPoint.y() - cameraPosition.y),
            (float) -(rotationPoint.z() - cameraPosition.z)
        );
        poseStack.scale((float) scale.x(), (float) scale.y(), (float) scale.z());

        renderCenterOfMass(sable$sublevelCamera, matrix4f, level, blockHitResult, minecraft, poseStack, blockPos);


    }

    private static void renderCenterOfMass(Camera camera, Matrix4f matrix4f, ClientLevel level, BlockHitResult blockHitResult, Minecraft minecraft, PoseStack ps, BlockPos blockPos) {
        final MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.LINES);

        final double cx = camera.getPosition().x;
        final double cy = camera.getPosition().y;
        final double cz = camera.getPosition().z;


        ps.mulPose(matrix4f);

        BlockState blockstate = level.getBlockState(blockPos);
        //this.renderHitOutline(ps, consumer, camera.getEntity(), cx, cy-1, cz, blockPos, blockstate);


        ps.pushPose();

        Vector3dc blockCenterOfMass = MassTracker.BLOCK_CENTER_OF_MASS.apply(level, blockstate);
        if(blockCenterOfMass.equals(JOMLConversion.HALF))return;
        double mass = PhysicsBlockPropertyHelper.getMass(level, blockPos, blockstate);
        if(CenterMassCalculator.equals(mass,0))return;

        final Vector3dc offset = new Vector3d(JOMLConversion.toJOML(blockHitResult.getBlockPos())).add(blockCenterOfMass);

        ps.translate(offset.x() - cx, offset.y() - cy, offset.z() - cz);
        //ps.mulPose(new Quaternionf(renderPose.orientation()));

        float size0 = 2.0f / 16.0f;
        int[] scales ={1,2,3};
        for(int scale : scales) {
            float size=size0*scale;
            LevelRenderer.renderLineBox(
                ps,
                consumer,
                -size,
                -size,
                -size,
                size,
                size,
                size,
                0.7f, 0.7f, 0.5f, 1.0f
            );
        }

        ps.popPose();
        bufferSource.endLastBatch();
    }

    public static void displayECOMTooltip(int mouseX, int mouseY, float areaOriginX, float areaOriginY, Vector2d screenCoords, List<FormattedText> tooltipList1, Vector3d eCOM) {
        if(screenCoords.distanceSquared(mouseX - areaOriginX, mouseY - areaOriginY) >= 8.0 * 8.0) return;
        int color = (0xff00_0000) | Config.EXPECTED_CENTER_OF_MASS_COLOR.getAsInt();
        MutableComponent centerOfMassTitle = Component.translatable("better_contraption_diagram.eCOM");
        tooltipList1.add(Component.translatable(
            "better_contraption_diagram.eCOM.tooltip",
            centerOfMassTitle.withColor(color),
            vectorToFormatted(eCOM).withColor((0xff << 24) | Config.FORCE_CORDS_COLOR.getAsInt())
        ));
    }
}
