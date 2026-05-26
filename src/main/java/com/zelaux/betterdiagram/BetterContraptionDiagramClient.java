package com.zelaux.betterdiagram;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllKeys;
import com.zelaux.betterdiagram.command.BCDCommand;
import com.zelaux.betterdiagram.gui.comp.WrappedTooltipComponent;
import com.zelaux.betterdiagram.gui.screen.COM.COMScreen;
import com.zelaux.betterdiagram.util.VecFormat;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.client.BlockPropertiesTooltip;
import dev.simulated_team.simulated.mixin.accessor.BlockBehaviourAccessor;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import mezz.jei.neoforge.events.PermanentEventSubscriptions;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = BetterContraptionDiagram.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = BetterContraptionDiagram.MODID, value = Dist.CLIENT)
public class BetterContraptionDiagramClient {
    public static final Logger LOGGER = LogUtils.getLogger();
    private final PermanentEventSubscriptions subscriptions;

    public BetterContraptionDiagramClient(ModContainer container) {
        PermanentEventSubscriptions subscriptions = this.subscriptions = new PermanentEventSubscriptions(NeoForge.EVENT_BUS, container.getEventBus());
        ;
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        final SimulatedRegistrate registrate = Simulated.getRegistrate();
        int priority = 7;

        BlockPropertiesTooltip.register(registrate, "center_mass", BetterContraptionDiagramClient::getCenterMassComponent, priority++);


        final IEventBus neoBus = NeoForge.EVENT_BUS;
        subscriptions.register(RegisterClientTooltipComponentFactoriesEvent.class, this::registerType);
        neoBus.addListener(this::registerCommand);
        neoBus.addListener(this::listenForItemDrop);
        neoBus.addListener(this::placeRendarableTooltip);
    }

    private void registerType(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(WrappedTooltipComponent.class, it -> it.component);
    }

    private void placeRendarableTooltip(RenderTooltipEvent.GatherComponents event) {
        var list = event.getTooltipElements();
        for(int i = 0; i < list.size(); i++) {
            var left = list.get(i).left();
            if(!(left.orElse(null) instanceof WrappedTooltipComponent wrapped)) continue;
            list.set(i, Either.right(wrapped));

        }
    }

    public void listenForItemDrop(ItemTossEvent event) {
        Player player = event.getPlayer();
        Minecraft instance = Minecraft.getInstance();
        if(player != instance.player) return;
        if(instance.screen instanceof COMScreen comScreen) {
            comScreen.handleItemDrop(event);
        }
    }

    private void registerCommand(final RegisterClientCommandsEvent event) {
        BCDCommand.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void itemTooltip(final ItemTooltipEvent event) {
        if(!AllKeys.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) return;
        List<Component> toolTips = event.getToolTip();
        int indexOf = toolTips.indexOf(HAS_CENTER_MASS);
        if(indexOf == -1) return;

        var blockItem = (BlockItem) event.getItemStack().getItem();
        Block block = blockItem.getBlock();
        StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
        HashSet<Vector3dc> set = new HashSet<>();
        var level = Minecraft.getInstance().level;
        var blockGetter = level != null ? level : EmptyBlockGetter.INSTANCE;

        boolean wasHalf = false;
        int color = 0xFF_aaaaaa;
        for(BlockState possibleState : stateDefinition.getPossibleStates()) {
            Vector3dc blockCenterOfMass = MassTracker.BLOCK_CENTER_OF_MASS.apply(blockGetter, possibleState);
            if(!set.add(blockCenterOfMass)) continue;
            if(set.size() == 1) {
                if(blockCenterOfMass.equals(JOMLConversion.HALF)) {
                    wasHalf = true;
                    continue;
                } else {
                    toolTips.remove(indexOf--);
                }
            }
            if(set.size() == 2 && wasHalf) {
                Vector3dc half = JOMLConversion.HALF;
                toolTips.set(indexOf, comTooltip(half));
            }
            toolTips.add(++indexOf, comTooltip(blockCenterOfMass));
        }
        if(set.size() == 1 && wasHalf) {
            toolTips.remove(indexOf);
        }
    }

    private static @NotNull MutableComponent comTooltip(Vector3dc half) {
        var position = VecFormat.Presets.blockCenterOfMass(half);

        return Component.literal(" ").append(Component.translatable("better_contraption_diagram.item.tooltip", position));
    }

    private static final Component HAS_CENTER_MASS = new Component() {
        public FormattedCharSequence getVisualOrderText() {return this.tmp.getVisualOrderText();}

        public MutableComponent copy() {return this.tmp.copy();}

        public MutableComponent plainCopy() {return this.tmp.plainCopy();}

        public List<Component> getSiblings() {return this.tmp.getSiblings();}

        public ComponentContents getContents() {return this.tmp.getContents();}

        public Style getStyle() {return this.tmp.getStyle();}

        Component tmp = Component.translatable("better_contraption_diagram.item-open.tooltip");

        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj instanceof Component mut) return mut.getSiblings().contains(this);
            return false;
        }
    };


    private static @Nullable Component getCenterMassComponent(BlockStateExtension blockStateExtension, BlockItem blockItem, boolean showNumbers) {
        final double mass = ((BlockBehaviourAccessor) blockItem.getBlock()).getHasCollision() ?
            blockStateExtension.sable$getProperty(PhysicsBlockPropertyTypes.MASS.get()) :
            0;
        if(mass == 0) return null;
        return HAS_CENTER_MASS;
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        BetterContraptionDiagram.LOGGER.info("HELLO FROM CLIENT SETUP");
        BetterContraptionDiagram.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
