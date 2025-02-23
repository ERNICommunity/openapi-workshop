package erni.betterask.openapi.resttemplate.test;

import io.swagger.v3.oas.models.media.Schema;
import lombok.SneakyThrows;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;
import net.jqwik.api.Builders;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectSupplier<U> implements ArbitrarySupplier<U> {

    private final OpenApiArbitrarySupplier<?> openApiArbitrarySupplier;
    private final Schema<?> schema;
    private final Class<U> modelClass;
    private final Class<?> builderClass;
    private final Supplier<Object> builderSupplier;
    private final Function<Object, U> buildFunction;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public ObjectSupplier(OpenApiArbitrarySupplier<?> openApiArbitrarySupplier, Schema<?> schema) {
        this.openApiArbitrarySupplier = openApiArbitrarySupplier;
        this.schema = schema;
        this.modelClass = (Class<U>) Class.forName(openApiArbitrarySupplier.modelClass.getPackageName() + "." + schema.getTitle());
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
            public U apply(Object builder) {
                return (U) buildMethod.invoke(builder);
            }

            final Method buildMethod = builderClass.getMethod("build");
        };
    }

    @SneakyThrows
    private BiFunction<Object, Object, Object> getObjectWith(String propertyName, String typeName) {
        return new BiFunction<>() {
            @Override
            @SneakyThrows
            public Object apply(Object builder, Object propertyValue) {
                return withFunction.invoke(builder, propertyValue);
            }

            final Class<?> objectClass = Class.forName(modelClass.getPackageName() + "." + typeName);
            final Method withFunction = builderClass.getMethod(propertyName, objectClass);
        };
    }

    @SneakyThrows
    private BiFunction<Object, Object, Object> getWith(String propertyName, Class<?> propertyType) {
        return new BiFunction<>() {
            @Override
            @SneakyThrows
            public Object apply(Object builder, Object propertyValue) {
                return withFunction.invoke(builder, propertyValue);
            }

            final Method withFunction = builderClass.getMethod(propertyName, propertyType);
        };
    }

    @Override
    public Arbitrary<U> get() {

        Builders.BuilderCombinator<Object> builderCombinator = Builders.withBuilder(builderSupplier);

        schema.getProperties().forEach((propertyName, propertySchema) -> {
            BiFunction<Object, Object, Object> builderMutator = getBuilderMutator(propertyName, propertySchema);
            Arbitrary<?> propertyArbitrary = getPropertyArbitrary(propertySchema);
            builderCombinator.use(propertyArbitrary).in(builderMutator);
        });

//            return Builders.withBuilder(MountainRideSegment::builder)
//                    .use(((ArbitrarySupplier<String>) null).get()).in(MountainRideSegment.Builder::from)
////                    .use(/* 1. Property */ map.get("from")).in(MountainRideSegment.Builder::from)

        return builderCombinator.build(buildFunction);
    }

    @SneakyThrows
    private Arbitrary<?> getPropertyArbitrary(Schema<?> propertySchema) {
        return switch (propertySchema.getType()) {
            case "object" -> {
                yield new ObjectSupplier<>(openApiArbitrarySupplier, propertySchema).get();
            }
            case "array" -> {
                var itemSchema = openApiArbitrarySupplier.getRef(propertySchema.getItems().get$ref());
                var itemArbitrary = getPropertyArbitrary(itemSchema);
                yield new ArraySupplier(itemSchema, itemArbitrary).get();
            }
            // TODO: better detection of discriminator case
            case String s when propertySchema.getEnum() != null -> {
                yield Arbitraries.defaultFor(Class.forName(modelClass.getName() + "$DiscriminatorEnum"));
            }
            case "string" -> new StringSupplier(propertySchema).get();
            case "integer" -> new IntegerSupplier(propertySchema).get();
            case null, default -> {
                if (propertySchema.get$ref() != null) {
                    System.out.println("$ref: " + propertySchema.get$ref());
                    yield new ObjectSupplier<>(openApiArbitrarySupplier, openApiArbitrarySupplier.getRef(propertySchema.get$ref())).get();
                }
                throw new IllegalStateException("Unexpected value: " + propertySchema.getType());
            }
        };
    }

    private BiFunction<Object, Object, Object> getBuilderMutator(String propertyName, Schema<?> propertySchema) {
        return switch (propertySchema.getType()) {
            case "object" -> getObjectWith(propertyName, propertySchema.getTitle());
            case "array" -> getWith(propertyName, List.class);
            case String s when schema.getDiscriminator() != null && propertyName.equals(schema.getDiscriminator().getPropertyName()) -> {
                yield getObjectWith(propertyName, schema.getTitle() + "$DiscriminatorEnum");
            }
            case "string" -> getWith(propertyName, String.class);
            case "integer" -> getWith(propertyName, Integer.class);
            case null, default -> {
                if (propertySchema.get$ref() != null) {
                    System.out.println("$ref: " + propertySchema.get$ref());
                    yield getObjectWith(propertyName, openApiArbitrarySupplier.getRef(propertySchema.get$ref()).getTitle());
                }
                throw new IllegalStateException("Unexpected value: " + propertySchema.getType());
            }
        };
    }
}
