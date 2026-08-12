package com.zelaux.betterdiagram.mixin_plugin.util;

import lombok.experimental.Delegate;
import org.spongepowered.asm.mixin.transformer.*;
import org.spongepowered.asm.transformers.TreeTransformer;

public class DelegateTransformer extends TreeTransformer implements IMixinTransformer {
    @Delegate
    public IMixinTransformer delegate;

    public DelegateTransformer(IMixinTransformer transformer) {
        this.delegate = transformer;
    }

    @Override
    public String getName() {
        return "Delegate: " + (delegate instanceof TreeTransformer another ? another.getName() : delegate);
    }

    @Override
    public boolean isDelegationExcluded() {
        return true;
    }
}
