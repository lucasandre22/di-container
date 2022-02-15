package com.dicontainer.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation that needs to be in the constructor that will 
 * be injected to receive the dependencies instances.
 * 
 * @author Lucas A S Almeida
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ToInject {

}
