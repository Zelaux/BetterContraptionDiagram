package com.zelaux.betterdiagram.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.ArrayList;

public class GridClicker extends AbstractSimiWidget {
    public static int ACTIVE_COLOR = (0xff << 24) | ChatFormatting.BLACK.getColor();
    public static int INACTIVE_COLOR = 0;//((0xff << 24) | ChatFormatting.GRAY.getColor());
    public int gridColor = ACTIVE_COLOR;

    public GridClicker gridColor(int gridColor) {
        this.gridColor = gridColor;
        return this;
    }

    public final ArrayList<MouseConsumer> mouseConsumers = new ArrayList<>();
    private final ArrayList<MouseConsumer> mouseConsumers_old = new ArrayList<>();
    private final Vector2i sizeInBlocks;
    private final Vector2i tmp = new Vector2i(), tmp2 = new Vector2i();
    public boolean drawMouse = false;

    public GridClicker(double x, double y, double x1, double y1, Vector2i sizeInBlocks) {
        super((int) Math.min(x, x1), (int) Math.min(y, y1), (int) Math.abs(x1 - x), (int) Math.abs(y1 - y));
        this.sizeInBlocks = sizeInBlocks;
        if(sizeInBlocks.x == 0) sizeInBlocks.x = 1;
        if(sizeInBlocks.y == 0) sizeInBlocks.y = 1;
    }

    public Vector2i toGlobal(Vector2i pos) {
        pos.mul(width, height);
        pos.x /= sizeInBlocks.x;
        pos.y /= sizeInBlocks.y;
        pos.add(getX(), getY());
        return pos;
    }

    public GridClicker drawMouse(boolean drawMouse) {
        this.drawMouse = drawMouse;
        return this;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int pointer) {
        if(mouseConsumers.isEmpty()) return;
        Vector2i mouse = tmp.set((int) mouseX, (int) mouseY);
        int x = mouse.x, y = mouse.y;
        toGrid(mouse);
        int x1 = mouse.x, y1 = mouse.y;
        mouseConsumers_old.clear();
        mouseConsumers_old.addAll(mouseConsumers);
        for(var consumer : mouseConsumers_old) {
            consumer.consume(this, tmp.set(x, y), tmp2.set(x1, y1), mouseX, mouseY, pointer);
        }
        mouseConsumers_old.clear();

    }

    public Vector2i toGrid(Vector2i pos) {
        return toSimpleGridFromAbs(toGridAbs(pos));
    }

    public Vector2i toSimpleGridFromAbs(Vector2i gridAbs) {
        int dx = 1 - ((sizeInBlocks.x >>> 31) << 1);
        int dy = ((sizeInBlocks.y >>> 31) << 1) - 1;
        if(dx < 0) gridAbs.x = Math.abs(sizeInBlocks.x) - 1 - gridAbs.x;
        if(dy < 0) gridAbs.y = sizeInBlocks.y - 1 - gridAbs.y;
        return gridAbs;
    }

    public Vector2d toSimpleGridFromAbs(Vector2d gridAbs) {
        int dx = 1 - ((sizeInBlocks.x >>> 31) << 1);
        int dy = ((sizeInBlocks.y >>> 31) << 1) - 1;
        if(dx < 0) gridAbs.x = Math.abs(sizeInBlocks.x) - 1 - gridAbs.x;
        if(dy < 0) gridAbs.y = sizeInBlocks.y - 1 - gridAbs.y;
        return gridAbs;
    }

    public Vector2i toGridAbs(Vector2i pos) {

        pos.sub(getX(), getY());
        //pos.y = height - pos.y;

        pos.mul(Math.abs(sizeInBlocks.x), Math.abs(sizeInBlocks.y));

        if(pos.x < 0) pos.x -= width - 1;
        if(pos.y < 0) pos.y -= height - 1;
        pos.x /= width;
        pos.y /= height;
        return pos;
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack pose = graphics.pose();
        int perWidth = Math.abs(sizeInBlocks.x);
        int perHeight = Math.abs(sizeInBlocks.y);
        int y = getY(), x = getX();
        for(int i = 0; i <= perWidth; i++) {
            int curX = x + i * width / perWidth;
            graphics.fill(curX, y, curX + 1, y + height, gridColor);
        }
        for(int i = 0; i <= perHeight; i++) {
            int curY = y + i * height / perHeight;
            graphics.fill(x, curY, x + width, curY + 1, gridColor);
        }

        if(!drawMouse) return;
        cellCornersFromGlobal(tmp.set(mouseX, mouseY), tmp2);
        graphics.fill(tmp.x, tmp.y, tmp2.x, tmp2.y, 0xaa5541E6);
    }

    public void cellCornersFromGlobal(Vector2i first, Vector2i topRightCorner) {
        cellCorners(toGridAbs(first), topRightCorner);
    }

    public void cellCorners(Vector2i gridPos, Vector2i topRightCorner) {
        toGlobalAbs(gridPos.add(1, 1, topRightCorner)).sub(0, 0);
        toGlobalAbs(gridPos).add(1, 1);
    }

    public Vector2i toGlobalAbs(Vector2i pos) {
        pos.mul(width, height);
        pos.x /= Math.abs(sizeInBlocks.x);
        pos.y /= Math.abs(sizeInBlocks.y);
        //pos.y = height - pos.y;
        return pos.add(getX(), getY());

    }

    public interface MouseConsumer {
        void consume(GridClicker grid, Vector2i rawMouse, Vector2i gridPos, double mouseX, double mouseY, int pointer);
    }
}
