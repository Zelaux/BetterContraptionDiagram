package com.zelaux.betterdiagram;

import com.mojang.logging.LogUtils;
import com.zelaux.betterdiagram.event.client.COM_Tooltip;
import com.zelaux.betterdiagram.gui.comp.WrappedTooltipComponent;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.client.BlockPropertiesTooltip;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = BetterContraptionDiagram.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = BetterContraptionDiagram.MODID, value = Dist.CLIENT)
public class BetterContraptionDiagramClient {
    public static final Logger LOGGER = LogUtils.getLogger();

    public BetterContraptionDiagramClient(ModContainer container) {
        ;
        container.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        final SimulatedRegistrate registrate = Simulated.getRegistrate();
        int priority = 7;

        BlockPropertiesTooltip.register(registrate, "center_mass", COM_Tooltip::getCenterMassComponent, priority++);


        final IEventBus neoBus = NeoForge.EVENT_BUS;
        //subscriptions.register(RegisterClientTooltipComponentFactoriesEvent.class, this::registerType);
    }
@SubscribeEvent
    private static void registerType(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(WrappedTooltipComponent.class, it -> it.component);
    }

}
