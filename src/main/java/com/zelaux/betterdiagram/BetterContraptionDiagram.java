package com.zelaux.betterdiagram;

import com.mojang.logging.LogUtils;
import com.zelaux.betterdiagram.annotations.JavaClassName;
import com.zelaux.betterdiagram.annotations.JavaClassOrInternalName;
import com.zelaux.betterdiagram.command.server.BCDCommand;
import com.zelaux.betterdiagram.compat.gadgetsandgizmos.AileronBearingActor;
import com.zelaux.betterdiagram.compat.gadgetsandgizmos.ClawActor;
import com.zelaux.betterdiagram.compat.gadgetsandgizmos.PhysicsGantryShaftActor;
import com.zelaux.betterdiagram.compat.gadgetsandgizmos.VectorBearingActor;
import com.zelaux.betterdiagram.feature.balloon_volume_calculator.BalloonIndexUpdate;
import com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler.*;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.network.BCDPacketManager;
import com.zelaux.betterdiagram.network.HelloMessage;
import com.zelaux.betterdiagram.network.features.ModFeaturesPacket;
import com.zelaux.betterdiagram.network.rope.RopeDataPacket;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.rope_connector.RopeConnectorBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchBlockEntity;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.link_block.SwivelBearingPlateBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import java.util.function.Function;

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
        modEventBus.addListener(this::afterAllMods);
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

    void afterAllMods(FMLLoadCompleteEvent event) {

        Function<BlockEntitySubLevelActor, ActorHandler> actor = FixedActor::new;
        String[] classes = {
            tname("dev/qwxon/compactflap/content/blocks/compact_swivel_flap/CompactSwivelFlapBlockEntity"),
            tname("dev/qwxon/compactflap/content/blocks/analog_swivel_bearing/AnalogSwivelBearingBlockEntity"),

            //gadgets and gizmos
            //tname("com/rieno/gadgetsandgizmos/content/VectorBearingBlockEntity"),//!
            tname("com/rieno/gadgetsandgizmos/content/VectorBearingLinkBlockEntity"),

            tname("com/rieno/gadgetsandgizmos/content/ScissorPistonBlockEntity"),
            tname("com/rieno/gadgetsandgizmos/content/ScissorPistonLinkBlockEntity"),

            //tname("com/rieno/gadgetsandgizmos/content/AileronBearingBlockEntity"),//!
            tname("com/rieno/gadgetsandgizmos/content/AileronBearingLinkBlockEntity"),

            tname("com/rieno/gadgetsandgizmos/content/PhysicsGantryCarriageBlockEntity"),
            //tname("com/rieno/gadgetsandgizmos/content/PhysicsGantryShaftBlockEntity"),

            tname("com/rieno/gadgetsandgizmos/content/ThrusterBearingBlockEntity"),
            tname("com/rieno/gadgetsandgizmos/content/ThrusterBearingLinkBlockEntity"),


        };
        for(@JavaClassName String typeName : classes) ActorHandler.registerOptional(typeName, actor);
        ActorHandler.registerOptional(tname("com/rieno/gadgetsandgizmos/content/VectorBearingBlockEntity"), VectorBearingActor::new);
        ActorHandler.registerOptional(tname("com/rieno/gadgetsandgizmos/content/AileronBearingBlockEntity"), AileronBearingActor::new);
        ActorHandler.registerOptional(tname("com/rieno/gadgetsandgizmos/content/PhysicsGantryShaftBlockEntity"), PhysicsGantryShaftActor::new);
        ActorHandler.registerOptional(tname("com/rieno/gadgetsandgizmos/content/ClawBlockEntity"), ClawActor::new);

    }

    @JavaClassName
    private String tname(@JavaClassOrInternalName String name) {
        return name.replace('/', '.');
    }

    public static ResourceLocation resource(String location) {
        return ResourceLocation.fromNamespaceAndPath(MODID, location);
    }

    public static ResourceLocation path(String location) {
        return resource(location);
    }

    @SubscribeEvent
    void syncHello(OnDatapackSyncEvent event) {
        if(event.getPlayer() == null) return;
        HelloMessage.INSTANCE.trySend(event.getPlayer());
        ModFeaturesPacket.SERVER_TO_CLIENT.trySend(event.getPlayer());
    }

    @SubscribeEvent
    void tick(ServerTickEvent.Pre event) {
        RopeDataPacket.tick();
        BalloonIndexUpdate.serverTick();
    }


    @SubscribeEvent
    private void registerResqueCommand(RegisterCommandsEvent event) {
        BCDCommand.register(event.getDispatcher(), event.getBuildContext());
    }
}
