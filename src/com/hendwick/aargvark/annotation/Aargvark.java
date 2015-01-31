package com.hendwick.aargvark.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by scott on 1/27/15.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Aargvark {
    boolean strictOptions() default false;
    boolean extrasAreFatal() default false;
    boolean enableHelp() default false;
}
