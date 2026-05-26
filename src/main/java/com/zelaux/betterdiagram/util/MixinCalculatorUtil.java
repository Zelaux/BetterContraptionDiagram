package com.zelaux.betterdiagram.util;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.data.OffCenteredBlock;
import com.zelaux.betterdiagram.extend.ProjectionAccessor;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.gui.comp.InTextBlockRenderer;
import com.zelaux.betterdiagram.gui.comp.SeparatorTooltipComponent;
import com.zelaux.betterdiagram.index.BCDTextures;
import com.zelaux.betterdiagram.struct.MassStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinhelpers.block_outline_render.SubLevelCamera;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import joptsimple.internal.Strings;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.ArrayList;
import java.util.List;

import static com.zelaux.betterdiagram.Config.COM_viewType;
import static net.createmod.catnip.lang.LangBuilder.DEFAULT_SPACE_WIDTH;

public class MixinCalculatorUtil {


    public static @Nullable Vector2d screenPositionOfExpectedCOM(DiagramScreen self, Quaternionf localOrientation, Vector3d localCameraPosition, Matrix4f projectionMat, int areaWidth, int areaHeight) {
        final var centerOfMass = CenterMassCalculator.expectedCenterOfMass(self);
        if(CenterMassCalculator.equals(centerOfMass, CenterMassCalculator.centerOfMass(self.subLevel))) return null;
        return DiagramScreen.getScreenCoords(new Vector3d(centerOfMass), localOrientation, localCameraPosition, projectionMat, areaWidth, areaHeight);
    }

