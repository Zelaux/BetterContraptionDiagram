package com.zelaux.betterdiagram.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Like {@link DeleteInRelease} but usefull for mixins
 * */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface EmptyInRelease {
}
