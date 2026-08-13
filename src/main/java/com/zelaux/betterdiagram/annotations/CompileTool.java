package com.zelaux.betterdiagram.annotations;

import org.objectweb.asm.Type;

public class CompileTool {
    /**
     * Will be substituted in compile time
     * */
    public static String classToString(Class<?> type){
        return type.getCanonicalName();
    }
    /**
     * Will be substituted in compile time
     * */
    public static Type bakeType(Class<?> type){
        return Type.getType(type);
    }
}
