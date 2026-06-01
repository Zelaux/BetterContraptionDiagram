package com.zelaux.betterdiagram;

import com.mojang.datafixers.util.Either;
import com.zelaux.betterdiagram.command.BCDCommand;
import com.zelaux.betterdiagram.gui.OffCenteredBlockTooltipHandler;
import com.zelaux.betterdiagram.gui.comp.WrappedTooltipComponent;
import com.zelaux.betterdiagram.gui.screen.COM.COMScreen;
import com.zelaux.betterdiagram.leveldata.DiagramEntityData;
import com.zelaux.betterdiagram.leveldata.LevelData;
import com.zelaux.betterdiagram.leveldata.LevelDatas;
import com.zelaux.betterdiagram.util.CenterMassCache;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = BetterContraptionDiagram.MODID, value = Dist.CLIENT)
public class ClientEvents {
    public static float clientTime(){
        return AnimationTickHolder.getTicks(true) + AnimationTickHolder.getPartialTicks();
    }
    @SubscribeEvent
    public static void onTickPre(ClientTickEvent.Pre event) {
        OffCenteredBlockTooltipHandler.tick();
        LevelData data = LevelDatas.levelData();
        if(data!=null)data.currentTick();

    }
    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        Level level = event.getLevel();
        if(!level.isClientSide) return;
        var entity = event.getEntity();
        if (entity instanceof DiagramEntity diagramEntity) {
            var reason = diagramEntity.getRemovalReason();

            if (reason == Entity.RemovalReason.KILLED) {
                DiagramEntityData.get(level).saveData(diagramEntity.getUUID(),null);
            }
        }
    }

    @SubscribeEvent
    private static void placeRendarableTooltip(RenderTooltipEvent.GatherComponents event) {
        var list = event.getTooltipElements();
        for(int i = 0; i < list.size(); i++) {
            var left = list.get(i).left();
            if(!(left.orElse(null) instanceof WrappedTooltipComponent wrapped)) continue;
            list.set(i, Either.right(wrapped));

        }
    }

    @SubscribeEvent
    public static void listenForItemDrop(ItemTossEvent event) {
        Player player = event.getPlayer();
        Minecraft instance = Minecraft.getInstance();
        if(player != instance.player) return;
        if(instance.screen instanceof COMScreen comScreen) {
            comScreen.handleItemDrop(event);
        }
    }

    @SubscribeEvent
    private static void registerCommand(final RegisterClientCommandsEvent event) {
        BCDCommand.register(event.getDispatcher(), event.getBuildContext());
    }
    @SubscribeEvent
    static void onLevelLoad(LevelEvent.Load load){
        if(load.getLevel() instanceof  ClientLevel) {
            CenterMassCache.resetCache(true);

        }
    }
    @SubscribeEvent
    static void onLevelUnload(LevelEvent.Unload load){
        if(load.getLevel() instanceof  ClientLevel level) {
            CenterMassCache.resetCache(false);
            DiagramEntityData.get(level).trySave();
        }
    }

    @SubscribeEvent
    static void onWorldLeave(ClientPlayerNetworkEvent.LoggingOut event){
        LevelDatas.disconnect();
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        //BetterContraptionDiagram.LOGGER.info("HELLO FROM CLIENT SETUP");
        //BetterContraptionDiagram.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
