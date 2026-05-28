package com.zelaux.betterdiagram.event.client;

import com.zelaux.betterdiagram.util.CenterMassCache;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.VecFormat;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import dev.simulated_team.simulated.mixin.accessor.BlockBehaviourAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

import java.util.HashSet;
import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
public class COM_Tooltip {


    private static final Component HAS_CENTER_MASS = new Component() {
        Component tmp = Component.translatable("better_contraption_diagram.item-open.tooltip");

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

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void itemTooltip(final ItemTooltipEvent event) {
        if(!Screen.hasShiftDown()) return;
        List<Component> toolTips = event.getToolTip();
        int indexOf = toolTips.indexOf(HAS_CENTER_MASS);
        if(indexOf == -1) return;

        var blockItem = (BlockItem) event.getItemStack().getItem();
        Block block = blockItem.getBlock();
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null) {
            toolTips.remove(indexOf);
            return;
        }
        toolTips.remove(indexOf);
        for(Vector3dc blockCenterOfMass : CenterMassCache.getBlock2Pairs(level).get(block).keySet().stream().sorted(VecUtil.CMP_AS_ARR).toList()) {
            toolTips.add(indexOf++, comTooltip(blockCenterOfMass));
        }
    }

    private static @NotNull MutableComponent comTooltip(Vector3dc half) {
        var position = VecFormat.Presets.blockCenterOfMass(half);

        return Component.literal(" ").append(Component.translatable("better_contraption_diagram.item.tooltip", position));
    }

    public static @Nullable Component getCenterMassComponent(BlockStateExtension blockStateExtension, BlockItem blockItem, boolean showNumbers) {
        Block block = blockItem.getBlock();
        final double mass = ((BlockBehaviourAccessor) block).getHasCollision() ?
            blockStateExtension.sable$getProperty(PhysicsBlockPropertyTypes.MASS.get()) :
            0;
        if(mass == 0) return null;
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null) return null;
        var pairs = CenterMassCache.getBlock2Pairs(level).get(block);
        return pairs.isEmpty() || pairs.size() == 1 && CenterMassCalculator.equals(pairs.keySet().iterator().next(), JOMLConversion.HALF) ?
            null :
            HAS_CENTER_MASS;
    }
}
