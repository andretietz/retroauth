package eu.unicate.retroauth.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This Annotation determins that the request which is annotated is an authenticated one
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Authenticated {
}
