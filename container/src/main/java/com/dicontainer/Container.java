package com.dicontainer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.reflections.Reflections;

import com.dicontainer.annotations.Dependency;
import com.dicontainer.annotations.ToInject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Getter;
import lombok.Setter;

public class Container {

    private static Container INSTANCE;
    public static final String CONFIG_FILE = "dicontainer.config";
    private static final Gson GSON = new Gson();
    private static final GsonBuilder GSON_BUILDER = new GsonBuilder();
    // Interface -> dependency
    private Map<Class, DependencyToInject> dependenciesToInjectByInterface;
    private Map<Class, Constructor> constructorsToInject;
    private Map<Class, Constructor> dependenciesConstructor;
    private Reflections reflections = new Reflections();
    private ConfigHolder configHolder;

    //procurar classe, procurar metodo com @toinject annotation
    static {
        GSON_BUILDER.setPrettyPrinting();
        //public <T> T fromJson(String json, Class<T> classOfT)
    }

    private class ConfigHolder {
        //interface -> class
        private Map<String, String> config;
    }

    @Getter @Setter
    private class DependencyToInject <T> {
        private Constructor<T> constructorToCall; //Constructor of the dependency to call.
        private Class<T> dependencyClass; //Class to be instantiated.
        private String annotatedValue; //Value of the Dependency annotation.

        public DependencyToInject(Constructor<T> constructor, Class<T> clazz, String annotation) {
            constructorToCall = constructor;
            dependencyClass = clazz;
            annotatedValue = annotation;
        }

        public T instantiate() {
            T instance = null;
            try {
                instance = constructorToCall.newInstance();
            } catch(Exception e) {
                
            }
            return instance;
        }
    }

    private Container() {
        //loadConfig();
        loadDependencies();
        loadConstructorsToInject();
    }

    private Constructor<?> getConstructorToInstantiateDependency(Class<?> dependency) {
        for(Constructor<?> constructor : dependency.getConstructors()) {
            if(constructor.getDeclaredAnnotation(ToInject.class) != null)
                return constructor;
        }
        return null;
    }

    private <V> void loadDependencies() {
        for(Class<?> clazz : reflections.getTypesAnnotatedWith(Dependency.class)) {
            Constructor<?> constructorToCall = getConstructorToInstantiateDependency(clazz);
            String annotatedValue = clazz.getDeclaredAnnotation(Dependency.class).to();
            Class referredInterface;
            try {
                referredInterface = Class.forName(annotatedValue);
            } catch(Exception e) {
                continue;
            }
            test(constructorToCall, clazz, annotatedValue);
            dependenciesToInjectByInterface.put(referredInterface, new DependencyToInject(constructorToCall, clazz, annotatedValue));
        }
    }

    // Test if it can work
    private <V> void test(Constructor<V> a, Class<V> b, String c) {
        DependencyToInject a = new DependencyToInject<V>(a, b, c);
    }

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

    private synchronized static Container getInstance() {
        if(INSTANCE == null)
            INSTANCE = new Container();
        return INSTANCE;
    }

    public Object getNewInstanceFor(Class dependencyReceiver) throws Exception {
        if(dependenciesToInjectByInterface.containsKey(dependencyReceiver)) {
            DependencyToInject toInject = dependenciesToInjectByInterface.get(dependencyReceiver);
            return toInject.getConstructorToCall().invoke(dependenciesToInjectByInterface.get(dependencyReceiver), (Object[]) null);
        }
        return null;
    }

    public static Object getInstanceFor(Class classToInstatiate) throws Exception
    {
        return Container.getInstance().getNewInstanceFor(classToInstatiate);
    }
}