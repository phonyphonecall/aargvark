package com.hendwick.aargvark.annotation;

/**
 * Created by scott on 1/27/15.
 */
public @interface Aargvark {
    boolean strictOptions() default false;
    boolean extrasAreFatal() default false;
}
