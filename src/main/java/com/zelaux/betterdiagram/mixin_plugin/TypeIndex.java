package com.zelaux.betterdiagram.mixin_plugin;


import com.zelaux.betterdiagram.compat.CompatHandles;
import com.zelaux.betterdiagram.compat.CompatModule;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import org.joml.Quaterniond;
import org.objectweb.asm.Type;

import static com.zelaux.betterdiagram.annotations.CompileTool.bakeType;

public interface TypeIndex {
    Type CoasterCartTrainLinkConstraint_ActiveLink =bakeType(CoasterCartTrainLinkConstraint.ActiveLink.class);

    Type SubLevelPhysicsSystem = bakeType(SubLevelPhysicsSystem.class);
    Type ServerSubLevel = bakeType(ServerSubLevel.class);
    Type SubLevel = bakeType(SubLevel.class);
    Type GenericConstraintConfiguration = bakeType(GenericConstraintConfiguration.class);
    Type BlockEntitySubLevelActor = bakeType(BlockEntitySubLevelActor.class);
    Type Quaterniond = bakeType(Quaterniond.class);
    Type Object = bakeType(Object.class);
    Type CompatModule = bakeType(CompatModule.class);
    Type CompatHandles = bakeType(CompatHandles.class);

    static Type bakedType(Class<?> typeName) {
        return Type.getType(typeName);
    }
    static Type type(String typeName) {
        return Type.getObjectType(typeName.replace('.', '/'));
    }
}
