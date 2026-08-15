package com.zelaux.betterdiagram.mixin_plugin.postprocessors;

import com.zelaux.bcd.asm_commons.LabelCompleter;
import com.zelaux.bcd.asm_commons.MethodUtil;
import com.zelaux.betterdiagram.annotations.CompileTool;
import com.zelaux.betterdiagram.extend.IRopeStrandHolderBlockEntity;
import com.zelaux.betterdiagram.feature.include_subsublevel.actor_handler.SwivelBearingActor;
import com.zelaux.betterdiagram.mixin.feature.include_subsublevel.actor.SwivelBearingBlockEntityMixin;
import com.zelaux.betterdiagram.mixin_plugin.system.ProcessorProvider;
import com.zelaux.betterdiagram.mixin_plugin.util.ServerToUnboundRenamer;
import dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintConfiguration;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBlockEntity;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import org.joml.Vector3d;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.*;

public enum VanilaProcessorProvider implements ProcessorProvider {
    INSTANCE;
    private final ClassNode DUMMY = new ClassNode();


    //public static final Method generatedMethod = new Method("bcd$hasActor", BlockEntitySubLevelActor, new Type[0]);
    //public static final Method generatedMethod = new Method("bcd$sable$getConnectionDependencies", Type.getType(Iterable.class), new Type[0]);
    public static final Method generatedMethod = CompileTool.GetMethod.methodOf(IRopeStrandHolderBlockEntity::bcd$sable$getConnectionDependencies).method();
    //Iterable<@NotNull SubLevel> sable$getConnectionDependencies()
    //public static final Method searchMethod = new Method("sable$getConnectionDependencies", Type.getType(Iterable.class), new Type[0]);
    public static final Method searchMethod = CompileTool.GetMethod.methodOf(RopeStrandHolderBlockEntity::sable$getConnectionDependencies).method();
    private Type bearingMixin = CompileTool.bakeType(SwivelBearingBlockEntityMixin.class);
    private final Method attachConstraints = new Method("attachConstraints", Type.VOID_TYPE, new Type[]{
        CompileTool.bakeType(ServerSubLevel.class),
        CompileTool.bakeType(Vector3d.class),
    });
    private static final Method bcd$attachConstraints_ = CompileTool.GetMethod.methodOf(SwivelBearingActor.Access::bcd$attachConstraints).method();
    //private static final Method bcd$attachConstraints_ = new Method("bcd$attachConstraints", CompileTool.bakeType(RotaryConstraintConfiguration.class), new Type[]{
    //    CompileTool.bakeType(SubLevel.class),
    //    CompileTool.bakeType(Vector3d.class)
    //});

    @Override
    public void preMixin(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if(mixinClassName.replace('.', '/').equals(bearingMixin.getInternalName())) {
            MethodNode method = MethodUtil.findMethod(targetClass, attachConstraints, bcd$attachConstraints_);
            if(method != null) {
                ClassNode classNode = MethodUtil.withWrappedMethodVisitor(AttachConstraintsVisitor::new);
                method.accept(classNode);
                MethodNode clone = classNode.methods.removeFirst();
                clone.access = Opcodes.ACC_PUBLIC;
                clone.name = bcd$attachConstraints_.getName();
                clone.desc = bcd$attachConstraints_.getDescriptor();
                targetClass.methods.add(clone);
            }
        }
        ProcessorProvider.super.preMixin(targetClassName, targetClass, mixinClassName, mixinInfo);
    }

    @Override
    public Transformations[] transformations() {
        return ProcessorProvider.types(
            Transformations.of(CompileTool.bakeType(SwivelBearingBlockEntity.class), targetClass -> true),
            Transformations.of(CompileTool.bakeType(RopeStrandHolderBlockEntity.class), targetClass -> {
                MethodNode found = MethodUtil.findMethod(targetClass, searchMethod, generatedMethod);
                if(found == null) return false;
                MethodNode clone = cloneMethod(found);
                clone.name = generatedMethod.getName();
                targetClass.methods.add(clone);
                final Type iRopeStrandHolderBlockEntity = CompileTool.bakeType(IRopeStrandHolderBlockEntity.class);
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

    private static class AttachConstraintsVisitor extends MethodVisitor {
        final LabelCompleter labelFixer;

        public AttachConstraintsVisitor(MethodNode node) {
            super(Opcodes.ASM9, ServerToUnboundRenamer.make(node));
            mv = labelFixer = new LabelCompleter(api, mv);
        }


        @Override
        public void visitInsn(int opcode) {
            if(opcode == Opcodes.RETURN) {
                returnNull();
                return;
            }
            super.visitInsn(opcode);
        }

        private void returnNull() {
            super.visitInsn(Opcodes.ACONST_NULL);
            super.visitInsn(Opcodes.ARETURN);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            if(name.equals("<init>") && owner.equals(CompileTool.classToInternal(RotaryConstraintConfiguration.class))) {

                super.visitInsn(Opcodes.ARETURN);
                if(labelFixer.completeLabels()) {
                    returnNull();
                }
                mv = null;
            }
        }
    }
}
