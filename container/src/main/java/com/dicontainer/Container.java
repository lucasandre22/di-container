package com.dicontainer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.dicontainer.annotations.Dependency;
import com.dicontainer.annotations.ToInject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Lucas A S Almeida
 */
public class Container {

    private static Container INSTANCE;
    public static final String CONFIG_FILE = "dicontainer.config";
    private static final Gson GSON = new Gson();
    private static final GsonBuilder GSON_BUILDER = new GsonBuilder();
    // Interface -> dependency
    private Map<Class<?>, DependencyToInject> dependenciesToInjectByInterface = 
            new HashMap<Class<?>, DependencyToInject>();

    private Map<Class<?>, Constructor<?>> constructorsToInject = 
            new HashMap<Class<?>, Constructor<?>>();

    private Reflections reflections = //new Reflections("all");
    new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage("all"))
            .setScanners(new FieldAnnotationsScanner(), new MethodParameterScanner()));
    private ConfigHolder configHolder;

    static {
        GSON_BUILDER.setPrettyPrinting();
        //public <T> T fromJson(String json, Class<T> classOfT)
    }

    private class ConfigHolder {
        //interface -> class
        private Map<String, String> config;
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
                instance = constructorToCall.newInstance();
            } catch(Exception e) {
                
            }
            return instance;
        }
        
        public Class<?> getDependencyClass() {
            return this.dependencyClass;
        }
    }

    private Container() {
        //loadConfig();
        loadDependencies();
        loadConstructorsToInject();
    }

    public synchronized static Container getInstance() {
        if(INSTANCE == null)
            INSTANCE = new Container();
        return INSTANCE;
    }

    private Constructor<?> getConstructorToInstantiateDependency(Class<?> dependency) {
        for(Constructor<?> constructor : dependency.getConstructors()) {
            if(constructor.getDeclaredAnnotation(ToInject.class) != null)
                return constructor;
        }
        return null;
    }

    private <V> void loadDependencies() {
        for(Class<?> dependencyClass : reflections.getTypesAnnotatedWith(Dependency.class)) {
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

    // Test if it can work
    /*private <V> void test(Constructor<V> a, Class<V> b, String c) {
        DependencyToInject a = new DependencyToInject<V>(a, b, c);
    }*/

    private void loadConstructorsToInject() {
        for(Constructor<?> constructor : reflections.getConstructorsAnnotatedWith(ToInject.class)) {
            constructorsToInject.put(constructor.getClass(), constructor);
        }
    }

    private void loadConfig() {
        String fileContent = null;
        try {
            fileContent = getFileContent(CONFIG_FILE);
        } catch(IOException e) {
            //throw e;
        }
        configHolder = GSON.fromJson(fileContent, ConfigHolder.class);
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
     * @return 
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
                constructorToInject.newInstance();
            }
        }
        if(i == 0)
            return constructorToInject.newInstance((Object[]) null);
        return constructorToInject.newInstance(parametersArray);
    }

    public Object getNewInstanceFor(Class<?> dependencyReceiver) throws Exception {
        if(constructorsToInject.containsKey(dependencyReceiver)) {
            return getDependencyInstance(dependencyReceiver);
            /*DependencyToInject toInject = dependenciesToInjectByInterface.get(dependencyReceiver);
            return toInject.instantiate();*/
        }
        return null;
    }
}