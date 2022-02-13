package com.dicontainer.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//On method
@Retention(RetentionPolicy.RUNTIME)
public @interface ToInject {
    //The class that will receive the dependency, will tell which constructor it is going to be injected.
}
