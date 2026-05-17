package com.zelaux.betterdiagram.gui;

import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.gui.widget.Label;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.Content;
import com.zelaux.betterdiagram.extend.*;
import com.zelaux.betterdiagram.gui.widget.FloatScrollInput;
import com.zelaux.betterdiagram.gui.widget.GridClicker;
import com.zelaux.betterdiagram.gui.widget.PartialInteration;
import com.zelaux.betterdiagram.index.BCDTextures;
import com.zelaux.betterdiagram.struct.BCDTexture;
import com.zelaux.betterdiagram.struct.TransformedAxes;
import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.FramebufferAttachmentDefinition;
import foundry.veil.api.client.render.framebuffer.FramebufferStack;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;

import static com.zelaux.betterdiagram.gui.widget.PartialInteration.boxes;
import static com.zelaux.betterdiagram.gui.widget.PartialInteration.partialInteration;
import static com.zelaux.betterdiagram.util.UIUtil.*;
import static com.zelaux.betterdiagram.util.VecUtil.*;

public class CenterMassMovingScreen extends AbstractSimiScreen {
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");
    DiagramScreen diagramScreen;
    DiagramStickyNote diagramStickyNote;
    DiagramScreenAccessors diagramScreenAccessors;
    DiagramStickyNoteAccessors noteAccessors;
    ClientData clientData;
    @Setter(AccessLevel.PRIVATE)
    private EditBox editX, editY, editZ;
    private boolean programatic;
    @Getter
    private boolean gridEnabled = false;
    private TransformedAxes mainProjectedAxes, subProjectedAxes;
    private PartialInteration partialInterationForScreen;
    private boolean wasDirty;
    private final ArrayList<Pair<MyGridClicker, PartialInteration>> grids = new ArrayList<>();


