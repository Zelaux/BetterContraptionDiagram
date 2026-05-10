package com.zelaux.betterdiagram.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllKeys;
import com.zelaux.betterdiagram.extend.*;
import com.zelaux.betterdiagram.gui.widget.GridClicker;
import com.zelaux.betterdiagram.gui.widget.PartialInteration;
import com.zelaux.betterdiagram.struct.TransformedAxes;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import lombok.AccessLevel;
import lombok.Setter;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import static com.zelaux.betterdiagram.util.UIUtil.*;
import static com.zelaux.betterdiagram.util.VecUtil.*;

public class CenterMassMovingScreen extends AbstractSimiScreen {
    DiagramScreen diagramScreen;
    DiagramStickyNote diagramStickyNote;
    DiagramScreenAccessors diagramScreenAccessors;
    DiagramStickyNoteAccessors noteAccessors;
    ClientData clientData;
    @Setter(AccessLevel.PRIVATE)
    private EditBox editX, editY, editZ;
    private boolean programatic;
    private MyGridClicker mainGrid, subGrid;
    private TransformedAxes mainProjectedAxes, subProjectedAxes;
    private PartialInteration partialInterationForScreen;
    private boolean wasDirty;


    public CenterMassMovingScreen(DiagramScreen diagramScreen) {
        this.diagramScreen = diagramScreen;
        diagramScreenAccessors = CenterMassCalculator.accessors(diagramScreen);
        diagramStickyNote = diagramScreenAccessors.betterContraptionDiagram$note();
        noteAccessors = CenterMassCalculator.accessors(diagramStickyNote);
        clientData = new ClientData(((WithClientData) diagramScreenAccessors.betterContraptionDiagram$diagram()));
        expectedCenterOfMass()
        ;
    }

    public Vector3d expectedCenterOfMass() {
        return CenterMassCalculator.expectedCenterOfMass(clientData, diagramScreen.subLevel);
    }

    private static double getOffset() {
        double offset;
        if(AllKeys.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) offset = 0.5;
        else if(AllKeys.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) offset = 10;
        else offset = 1;
        return offset;
    }

