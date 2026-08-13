package com.zelaux.bcd.asm_commons;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.*;

public class MethodUtil {
    public static ClassNode withWrappedMethodVisitor(WrapVisitMethod wrappedMethodVisitor) {
        return new ClassNode(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return wrappedMethodVisitor.visitMethod(access, name, descriptor, signature, exceptions, (MethodNode) super.visitMethod(access, name, descriptor, signature, exceptions));
            }
        };
    }
    public static ClassNode withWrappedMethodVisitor(WrapVisitMethod.Simple wrappedMethodVisitor) {
        return withWrappedMethodVisitor((WrapVisitMethod) wrappedMethodVisitor);
    }

    ;



    @FunctionalInterface
    public interface WrapVisitMethod {
        @FunctionalInterface
         interface Simple extends WrapVisitMethod {

            MethodVisitor visitMethod(MethodNode createdVisitor);

            @Override
            default MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions, MethodNode createdVisitor) {
                return visitMethod(createdVisitor);
            }
        }

        MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions, MethodNode createdVisitor);
    }

    public static boolean equal(MethodInsnNode node, Method method) {
        return node.name.equals(method.getName()) && node.desc.equals(method.getDescriptor());
    }

    public static boolean equal(MethodNode node, Method method) {
        return node.name.equals(method.getName()) && node.desc.equals(method.getDescriptor());
    }

    public static boolean processInstruction(InsnList instructions, BoundMethod[] boundMethods, FoundMethodInvokeHandler handler) {
        AbstractInsnNode[] array = instructions.toArray();
        boolean any = false;
        for(int i = 0; i < array.length; i++) {
            AbstractInsnNode insn = array[i];
            if(!(insn instanceof MethodInsnNode node)) continue;
            int i1 = indexOf(node, boundMethods);
            if(i1 < 0) continue;
            any |= handler.extracted(instructions, array, i, node, boundMethods[i1]);
        }
        return any;
    }

    public static int indexOf(MethodInsnNode node, BoundMethod[] methods) {
        for(int i = 0; i < methods.length; i++) {
            BoundMethod method = methods[i];
            if(method.matches(node)) {
                return i;
            }
        }
        return -1;
    }

    @FunctionalInterface
    public interface FoundMethodInvokeHandler {
        boolean extracted(InsnList instructions, AbstractInsnNode[] isnsArray, int i, MethodInsnNode insn, BoundMethod boundMethod);
    }
}