    public CenterMassMovingScreen(DiagramScreen diagramScreen) {
        this.diagramScreen = diagramScreen;
        diagramScreenAccessors = CenterMassCalculator.accessors(diagramScreen);

        clientData = new ClientData(((WithClientData) diagramScreenAccessors.betterContraptionDiagram$diagram()));
        expectedCenterOfMass()
        ;
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


        minecraft.screen = Objects.requireNonNull(screen);
        screen.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        minecraft.getNarrator().sayNow(screen.getNarrationMessage());

        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_WORK_CARTOGRAPHER, 1.0f));
    }

    @Override
    protected void init() {
        super.init();
        if(fbo == null || fbo.getWidth() != width || fbo.getHeight() != height) {
            freeFramebuffers();
            fbo = AdvancedFbo.withSize(width, height)
                             .setFormat(FramebufferAttachmentDefinition.Format.RGBA8)
                             .addColorTextureBuffer()
                             .setDepthTextureBuffer()
                             .build(true);
        }
        diagramStickyNote = diagramScreenAccessors.betterContraptionDiagram$note();
        noteAccessors = CenterMassCalculator.accessors(diagramStickyNote);
        LinearLayout mainLayout = LinearLayout.vertical().spacing(5);


        var editX = editBox(0, this::editX);
        var editY = editBox(1, this::editY);
        var editZ = editBox(2, this::editZ);
        BoundingBox3ic bb = diagramScreen.subLevel.getPlot().getBoundingBox();
        updateEdit(minVec3d(bb).negate().add(expectedCenterOfMass()));
        mainLayout.addChild(
            horizontal(5, editX, editY, editZ)
        );

        final var tmp = new Vector3d();
        final var centerButton = makeButton(Component.translatable("better_contraption_diagram.calculator.move_to_struct_center"), () -> {
            Vector3d COM = expectedCenterOfMass();
            COM.set(centerOfSubLevel());
            positionUpdatedNotFromEditBox(tmp.set(COM).sub(bb.minX(), bb.minY(), bb.minZ()));
        }, () -> Component.translatable("better_contraption_diagram.calculator.move_to_struct_center"));
        final var selectBlock = makeButton(Component.translatable("better_contraption_diagram.calculator.select_block"), () -> {
            selectBlock((grid, rawMouse, gridPos, mouseX, mouseY, pointer) -> {
                Vector3d offset = minVec3d(diagramScreen.subLevel.getPlot().getBoundingBox());
                Vector3d COM = expectedCenterOfMass();
                gridToNormalVector(grid, gridPos, tmp.set(COM).sub(offset));
                COM.set(tmp).add(offset);
                positionUpdatedNotFromEditBox(tmp);
            });
        }, () -> Component.translatable("better_contraption_diagram.calculator.select_block"));
        final var resetBlock = makeButton(Component.translatable("better_contraption_diagram.calculator.reset_block"), () -> {
            Vector3d COM = expectedCenterOfMass();
            COM.set(currentCenterOfMass());
            positionUpdatedNotFromEditBox(tmp.set(COM).sub(bb.minX(), bb.minY(), bb.minZ()));
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
        //var bb = diagramScreen.subLevel.getPlot().getBoundingBox();


        mainProjectedAxes = VecUtil.projectAxises(DiagramScreen.LOCAL_ORIENTATION);
        subProjectedAxes = VecUtil.projectAxises(noteAccessors.NOTE_ORIENTATION());

        int diaX = width / 2 - areaWidth / 2;
        int diaY = height / 2 - areaHeight / 2;


        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(BetterContraptionDiagram.MODID, "fbo");
        partialInterationForScreen = (new PartialInteration(
            diagramScreen,
            (object, graphics, mouseX, mouseY, partialTick) -> {
                graphics.flush();
                FramebufferStack.push(location);
                fbo.bind(true);
                //RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
                //fbo.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

                fbo.clear(1.0F, 1.0F, 1.0F, 0.0F, 1.0F, fbo.getClearMask(), fbo.getDrawBuffers());
                //final AdvancedFbo drawFbo = VeilRenderSystem.renderer().getDynamicBufferManger().getDynamicFbo(fbo);
                //drawFbo.bind(true);

                RenderSystem.enableDepthTest();
                RenderSystem.enableColorLogicOp();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                graphics.fill(0, 0, this.width, this.height, -10, 0x4fffffff);
                //graphics.fill(0, 0, this.width/4, this.height, -10, 0xaaffffff);
                //graphics.fill(width/4, 0, this.width/2, this.height, -10, 0xffffffff);
                //graphics.fill( this.width/2, 0, this.width, this.height, 100, 0xaaffffff);


                PoseStack pose = graphics.pose();
                pose.pushPose();
                object.render(graphics, mouseX, mouseY, partialTick);
                pose.popPose();

                graphics.flush();

                FramebufferStack.pop(location);

                if(fbo.isColorTextureAttachment(0)) {
                    int textureId = fbo.getColorTextureAttachment(0).getId();

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                    this.drawFboTexture(graphics, textureId, 0, 0, graphics.guiWidth(), graphics.guiHeight());

                    graphics.flush();
                    //RenderSystem.disableBlend();
                }
            },
            BoundingBox2i.box2d(
                diaX + 228, diaY + 8,
                (diaX + 243 + 8), (diagramY + 8 + 14 + 8)
            ),
            BoundingBox2i.box2d(diagramScreen),
            BoundingBox2i.box2d(diagramStickyNote)
        ));


        grids.clear();

        MyGridClicker myGridClicker = makeGrid(bb, diaX, diaY, mainProjectedAxes, diagramScreenAccessors);
        MyGridClicker subGrid = addSubGrid(bb, diaX, diaY);
        ArrayList<Pair<MyGridClicker, PartialInteration>> g = grids;

        addGrid(myGridClicker, boxes(BoundingBox2i.box2d(this)),
            subGrid == null ? BoundingBox2i.EMPTY_ARRAY : boxes(BoundingBox2i.box2d(subGrid))
        );
        if(subGrid != null) addGrid(subGrid, boxes(BoundingBox2i.box2d(subGrid)), BoundingBox2i.EMPTY_ARRAY);

        mainLayout.visitWidgets(this::addRenderableWidget);

        addWidget(partialInterationForScreen);
        for(var pair : grids) {
            addWidget(pair.second);
        }
    }

    private Vector3d expectedCenterOfMassOffset() {
        return minVec3d(diagramScreen.subLevel.getPlot().getBoundingBox()).negate().add(expectedCenterOfMass());
    }

    private void addGrid(MyGridClicker myGridClicker, BoundingBox2i[] boxes1, BoundingBox2i[] boxes) {
        grids.add(Pair.of(
            myGridClicker,
            addRenderableOnly(
                partialInteration(
                    myGridClicker,
                    boxes1,
                    boxes
                )
            )
        ));
    }

    private void drawFboTexture(GuiGraphics graphics, int id, int x, int y, int width, int height) {
        //if(true)return;
        RenderSystem.setShaderTexture(0, id);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        final Matrix4f matrix4f = graphics.pose().last().pose();
        final BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        final float x1 = x, x2 = x + width;
        final float y1 = y, y2 = y + height;
        bufferbuilder.addVertex(matrix4f, x1, y1, 0.0f).setUv(0.0f, 1.0f).setColor(0xFFFFFFFF);
        bufferbuilder.addVertex(matrix4f, x1, y2, 0.0f).setUv(0.0f, 0.0f).setColor(0xFFFFFFFF);
        bufferbuilder.addVertex(matrix4f, x2, y2, 0.0f).setUv(1.0f, 0.0f).setColor(0xFFFFFFFF);
        bufferbuilder.addVertex(matrix4f, x2, y1, 0.0f).setUv(1.0f, 1.0f).setColor(0xFFFFFFFF);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private AdvancedFbo fbo;

    private void freeFramebuffers() {
        if(fbo != null) {
            fbo.free();
            fbo = null;
        }

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

    private MyGridClicker addSubGrid(BoundingBox3ic bb, int diaX, int diaY) {
        DiagramConfig.NoteConfigs noteConfigs = diagramScreenAccessors.betterContraptionDiagram$config().getNoteConfigs();
        if(!noteConfigs.isActive()) return null;

        var grid = makeGrid(
            new BoundingBox3i(noteConfigs.getNoteScope()),
            noteAccessors.bcd$originX(),
            noteAccessors.bcd$originY(),
            subProjectedAxes,
            noteAccessors
        );
        grid.offset = minVec3d(
            new BoundingBox3i(noteConfigs.getNoteScope())
        ).sub(minVec3d(bb));
        return grid;
    }

    private void gridToNormalVector(GridClicker grid, Vector2i gridPos, Vector3d out) {

        TransformedAxes projectedAxes = grid instanceof MyGridClicker my ? my.projectedAxes : mainProjectedAxes;
        var offset = grid instanceof MyGridClicker my ? my.offset : ZERO_V;
        int xi = projectedAxes.xyzIndex[0];
        int yi = projectedAxes.xyzIndex[1];
        SETTERS_3d[xi].accept(out, gridPos.x + 0.5 + GETTERS_3d[xi].applyAsDouble(offset));
        SETTERS_3d[yi].accept(out, gridPos.y + 0.5 + GETTERS_3d[yi].applyAsDouble(offset));
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
            widget.active = false;
        }
        for(Pair<MyGridClicker, PartialInteration> pair : grids) {
            pair.second.active = true;
            MyGridClicker grid = pair.first;
            grid.mouseConsumers.add(mouseConsumer);
            grid.drawMouse = true;
            //partialInterationForScreen.active = false;
            grid.gridColor = GridClicker.BLACK_COLOR;
        }
        gridEnabled = true;
    }

    private Vector3d centerOfSubLevel() {
        BoundingBox3ic box = diagramScreen.subLevel.getPlot().getBoundingBox();
        return maxVec3d(box).add(1, 1, 1).add(minVec3d(box)).div(2);
    }

    private Vector3d currentCenterOfMass() {
        return CenterMassCalculator
            .centerOfMass(diagramScreen.subLevel);
    }


    public Vector3d expectedCenterOfMass() {
        return CenterMassCalculator
            .expectedCenterOfMass(clientData, diagramScreen.subLevel);
    }

    private AbstractWidget makeButton(MutableComponent title, Runnable onclicl, Supplier<Component> diagramTooltip) {
        return makeButton(title, 64, 24, BCDTextures.Diagram.DIAGRAM_BACKGROUND_64_24, onclicl, diagramTooltip);
    }

    private @NotNull ExtendedButton makeButton(MutableComponent title, int width, int height, BCDTexture texture, Runnable onclicl, Supplier<Component> diagramTooltip) {
        return makeButton0(title, width, height,texture, onclicl, () -> List.of(diagramTooltip.get()));
    }

    private @NotNull ExtendedButton makeButton0(MutableComponent title, int width, int height, BCDTexture texture, Runnable onclicl, Supplier<List<Component>> diagramTooltip) {

        return new ExtendedButton(0, 0, width, height, title, self -> onclicl.run()) {

            public static final Color GRAY = new Color(0xaaaaaaaa);

            @Override
            public void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTicks) {
                Minecraft mc = Minecraft.getInstance();
                //guiGraphics.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());

                Color c;
                if(!active) c = GRAY;
                else if(this.isHovered()) c = DiagramScreen.BUTTON_COLOR;
                else c=Color.WHITE /*c = DiagramScreen.DULL_BUTTON_COLOR*/;

                texture.render(guiGraphics, this.getX() - 1, this.getY() - 1, c);

                final FormattedText buttonText = mc.font.ellipsize(this.getMessage(), this.width - 6); // Remove 6 pixels so that the text is always contained within the button's borders
                guiGraphics.drawCenteredString(mc.font, Language.getInstance().getVisualOrder(buttonText), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, getFGColor());

                //super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);

                if(diagramTooltip != null && this.isHovered()) {
                    //noinspection unchecked,rawtypes
                    final List<FormattedText> lines = (List)diagramTooltip.get();
                    DiagramScreen.renderTooltip(guiGraphics, mouseX, mouseY, lines);
                }
            }
        };
    }

    private void updateEdit(Vector3d expectedCenterOfMass) {
        programatic = true;
        editX.setValue(DECIMAL_FORMAT.format(expectedCenterOfMass.x));
        editY.setValue(DECIMAL_FORMAT.format(expectedCenterOfMass.y));
        editZ.setValue(DECIMAL_FORMAT.format(expectedCenterOfMass.z));
        programatic = false;
    }

    private @NotNull LayoutElement editBox(int axisIndex, Consumer<EditBox> boxConsumer) {
        Content.BCDForceGroup axisGroup = Content.AXIS_GROUPS[axisIndex];
        final var boxName = axisGroup.axisName.copy();
        final var setter = SETTERS_3d[axisIndex];
        final var getter = GETTERS_3d[axisIndex];

        final var editBox = initEditBox(boxName, setter, boxConsumer);

        List<Component> shiftCtrlScale = List.of(
            Component.translatable("better_contraption_diagram.common.shift-scale"),
            Component.translatable("better_contraption_diagram.common.ctrl-scale")
        );
        final var tmp = new Vector3d();
        final var tmp2 = new Vector3d();
        final var bb = diagramScreen.subLevel.getPlot().getBoundingBox();
        final var incBtn = makeButton0(Component.literal("+"), 12, 10, BCDTextures.Diagram.DIAGRAM_BACKGROUND_12_10, () -> {
            var COM = expectedCenterOfMass();
            tmp.set(COM).sub(minVec3d(bb, tmp2));
            setter.accept(tmp, getter.applyAsDouble(tmp) + getOffset());
            COM.set(tmp).add(tmp2);
            positionUpdatedNotFromEditBox(tmp);
        }, () -> shiftCtrlScale);
        final var decBtn = makeButton0(Component.literal("-"), 12, 10, BCDTextures.Diagram.DIAGRAM_BACKGROUND_12_10, () -> {
            var COM = expectedCenterOfMass();
            tmp.set(COM).sub(minVec3d(bb, tmp2));
            setter.accept(tmp, getter.applyAsDouble(tmp) - getOffset());
            COM.set(tmp).add(tmp2);
            positionUpdatedNotFromEditBox(tmp);
        }, () -> shiftCtrlScale);
        final var center = makeButton(Component.literal("C"), 16, 20, BCDTextures.Diagram.DIAGRAM_BACKGROUND_16_20, () -> {
            var COM = expectedCenterOfMass();
            setter.accept(COM, getter.applyAsDouble(centerOfSubLevel()));
            positionUpdatedNotFromEditBox(tmp.set(COM).sub(minVec3d(bb, tmp2)));
        }, () -> Component.translatable("better_contraption_diagram.calculator.move_to_struct_center"));
        final var choose = makeButton(Component.literal("D"), 16, 20, BCDTextures.Diagram.DIAGRAM_BACKGROUND_16_20, () -> {
            selectBlock((grid, rawMouse, gridPos, mouseX, mouseY, pointer) -> {
                gridToNormalVector(grid, gridPos, tmp);
                tmp.add(minVec3d(bb, tmp2));
                Vector3d v = expectedCenterOfMass();
                setter.accept(v, getter.applyAsDouble(tmp));
                positionUpdatedNotFromEditBox(tmp.set(v).sub(tmp2));
            });
        }, () -> Component.translatable("better_contraption_diagram.calculator.select_block"));
        final var reset = makeButton(Component.literal("R"), 16, 20, BCDTextures.Diagram.DIAGRAM_BACKGROUND_16_20, () -> {
            var v = expectedCenterOfMass();
            setter.accept(v, getter.applyAsDouble(currentCenterOfMass()));
            positionUpdatedNotFromEditBox(tmp.set(v).sub(minVec3d(bb, tmp2)));
        }, () -> Component.translatable("better_contraption_diagram.calculator.reset_block"));

        MutableComponent append = boxName.append(":");
        Label label = label(append)
            .colored(axisGroup.color)
            .withShadow();
        label.setHeight(font.lineHeight);

        LayoutElement[] widgets = new LayoutElement[]{
            label,
            editBox,
            //vertical(0, incBtn, decBtn),
            center,
            choose,
            reset
        };
        LinearLayout layout = LinearLayout.horizontal().spacing(0);
        layout.defaultCellSetting().alignVerticallyMiddle();
        for(var widget : widgets) layout.addChild(widget);
        return layout;
    }

    private void positionUpdatedNotFromEditBox(Vector3d v) {
        updateEdit(v);
        positionUpdated();
    }

    private @NotNull LayoutElement initEditBox(MutableComponent boxName, ObjDoubleConsumer<Vector3d> setter, Consumer<EditBox> boxConsumer) {
        FloatScrollInput scrollInput = new FloatScrollInput(0, 0, 44, 20) {
            @Override
            protected void updateTooltip() {
                toolTip.clear();
                toolTip.add(scrollToModify.plainCopy().withStyle( ChatFormatting.WHITE));
                toolTip.add(shiftScrollsFaster.plainCopy().withStyle(ChatFormatting.ITALIC, ChatFormatting.WHITE));
                toolTip.add(shiftScrollsSlower.plainCopy().withStyle(ChatFormatting.ITALIC, ChatFormatting.WHITE));
            }

            @Override
            protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                if(this.isHovered()) {
                    List<Component> tooltip = this.getToolTip();
                    if(tooltip.isEmpty())
                        return;
                    //int ttx = this.lockedTooltipX == -1 ? mouseX : this.lockedTooltipX + this.getX();
                    //int tty = this.lockedTooltipY == -1 ? mouseY : this.lockedTooltipY + this.getY();

                    //Font font = Minecraft.getInstance().font;
                    //noinspection rawtypes,unchecked
                    DiagramScreen.renderTooltip(graphics, mouseX, mouseY, (List) tooltip);
                }
            }
        }
            .calling(v -> {
                if(programatic) return;
                Vector3d offset = minVec3d(diagramScreen.subLevel.getPlot().getBoundingBox());
                Vector3d COM = expectedCenterOfMass();
                setter.accept(COM.sub(offset), v);
                COM.add(offset);
                positionUpdatedNotFromEditBox(offset.negate().add(COM));
                //positionUpdated();
            })
            .withRange(Float.MIN_VALUE, Float.MAX_VALUE)
            .titled(boxName)
            .format(x -> Component.literal(DECIMAL_FORMAT.format(x)))
            .withStepFunction(ctx->ctx.control?0.5f:(ctx.shift?10:1))
            ;
        final var editBox = new EditBox(minecraft.font, scrollInput.getWidth(), scrollInput.getHeight(), boxName) {
            @Override
            public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
                return scrollInput.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
            }
        };
        EditBoxAccessors boxAccessors = EditBoxAccessors.of(editBox);
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
            boxAccessors.bcd$displayPos(0);
            if(programatic) {
                scrollInput.setState(Float.parseFloat(s));
                return;
            }
            try {
                double v = Double.parseDouble(s);
                scrollInput.setState(Float.parseFloat(s));

                Vector3d offset = minVec3d(diagramScreen.subLevel.getPlot().getBoundingBox());
                Vector3d COM = expectedCenterOfMass();
                setter.accept(COM.sub(offset), v);
                COM.add(offset);
                //positionUpdatedNotFromEditBox(offset.negate().add(COM));
                positionUpdated();
                editBox.setTooltip(null);
            } catch(NumberFormatException e) {
                editBox.setTooltip(Tooltip.create(Component.literal(e.getLocalizedMessage()).withColor(ChatFormatting.RED.getColor())));
            }
        });
        boxConsumer.accept(editBox);
        return stackFill(
            editBox,
            scrollInput
        );
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
            //minecraft.scfreen = diagramScreen;
            diagramScreen.onClose();
            return;
        }
        diagramScreen.tick();
    }

    @Override
    public void onClose() {
        //ClientHooks.popGuiLayer(minecraft);
        //this.minecraft.popGuiLayer();
        removed();
        minecraft.screen = diagramScreen;
        freeFramebuffers();
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        diagramScreen.resize(minecraft, width, height);
        super.resize(minecraft, width, height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        partialInterationForScreen.render(graphics, mouseX, mouseY, partialTicks);

        if(wasDirty) {
            this.repositionElements();
            wasDirty = false;
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderMenuBackground(GuiGraphics partialTick) {
        if(gridEnabled) super.renderMenuBackground(partialTick);
    }

    @Override
    protected void renderWindowBackground(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        //RenderSystem.disableDepthTest();
        //graphics.fill(0, 0, this.width/2, this.height, -10, 0x4fffffff);
        //graphics.fill(width/2, height/2, this.width, this.height, 0, 0xaa00ffff);

        //if(gridEnabled)
        //RenderSystem.enableDepthTest();
    }

    @Override
    protected void renderWindow(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == 256 && this.shouldCloseOnEsc()) {
            if(gridEnabled) {
                disableGrid();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * @see CenterMassMovingScreen#enableGrid(GridClicker.MouseConsumer)
     *
     */
    private void disableGrid() {
        for(GuiEventListener child : children()) {
            if(!(child instanceof AbstractWidget widget)) continue;
            widget.active = true;
        }
        for(Pair<MyGridClicker, PartialInteration> pair : grids) {
            //pair.second.active=false;
            MyGridClicker grid = pair.first;
            grid.mouseConsumers.clear();
            grid.drawMouse = false;
            grid.gridColor = GridClicker.GRAY_COLOR;
        }
        gridEnabled = false;
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
