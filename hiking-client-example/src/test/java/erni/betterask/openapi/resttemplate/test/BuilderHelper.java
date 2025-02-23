package erni.betterask.openapi.resttemplate.test;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BuilderHelper<T> {

    private final Class<?> modelClass;
    private final Class<?> builderClass;
    @Getter
    private final Supplier<Object> builderSupplier;
    @Getter
    private final Function<Object, T> buildFunction;

    @SneakyThrows
    public BuilderHelper(Class<?> modelClass) {

        this.modelClass = modelClass;
        this.builderClass = Class.forName(modelClass.getName() + "$Builder");

        builderSupplier = new Supplier<>() {
            @Override
            @SneakyThrows
            public Object get() {
                return builderMethod.invoke(null);
            }

            final Method builderMethod = modelClass.getMethod("builder");
        };

        buildFunction = new Function<>() {
            @Override
            @SneakyThrows
            @SuppressWarnings("unchecked")
            public T apply(Object builder) {
                return (T) buildMethod.invoke(builder);
            }

            final Method buildMethod = builderClass.getMethod("build");
        };
    }

    @SneakyThrows
    public BiFunction<Object, Object, Object> getWith(String propertyName, Class<?> propertyType) {
        return new BiFunction<>() {
            @Override
            @SneakyThrows
            public Object apply(Object builder, Object propertyValue) {
                return withFunction.invoke(builder, propertyValue);
            }

            final Method withFunction = builderClass.getMethod(propertyName, propertyType);
        };
    }

    @SneakyThrows
    public BiFunction<Object, Object, Object> getObjectWith(String propertyName, String propertyTypeName) {
        final Class<?> propertyType = Class.forName(modelClass.getPackageName() + "." + propertyTypeName);
        return getWith(propertyName, propertyType);
    }
}
