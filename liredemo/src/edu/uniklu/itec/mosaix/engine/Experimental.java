package edu.uniklu.itec.mosaix.engine;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks implementations as HIGHLY experimental and
 * possibly subject to change.

 * @author Manuel Warum
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value={ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Experimental {}
