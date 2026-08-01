package com.zelaux.betterdiagram.util.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

public class GuiGraphicsUtil {

    public static void renderSlotContents(
        GuiGraphics guiGraphics,
        ItemStack itemstack,
        Font font,
        int imageWidth,
        int imageHeight,
        int x,
        int y,
        boolean fake,
        @org.jetbrains.annotations.Nullable String countString
    ) {

        int seed = x + y * imageWidth;
        var ps = guiGraphics.pose();
        ps.pushPose();
        ps.translate(x,y,0);
        ps.scale(imageWidth/16f,imageHeight/16f,1);

        x=-8;
        y=-8;
        if(fake) {
            guiGraphics.renderFakeItem(itemstack, x, y, seed);
        } else {
            guiGraphics.renderItem(itemstack, x, y, seed);
        }

        guiGraphics.renderItemDecorations(font, itemstack, x, y, countString);
        ps.popPose();
    }

    public static void renderLine(GuiGraphics graphics, double x1, double y1, double x2, double y2, int argb) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lenSquared = dx * dx + dy * dy;
        if(lenSquared < 0.0001f) return;
        PoseStack pose = graphics.pose();
        pose.pushPose();

        float len = (float) Math.sqrt(lenSquared);
        float angle = (float) Math.atan2(dy, dx);


        pose.translate(x1, y1, 0);
        pose.mulPose(Axis.ZP.rotation(angle));
        pose.scale(len, len, 0);


        VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(RenderType.gui());
        final int z = 0;
        final float minX = 0, maxX = 1;
        final float minY = -1 / len / 2, maxY = 1 / len / 2;
        Matrix4f matrix = pose.last().pose();
        vertexconsumer.addVertex(matrix, minX, minY, z).setColor(argb);
        vertexconsumer.addVertex(matrix, minX, maxY, z).setColor(argb);
        vertexconsumer.addVertex(matrix, maxX, maxY, z).setColor(argb);
        vertexconsumer.addVertex(matrix, maxX, minY, z).setColor(argb);
        //graphics.flush();

        //graphics.hLine(0,1,0,argb);


        pose.popPose();


    }
}
