package processor.compileTool;

import asmlib.transform.AbstractClassFileTransformer;
import asmlib.transform.LazyByteCodeProvider;
import asmlib.transform.TransformationWriter;
import asmlib.transform.context.TransformationContext;
import asmlib.util.ClassFileMetaData;
import com.zelaux.bcd.asm_commons.BoundMethod;
import com.zelaux.bcd.asm_commons.MethodUtil;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.*;

public class LambdaToBoundMethod extends AbstractClassFileTransformer {

    public static final BoundMethod BoundMethod_make = BoundMethod.make(
        Type.getType(BoundMethod.class),
        "make",
        Type.getType(Type.class),

        Type.getType(Type.class),
        Type.getType(String.class),
        Type.getType(Type.class),
        Type.getType(Type[].class)
    );
    public static final BoundMethod type_getType = BoundMethod.make(
        Type.getType(Type.class),
        "getType",
        Type.getType(Type.class),
        Type.getType(String.class)
    );
    public static final BoundMethod sampleMethod = new BoundMethod(
        CompileTool.COMPILE_TOOL_GET_METHOD_TYPE,
        new Method(
            "methodOf", Type.getType(Type.class), new Type[]{Type.getType(Class.class)}
        )
    );
    private static final Handle LAMBDA_MAKER_HANDLE_SAMPLE = new Handle(
        Opcodes.H_INVOKESTATIC,
        "java/lang/invoke/LambdaMetafactory",
        "metafactory",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
        false
    );
    private final Type TYPE;

    public LambdaToBoundMethod() {
        super(1);
        TYPE = Type.getType(Type.class);
    }

    @Override
    public boolean shouldAnalyze(String className, TransformationContext context) {
        return true;
    }


    @Override
    public @Nullable TransformationWriter analyze(String className, LazyByteCodeProvider byteCodeProvider, TransformationContext context) {
        ClassFileMetaData metaData = new ClassFileMetaData(byteCodeProvider.getCloned());
        if(!metaData.usesMethod(sampleMethod.getOwner(), sampleMethod.getName())) {
            return null;
        }


        return TransformationWriter.nodeTransformer(classNode -> {
            boolean any = false;
            for(MethodNode method : classNode.methods) {
                any |= MethodUtil.processInstruction(method.instructions,
                    (instructions, insnArr, location, methodInsn, nil) -> {
                        if(!methodInsn.owner.equals(sampleMethod.owner().getInternalName()) || !methodInsn.name.equals(sampleMethod.getName())) {
                            return false;
                        }
                        if(!(insnArr[location - 1] instanceof InvokeDynamicInsnNode dynamicInsnNode)) return false;
                        if(!dynamicInsnNode.bsm.equals(LAMBDA_MAKER_HANDLE_SAMPLE)) {
                            return false;
                        }
                        Handle targetHandle = null;
                        for(Object bsmArg : dynamicInsnNode.bsmArgs) {
                            if(bsmArg instanceof Handle h) {
                                targetHandle = h;
                                break;
                            }
                        }
                        if(targetHandle == null) return false;

                        Handle println = new Handle(
                            Opcodes.H_INVOKEVIRTUAL,
                            "java/io/PrintStream",
                            "println",
                            "(Ljava/lang/String;)V",
                            false
                        );


                        InsnList nodes = new InsnList();
                        for(Type type : Type.getArgumentTypes(dynamicInsnNode.desc)) {
                            nodes.add(new InsnNode(Opcodes.POP + type.getSize() - 1));
                        }

                        typeGetInsn(nodes,Type.getObjectType(targetHandle.getOwner()));

                        nodes.add(new LdcInsnNode(targetHandle.getName()));

                        Type desc = Type.getMethodType(targetHandle.getDesc());
                        typeGetInsn(nodes, desc.getReturnType());

                        Type[] args = desc.getArgumentTypes();
                        nodes.add(push(args.length));
                        nodes.add(new TypeInsnNode(Opcodes.ANEWARRAY, TYPE.getInternalName()));
                        for(int argI = 0; argI < args.length; argI++) {
                            nodes.add(new InsnNode(Opcodes.DUP));
                            nodes.add(push(argI));
                            typeGetInsn(nodes, args[argI]);
                            nodes.add(new InsnNode(Opcodes.AASTORE));
                        }
                        nodes.add(BoundMethod_make.invokeInsn(Opcodes.INVOKESTATIC));

                        instructions.insertBefore(dynamicInsnNode, nodes);
                        instructions.remove(dynamicInsnNode);
                        instructions.remove(methodInsn);


                        //instructions.remove(methodInsn);
                        return true;
                    });
            }
            return any ? classNode : null;
        });
    }

    public static AbstractInsnNode push(final int value) {
        if(value >= -1 && value <= 5) {
            return new InsnNode(Opcodes.ICONST_0 + value);
        } else if(value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            return new IntInsnNode(Opcodes.BIPUSH, value);
        } else if(value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            return new IntInsnNode(Opcodes.SIPUSH, value);
        } else {
            return new LdcInsnNode(value);
        }
    }

    private void typeGetInsn(InsnList nodes, Type type) {
        nodes.add(new LdcInsnNode(type.getDescriptor()));
        nodes.add(type_getType.invokeInsn(Opcodes.INVOKESTATIC));

    }

}
