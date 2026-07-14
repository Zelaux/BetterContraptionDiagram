package processor;

import asmlib.transform.AbstractClassFileTransformer;
import asmlib.transform.LazyByteCodeProvider;
import asmlib.transform.TransformationWriter;
import asmlib.transform.context.TransformationContext;
import asmlib.util.ClassFileMetaData;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.*;

public class SubstituteIsDebug_Transformer extends AbstractClassFileTransformer {
    public static final String DEBUG_RUNTIME_CLASS_NAME = "com.zelaux.betterdiagram.DebugRuntime";
    public static final Type DEBUG_RUNTIME_TYPE = Type.getObjectType(DEBUG_RUNTIME_CLASS_NAME.replace('.', '/'));
    public static final String DEBUG_RUNTIME_TYPE_DESCRIPTOR = DEBUG_RUNTIME_TYPE.getDescriptor();
    public static final Method isDebug_Method = new Method(
        "isDebug",
        Type.BOOLEAN_TYPE,
        new Type[0]
    );
    public static final InsnList DUMMY_LIST = new InsnList();
    public final boolean isDebug;


    public SubstituteIsDebug_Transformer(TransformationContext context) {
        super(3);
        isDebug = context.extraArguments().isDebug();
    }

    @Override
    public boolean shouldAnalyze(String className, TransformationContext context) {
        return !isDebug;
    }

    @Override
    public @Nullable TransformationWriter analyze(String className, LazyByteCodeProvider byteCodeProvider, TransformationContext context) {
        ClassFileMetaData metaData = new ClassFileMetaData(byteCodeProvider.getCloned());

        if(!metaData.usesMethod(DEBUG_RUNTIME_TYPE.getInternalName(), isDebug_Method.getName(), isDebug_Method.getDescriptor()) && !className.equals(DEBUG_RUNTIME_CLASS_NAME))
            return null;

        return TransformationWriter.nodeTransformer(classNode -> {
            boolean any = false;
            for(MethodNode method : classNode.methods) {
                any |= processMethod(className, classNode, method);
            }
            if(any) return classNode;
            return null;
        });
    }

    private boolean processMethod(String className, ClassNode classNode, MethodNode method) {
        if(className.equals(DEBUG_RUNTIME_CLASS_NAME) && method.name.equals(isDebug_Method.getName())) {
            for(AbstractInsnNode node : method.instructions) {
                if(node.getOpcode() == Opcodes.ICONST_1) {
                    method.instructions.set(node, isDebugInsnNode());
                    break;
                }
            }
            return true;
        }
        InsnList instructions = method.instructions;
        AbstractInsnNode[] array = instructions.toArray();
        Pointers[] pointers = new Pointers[array.length];
        var candidates = new ObjectArrayList<DeletionCandidate>();
        boolean any = false;
        for(int i = 0; i < array.length; i++) {
            AbstractInsnNode node = array[i];
            if(node.getType() == AbstractInsnNode.JUMP_INSN) {
                JumpInsnNode jump = (JumpInsnNode) node;
                pointer(pointers, jump.label).from(i);
            }
            if(node.getOpcode() != Opcodes.INVOKESTATIC) continue;
            MethodInsnNode methodInsnNode = (MethodInsnNode) node;
            if(!methodInsnNode.name.equals(isDebug_Method.getName())) continue;
            if(!methodInsnNode.desc.equals(isDebug_Method.getDescriptor())) continue;
            int ifI = findIf(array, i + 1);
            any = true;
            if(ifI == -1) {
                instructions.set(node, isDebugInsnNode());
                continue;
            }

            JumpInsnNode jump = (JumpInsnNode) array[ifI];
            //todo: removing goto X for ifelse
            switch(jump.getOpcode()) {
                case Opcodes.IFNE -> {//Should be in release,but not in debug
                    candidates.add(new DeletionCandidate(i, ifI, i, ifI));
                    i = ifI + 1;
                }
                case Opcodes.IFEQ -> {//Should be in debug,but not in release
                    candidates.add(new DeletionCandidate(i, ifI, i, index(jump.label)));
                    i = index(jump.label);
                }
            }
        }
        if(!candidates.isEmpty()) {
            candidatesLoop:
            for(DeletionCandidate candidate : candidates) {
                for(int i = candidate.from; i <= candidate.to; i++) {
                    Pointers pointer = pointers[i];
                    if(pointer == null || pointer.inside(candidate.from, candidate.to)) continue;
                    instructions.set(array[candidate.invokeIndex], isDebugInsnNode());
                    continue candidatesLoop;
                }
                for(int i = candidate.from; i <= candidate.to; i++) {
                    AbstractInsnNode insn = array[i];
                    tryRemove(instructions, insn);
                }
            }
        }
        return any;
    }

    private void tryRemove(InsnList instructions, AbstractInsnNode insn) {
        if(instructions.contains(insn)) instructions.remove(insn);
    }

    private @NotNull AbstractInsnNode isDebugInsnNode() {
        return new InsnNode(isDebug ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
    }

    private Pointers pointer(Pointers[] pointers, LabelNode label) {
        int i = index(label);
        Pointers pointer = pointers[i];
        if(pointer == null) pointers[i] = pointer = new Pointers(i);
        return pointer;
    }

    private int index(AbstractInsnNode label) {
        return DUMMY_LIST.indexOf(label);
    }

    private int findIf(AbstractInsnNode[] array, int i) {
        for(; i < array.length; i++) {
            AbstractInsnNode node = array[i];
            if(node.getType() == AbstractInsnNode.LABEL) continue;
            if(node.getType() == AbstractInsnNode.LINE) continue;
            if(node.getType() == AbstractInsnNode.FRAME) continue;
            if(node.getType() == AbstractInsnNode.JUMP_INSN) {
                if(node.getOpcode() != Opcodes.GOTO) {
                    return i;
                }
            }
            break;
        }
        return -1;
    }


    private static class Pointers {
        IntArrayList from = new IntArrayList();
        int min, max;

        public Pointers(int i) {
            min = i;
            max = i;
        }

        public void from(int i) {
            from.add(i);
            min = Math.min(min, i);
            max = Math.max(max, i);
        }

        public boolean inside(int from, int to) {
            return from <= min && max <= to;
        }
    }

    private record DeletionCandidate(int invokeIndex, int ifIndex, int from, int to) {}
}
