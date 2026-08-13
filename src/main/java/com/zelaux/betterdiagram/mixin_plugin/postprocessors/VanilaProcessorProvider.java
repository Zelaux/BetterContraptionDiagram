package com.zelaux.betterdiagram.mixin_plugin.postprocessors;

import com.zelaux.betterdiagram.annotations.CompileTool;
import com.zelaux.betterdiagram.extend.IRopeStrandHolderBlockEntity;
import com.zelaux.betterdiagram.mixin_plugin.system.ProcessorProvider;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBlockEntity;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public enum VanilaProcessorProvider implements ProcessorProvider {
    INSTANCE;
    private final ClassNode DUMMY = new ClassNode();
    public Type ropeStrandHolderBlockEntity = CompileTool.bakeType(RopeStrandHolderBlockEntity.class);
    public Type iRopeStrandHolderBlockEntity = CompileTool.bakeType(IRopeStrandHolderBlockEntity.class);

    //public static final Method generatedMethod = new Method("bcd$hasActor", BlockEntitySubLevelActor, new Type[0]);
    //public static final Method generatedMethod = new Method("bcd$sable$getConnectionDependencies", Type.getType(Iterable.class), new Type[0]);
    public static final Method generatedMethod = CompileTool.GetMethod.methodOf(IRopeStrandHolderBlockEntity::bcd$sable$getConnectionDependencies).method();
    //Iterable<@NotNull SubLevel> sable$getConnectionDependencies()
    //public static final Method searchMethod = new Method("sable$getConnectionDependencies", Type.getType(Iterable.class), new Type[0]);
    public static final Method searchMethod = CompileTool.GetMethod.methodOf(RopeStrandHolderBlockEntity::sable$getConnectionDependencies).method();

    @Override
    public Transformations[] transformations() {
        return ProcessorProvider.types(
            Transformations.of(ropeStrandHolderBlockEntity, targetClass -> {
                MethodNode found = GadgetsProcessorProvider.findMethod(targetClass, generatedMethod, searchMethod);
                if(found == null) return false;
                MethodNode clone = cloneMethod(found);
                clone.name = generatedMethod.getName();
                targetClass.methods.add(clone);
                targetClass.interfaces.add(iRopeStrandHolderBlockEntity.getInternalName());
                targetClass.signature += iRopeStrandHolderBlockEntity.getDescriptor();
                return true;
            })
        );
    }


    private MethodNode cloneMethod(MethodNode found) {
        DUMMY.methods.clear();
        found.accept(DUMMY);
        return DUMMY.methods.removeFirst();
    }
}
