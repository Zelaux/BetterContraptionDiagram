package com.zelaux.betterdiagram.mixin;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.zelaux.betterdiagram.extend.BufferSourceAccessors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.*;

import java.util.SequencedMap;

@Mixin(MultiBufferSource.BufferSource.class)
public abstract class BufferSourceMixin implements BufferSourceAccessors {

    @Shadow
    @Final
    protected SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers;

    @Shadow
    public abstract void endBatch();

    @Override
    public void realyEndBatch() {
        //endBatch();
    }
}
