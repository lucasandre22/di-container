package com.dicontainer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import com.dicontainer.annotations.Dependency;
import com.dicontainer.annotations.ToInject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * A basic Dependency Injection Container built to understand the principle
 * 
 * @author Lucas A S Almeida
 */
public class Container {

    private static Container INSTANCE;
    private static final GsonBuilder GSON_BUILDER = new GsonBuilder();

    // Interface -> dependency
    private Map<Class<?>, DependencyToInject> dependenciesToInjectByInterface = 
            new HashMap<Class<?>, DependencyToInject>();

    private Map<Class<?>, Constructor<?>> constructorsToInject = 
            new HashMap<Class<?>, Constructor<?>>();

    private Reflections reflections =
            new Reflections(new ConfigurationBuilder()
            .forPackage("all")
            .setScanners(Scanners.ConstructorsAnnotated, Scanners.TypesAnnotated));

    static {
        GSON_BUILDER.setPrettyPrinting();
    }

    @Getter @Setter
    private class DependencyToInject {
        private Constructor<?> constructorToCall; //Constructor of the dependency to call.
        private Class<?> dependencyClass; //Class to be instantiated.
        private String annotatedValue; //Value of the Dependency annotation.

        public DependencyToInject(Constructor<?> constructor, Class<?> clazz, String annotation) {
            constructorToCall = constructor;
            dependencyClass = clazz;
            annotatedValue = annotation;
        }

        public Object instantiate() {
            Object instance = null;
            try {
                //TODO: fix to be able to inject dependencies into dependencies recursively
                instance = constructorToCall.newInstance();
            } catch(Exception e) {
                System.out.println(e);
            }
            return instance;
        }
    }

    private Container() {
        loadDependencies();
        loadConstructorsToInject();
    }

    public synchronized static Container getInstance() {
        if(INSTANCE == null)
            INSTANCE = new Container();
        return INSTANCE;
    }

    private Constructor<?> getConstructorToInstantiateDependency(Class<?> dependency) {
        //TODO: fix to get the exactly construtor to instantiate dependency
        for(Constructor<?> constructor : dependency.getConstructors()) {
                return constructor;
        }
        return null;
    }

    private <V> void loadDependencies() {
        for(Class<?> dependencyClass : reflections.get(
                Scanners.TypesAnnotated.with(Dependency.class).as(Class.class))) {
            Constructor<?> constructorToCall = getConstructorToInstantiateDependency(dependencyClass);
            String annotatedValue = dependencyClass.getDeclaredAnnotation(Dependency.class).to();
            Class<?> referredInterface;
            try {
                referredInterface = Class.forName(annotatedValue);
            } catch(Exception e) {
                continue;
            }
            dependenciesToInjectByInterface.put(referredInterface, 
                    new DependencyToInject(constructorToCall, dependencyClass, annotatedValue));
        }
    }

    private void loadConstructorsToInject() {
        for(Constructor<?> constructor : reflections.get(
                Scanners.ConstructorsAnnotated.with(ToInject.class).as(Constructor.class))) {
            constructorsToInject.put(constructor.getDeclaringClass(), constructor);
        }
    }

    private String getFileContent(String filename) throws IOException {
        Path filePath = Path.of(filename);
        return Files.readString(filePath);
    }

    private Constructor<?> getConstructorToInject(Class<?> clazz) {
        return constructorsToInject.get(clazz);
    }

    /**
     * Instantiate the dependency based on a dependencyReceiver class parameter.
     * It injects all dependencies that are marked by the @Dependency annotation
     * in the constructor registrated.
     * 
     * @param dependencyReceiver the class that will have the dependency injected
     * 
     * @return the dependency instantiated
     */
    private Object getDependencyInstance(Class<?> dependencyReceiver)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException
    {
        Constructor<?> constructorToInject = getConstructorToInject(dependencyReceiver);
        Object[] parametersArray = new Object[constructorToInject.getParameterCount()];
        int i = 0;
        for(Class<?> constructorParameter : constructorToInject.getParameterTypes())
        {
            DependencyToInject dependency = dependenciesToInjectByInterface.get(constructorParameter);
            if(dependency != null) {
                parametersArray[i++] = dependency.instantiate();
            }
        }
        if(i == 0)
            return constructorToInject.newInstance((Object[]) null);
        return constructorToInject.newInstance(parametersArray);
    }
    
    /**
     * Gets a new instance of the class that has a dependency to be injected.
     * 
     * @param dependencyReceiver the class that will have the dependency injected
     * 
     * @return the dependencyReceiver class instance
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T getNewInstanceFor(Class<T> dependencyReceiver) throws Exception {
        if(getInstance().constructorsToInject.containsKey(dependencyReceiver)) {
            return (T) getInstance().getDependencyInstance(dependencyReceiver);
        }
        return null;
    }
}