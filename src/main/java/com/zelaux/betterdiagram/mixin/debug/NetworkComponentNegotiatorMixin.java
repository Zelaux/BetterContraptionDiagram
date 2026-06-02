package com.zelaux.betterdiagram.mixin.debug;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import net.neoforged.neoforge.network.negotiation.NegotiableNetworkComponent;
import net.neoforged.neoforge.network.negotiation.NetworkComponentNegotiator;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

import java.util.List;
import java.util.function.Predicate;

import static com.zelaux.betterdiagram.debug.ServerTestWithoutTwoInstances.kind;

@SuppressWarnings("UnstableApiUsage")
@Mixin(NetworkComponentNegotiator.class)
public class NetworkComponentNegotiatorMixin {
    @WrapOperation(
        method = "negotiate",
        at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/neoforged/neoforge/network/negotiation/NetworkComponentNegotiator;buildDisabledOptionalComponents(Ljava/util/List;Ljava/util/List;)Ljava/util/List;")
    )
    private static List<NegotiableNetworkComponent> initializeNeoForgeConnection(List<NegotiableNetworkComponent> client,
                                                                                 List<NegotiableNetworkComponent> server,
                                                                                 Operation<List<NegotiableNetworkComponent>> original
    ) {
        if(kind.both()) return original.call(client, server);
        Predicate<NegotiableNetworkComponent> filter = x -> x.id().getNamespace().equals(BetterContraptionDiagram.MODID);
        if(!kind.hasClient) client.removeIf(filter);
        if(!kind.hasServer) server.removeIf(filter);
        return original.call(client, server);
    }


}
