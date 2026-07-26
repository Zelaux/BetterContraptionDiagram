package com.zelaux.betterdiagram.feature.clientside_forces;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zelaux.betterdiagram.index.BTextures;
import com.zelaux.betterdiagram.struct.BTexture;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramForceGroupToggle;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.index.SimGUITextures;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

public class DiagramForceGroupToggleList extends ObjectSelectionList<DiagramForceGroupToggleList.DiagramForceGroupToggleEntry> {

    private final BTexture arrowTexture = BTextures.Diagram.ICON_LIST_ARROW;
    private final DiagramScreen screen;

    public DiagramForceGroupToggleList(Minecraft minecraft, int width, int height, int itemHeight, DiagramScreen screen) {
        super(minecraft, width, height, 0, itemHeight);
        headerHeight = -4;
        this.screen=screen;
    }

    public boolean renderTab = false;

    @Override
    public void renderListItems(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderListItems(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderSelection(GuiGraphics guiGraphics, int top, int width, int height, int outerColor, int innerColor) {
        //super.renderSelection(guiGraphics, top, width, height, outerColor, innerColor);
    }

    @Override
    public int getRowWidth() {
        return SimGUITextures.DIAGRAM_PAPER.width;
    }

    @Override
    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight;
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
        //super.renderListBackground(guiGraphics);
    }

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {
        //super.renderListSeparators(guiGraphics);
    }

    @Override
    public DiagramForceGroupToggleEntry getEntry(int index) {
        return super.getEntry(index);
    }


    public void addEntry(DiagramForceGroupToggle toggle) {
        addEntry(new DiagramForceGroupToggleEntry(toggle));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        PoseStack ps = guiGraphics.pose();
        ps.pushPose();

        BTexture texture = this.arrowTexture;
        final float paperOffset = screen.getPaperOffset(partialTick);

        ps.translate(DiagramScreen.MAX_PAPER_OFFSET - paperOffset, 0, 0);
        int centerX = getX() + getWidth() / 2;
        if(getRowTop(getItemCount() - 1)+itemHeight > getBottom()) {
            texture.render(guiGraphics, centerX - texture.width / 2, getBottom());
        }

        if(getRowBottom(0)-itemHeight/2<getY()) {
            ps.pushPose();
            ps.translate(getX() + getWidth() / 2f, getY() ,0);
            ps.mulPose(Axis.ZP.rotation((float) Math.PI));
            ps.translate(-texture.width/2f, 0,0);
            texture.render(guiGraphics,0,0);
            ps.popPose();
        }
        ps.popPose();
    }

    public void renderTabs(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderTab = true;
        render(graphics, mouseX, mouseY, partialTicks);

        renderTab = false;
    }

    public void renderItems(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderTab = false;
        render(graphics, mouseX, mouseY, partialTicks);
        renderTab = false;
    }

    @AllArgsConstructor
    public class DiagramForceGroupToggleEntry extends Entry<DiagramForceGroupToggleEntry> {
        public final DiagramForceGroupToggle toggle;

        @Override
        public Component getNarration() {
            return toggle.getMessage();
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return toggle.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return toggle.keyReleased(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean mouseClicked(double p_331676_, double p_330254_, int p_331536_) {
            return toggle.mouseClicked(p_331676_, p_330254_, p_331536_);
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            toggle.mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return toggle.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return toggle.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            return toggle.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return toggle.charTyped(codePoint, modifiers);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            toggle.setPosition(left, top);
            if(DiagramForceGroupToggleList.this.renderTab) {
                toggle.renderTab(guiGraphics, mouseX, mouseY, partialTick);
            } else {
                toggle.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }
}
