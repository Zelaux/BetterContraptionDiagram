package com.zelaux.bcd.asm_commons;

import lombok.With;
import lombok.experimental.WithBy;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

@With
@WithBy
public record BoundMethod(Type owner, Method method) {

    public BoundMethod(Type owner, String methodName, Type returnType, Type... parameters) {
        this(owner, new Method(methodName, returnType, parameters));
    }

    public static BoundMethod make(Type owner, String methodName, Type returnType, Type... parameters) {
        return new BoundMethod(owner, methodName, returnType, parameters);
    }
/*
    public static BoundMethod fromIntellijString(String method, Type returnType) {
        int open = method.indexOf('(');
        int close = method.indexOf(')');
        int methodNameStart = method.lastIndexOf('.', open);
        return new BoundMethod(
            Type.getObjectType(method.substring(methodNameStart).replace('.','/')),
            method.substring(methodNameStart+1,open)
        );
    }*/

    public boolean matches(MethodInsnNode insn) {
        return insn.owner.equals(owner.getInternalName()) &&
               MethodUtil.equal(insn, method);
    }

    public boolean matches(ClassNode classNode, MethodNode methodNode) {
        return classNode.name.equals(owner.getInternalName()) &&
               MethodUtil.equal(methodNode, method);
    }

    public String getName() {
        return method.getName();
    }

    public String getDescriptor() {
        return method.getDescriptor();
    }

    public Type getDescriptorType() {
        return Type.getMethodType(method.getDescriptor());
    }

    public Type getReturnType() {
        return method.getReturnType();
    }

    public Type[] getArgumentTypes() {
        return method.getArgumentTypes();
    }

    @Override
    public @NotNull String toString() {
        return owner.getInternalName() + " " + method.toString();
    }

    public MethodInsnNode invokeInsn(@MagicConstant(intValues = {Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESPECIAL, Opcodes.INVOKESTATIC, Opcodes.INVOKEINTERFACE}) int opcode) {
        MethodInsnNode methodInsnNode = new MethodInsnNode(0, "", "", "");
        setTo(methodInsnNode, opcode);
        return methodInsnNode;
    }

    public void setTo(MethodInsnNode dest, @MagicConstant(intValues = {Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESPECIAL, Opcodes.INVOKESTATIC, Opcodes.INVOKEINTERFACE}) int opcode) {
        dest.setOpcode(opcode);
        dest.itf = opcode == Opcodes.INVOKEINTERFACE;
        dest.owner = owner().getInternalName();
        dest.name = getName();
        dest.desc = getDescriptor();
    }

    public String getOwner() {
        return owner.getInternalName();
    }
}
