package com.zelaux.betterdiagram.event.client;

import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.util.CenterMassCache;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.VecFormat;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import dev.simulated_team.simulated.mixin.accessor.BlockBehaviourAccessor;
import lombok.ToString;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

@EventBusSubscriber(Dist.CLIENT)
public class COM_Tooltip {


    public static final MutableComponent WRAPPED_OPEN = translatable("better_contraption_diagram.item.tooltip.com.open",
        translatable("key.keyboard.shift").withStyle(ChatFormatting.GRAY)
    )
        .withStyle(ChatFormatting.DARK_GRAY);
    private static final Component HAS_CENTER_MASS = new Component() {
        Component tmp = WRAPPED_OPEN;

        public FormattedCharSequence getVisualOrderText() {return this.tmp.getVisualOrderText();}

        public MutableComponent copy() {return this.tmp.copy();}

        public MutableComponent plainCopy() {return this.tmp.plainCopy();}

        public List<Component> getSiblings() {return this.tmp.getSiblings();}

        public ComponentContents getContents() {return this.tmp.getContents();}

        public Style getStyle() {return this.tmp.getStyle();}

        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj instanceof Component mut) return mut.getSiblings().contains(this);
            return false;
        }
    };
    public static final MutableComponent TRIVIAL_COM =
        translatable("better_contraption_diagram.item.tooltip.com.trivial",
            VecFormat.Presets.trivialCenterOfMass(JOMLConversion.HALF))
            .withStyle(ChatFormatting.DARK_GRAY);

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void itemTooltip(final ItemTooltipEvent event) {

        List<Component> toolTips = event.getToolTip();
        int indexOf = toolTips.indexOf(HAS_CENTER_MASS);
        if(indexOf == -1) return;
        toolTips.set(indexOf, WRAPPED_OPEN);

        var blockItem = (BlockItem) event.getItemStack().getItem();
        Block block = blockItem.getBlock();
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null) {
            toolTips.remove(indexOf);
            return;
        }
        boolean shouldShow = Config.COM_Goggles_tooltip.get().shouldShow(Screen.hasShiftDown());

        CenterMassCache.BlockTo_CenterMass2State map = CenterMassCache.getBlock2Pairs(level).left().orElse(null);
        if(map == null) {
            toolTips.set(indexOf, translatable("better_contraption_diagram.item.tooltip.com.loading").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        var list = map.get(block).entrySet().stream().sorted(Map.Entry.comparingByKey(VecUtil.CMP_AS_ARR)).toList();
        int size = list.size();
        if(size == 1 && CenterMassCalculator.equals(list.get(0).getKey(), JOMLConversion.HALF)) {
            toolTips.set(indexOf, TRIVIAL_COM);
            return;
        }
        if(!shouldShow) return;
        toolTips.set(indexOf++, translatable("better_contraption_diagram.item.tooltip.com.title").withStyle(ChatFormatting.DARK_GRAY));


        var visited = new HashMap<BlockState, SymmetryEntry>();
        var added = new ArrayList<SymmetryEntry>();
        Mirror[] mirrors = Mirror.values();
        Rotation[] rotations = Rotation.values();
        Vector3d tmp = new Vector3d();
        BiFunction<BlockState, SymmetryEntry, SymmetryEntry> putIfAbsent = visited::putIfAbsent;
        BiFunction<BlockState, SymmetryEntry, SymmetryEntry> get = (a, b) -> visited.get(a);
        outer:
        for(int i = 0; i < size; i++) {
            var entry = list.get(i);
            SymmetryEntry symmetryEntry = new SymmetryEntry(entry.getKey());
            for(BlockState blockState : entry.getValue()) {
                for(int mirrorI = 0; mirrorI < mirrors.length; mirrorI++) {
                    Mirror mirror = mirrors[mirrorI];
                    BlockState state = blockState.mirror(mirror);
                    for(int rotationI = 0; rotationI < rotations.length; rotationI++) {
                        Rotation rotation = rotations[rotationI];

                        SymmetryEntry exists = putIfAbsent.apply(state.rotate(rotation), symmetryEntry);
                        if(exists != null) {
                            SymmetryEntry link = exists.link();
                            if(link != symmetryEntry) {

                                VecUtil.applyTransforms(tmp.set(link.com).sub(JOMLConversion.HALF), mirror, rotation);
                                tmp.add(JOMLConversion.HALF);
                                if(CenterMassCalculator.equals(tmp, symmetryEntry.com)) {
                                    symmetryEntry.link = link;
                                    link.addSymmetricPair(new TransformationPair(mirror, rotation));
                                    continue outer;
                                }
                            }
                        }
                    }
                }
            }
            added.add(symmetryEntry);
        }
        for(int i = 0; i < added.size(); i++) {
            SymmetryEntry entry = added.get(i);

            Vector3dc blockCenterOfMass = entry.com;
            toolTips.add(indexOf++, comTooltip(blockCenterOfMass, i == added.size() - 1, entry));
        }
    }

    @ToString
    public static class SymmetryEntry {

        public final ArrayList<TransformationPair> symmetricPairs = new ArrayList<>();
        public Vector3dc com;
        public SymmetryEntry link;
        public int rotationSymmetryMask = 0;

        public SymmetryEntry(Vector3dc com) {
            this.com = com;
        }

        public SymmetryEntry link() {
            return link == null ? this : link;
        }

        public void addSymmetricPair(TransformationPair pair) {
            if(pair.mirror == Mirror.NONE) {
                rotationSymmetryMask |= pair.rotationFlag();
            }
            symmetricPairs.add(pair);

        }
    }

    static record TransformationPair(Mirror mirror, Rotation rotation) {
        public static final int fullRotationFlag = 0b1110;

        public int rotationFlag() {
            return 1 << rotation.ordinal();
        }
    }

    private static @NotNull MutableComponent comTooltip(Vector3dc half, boolean isLast, SymmetryEntry entry) {
        var position = VecFormat.Presets.blockCenterOfMass(half);

        MutableComponent symmetryComponent;
        if(entry.rotationSymmetryMask == TransformationPair.fullRotationFlag) {
            symmetryComponent = translatable("better_contraption_diagram.item.tooltip.com.full-rotation-symmetric");
        } else if(entry.symmetricPairs.size()>0) {
            symmetryComponent = translatable("better_contraption_diagram.item.tooltip.com.amount-of-symmetric", entry.symmetricPairs.size());
        }else{
            symmetryComponent=literal("");
        }
        symmetryComponent=symmetryComponent.withStyle(ChatFormatting.DARK_GRAY);
        return literal("   ")
            .append(
                translatable("better_contraption_diagram.item.tooltip.com.non-trivial", position, symmetryComponent)
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
    }

    public static @Nullable Component getCenterMassComponent(BlockStateExtension blockStateExtension, BlockItem blockItem, boolean showNumbers) {
        Block block = blockItem.getBlock();
        final double mass = ((BlockBehaviourAccessor) block).getHasCollision() ?
            blockStateExtension.sable$getProperty(PhysicsBlockPropertyTypes.MASS.get()) :
            0;
        if(mass == 0) return null;
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null) return null;
        return HAS_CENTER_MASS;
    }
}
