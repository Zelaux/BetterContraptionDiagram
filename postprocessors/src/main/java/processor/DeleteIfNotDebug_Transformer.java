package processor;

import asmlib.transform.AbstractClassFileTransformer;
import asmlib.transform.LazyByteCodeProvider;
import asmlib.transform.TransformationWriter;
import asmlib.transform.context.TransformationContext;
import asmlib.util.AnnotationArgumentMap;
import asmlib.util.ClassFileMetaData;
import asmlib.util.NodeUtil;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import processor.debugOnly.AnnotatedElementsCollector;
import processor.debugOnly.DebugEntry;
import processor.debugOnly.NoDebugOnlyInRelease_Transformer;

import java.util.List;

public class DeleteIfNotDebug_Transformer extends AbstractClassFileTransformer {
    public static final String DELETE_ANNOTATION_CLASS_NAME = "com.zelaux.betterdiagram.annotations.DeleteInRelease";
    public static final String EMPTY_ANNOTATION_CLASS_NAME = "com.zelaux.betterdiagram.annotations.EmptyInRelease";
    public static final Type DELETE_ANNOTATION_TYPE = Type.getObjectType(DELETE_ANNOTATION_CLASS_NAME.replace('.', '/'));
    public static final Type EMPTY_ANNOTATION_TYPE = Type.getObjectType(EMPTY_ANNOTATION_CLASS_NAME.replace('.', '/'));
    public static final String DELETE_ANNOTATION_TYPE_DESCRIPTOR = DELETE_ANNOTATION_TYPE.getDescriptor();
    public static final String EMPTY_ANNOTATION_TYPE_DESCRIPTOR = EMPTY_ANNOTATION_TYPE.getDescriptor();
    public final boolean isDebug;
    private static final IntSet FIELD_SET_OPCODES = IntSet.of(Opcodes.PUTSTATIC, Opcodes.PUTFIELD);


    public DeleteIfNotDebug_Transformer(TransformationContext context) {
        super(1);
        isDebug = context.extraArguments().isDebug();
        if(isDebug) {
            System.out.println("Postprocessing DEBUG code");
        } else {
            System.out.println("Postprocessing release code");
        }
    }

    @Override
    public boolean shouldAnalyze(String className, TransformationContext context) {
        return !isDebug;
    }

    public ObjectOpenHashSet<String> nestedToDelete=new ObjectOpenHashSet<>();
    private static final AnnotatedElementsCollector NESTED_COLLECTOR =new AnnotatedElementsCollector(null,"dummy"){{
        isAnnotated=true;
    }};
    @Override
    public @Nullable TransformationWriter analyze(String className, LazyByteCodeProvider byteCodeProvider, TransformationContext context) {
        ClassFileMetaData metaData = new ClassFileMetaData(byteCodeProvider.getCloned());
        if(!metaData.usesAnnotation(DELETE_ANNOTATION_TYPE_DESCRIPTOR)
           && !metaData.usesAnnotation(EMPTY_ANNOTATION_TYPE_DESCRIPTOR) &&
           !nestedToDelete.contains(metaData.getClassName()))
            return null;

        ClassNode classNode = byteCodeProvider.createClassNode();
        AnnotatedElementsCollector debugCollector = getCollector(classNode);
        Type dataClassName = Type.getObjectType(classNode.name);
        if(debugCollector.isAnnotated()) {
            List<String> nestMembers = classNode.nestMembers;
            if(nestMembers != null && !nestMembers.isEmpty()) {
                roundLeft=2;
                nestedToDelete.addAll(nestMembers);
            }
            NoDebugOnlyInRelease_Transformer.debugEntries.add(DebugEntry.classEntry(dataClassName));
            addAllToDebug(classNode, dataClassName);
        } else{
            for(var node : debugCollector.annotatedFields()) {
                NoDebugOnlyInRelease_Transformer.debugEntries.add(DebugEntry.fieldEntry(dataClassName, node.node));
            }
            for(var node : debugCollector.annotatedMethods()) {
                NoDebugOnlyInRelease_Transformer.debugEntries.add(DebugEntry.methodEntry(dataClassName, node.node));
            }
        }
        if(!debugCollector.isAnnotated() && NodeUtil.findAnnotation(
            EMPTY_ANNOTATION_TYPE_DESCRIPTOR, classNode.invisibleAnnotations, classNode.visibleAnnotations
        ) != null) {
            return TransformationWriter.nodeTransformer(x -> {
                for(FieldNode field : x.fields) {
                    NoDebugOnlyInRelease_Transformer.debugEntries.add(
                        DebugEntry.fieldEntry(dataClassName,field)
                    );
                }
                x.fields.clear();
                List<MethodNode> methods = x.methods;

                for(int i = 0; i < methods.size(); i++) {
                    MethodNode methodNode = methods.get(i);
                    if(methodNode.name.endsWith("init>")) continue;
                    NoDebugOnlyInRelease_Transformer.debugEntries.add(
                        DebugEntry.methodEntry(dataClassName,methodNode)
                    );
                    methods.remove(i--);
                }
                return x;
            });
        }
        return lastRound(className, debugCollector);
    }

