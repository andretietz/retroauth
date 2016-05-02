package com.andretietz.retroauth;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This is the annotation you can use to add authentication handling onto your request.
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Authenticated {
    String[] value();
}
