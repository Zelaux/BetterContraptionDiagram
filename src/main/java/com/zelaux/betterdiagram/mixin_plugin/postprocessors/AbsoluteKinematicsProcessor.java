package com.zelaux.betterdiagram.mixin_plugin.postprocessors;

import com.llamalad7.mixinextras.lib.apache.commons.ClassUtils;
import com.zelaux.bcd.asm_commons.*;
import com.zelaux.betterdiagram.DebugRuntime;
import com.zelaux.betterdiagram.annotations.CompileTool;
import com.zelaux.betterdiagram.compat.absolute_kinematics.AbsoluteKinematicsModule;
import com.zelaux.betterdiagram.compat.absolute_kinematics.IBaseJointBlockEntity;
import com.zelaux.betterdiagram.mixin_plugin.TypeIndex;
import com.zelaux.betterdiagram.mixin_plugin.system.ProcessorProvider;
import com.zelaux.betterdiagram.mixin_plugin.util.ServerToUnboundRenamer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.*;

public enum AbsoluteKinematicsProcessor implements ProcessorProvider {
    INSTANCE;
    private static final Type BaseJointBlockEntity = TypeIndex.type("com/rcx/absolutekinematics/blockentity/BaseJointBlockEntity");
    private static final Type HingeBlockEntity = TypeIndex.type("com/rcx/absolutekinematics/blockentity/HingeBlockEntity");
    private static final Type BallJointBlockEntity = TypeIndex.type("com/rcx/absolutekinematics/blockentity/BallJointBlockEntity");

    private static final Type BallJointPlateBlockEntity = TypeIndex.type("com/rcx/absolutekinematics/blockentity/BallJointPlateBlockEntity");
    private static final Type BaseJointPlateBlockEntity = TypeIndex.type("com/rcx/absolutekinematics/blockentity/BaseJointPlateBlockEntity");
    //private static final Type HingeLeafBlockEntity= TypeIndex.type("com/rcx/absolutekinematics/blockentity/HingeLeafBlockEntity");
    private static Method attachConstraints = new Method("attachConstraints", Type.VOID_TYPE, new Type[]{
        TypeIndex.ServerSubLevel,
        TypeIndex.bakedType(Vector3d.class),
    });
    private static Method bcd$attachConstraints = CompileTool.GetMethod.methodOf(IBaseJointBlockEntity::bcd$attachConstraints).method();
    /*private static Method bcd$attachConstraints = new Method("bcd$attachConstraints", Type.VOID_TYPE, new Type[]{
        TypeIndex.SubLevel,
        TypeIndex.bakedType(Vector3d.class),
    });*/
    //TypeIndex
    private static final BoundMethod configToHandle = CompileTool.GetMethod.methodOf(AbsoluteKinematicsModule::makeHandle);

    private boolean transform(ClassNode targetClass) {
        MethodNode method = MethodUtil.findMethod(targetClass, attachConstraints, bcd$attachConstraints);
        if(method == null) return false;
        ClassNode copyNode = MethodUtil.withWrappedMethodVisitor(
            targetClass.name.equals(BaseJointBlockEntity.getInternalName()) ? methodNode -> base(methodNode, method) : AbsoluteKinematicsProcessor::simpleRenamer
        );
        method.accept(copyNode);



        MethodNode node = copyNode.methods.removeFirst();
        node.name = bcd$attachConstraints.getName();
        node.desc = bcd$attachConstraints.getDescriptor();

        if(DebugRuntime.isDebug()){
            DebugRuntime.saveMethodInFile(targetClass.name, node);
        }


        targetClass.methods.add(node);
        return true;
    }

    private static @NotNull Renamer simpleRenamer(MethodNode visitor) {
        Renamer make = ServerToUnboundRenamer.make(visitor);
        make.addMapping(CodeEntry.methodEntry(BaseJointBlockEntity,attachConstraints),CodeEntry.methodEntry(BaseJointBlockEntity,bcd$attachConstraints));
        return make;
    }

    private MethodVisitor base(MethodNode methodNode, MethodNode original) {


        return new BaseMethodMapper(methodNode, original);
    }

    @Override
    public Transformations[] transformations() {
        Type[] types = {
            BaseJointBlockEntity,
            BallJointBlockEntity,
            HingeBlockEntity,
        };
        return new Transformations[]{
            Transformations.of(types, this::transform)
        };
    }

    private static class BaseMethodMapper extends MethodVisitor {
        private final Renamer renamer;
        private final MethodNode methodNode;
        private final InsnList instructions;
        MethodNode original;
        private boolean nextStoreIsConfig;

        public BaseMethodMapper(MethodNode methodNode, MethodNode original) {
            super(Opcodes.ASM9);
            this.original = original;
            this.methodNode = methodNode;
            mv = renamer = simpleRenamer(this.methodNode);
            instructions = methodNode.instructions;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if(opcode == Opcodes.INVOKESPECIAL && owner.equals(TypeIndex.GenericConstraintConfiguration.getInternalName()) && name.equals("<init>")) {
                nextStoreIsConfig = true;
            }
            if(name.equals("getLevel") || name.equals("physicsSystem")) {
                mv = null;
                instructions.remove(instructions.getLast());
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitInsn(int opcode) {
            super.visitInsn(opcode);
        }

        boolean finished = false;

        int configVar = -1;

        //IntSet canceled=new IntOpenHashSet();
        @Override
        public void visitVarInsn(int opcode, int varIndex) {
            super.visitVarInsn(opcode, varIndex);
            if(opcode == Opcodes.ASTORE && nextStoreIsConfig) {
                nextStoreIsConfig = false;
                configVar = varIndex;
            }
            if(opcode == Opcodes.ASTORE && mv == null && !finished) {
                //canceled.add(varIndex);
                mv = renamer;
            }
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            if(finished) return;
            if(name.equals("handle") && owner.equals(BaseJointBlockEntity.getInternalName())) {
                if(configVar == -1) {
                    for(LocalVariableNode variable : original.localVariables) {
                        if(variable.name.equals("constraint")) {
                            configVar = variable.index;
                            break;
                        }
                    }
                }
                InsnList nodes = instructions;
                for(AbstractInsnNode insn = nodes.getLast(); insn != null;) {
                    AbstractInsnNode cur = insn;
                    insn = cur.getPrevious();
                    if(cur instanceof VarInsnNode var && var.var == 0) {
                        break;
                    }
                    nodes.remove(cur);
                }
                BoundMethod boundMethod = configToHandle;
                visitVarInsn(Opcodes.ALOAD, configVar);
                boundMethod.invokeInsn(Opcodes.INVOKESTATIC).accept(this);
                super.visitFieldInsn(opcode, owner, name, descriptor);
                visitInsn(Opcodes.RETURN);
                mv = null;
                finished = false;
            }
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }
    }
}
