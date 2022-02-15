package com.dicontainer.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that needs to be in the constructor that will 
 * be injected to receive the dependencies instances.
 * 
 * @author Lucas A S Almeida
 *
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToInject {

}
