package com.zelaux.betterdiagram.util.async;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.datafixers.util.Either;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LevelCache<Value> {
    public final AsyncComputeHolder<Value,? super Object> holder=new AsyncComputeHolder<>();
    public final Computer<Value> computer;

    public LevelCache(Computer<Value> computer) {this.computer = computer;}

    public Either<Value, CompletableFuture<Value>> get(HolderLookup.RegistryLookup<Block> blocks, Level level){
        return holder.getAsync(
            x -> computer.compute(blocks, level, x),
            Util.backgroundExecutor()
        ).mapRight(AsyncComputeHolder.Pair::future);
    }

    public void put(Value it) {
        holder.put(it);
    }

    public Either<Value, CompletableFuture<Value>> getClient() {
        Minecraft instance = Minecraft.getInstance();
        Level level = instance.player.level();
        return get(level.registryAccess().lookup(Registries.BLOCK).orElse(null),level);
    }

    public interface Computer<Value>{
        Value compute(HolderLookup.RegistryLookup<Block> blocks, Level level, ConcurrentLinkedQueue list);
    }
}
