package com.zelaux.betterdiagram;

import com.mojang.logging.LogUtils;
import com.zelaux.betterdiagram.command.ServerHelpers;
import com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler.RopeActor;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler.DockingConnectorActor;
import com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler.SwivelBearingActor;
import com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler.SwivelBearingPlateActor;
import com.zelaux.betterdiagram.network.BCDPacketManager;
import com.zelaux.betterdiagram.network.HelloMessage;
import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.rope_connector.RopeConnectorBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchBlockEntity;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.link_block.SwivelBearingPlateBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(BetterContraptionDiagram.MODID)
public class BetterContraptionDiagram {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "better_contraption_diagram";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public BetterContraptionDiagram(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        //modEventBus.addListener(RegisterCommandsEvent.class,this::registerResqueCommand);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        BCDPacketManager.init();


        ActorHandler.register(SwivelBearingBlockEntity.class, SwivelBearingActor::new);
        ActorHandler.register(SwivelBearingPlateBlockEntity.class, SwivelBearingPlateActor::new);
        ActorHandler.register(DockingConnectorBlockEntity.class, DockingConnectorActor::new);
        ActorHandler.register(RopeWinchBlockEntity.class, RopeActor::new);
        ActorHandler.register(RopeConnectorBlockEntity.class, RopeActor::new);

        // Register the item to a creative tab
        //modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the data file for us
        //modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static ResourceLocation resource(String location) {
        return ResourceLocation.fromNamespaceAndPath(MODID, location);
    }
    @SubscribeEvent
    void syncHello(OnDatapackSyncEvent event){
        if(event.getPlayer() == null) return;
        HelloMessage.INSTANCE.trySend(event.getPlayer());
    }


    @SubscribeEvent
    private void registerResqueCommand(RegisterCommandsEvent event) {
        ServerHelpers.register(event.getDispatcher(),event.getBuildContext());
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            //event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
