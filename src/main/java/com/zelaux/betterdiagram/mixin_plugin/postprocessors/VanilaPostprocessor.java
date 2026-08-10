package com.zelaux.betterdiagram.mixin_plugin.postprocessors;

import com.zelaux.betterdiagram.mixin_plugin.BCDPostprocessors;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.regex.Pattern;

public enum VanilaPostprocessor implements BCDPostprocessors.Postprocessor {
    INSTANCE;
    private final ClassNode DUMMY = new ClassNode();
    public Type RopeStrandHolderBlockEntity = Type.getObjectType("dev/simulated_team/simulated/content/blocks/rope/RopeStrandHolderBlockEntity");
    public Type IRopeStrandHolderBlockEntity  = Type.getObjectType("com/zelaux/betterdiagram/extend/IRopeStrandHolderBlockEntity");

    //public static final Method generatedMethod = new Method("bcd$hasActor", BlockEntitySubLevelActor, new Type[0]);
    public static final Method generatedMethod = new Method("bcd$sable$getConnectionDependencies", Type.getType(Iterable.class), new Type[0]);
    //Iterable<@NotNull SubLevel> sable$getConnectionDependencies()
    public static final Method searchMethod = new Method("sable$getConnectionDependencies", Type.getType(Iterable.class), new Type[0]);

    @Override
    public Pattern filter() {
        return BCDPostprocessors.Postprocessor.orFilter(
            RopeStrandHolderBlockEntity.getInternalName()
        );
    }

    @Override
    public boolean process(ClassNode targetClass) {
        MethodNode found = GadgetsPostprocessor.findMethod(targetClass, generatedMethod, searchMethod);
        if(found == null) return false;
        MethodNode clone = cloneMethod(found);
        clone.name = generatedMethod.getName();
        targetClass.methods.add(clone);
        targetClass.interfaces.add(IRopeStrandHolderBlockEntity.getInternalName());
        targetClass.signature+=IRopeStrandHolderBlockEntity.getDescriptor();
        return true;
    }

    private MethodNode cloneMethod(MethodNode found) {
        DUMMY.methods.clear();
        found.accept(DUMMY);
        return DUMMY.methods.removeFirst();
    }
}
