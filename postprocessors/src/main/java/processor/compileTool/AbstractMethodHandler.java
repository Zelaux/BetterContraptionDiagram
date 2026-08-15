package processor.compileTool;

import asmlib.transform.AbstractClassFileTransformer;
import asmlib.transform.LazyByteCodeProvider;
import asmlib.transform.TransformationWriter;
import asmlib.transform.context.TransformationContext;
import asmlib.util.ClassFileMetaData;
import com.zelaux.bcd.asm_commons.BoundMethod;
import com.zelaux.bcd.asm_commons.MethodUtil;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;

public abstract class AbstractMethodHandler extends AbstractClassFileTransformer {

    private static final IntSet meaningLess=IntSet.of(
        AbstractInsnNode.LABEL,AbstractInsnNode.LINE,AbstractInsnNode.FRAME
    );
    public final BoundMethod[] typeMethods ;

    protected BoundMethod[] exists;

    public AbstractMethodHandler(int maxRound, BoundMethod[] typeMethods) {
        super(maxRound);
        this.typeMethods = typeMethods;
        exists = new BoundMethod[typeMethods.length];
    }

    static AbstractInsnNode findPrevMeaningFull(AbstractInsnNode[] arr, int i) {
            while(i>0){
                i--;
                AbstractInsnNode node = arr[i];
                if(meaningLess.contains(node.getType()))continue;
                return node;
            }
            return null;
        }

    public abstract boolean handle(InsnList instructions, AbstractInsnNode[] array, int index, MethodInsnNode insn, BoundMethod boundMethod);

    @Override
    public boolean shouldAnalyze(String className, TransformationContext context) {
        return typeMethods.length>0;
    }

    @Override
    public @Nullable TransformationWriter analyze(String className, LazyByteCodeProvider byteCodeProvider, TransformationContext context) {
        ClassFileMetaData metaData = new ClassFileMetaData(byteCodeProvider.getCloned());
        int count = 0;
        for(BoundMethod typeMethod : typeMethods) {
            if(metaData.usesMethod(typeMethod.getOwner(), typeMethod.getName())) {
                exists[count++] = typeMethod;
            }
        }
        if(count == 0) return null;

        BoundMethod[] boundMethods = Arrays.copyOf(exists, count);

        return TransformationWriter.nodeTransformer(classNode -> {
            boolean any = false;
            for(MethodNode method : classNode.methods) {
                any |= MethodUtil.processInstruction(method.instructions, boundMethods, this::handle);
            }
            return any ? classNode : null;
        });
    }



}
