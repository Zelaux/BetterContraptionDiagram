package com.zelaux.bcd.asm_commons;

public class ClassUtil {
    public static String simpleName(String name){
        return name.substring(Math.max(name.lastIndexOf('.'),name.lastIndexOf('/'))+1);
    }
}
