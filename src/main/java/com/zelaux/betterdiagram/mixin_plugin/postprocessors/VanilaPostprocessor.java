package com.zelaux.betterdiagram.mixin_plugin.postprocessors;

import com.zelaux.betterdiagram.annotations.CompileTool;
import com.zelaux.betterdiagram.extend.IRopeStrandHolderBlockEntity;
import com.zelaux.betterdiagram.mixin_plugin.BCDPostprocessors;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBlockEntity;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public enum VanilaPostprocessor implements BCDPostprocessors.Postprocessor {
    INSTANCE;
    private final ClassNode DUMMY = new ClassNode();
    public Type RopeStrandHolderBlockEntity = CompileTool.bakeType(RopeStrandHolderBlockEntity.class);
    public Type IRopeStrandHolderBlockEntity  = CompileTool.bakeType(IRopeStrandHolderBlockEntity.class);

    //public static final Method generatedMethod = new Method("bcd$hasActor", BlockEntitySubLevelActor, new Type[0]);
    public static final Method generatedMethod = new Method("bcd$sable$getConnectionDependencies", Type.getType(Iterable.class), new Type[0]);
    //Iterable<@NotNull SubLevel> sable$getConnectionDependencies()
    public static final Method searchMethod = new Method("sable$getConnectionDependencies", Type.getType(Iterable.class), new Type[0]);

    @Override
    public Type[] usedClasses() {
        return BCDPostprocessors.Postprocessor.types(
            RopeStrandHolderBlockEntity
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
