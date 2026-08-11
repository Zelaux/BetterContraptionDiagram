package com.zelaux.betterdiagram.annotations;

public class CompileTool {
    /**
     * Will be substituted in compile time
     * */
    public static String classToString(Class<?> type){
        return type.getCanonicalName();
    }
}
