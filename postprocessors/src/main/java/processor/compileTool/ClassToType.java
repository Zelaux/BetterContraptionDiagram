package processor.compileTool;

import com.zelaux.bcd.asm_commons.BoundMethod;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class ClassToType extends AbstractMethodHandler {

    public static final BoundMethod getObjectType = BoundMethod.make(
        Type.getType(Type.class),
        "getObjectType",
        Type.getType(Type.class),
        Type.getType(String.class)
    );
    public static final BoundMethod sampleMethod = new BoundMethod(
        CompileTool.COMPILE_TOOL_TYPE,
        new Method(
            "bakeType", Type.getType(Type.class), new Type[]{Type.getType(Class.class)}
        )
    );
    private static final Type TYPE_INDEX_TYPE = Type.getObjectType("com.zelaux.betterdiagram.mixin_plugin.TypeIndex".replace('.', '/'));
    public static final BoundMethod[] typeMethods = {
        sampleMethod,
        sampleMethod.withMethod(new Method("classToType", sampleMethod.method().getDescriptor())),
        sampleMethod.withMethod(new Method("asType", sampleMethod.method().getDescriptor())),
        BoundMethod.make(TYPE_INDEX_TYPE,"bakedType", sampleMethod.getReturnType(), sampleMethod.getArgumentTypes()),
        BoundMethod.make(TYPE_INDEX_TYPE,"bakeType", sampleMethod.getReturnType(), sampleMethod.getArgumentTypes()),
        getObjectType

    };

    public ClassToType() {
        super(1, typeMethods);
    }

    public boolean handle(InsnList instructions, AbstractInsnNode[] insnArr, int i, MethodInsnNode methodInsn, BoundMethod boundMethod) {

        if(!(findPrevMeaningFull(insnArr,i) instanceof LdcInsnNode ldc)) return false;
        if(!(ldc.cst instanceof Type innerType)) return false;


        ldc.cst = innerType.getInternalName();
        getObjectType.setTo(methodInsn, Opcodes.INVOKESTATIC);
        //instructions.remove(methodInsn);
        return true;
    }


}
