package com.zelaux.betterdiagram.mixin_plugin;

import com.llamalad7.mixinextras.utils.MixinInternals;
import com.zelaux.betterdiagram.compat.CompatIndex;
import com.zelaux.betterdiagram.mixin_plugin.postprocessors.CoasterProcessorProvider;
import com.zelaux.betterdiagram.mixin_plugin.postprocessors.GadgetsProcessorProvider;
import com.zelaux.betterdiagram.mixin_plugin.system.BCDPostprocessors;
import com.zelaux.betterdiagram.mixin_plugin.system.ProcessorProvider;
import com.zelaux.betterdiagram.mixin_plugin.util.WithPostMixinTransformer;
import foundry.veil.Veil;
import foundry.veil.platform.VeilPlatform;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.*;

import java.util.List;
import java.util.Set;

public class BetterContraptionDiagramCompatMixinPlugin implements IMixinConfigPlugin {
    private final FilterEntry[] filters = {
        filter(CompatIndex.gadgets, "gadgets", GadgetsProcessorProvider.INSTANCE),
        filter(CompatIndex.absoluteKinematics, "absolute_kinematics", null),
        filter(CompatIndex.compactflap, "compact_flap", null),
        filter(CompatIndex.coasters, "simulatedcoasters", CoasterProcessorProvider.INSTANCE),
    };
    private String compatPackage;

    static FilterEntry filter(String modId, String packagePath, ProcessorProvider processorProvider) {
        return new FilterEntry(modId, packagePath, processorProvider);
    }

    @Override
    public void onLoad(final String mixinPackage) {
        BCDExtension extension = new BCDExtension();
        MixinInternals.registerExtension(extension);
        //MixinInternals.getExtensions().add((IClassGenerator)extension);


        compatPackage = mixinPackage + ".compat";
        VeilPlatform platform = Veil.platform();
        for(FilterEntry filter : filters) {
            filter.enabled = platform.isModLoaded(filter.modId);
            filter.packagePath = compatPackage +"."+ filter.packagePath;
            if(filter.enabled && filter.processorProvider != null) {
                BCDPostprocessors.postprocessor(filter.processorProvider);
            }
        }
        WithPostMixinTransformer.register(extension,BCDPostprocessors::hasAnyPostprocessorFor);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        for(FilterEntry filter : filters) {
            if(mixinClassName.startsWith(filter.packagePath)) {
                return filter.enabled;
            }
        }
        if(mixinClassName.startsWith(compatPackage)) {
            System.err.println("Hmmm");
            return false;
        }
        return true;
    }

    static final class FilterEntry {
        public final String modId;
        public String packagePath;
        public boolean enabled;
        public final ProcessorProvider processorProvider;

        public FilterEntry(String modId, String packagePath, ProcessorProvider processorProvider) {
            this.modId = modId;
            this.packagePath = packagePath;
            this.processorProvider = processorProvider;
        }
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
        BCDPostprocessors.all().forEach(postprocessor -> {
            postprocessor.preMixin(targetClassName,targetClass,mixinClassName,mixinInfo);
        });
    }

    @Override
    public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
        BCDPostprocessors.all().forEach(postprocessor -> {
            postprocessor.postMixin(targetClassName,targetClass,mixinClassName,mixinInfo);
        });
        //if(gadgetsLoaded && mixinClassName.startsWith(gadgetsPackage))
        //GadgetsPostprocessor.apply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }

}
