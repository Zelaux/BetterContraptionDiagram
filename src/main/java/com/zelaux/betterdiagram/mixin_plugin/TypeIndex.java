package com.zelaux.betterdiagram.mixin_plugin;


import org.objectweb.asm.Type;

public interface TypeIndex {
     Type SubLevelPhysicsSystem=type("dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem");
     Type CoasterCartTrainLinkConstraint_ActiveLink = type("dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint$ActiveLink");
     Type ServerSubLevel = type("dev.ryanhcode.sable.sublevel.ServerSubLevel");
     Type SubLevel  = type("dev.ryanhcode.sable.sublevel.SubLevel");
    Type GenericConstraintConfiguration=type("dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration".replace('.','/'));
    Type BlockEntitySubLevelActor = type("dev/ryanhcode/sable/api/block/BlockEntitySubLevelActor");
    Type Quaterniond = type("org.joml.Quaterniond");
    Type Object = Type.getType(Object.class);
    Type CompatModule = type("com.zelaux.betterdiagram.compat.CompatModule");
    Type CompatHandles = type("com.zelaux.betterdiagram.compat.CompatHandles");

    static Type type(String typeName) {
        return Type.getObjectType(typeName.replace('.','/'));
    }
}
