package processor.compileTool;

import asmlib.transform.AbstractClassFileTransformer;
import asmlib.transform.LazyByteCodeProvider;
import asmlib.transform.TransformationWriter;
import asmlib.transform.context.TransformationContext;
import asmlib.util.ClassFileMetaData;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.*;
import com.zelaux.bcd.asm_commons.MethodUtil;

public class ClassToString extends AbstractClassFileTransformer {
    public static final Type COMPILE_TOOL_TYPE = Type.getObjectType("com.zelaux.betterdiagram.annotations.CompileTool".replace('.', '/'));
    public static final Method classToString = new Method(
        "classToString", Type.getType(String.class), new Type[]{Type.getType(Class.class)}
    );

    public ClassToString() {
        super(1);
    }

    @Override
    public boolean shouldAnalyze(String className, TransformationContext context) {
        return true;
    }

    @Override
    public @Nullable TransformationWriter analyze(String className, LazyByteCodeProvider byteCodeProvider, TransformationContext context) {
        ClassFileMetaData metaData = new ClassFileMetaData(byteCodeProvider.getCloned());
        if(!metaData.usesMethod(COMPILE_TOOL_TYPE.getInternalName(), classToString.getName())) {
            return null;
        }


        return TransformationWriter.nodeTransformer(classNode -> {
            String compileToolType = COMPILE_TOOL_TYPE.getInternalName();
            boolean any = false;
            for(MethodNode method : classNode.methods) {

                InsnList instructions = method.instructions;
                any |= processInstruction(instructions, compileToolType);
            }
            return any ? classNode : null;
        });
    }

    private boolean processInstruction(InsnList instructions, String compileToolType) {
        AbstractInsnNode[] array = instructions.toArray();
        boolean any = false;
        for(int i = 0; i < array.length; i++) {
            AbstractInsnNode insn = array[i];
            if(!(insn instanceof MethodInsnNode node)) continue;
            if(!node.owner.equals(compileToolType) || !MethodUtil.equal(node, classToString)) continue;
            if(!(array[i - 1] instanceof LdcInsnNode ldc)) continue;
            if(!(ldc.cst instanceof Type innerType)) continue;
            any = true;
            instructions.remove(insn);
            ldc.cst = innerType.getInternalName().replace('/', '.');
        }
        return any;
    }

}
