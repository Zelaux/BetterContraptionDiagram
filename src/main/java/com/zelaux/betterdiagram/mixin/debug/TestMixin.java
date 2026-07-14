package com.zelaux.betterdiagram.mixin.debug;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.zelaux.betterdiagram.DebugRuntime;
import com.zelaux.betterdiagram.annotations.DeleteInRelease;
import com.zelaux.betterdiagram.annotations.EmptyInRelease;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Collection;
import java.util.Scanner;

@Mixin(AbstractRegistrate.class)
@EmptyInRelease
public abstract class TestMixin {
    @Shadow
    public static boolean isDevEnvironment(){return false;}

    @WrapOperation(method = "accept",at= @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;removeAll(Ljava/lang/Object;)Ljava/util/Collection;"))
    Collection onRemoveAll(Multimap instance, Object o, Operation<Collection> original){
        // DebugRuntime.removeAll(this+" "+o.toString());
        return original.call(instance, o);
    }
    @WrapOperation(method = "addRegisterCallback(Ljava/lang/String;Lnet/minecraft/resources/ResourceKey;Lcom/tterrag/registrate/util/nullness/NonNullConsumer;)Lcom/tterrag/registrate/AbstractRegistrate;",
        at= @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;put(Ljava/lang/Object;Ljava/lang/Object;)Z"))
    boolean onAdd(Multimap instance, Object k, Object v, Operation<Boolean> original){
        // DebugRuntime.add( this+" "+k.toString());
        return original.call(instance, k, v);
    }
    @WrapOperation(method = "onRegister",at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;isEmpty()Z",ordinal = 0))
    protected boolean onRegister(Multimap<Pair<String, ResourceKey<? extends Registry<?>>>, NonNullConsumer<?>> instance, Operation<Boolean> original){
        Boolean call = original.call(instance);
        if(!call && isDevEnvironment()){
             System.out.println("Press enter");
             new Scanner(System.in).nextLine();
             int x=0;
             x++;
        }
        return call;
    }

}
