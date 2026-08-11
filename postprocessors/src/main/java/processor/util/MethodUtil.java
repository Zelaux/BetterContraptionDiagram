package processor.util;

import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodInsnNode;

public class MethodUtil {
    public static boolean equal(MethodInsnNode node, Method classToString1) {
        return node.name.equals(classToString1.getName()) && node.desc.equals(classToString1.getDescriptor());
    }
}