    private @NotNull AnnotatedElementsCollector getCollector(ClassNode classNode) {
        if(nestedToDelete.contains(classNode.name))return NESTED_COLLECTOR;
        AnnotatedElementsCollector debugCollector = new AnnotatedElementsCollector(null, DELETE_ANNOTATION_TYPE_DESCRIPTOR);
        classNode.accept(debugCollector);
        return debugCollector;
    }

    private void addAllToDebug(ClassNode classNode, Type dataClassName) {
        for(var field : classNode.fields) {
            NoDebugOnlyInRelease_Transformer.debugEntries.add(
                DebugEntry.fieldEntry(dataClassName,field)
            );
        }
        for(var method : classNode.methods) {
            NoDebugOnlyInRelease_Transformer.debugEntries.add(
                DebugEntry.methodEntry(dataClassName,method)
            );
        }
    }

    private TransformationWriter lastRound(String className, AnnotatedElementsCollector debugCollector) {

        if(debugCollector.isAnnotated()) {
            System.out.println("    Deleted class "+className);
            return TransformationWriter.DELETE_WRITER;}

        var set = new ObjectOpenHashSet<String>();
        var staticFieldToDelete = new Object2BooleanOpenHashMap<String>();
        var instanceFieldToDelete = new Object2BooleanOpenHashMap<String>();
        debugCollector.annotatedFields().forEach(x -> set.add(x.node.name));
        debugCollector.annotatedMethods().forEach(x -> set.add(x.node.name + "#" + x.node.desc));

        for(AnnotatedElementsCollector.AnnotatedElement<FieldNode> field : debugCollector.annotatedFields()) {
            AnnotationNode node = field.annotations.getFirst();
            AnnotationArgumentMap map = new AnnotationArgumentMap(node);
            boolean saveSideEffect = false;
            if(map.get("saveSideEffectForFieldInitialization") instanceof Boolean b) {
                saveSideEffect = b;
            }
            var fields = (field.node.access & Opcodes.ACC_STATIC) != 0 ? staticFieldToDelete : instanceFieldToDelete;

            fields.put(field.node.name, saveSideEffect);
        }
        return TransformationWriter.nodeTransformer(classNode -> {
            var fields = classNode.fields;

            for(int i = 0; i < fields.size(); i++) {
                var node = fields.get(i);
                if(!set.contains(node.name)) continue;
                fields.remove(i--);
                System.out.println("    Deleted  field "+classNode.name+"#"+node.name);
            }
            var methods = classNode.methods;
            for(int i = 0; i < methods.size(); i++) {
                var node = methods.get(i);
                if(node.name.equals("<clinit>")) {
                    removeFields(classNode, node, staticFieldToDelete);
                    continue;
                } else if(node.name.equals("<init>")) {
                    removeFields(classNode, node, instanceFieldToDelete);
                    continue;
                }
                if(set.contains(node.name + "#" + node.desc)) {
                    System.out.println("    Deleted method "+classNode.name+"#"+node.name +node.desc);
                    methods.remove(i--);
                }
            }

            return classNode;
        });
    }

    @SneakyThrows
    private void removeFields(ClassNode classNode, MethodNode node, Object2BooleanOpenHashMap<String> fields) {
        Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicInterpreter());
        Frame<BasicValue>[] frames = analyzer.analyze(classNode.name, node);
        InsnList instructionsList = node.instructions;
        AbstractInsnNode[] instructions = instructionsList.toArray();
        int hasThis = (node.access & Opcodes.ACC_STATIC) == 0 ? 1 : 0;
        for(int i = 0; i < instructions.length; i++) {
            AbstractInsnNode instruction = instructions[i];
            if(instruction.getType() != AbstractInsnNode.FIELD_INSN || !FIELD_SET_OPCODES.contains(instruction.getOpcode())) {
                continue;
            }
            FieldInsnNode fieldNode = ((FieldInsnNode) instruction);
            if(!fields.containsKey(fieldNode.name)) continue;
            boolean shouldSaveSideEffect = fields.getBoolean(fieldNode.name);
            if(shouldSaveSideEffect) {
                int singleType = (Type.getType(fieldNode.desc).getSize() == 1) ? 0b10 : 0b00;
                switch(singleType | hasThis) {
                    case 0b00,
                         0b11 -> instructionsList.set(instruction, new InsnNode(Opcodes.POP2));
                    case 0b01 -> {
                        InsnNode insnNode = new InsnNode(Opcodes.POP2);
                        instructionsList.set(instruction, insnNode);
                        instructionsList.insert(insnNode, new InsnNode(Opcodes.POP));
                    }
                    case 0b10 -> instructionsList.set(instruction, new InsnNode(Opcodes.POP));
                }

                continue;
            }
            Frame<BasicValue> frame = frames[i];
            int stackSize = frame.getStackSize();
            int j;
            for(j = i - 1; j >= 0; j--) {
                if(frames[j].getStackSize() == stackSize - 1) {
                    break;
                }
            }
            if(j >= 0) {
                for(int innerI = j; innerI <= i; innerI++) {
                    instructionsList.remove(instructions[innerI]);
                }
            }
        }
    }


}
