package com.zelaux.betterdiagram.annotations;

import org.intellij.lang.annotations.Pattern;

@JavaClassName
@Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(/\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)*(\\$\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)*")
public @interface JavaClassOrInternalName {
}
