package processor.util;

import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodUtil {
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
            any |= handler.extracted(instructions, array, i, node,boundMethods[i1]);
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
