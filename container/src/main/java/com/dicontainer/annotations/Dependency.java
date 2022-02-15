package com.dicontainer.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that represents the dependency and stores the respective
 * class that it will be injecting.
 * 
 * @author Lucas A S Almeida
 *
 * @param to the class canonical name that the dependency will be injected
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dependency {
    public String to();
}