    /**
     * @see dev.ryanhcode.sable.mixin.debug_render.LevelRendererMixin#renderLevel(DeltaTracker, boolean, Camera, GameRenderer, LightTexture, Matrix4f, Matrix4f, CallbackInfo)
     * @see dev.ryanhcode.sable.neoforge.mixin.block_outline_render.LevelRendererMixin#sable$preRenderHitOutline(LevelRenderer, Camera, HitResult, DeltaTracker, PoseStack, MultiBufferSource, Operation, LocalBooleanRef)
     *
     */
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
        if(!GogglesItem.isWearingGoggles(minecraft.player)) return;
        if(!COM_viewType.get().shouldShow(minecraft.player.isShiftKeyDown()) || Minecraft.getInstance().showOnlyReducedInfo()) {
            return;
        }
        HitResult hitResult = minecraft.hitResult;
        final PoseStack ps = new PoseStack();
        if(!(hitResult instanceof BlockHitResult blockHitResult)) return;


        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockstate = level.getBlockState(blockPos);
        if(blockstate.isAir()) return;
        final ClientSubLevel subLevel = (ClientSubLevel) Sable.HELPER.getContaining(level, blockPos);
        renderCenterOfMass(camera, matrix4f, level, blockHitResult, minecraft, ps, blockPos, subLevel);


    }

    private static void renderCenterOfMass(Camera camera,
                                           Matrix4f matrix4f,
                                           ClientLevel level,
                                           BlockHitResult blockHitResult,
                                           Minecraft minecraft,
                                           PoseStack ps,
                                           BlockPos blockPos,
                                           @Nullable ClientSubLevel subLevel) {
        final MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.LINES);

        final double cx = camera.getPosition().x;
        final double cy = camera.getPosition().y;
        final double cz = camera.getPosition().z;

        ps.pushPose();
        ps.mulPose(matrix4f);

        BlockState blockstate = level.getBlockState(blockPos);

        Vector3dc blockCenterOfMass = MassTracker.BLOCK_CENTER_OF_MASS.apply(level, blockstate);
        if(blockCenterOfMass.equals(JOMLConversion.HALF)) {
            return;
        }
        double mass = PhysicsBlockPropertyHelper.getMass(level, blockPos, blockstate);
        if(CenterMassCalculator.equals(mass, 0)) {
            return;
        }

        final Vector3dc offset = new Vector3d(JOMLConversion.toJOML(blockHitResult.getBlockPos())).add(blockCenterOfMass);


        if(subLevel != null) {
            final Pose3dc renderPose = subLevel.renderPose();

            Vector3dc center = renderPose.position();
            Vector3dc locCenter = renderPose.rotationPoint();
            ps.translate(center.x() - cx, center.y() - cy, center.z() - cz);
            ps.mulPose(new Quaternionf(renderPose.orientation()));
            ps.translate(offset.x() - locCenter.x(), offset.y() - locCenter.y(), offset.z() - locCenter.z());
        } else {
            ps.translate(offset.x() - cx, offset.y() - cy, offset.z() - cz);
        }
        float size0 = 2.0f / 16.0f;
        int[] scales = {1, 2, 3};
        for(int scale : scales) {
            float size = size0 * scale;
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
            VecFormat.Presets.forceCords(eCOM)
        ));
    }

    public static int getIndents(Font font, int defaultIndents) {
        int spaceWidth = font.width(" ");
        if(DEFAULT_SPACE_WIDTH == spaceWidth) {
            return defaultIndents;
        }
        return Mth.ceil(DEFAULT_SPACE_WIDTH * defaultIndents / spaceWidth);
    }

    public static void forGoogles(List<Component> tooltip, int indent, MutableComponent component) {
        tooltip.add(indented(indent, component)
        );
    }

    public static @NotNull MutableComponent indented(int indent, MutableComponent component) {
        return Component
            .literal(Strings.repeat(' ', getIndents(Minecraft.getInstance().font, 4 + indent)))
            .append(component);
    }

    /**
     * For HotSwap purpose
     *
     */
    public static HoveredOffCenteredBlock renderOffCentered(WithClientData withClientData,
                                                            @NotNull DiagramScreen self, List<FormattedText> tooltipList,
                                                            ProjectionAccessor accessor,
                                                            GuiGraphics graphics,
                                                            int mouseX,
                                                            int mouseY, boolean shouldClipWeights) {
        ArrayList<OffCenteredBlock> blocks = CenterMassCalculator.getOrCreateOffCenteredBlocks(withClientData, self);
        if(blocks == null || blocks.isEmpty()) return null;
        final int shadowColor = 0xfff9f2de;
        Vector3d tmp = new Vector3d();
        boolean wasAnyOne = false;
        double hoveredDistance2 = Double.MAX_VALUE;
        int hoveredIndex = -1;
        OffCenteredBlock hovered = null;
        for(OffCenteredBlock block : blocks) {
            MutableComponent localBlockPos = VecFormat.Presets.gray(tmp);

            Vector2d originCoords = accessor.betterContraptionDiagram$getScreenCoords(JOMLConversion.toJOML(block.pos().getCenter(), tmp));
            if(shouldClipWeights && !accessor.bcd$canDrawAt((int) originCoords.x, (int) originCoords.y)) continue;
            double dst2 = originCoords.distanceSquared(mouseX - accessor.bcd$originX(), mouseY - accessor.bcd$originY());
            if(dst2 < 8 * 8) {

                if(wasAnyOne) {
                    tooltipList.add(new SeparatorTooltipComponent().wrap());
                }
                if(dst2 < hoveredDistance2) {
                    hovered = block;
                    hoveredDistance2 = dst2;
                    hoveredIndex = tooltipList.size();
                }
                tooltipList.add(Component.translatable(
                    "better_contraption_diagram.google-overlay.com-offset",
                    VecFormat.Presets.gray(block.COM())
                ));
                tooltipList.add(Component.translatable(
                    "better_contraption_diagram.diagram.offenter-blocks.blockpos",
                    localBlockPos
                ));
                tooltipList.add(new InTextBlockRenderer(block.state()).wrap());
                wasAnyOne = true;
            }
            BCDTextures.Diagram.ICON_OFF_CENTERED_BLOCK_SHADOW.render(graphics, (int) originCoords.x - 8, (int) originCoords.y - 8, new Color(shadowColor));
            BCDTextures.Diagram.ICON_COLORED_OFF_CENTERED_BLOCK.render(graphics, (int) originCoords.x - 8, (int) originCoords.y - 8, Color.WHITE);
        }
        return hovered == null ? null : new HoveredOffCenteredBlock(hoveredDistance2, hoveredIndex, hovered);
    }

    public record HoveredOffCenteredBlock(double distanceSquared, int tooltipStartIndex, OffCenteredBlock block) {}

    public interface TooltipAdder {
        void apply(List<FormattedText> tooltips, MassStack stack, Component v);
    }
}
