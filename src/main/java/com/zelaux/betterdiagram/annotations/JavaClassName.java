package com.zelaux.betterdiagram.annotations;

import org.intellij.lang.annotations.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(\\.\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)*(\\$\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)*")
public @interface JavaClassName {
}
