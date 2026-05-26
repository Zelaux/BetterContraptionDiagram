package processor;

import asmlib.transform.AbstractClassFileTransformer;
import asmlib.transform.LazyByteCodeProvider;
import asmlib.transform.TransformationWriter;
import asmlib.transform.context.TransformationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.HashSet;

public class AddDataCorrector extends AbstractClassFileTransformer {

    public static final String CLASS_NAME = "com.zelaux.betterdiagram.data.BCDData";
    public static final Type DATA_INTERNAL_NAME = Type.getObjectType("com/zelaux/betterdiagram/data/BCDData");
    public static final String METHOD_NAME = "onWithReturn";


    public AddDataCorrector() {
        super(1);
    }

    @Override
    public boolean shouldAnalyze(String className, TransformationContext context) {
        return className.equals(CLASS_NAME);
    }

    @Override
    public @Nullable TransformationWriter analyze(String className, LazyByteCodeProvider byteCodeProvider, TransformationContext context) {

        return TransformationWriter.nodeTransformer(node -> {
            HashSet<String> methodsToProcess = new HashSet<>();
            for(FieldNode field : node.fields) {
                if((Opcodes.ACC_STATIC & field.access) != 0) continue;
                String name = field.name;
                methodsToProcess.add(withMethod(name));
            }
            for(MethodNode method : node.methods) {
                if(!methodsToProcess.contains(method.name)) continue;
                InsnList list = method.instructions;
                AbstractInsnNode[] cache = list.toArray();
                for(AbstractInsnNode insnNode : cache) {
                    int opcode = insnNode.getOpcode();
                    if(opcode < Opcodes.IRETURN || opcode > Opcodes.ARETURN) continue;

                    list.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
                    list.insertBefore(insnNode, new InsnNode(Opcodes.SWAP));
                    list.insertBefore(insnNode, new MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL, DATA_INTERNAL_NAME.getInternalName(), METHOD_NAME,
                        Type.getMethodDescriptor(DATA_INTERNAL_NAME, DATA_INTERNAL_NAME), false
                    ));

                }
            }
            return node;
        });
    }

    private @NotNull String withMethod(String name) {
        return "with" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