    public static void open(@NotNull final DiagramScreen owner) {
        final Minecraft minecraft = Minecraft.getInstance();
        final CenterMassMovingScreen screen = new CenterMassMovingScreen(owner);
        minecraft.pushGuiLayer(screen);
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_WORK_CARTOGRAPHER, 1.0f));
    }

    @Override
    protected void init() {
        super.init();
        LinearLayout mainLayout = LinearLayout.vertical().spacing(5);


        var editX = editBox(Component.literal("X"), VecUtil.GETTERS_3d[0], SETTERS_3d[0], this::editX);
        var editY = editBox(Component.literal("Y"), VecUtil.GETTERS_3d[1], SETTERS_3d[1], this::editY);
        var editZ = editBox(Component.literal("Z"), VecUtil.GETTERS_3d[2], SETTERS_3d[2], this::editZ);
        updateEdit(expectedCenterOfMass());
        mainLayout.addChild(
            horizontal(5, editX, editY, editZ)
        );

        final var centerButton = makeButton(Component.translatable("better_contraption_diagram.calculator.move_to_struct_center"), () -> {
            expectedCenterOfMass().set(centerOfSubLevel());
            positionUpdatedNotFromEditBox(expectedCenterOfMass());
        }, () -> Component.translatable("better_contraption_diagram.calculator.move_to_struct_center"));
        final var selectBlock = makeButton(Component.translatable("better_contraption_diagram.calculator.select_block"), () -> {
            selectBlock((grid, rawMouse, gridPos, mouseX, mouseY, pointer) -> {
                Vector3d COM = expectedCenterOfMass();
                gridToNormalVector(grid, gridPos, COM);
                positionUpdatedNotFromEditBox(COM);
            });
        }, () -> Component.translatable("better_contraption_diagram.calculator.select_block"));
        final var resetBlock = makeButton(Component.translatable("better_contraption_diagram.calculator.reset_block"), () -> {
            Vector3d COM = expectedCenterOfMass();
            COM.set(currentCenterOfMass());
            positionUpdatedNotFromEditBox(COM);
        }, () -> Component.translatable("better_contraption_diagram.calculator.reset_block"));
        mainLayout.addChild(
            horizontal(5, centerButton, selectBlock, resetBlock)
        );

        final int diagramX = this.width / 2;
        final int diagramY = this.height / 2;

        mainLayout.arrangeElements();

        mainLayout.setX(diagramX - mainLayout.getWidth() / 2);
        mainLayout.setY(diagramY - DiagramScreen.DIAGRAM_TEXTURE.height / 2 - mainLayout.getHeight());
        mainLayout.arrangeElements();


        var areaWidth = DiagramScreen.DIAGRAM_TEXTURE.width;
        var areaHeight = DiagramScreen.DIAGRAM_TEXTURE.height;
        var bb = diagramScreen.subLevel.getPlot().getBoundingBox();


        mainProjectedAxes = VecUtil.projectAxises(DiagramScreen.LOCAL_ORIENTATION);
        subProjectedAxes = VecUtil.projectAxises(noteAccessors.NOTE_ORIENTATION());

        int diaX = width / 2 - areaWidth / 2;
        int diaY = height / 2 - areaHeight / 2;

        mainGrid = addRenderableWidget(makeGrid(bb, diaX, diaY, mainProjectedAxes, diagramScreenAccessors));
        addSubGrid(bb, diaX, diaY);

        partialInterationForScreen = addWidget(new PartialInteration(
            diaX + 228, diaY + 8,
            (diaX + 243 + 8) - (diaX + 228), (diagramY + 8 + 14 + 8) - (diagramY + 8),
            diagramScreen
        ));

        mainLayout.visitWidgets(this::addRenderableWidget);
    }

    private MyGridClicker makeGrid(BoundingBox3ic bb, int diaX, int diaY, TransformedAxes projectedAxes, ProjectionAccessor projectionAccessor) {
        var sizeInBlocks = new Vector2i();

        sizeInBlocks.add(projectedAxes.xInt.x * bb.width(), projectedAxes.xInt.y * bb.width());
        sizeInBlocks.add(projectedAxes.yInt.x * bb.height(), projectedAxes.yInt.y * bb.height());
        sizeInBlocks.add(projectedAxes.zInt.x * bb.length(), projectedAxes.zInt.y * bb.length());

        Vector2d minScreen = projectionAccessor
            .betterContraptionDiagram$getScreenCoords(minVec3d(bb), null)
            .add(diaX, diaY);
        Vector2d maxScreen = projectionAccessor
            .betterContraptionDiagram$getScreenCoords(maxVec3d(bb).add(1, 1, 1), null)
            .add(diaX, diaY);

        return new MyGridClicker(minScreen.x, minScreen.y, maxScreen.x, maxScreen.y, sizeInBlocks, projectedAxes, new Vector3d()).gridColor(GridClicker.GRAY_COLOR);
    }

    private void addSubGrid(BoundingBox3ic bb, int diaX, int diaY) {
        var grid=subGrid = addRenderableWidget(
            makeGrid(
                new BoundingBox3i(diagramScreenAccessors.betterContraptionDiagram$config().getNoteConfigs().getNoteScope()),
                diagramStickyNote.getX()+noteAccessors.SUBLEVEL_RENDER_X_OFFSET(),
                diagramStickyNote.getY()+noteAccessors.SUBLEVEL_RENDER_Y_OFFSET(),
                subProjectedAxes,
                noteAccessors
            )
        );
        grid.offset=minVec3d(
            new BoundingBox3i(diagramScreenAccessors.betterContraptionDiagram$config().getNoteConfigs().getNoteScope())
        ).sub(minVec3d(bb));
    }

    private void gridToNormalVector(GridClicker grid, Vector2i gridPos, Vector3d out) {

        TransformedAxes projectedAxes = grid instanceof MyGridClicker my ? my.projectedAxes : mainProjectedAxes;
        var offset = grid instanceof MyGridClicker my ? my.offset : ZERO_V;
        int xi = projectedAxes.xyzIndex[0];
        int yi = projectedAxes.xyzIndex[1];
        SETTERS_3d[xi].accept(out, gridPos.x + 0.5+GETTERS_3d[xi].applyAsDouble(offset));
        SETTERS_3d[yi].accept(out, gridPos.y + 0.5+GETTERS_3d[yi].applyAsDouble(offset));
    }

    private Vector2d normalToGridVector(Vector3d in, Vector2d gridPos) {
        gridPos.x = VecUtil.GETTERS_3d[mainProjectedAxes.xyzIndex[0]].applyAsDouble(in) - 0.5;
        gridPos.y = VecUtil.GETTERS_3d[mainProjectedAxes.xyzIndex[1]].applyAsDouble(in) - 0.5;
        return gridPos;
    }

    public void selectBlock(GridClicker.MouseConsumer consumer) {
        enableGrid((grid, rawMouse, gridPos, mouseX, mouseY, pointer) -> {
            consumer.consume(grid, rawMouse, gridPos, mouseX, mouseY, pointer);
            disableGrid();
        });
    }

    /**
     * @see CenterMassMovingScreen#disableGrid
     *
     */
    private void enableGrid(GridClicker.MouseConsumer mouseConsumer) {
        for(GuiEventListener child : children()) {
            if(!(child instanceof AbstractWidget widget)) continue;
            if(widget instanceof GridClicker) {
                if(!(widget instanceof MyGridClicker grid)) continue;
                grid.mouseConsumers.add(mouseConsumer);
                grid.drawMouse = true;
                //partialInterationForScreen.active = false;
                grid.gridColor = GridClicker.BLACK_COLOR;
                continue;
            }
            widget.active = false;
        }
    }

    private Vector3d centerOfSubLevel() {
        BoundingBox3ic box = diagramScreen.subLevel.getPlot().getBoundingBox();
        return maxVec3d(box).add(1, 1, 1).sub(minVec3d(box)).div(2);
    }

    private Vector3d currentCenterOfMass() {
        return CenterMassCalculator.centerOfMass(diagramScreen.subLevel);
    }

    private AbstractWidget makeButton(MutableComponent title, Runnable onclicl, Supplier<Component> diagramTooltip) {
        return makeButton(title, 64, 24, onclicl, diagramTooltip);
    }

    private @NotNull ExtendedButton makeButton(MutableComponent title, int width, int height, Runnable onclicl, Supplier<Component> diagramTooltip) {
        ExtendedButton extendedButton = new ExtendedButton(0, 0, width, height, title, self -> onclicl.run()) {

            @Override
            public void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTicks) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);

                if(diagramTooltip != null && this.isHovered()) {
                    final List<FormattedText> lines = List.of(diagramTooltip.get());
                    DiagramScreen.renderTooltip(guiGraphics, mouseX, mouseY, lines);
                }
            }
        };
        return extendedButton;
    }

    private void updateEdit(Vector3d expectedCenterOfMass) {
        DecimalFormat format = new DecimalFormat("#.#######");
        programatic = true;
        editX.setValue(format.format(expectedCenterOfMass.x));
        editY.setValue(format.format(expectedCenterOfMass.y));
        editZ.setValue(format.format(expectedCenterOfMass.z));
        programatic = false;
    }

    private @NotNull LayoutElement editBox(MutableComponent boxName,
                                           ToDoubleFunction<Vector3d> getter,
                                           ObjDoubleConsumer<Vector3d> setter,
                                           Consumer<EditBox> boxConsumer) {
        final var editBox = initEditBox(boxName, setter, boxConsumer);

        MutableComponent translatable = Component.translatable("better_contraption_diagram.common.shift-ctrl-scale");
        final var incBtn = makeButton(Component.literal("+"), 15, 10, () -> {
            var v = expectedCenterOfMass();
            setter.accept(v, getter.applyAsDouble(v) + getOffset());
            positionUpdatedNotFromEditBox(v);
        }, () -> translatable);
        final var decBtn = makeButton(Component.literal("-"), 15, 10, () -> {
            var v = expectedCenterOfMass();
            setter.accept(v, getter.applyAsDouble(v) - getOffset());
            positionUpdatedNotFromEditBox(v);
        }, () -> translatable);
        final var center = makeButton(Component.literal("C"), 15, 20, () -> {
            var v = expectedCenterOfMass();
            setter.accept(v, getter.applyAsDouble(centerOfSubLevel()));
            positionUpdatedNotFromEditBox(v);
        }, () -> Component.translatable("better_contraption_diagram.calculator.move_to_struct_center"));
        final var choose = makeButton(Component.literal("D"), 15, 20, () -> {
            selectBlock((grid, rawMouse, gridPos, mouseX, mouseY, pointer) -> {

                Vector3d com = new Vector3d();
                gridToNormalVector(grid, gridPos, com);
                Vector3d v = expectedCenterOfMass();
                setter.accept(v, getter.applyAsDouble(com));
                positionUpdatedNotFromEditBox(v);
            });
        }, () -> Component.translatable("better_contraption_diagram.calculator.select_block"));
        final var reset = makeButton(Component.literal("R"), 15, 20, () -> {
            var v = expectedCenterOfMass();
            setter.accept(v, getter.applyAsDouble(currentCenterOfMass()));
            positionUpdatedNotFromEditBox(v);
        }, () -> Component.translatable("better_contraption_diagram.calculator.reset_block"));

        MutableComponent append = boxName.append(":");
        return horizontal(1,
            label(append),
            editBox,
            vertical(0, incBtn, decBtn),
            center,
            choose,
            reset
        );
    }

    private void positionUpdatedNotFromEditBox(Vector3d v) {
        updateEdit(v);
        positionUpdated();
    }

    private @NotNull EditBox initEditBox(MutableComponent boxName, ObjDoubleConsumer<Vector3d> setter, Consumer<EditBox> boxConsumer) {
        final var editBox = new EditBox(minecraft.font, 64, 20, boxName);
        editBox.setFilter(s -> {
            if(s.isEmpty() || s.equals("-"))
                return true;
            try {
                Double.parseDouble(s);
                editBox.setTooltip(null);
                return true;
            } catch(NumberFormatException e) {
                editBox.setTooltip(Tooltip.create(Component.literal(e.getLocalizedMessage()).withColor(ChatFormatting.RED.getColor())));
                return false;
            }
        });
        editBox.setResponder(s -> {
            if(programatic) return;
            try {
                double v = Double.parseDouble(s);
                setter.accept(expectedCenterOfMass(), v);
                positionUpdated();
                editBox.setTooltip(null);
            } catch(NumberFormatException e) {
                editBox.setTooltip(Tooltip.create(Component.literal(e.getLocalizedMessage()).withColor(ChatFormatting.RED.getColor())));
            }
        });
        boxConsumer.accept(editBox);
        return editBox;
    }

    private void positionUpdated() {
        CenterMassCalculator.recalculateStacks(diagramScreen);
    }

    @Override
    public void tick() {
        wasDirty |= diagramScreenAccessors.betterContraptionDiagram$configDirty();

        super.tick();

        if(diagramScreen.subLevel.isRemoved() || diagramScreenAccessors.betterContraptionDiagram$diagram().isRemoved()) {
            super.onClose();
            //minecraft.screen = diagramScreen;
            diagramScreen.onClose();
            return;
        }
        diagramScreen.tick();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        diagramScreen.resize(minecraft, width, height);
        super.resize(minecraft, width, height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0, 0, -2);
        diagramScreen.render(graphics, mouseX, mouseY, partialTicks);
        pose.popPose();
        graphics.flush();

        if(wasDirty) {
            this.repositionElements();
            wasDirty = false;
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderWindowBackground(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        RenderSystem.disableDepthTest();
        graphics.fill(0, 0, this.width, this.height, -10, 0x4fffffff);
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        /*Vector2i left = new Vector2i(),right=new Vector2i();
        Vector3d vector3d = ;
        Quaternionf localOrientation = DiagramScreen.LOCAL_ORIENTATION;
        Vector3d localCameraPosition = diagramScreenAccessors.LOCAL_CAMERA_POSITION();
        Matrix4f projectionMat = diagramScreenAccessors.PROJECTION_MAT();

        Vector2d eCOM = MixinCalculatorUtil.screenPositionOfExpectedCOM(
            diagramScreen, localOrientation, localCameraPosition, projectionMat, DiagramScreen.DIAGRAM_TEXTURE.width, DiagramScreen.DIAGRAM_TEXTURE.height
        );
        if(eCOM!=null) {
            mainGrid.cellCornersFromGlobal(left.set(eCOM), right);
            graphics.fill(left.x, left.y, right.x, right.y, 0xaa5541E6);
        }*/
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == 256 && this.shouldCloseOnEsc()) {
            if(mainGrid.drawMouse) {
                disableGrid();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void disableGrid() {
        for(GuiEventListener child : children()) {
            if(!(child instanceof AbstractWidget widget)) continue;
            if(widget instanceof GridClicker grid) {
                grid.mouseConsumers.clear();
                grid.drawMouse = false;
                grid.gridColor = GridClicker.GRAY_COLOR;
                continue;
            }
            widget.active = true;
        }
        //partialInterationForScreen.active = true;
    }

    public static class MyGridClicker extends GridClicker {
        public TransformedAxes projectedAxes;
        public Vector3d offset;

        public MyGridClicker(double x, double y, double x1, double y1, Vector2i sizeInBlocks, TransformedAxes projectedAxes, Vector3d offset) {
            super(x, y, x1, y1, sizeInBlocks);
            this.projectedAxes = projectedAxes;
            this.offset = offset;
        }

        @Override
        public MyGridClicker drawMouse(boolean drawMouse) {return (MyGridClicker) super.drawMouse(drawMouse);}

        @Override
        public MyGridClicker gridColor(int gridColor) {return (MyGridClicker) super.gridColor(gridColor);}
    }

}
