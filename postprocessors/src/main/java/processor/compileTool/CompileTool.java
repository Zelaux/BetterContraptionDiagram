package processor.compileTool;

import org.objectweb.asm.Type;

public class CompileTool {
    public static final Type COMPILE_TOOL_TYPE = Type.getObjectType("com.zelaux.betterdiagram.annotations.CompileTool".replace('.', '/'));
    public static final Type COMPILE_TOOL_GET_METHOD_TYPE = Type.getObjectType("com.zelaux.betterdiagram.annotations.CompileTool$GetMethod".replace('.', '/'));
}
