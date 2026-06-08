package com.zelaux.betterdiagram.mixin.fix;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramOpenPacket;
import foundry.veil.api.network.handler.ClientPacketContext;
import lombok.Lombok;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.throwables.*;
import org.spongepowered.asm.mixin.transformer.*;

import java.lang.reflect.Field;
import java.util.Map;

@Mixin(DiagramOpenPacket.class)
public abstract class DiagramOpenPacketMixin {
    @WrapMethod(method = "handle")
    public void handle(ClientPacketContext context, Operation<Void> original){
        try {
            original.call(context);
        } catch(MixinError | MixinException e) {
            try{
                Field field = ClassInfo.class.getDeclaredField("cache");
                field.setAccessible(true);
                Map<String, ClassInfo> map= (Map<String, ClassInfo>) field.get(null);
                map.remove("dev/simulated_team/simulated/content/entities/diagram/screen/DiagramScreen");
                original.call(context);
            }catch(Exception ignore){
                throw Lombok.sneakyThrow(e);
            }
        }
    }



}
