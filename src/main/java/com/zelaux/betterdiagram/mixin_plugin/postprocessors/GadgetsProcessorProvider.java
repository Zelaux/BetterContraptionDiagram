package com.zelaux.betterdiagram.mixin_plugin.postprocessors;

import com.zelaux.betterdiagram.compat.CompatHandles;
import com.zelaux.betterdiagram.mixin.compat.gadgets.Mini;
import com.zelaux.betterdiagram.mixin_plugin.TypeIndex;
import com.zelaux.betterdiagram.mixin_plugin.system.ProcessorProvider;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.*;

import java.util.List;


public class GadgetsProcessorProvider implements ProcessorProvider {
    public static final GadgetsProcessorProvider INSTANCE = new GadgetsProcessorProvider();
    private static final Type physicsShaft = Type.getObjectType("com/rieno/gadgetsandgizmos/content/PhysicsGantryShaftBlockEntity");
    private static final Type mini = TypeIndex.type("com.rieno.gadgetsandgizmos.neoforge.client.CTPhysicsGogglesClient$SimulatedDiagramMiniRenderer");

    public static final Method generatedMethod = new Method("bcd$hasActor", TypeIndex.BlockEntitySubLevelActor, new Type[0]);
    public static final Method searchMethod = new Method("hasActiveAttachedCarriage", Type.BOOLEAN_TYPE, new Type[0]);

    @Override
    public Transformations[] transformations() {
        return new Transformations[]{
            Transformations.of(mini, it -> true),
            Transformations.of(physicsShaft, targetClass -> {
                MethodNode found = findMethod(targetClass, generatedMethod, searchMethod);
                if(found != null) {
                    addMethod(targetClass, found);
                    return true;
                }

                return false;
            }),
        };
    }
    private static final Type MiniMixin=TypeIndex.bakedType(Mini.class);

    /**
     * @see CompatHandles#onPackageUpdate(Object)
     * @see CompatHandles#setGadgetsScreen(Object)
     *
     */
    @Override
    public void preMixin(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if(mixinClassName.replace('.','/').equals(MiniMixin.getInternalName())) {

            MethodInsnNode setScreen = new MethodInsnNode(
                Opcodes.INVOKESTATIC, TypeIndex.CompatHandles.getInternalName(), "setGadgetsScreen", "(Ljava/lang/Object;)V", false
            );
            List<MethodNode> methods = targetClass.methods;
            for(int i = 0; i < methods.size(); i++) {
                MethodNode method = methods.get(i);
                if(method.name.equals("prepare")) {
                    InsnList instructions = method.instructions;
                    addPrepareHeadMethod(targetClass, instructions);
                    addSetScreen(targetClass, instructions);
                }
                if(method.name.equals("close")) {
                    addSetScreen(targetClass, method.instructions);
                }
                if(method.name.equals("updateDiagramData")) {
                    InsnList instructions = method.instructions;
                    InsnList nodes = new InsnList();
                    for(AbstractInsnNode node : instructions.toArray()) {
                        if(node.getOpcode() == Opcodes.RETURN) {
                            nodes.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            nodes.add(new FieldInsnNode(Opcodes.GETFIELD, targetClass.name, "screen", "Ljava/lang/Object;"));
                            nodes.add(new MethodInsnNode(Opcodes.INVOKESTATIC, TypeIndex.CompatHandles.getInternalName(), "onPackageUpdate", "(Ljava/lang/Object;)V", false));
                            instructions.insertBefore(node, nodes);
                        }
                    }

                }
            }
        }
    }

    private void addSetScreen(ClassNode targetClass, InsnList instructions) {

        MethodInsnNode setGadgetsScreen = new MethodInsnNode(
            Opcodes.INVOKESTATIC, TypeIndex.CompatHandles.getInternalName(), "setGadgetsScreen", "(Ljava/lang/Object;)V", false
        );
        for(AbstractInsnNode node : instructions.toArray()) {
            if(!isScreenField(targetClass, node)) continue;

            //com.zelaux.betterdiagram.compat.CompatModule.gadgetsScreen
            instructions.insertBefore(node, new InsnNode(Opcodes.DUP));
            instructions.insertBefore(node, setGadgetsScreen);
        }
    }

    private boolean isScreenField(ClassNode targetClass, AbstractInsnNode node) {
        if(!(node instanceof FieldInsnNode field) || field.getOpcode() != Opcodes.PUTFIELD) return false;
        if(!field.name.equals("screen")) return false;
        if(!field.owner.equals(targetClass.name)) return false;
        return true;
    }

    private void addPrepareHeadMethod(ClassNode targetClass, InsnList instructions) {
        String prepareHead = "prepare_head";
        instructions.insertBefore(instructions.getFirst(), new MethodInsnNode(
            Opcodes.INVOKEVIRTUAL, targetClass.name, prepareHead, "()V", false
        ));
        instructions.insertBefore(instructions.getFirst(), new VarInsnNode(Opcodes.ALOAD, 0));
        MethodNode e = new MethodNode(Opcodes.ACC_PUBLIC, prepareHead, "()V", null, null);
        e.instructions.add(new InsnNode(Opcodes.RETURN));
        targetClass.methods.add(e);
    }

    static @Nullable MethodNode findMethod(ClassNode targetClass, Method generatedMethod, Method searchMethod) {
        MethodNode found = null;
        for(MethodNode method : targetClass.methods) {
            if(matches(method, generatedMethod)) {
                found = null;
                break;
            }
            if(matches(method, searchMethod)) found = method;

        }
        return found;
    }

    public static boolean matches(MethodNode method, Method searchMethod1) {
        return method.name.equals(searchMethod1.getName()) && method.desc.equals(searchMethod1.getDescriptor());
    }

    private static void addMethod(ClassNode targetClass, MethodNode method) {
        MethodNode node = new MethodNode(Opcodes.ACC_PUBLIC, generatedMethod.getName(), generatedMethod.getDescriptor(), null, null);

        method.accept(new MyMethodVisitor(node, method));
        targetClass.methods.add(node);

    }

    private static class MyMethodVisitor extends GeneratorAdapter {
        private final MethodNode node;
        private final InsnList instructions;
        private int carriageIndex = -1;

        public MyMethodVisitor(MethodNode node, MethodNode original) {
            super(Opcodes.ASM9, node, node.access, node.name, node.desc);
            this.node = node;
            instructions = node.instructions;
            for(LocalVariableNode localVariable : original.localVariables) {
                if(localVariable.name.equals("carriage")) {
                    this.carriageIndex = localVariable.index;
                }
            }

        }

        @Override
        public void visitInsn(int opcode) {
            if(opcode == Opcodes.IRETURN) {
                AbstractInsnNode last = instructions.getLast();
                boolean wasFalse = last.getOpcode() == Opcodes.ICONST_0;
                boolean wasTrue = last.getOpcode() == Opcodes.ICONST_1;
                if(wasTrue || wasFalse) {
                    instructions.remove(last);
                    if(wasFalse) {
                        pushNull();
                    } else {
                        loadCarriage();
                    }
                    returnValue();
                    return;
                }

                Label isTrue = new Label(), goAfter = new Label();

                ifICmp(Opcodes.IFGT, isTrue);
                pushNull();
                goTo(goAfter);

                visitLabel(isTrue);
                loadCarriage();

                visitLabel(goAfter);
                returnValue();
                return;
            }
            super.visitInsn(opcode);
        }

        private void loadCarriage() {
            loadLocal(carriageIndex + firstLocal);
        }

        private void pushNull() {
            push((String) null);
        }
    }
}
