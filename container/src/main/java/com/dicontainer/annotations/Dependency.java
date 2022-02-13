package com.dicontainer.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//On class
@Retention(RetentionPolicy.RUNTIME)
public @interface Dependency {
    //The dependency will tell which class it is going to be injected.
    public String to();
}
