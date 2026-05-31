package com.zelaux.betterdiagram.gui.screen.COM;

import com.zelaux.betterdiagram.extend.accessors.EditBoxAccessors;
import com.zelaux.betterdiagram.util.StringUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

@FieldDefaults(level = AccessLevel.PUBLIC)
@Getter
@RequiredArgsConstructor
public class Entry {
    final Direction.Axis axis;
    EditBox editBox;
    float value = 0.5f;
    boolean enabled = true;

    static Entry makeEntry(Direction.Axis axis) {
        final var entry = new Entry(axis);
        var editBox = new EditBox(Minecraft.getInstance().font, 60, 16, null) {
            Tooltip error;
            final EditBoxAccessors accessors = (EditBoxAccessors) (EditBox) this;

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

                int x = getX(), y = getY(), x2 = x + getWidth(), y2 = y + getHeight();
                if(error != null && entry.enabled) {
                    setTooltip(error);
                    int color = ChatFormatting.RED.getColor() | 0xff00_0000;
                    guiGraphics.hLine(x - 1, x2, y - 1, color);
                    guiGraphics.hLine(x - 1, x2, y2, color);
                    guiGraphics.vLine(x - 1, y - 1, y2, color);
                    guiGraphics.vLine(x2, y - 1, y2, color);
                } else setTooltip(null);
                boolean was = this.active;
                this.active = entry.enabled && was;
                int i = accessors.bcd$highlightPos();
                if(!entry.enabled) accessors.bcd$highlightPos(getCursorPosition());
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                if(!entry.enabled) {
                    guiGraphics.fill(x, y, x2, y2, 0xbb_00_00_00);
                    accessors.bcd$highlightPos(i);
                }
                this.active = was;
            }

            @Override
            protected boolean isValidClickButton(int button) {
                return button == 0 || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
            }

            @Override
            public void onClick(double mouseX, double mouseY, int button) {
                if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    entry.enabled = !entry.enabled;
                    return;
                }
                super.onClick(mouseX, mouseY, button);
            }

            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
            }
        };
        entry.editBox = editBox;

        editBox.setFilter(txt -> entry.enabled);

        editBox.setResponder(txt -> {
            try {
                entry.value = Float.parseFloat(txt);
                editBox.error = null;
            } catch(NumberFormatException e) {
                editBox.error = Tooltip.create(Component.literal(e.getLocalizedMessage()));
            }
        });
        return entry;
    }

    public void setValue(double value) {
        boolean was = enabled;
        enabled = true;
        editBox.setValue(StringUtil.PARSABLE_FORMAT_5.format(value));
        enabled = was;
    }
}
