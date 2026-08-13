package processor.compileTool;

import asmlib.transform.AbstractClassFileTransformer;
import asmlib.transform.LazyByteCodeProvider;
import asmlib.transform.TransformationWriter;
import asmlib.transform.context.TransformationContext;
import asmlib.util.ClassFileMetaData;
import com.zelaux.bcd.asm_commons.BoundMethod;
import com.zelaux.bcd.asm_commons.MethodUtil;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;

public class ClassToType extends AbstractClassFileTransformer {

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
    public static final BoundMethod[] typeMethods = {
        sampleMethod,
        sampleMethod.withMethod(new Method("classToType", sampleMethod.method().getDescriptor())),
        sampleMethod.withMethod(new Method("asType", sampleMethod.method().getDescriptor())),
        BoundMethod.make(
            Type.getObjectType("com.zelaux.betterdiagram.mixin_plugin.TypeIndex".replace('.', '/')),
            "bakedType", sampleMethod.getReturnType(), sampleMethod.getArgumentTypes()),
        getObjectType

    };

    public ClassToType() {
        super(1);
    }

    @Override
    public boolean shouldAnalyze(String className, TransformationContext context) {
        return true;
    }

    BoundMethod[] exists = new BoundMethod[typeMethods.length];

    @Override
    public @Nullable TransformationWriter analyze(String className, LazyByteCodeProvider byteCodeProvider, TransformationContext context) {
        ClassFileMetaData metaData = new ClassFileMetaData(byteCodeProvider.getCloned());
        int count = 0;
        for(BoundMethod typeMethod : typeMethods) {
            if(metaData.usesMethod(CompileTool.COMPILE_TOOL_TYPE.getInternalName(), typeMethod.getName())) {
                exists[count++] = typeMethod;
            }
        }
        if(count == 0) return null;

        BoundMethod[] boundMethods = Arrays.copyOf(exists, count);

        return TransformationWriter.nodeTransformer(classNode -> {
            boolean any = false;
            for(MethodNode method : classNode.methods) {
                any |= MethodUtil.processInstruction(method.instructions, boundMethods,
                    (instructions, insnArr, i, methodInsn, boundMethod) -> {
                        if(!(insnArr[i - 1] instanceof LdcInsnNode ldc)) return false;
                        if(!(ldc.cst instanceof Type innerType)) return false;


                        ldc.cst = innerType.getInternalName();
                        getObjectType.setTo(methodInsn, Opcodes.INVOKESTATIC);
                        //instructions.remove(methodInsn);
                        return true;
                    });
            }
            return any ? classNode : null;
        });
    }

}
