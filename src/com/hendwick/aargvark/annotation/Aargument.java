package com.hendwick.aargvark.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Martin Wickham on 1/27/2015.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Aargument {
    char shortName() default '\0';
    boolean require() default false;
    String usage() default "";
    boolean hidden() default false;
}
