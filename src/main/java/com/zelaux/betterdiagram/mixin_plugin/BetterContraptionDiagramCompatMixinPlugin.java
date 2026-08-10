package com.zelaux.betterdiagram.mixin_plugin;

import com.llamalad7.mixinextras.utils.MixinInternals;
import com.zelaux.betterdiagram.mixin_plugin.postprocessors.GadgetsPostprocessor;
import com.zelaux.betterdiagram.mixin_plugin.postprocessors.VanilaPostprocessor;
import foundry.veil.Veil;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.*;

import java.util.List;
import java.util.Set;

public class BetterContraptionDiagramCompatMixinPlugin implements IMixinConfigPlugin {
    private boolean gadgetsLoaded;
    private String gadgetsPackage;

    @Override
    public void onLoad(final String mixinPackage) {
        this.gadgetsLoaded = Veil.platform().isModLoaded("createthrusters");
        this.gadgetsPackage = mixinPackage + ".compat.gadgets";
        MixinInternals.registerExtension(new BCDIExtension());
        if(gadgetsLoaded){
            BCDPostprocessors.postprocessor(GadgetsPostprocessor.INSTANCE);
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        if(mixinClassName.startsWith(gadgetsPackage)) {
            return gadgetsLoaded;
        }

        return true;
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
        //if(gadgetsLoaded && mixinClassName.startsWith(gadgetsPackage))
            //GadgetsPostprocessor.apply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }

}
