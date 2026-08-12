package com.zelaux.betterdiagram.compat;

import com.zelaux.betterdiagram.annotations.CompileTool;
import com.zelaux.betterdiagram.annotations.JavaClassName;
import com.zelaux.betterdiagram.annotations.JavaClassOrInternalName;
import com.zelaux.betterdiagram.compat.absolute_kinematics.AbsoluteKinematicsModule;
import com.zelaux.betterdiagram.compat.compactflap.CompactFlapModule;
import com.zelaux.betterdiagram.compat.gadgetsandgizmos.GadgetsModule;
import com.zelaux.betterdiagram.compat.simulatedcoasters.CoastersModule;
import foundry.veil.Veil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;

public abstract class CompatModule {
    private static boolean loaded = false;
    private final static ObjectArrayList<CompatModule> modules = new ObjectArrayList<>();

    public static void loadAll() {
        if(loaded) return;
        loaded = true;
        tryLoad(CompileTool.classToString(AbsoluteKinematicsModule.class), CompatIndex.absoluteKinematics);
        tryLoad(CompileTool.classToString(CompactFlapModule.class), CompatIndex.compactflap);
        tryLoad(CompileTool.classToString(GadgetsModule.class), CompatIndex.gadgets);
        tryLoad(CompileTool.classToString(CoastersModule.class), CompatIndex.coasters);
    }

    @JavaClassName
    protected String tname(@JavaClassOrInternalName String name) {
        return name.replace('/', '.');
    }


    @SneakyThrows
    static void tryLoad(String typeName, String modId) {
        if(!Veil.platform().isModLoaded(modId)) return;
        Class<?> name = Class.forName(typeName);
        var module = (CompatModule) name.getConstructor().newInstance();
        module.load();
        CompatModule.modules.add(module);
    }

    protected abstract void load();
}
