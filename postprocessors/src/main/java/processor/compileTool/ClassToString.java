package processor.compileTool;

import com.zelaux.bcd.asm_commons.BoundMethod;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class ClassToString extends AbstractMethodHandler {
    public static final Type COMPILE_TOOL_TYPE = Type.getObjectType("com.zelaux.betterdiagram.annotations.CompileTool".replace('.', '/'));

    private static final String str2str = Type.getMethodDescriptor(Type.getType(String.class), new Type[]{Type.getType(Class.class)});

    public ClassToString() {
        super(1, new BoundMethod[]{
            new BoundMethod(COMPILE_TOOL_TYPE, new Method("classToString", str2str)),
            new BoundMethod(COMPILE_TOOL_TYPE, new Method("classToInternal", str2str)),
        });
    }

    @Override
    public boolean handle(InsnList instructions, AbstractInsnNode[] array, int index, MethodInsnNode insn, BoundMethod boundMethod) {
        if(!(findPrevMeaningFull(array,index) instanceof LdcInsnNode ldc)) return false;
        if(!(ldc.cst instanceof Type innerType)) return false;
        instructions.remove(insn);
        boolean isInternal = boundMethod.getName().toLowerCase().endsWith("internal");
        if(isInternal) {
            ldc.cst = innerType.getInternalName();
        } else {
            ldc.cst = innerType.getInternalName().replace('/', '.');
        }
        return true;
    }

}
