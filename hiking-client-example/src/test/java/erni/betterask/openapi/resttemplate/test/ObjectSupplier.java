package erni.betterask.openapi.resttemplate.test;

import io.swagger.v3.oas.models.media.Schema;
import lombok.SneakyThrows;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;
import net.jqwik.api.Builders;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ObjectSupplier<U> implements ArbitrarySupplier<U> {

    private final ValidArbitrarySupplier<?> validArbitrarySupplier;
    private final Schema<?> schema;
    private final Class<U> modelClass;
    private final Class<?> builderClass;
    private final Supplier<Object> builderSupplier;
    private final Function<Object, U> buildFunction;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public ObjectSupplier(ValidArbitrarySupplier<?> validArbitrarySupplier, Schema<?> schema) {
        this.validArbitrarySupplier = validArbitrarySupplier;
        this.schema = schema;
        this.modelClass = (Class<U>) Class.forName(validArbitrarySupplier.modelClass.getPackageName() + "." + schema.getTitle());
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

        // TODO: avoid hardcoding and generalize
        //       --> how to detect abstract types and all of their concrete "child" types ?
        if (schema.getTitle().equals("RouteSegment")) {
            return Arbitraries.oneOf(
                (Arbitrary<U>) new ObjectSupplier<>(validArbitrarySupplier, validArbitrarySupplier.getSchema("HikingSegment")).get(),
                (Arbitrary<U>) new ObjectSupplier<>(validArbitrarySupplier, validArbitrarySupplier.getSchema("MountainRideSegment")).get()
            );
        }
        if (schema.getTitle().equals("TourSegment")) {
            return Arbitraries.oneOf(
                    // FIXME: grundlegendes API Design- bzw. Implementation-Problem:
                    //        "public class HikingSegment extends RouteSegment", da mit Java Klassen keine Mehrfachvererbung möglich
                    //        --> "Could not resolve type id 'HIKING_SEGMENT' as a subtype of `erni.betterask.hiking.client.model.TourSegment`"
//                (Arbitrary<U>) new ObjectSupplier<>(validArbitrarySupplier, validArbitrarySupplier.getSchema("HikingSegment")).get(),
                (Arbitrary<U>) new ObjectSupplier<>(validArbitrarySupplier, validArbitrarySupplier.getSchema("ClimbingSegment")).get()
            );
        }

        Builders.BuilderCombinator<Object> builderCombinator = Builders.withBuilder(builderSupplier);

        builderCombinator = getPropertyStream().reduce(
                builderCombinator,
                (bc, entry) -> {
                    String propertyName = entry.getKey();
                    Schema<?> propertySchema = entry.getValue();
                    BiFunction<Object, Object, Object> builderMutator = getBuilderMutator(propertyName, propertySchema);
                    Arbitrary<?> propertyArbitrary = getPropertyArbitrary(propertyName, propertySchema);
                    return bc.use(propertyArbitrary).in(builderMutator);
                },
                (bc1, bc2) -> {
                    throw new UnsupportedOperationException();
                }
        );

//            return Builders.withBuilder(MountainRideSegment::builder)
//                    .use(((ArbitrarySupplier<String>) null).get()).in(MountainRideSegment.Builder::from)
////                    .use(/* 1. Property */ map.get("from")).in(MountainRideSegment.Builder::from)

        return builderCombinator.build(buildFunction);
    }

    private Stream<Map.Entry<String, Schema>> getPropertyStream() {

        Stream<Schema<?>> schemaStream = Stream.of(
                        Stream.of(schema),
                        Optional.ofNullable(schema.getAllOf()).stream()
                                .flatMap(Collection::stream)
                                .map(Schema::get$ref)
                                .map(validArbitrarySupplier::getRef)
                )
                .flatMap(Function.identity());

        return schemaStream
                .map(Schema::getProperties)
                .filter(Objects::nonNull)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .filter(e -> !"discriminator".equals(e.getKey()));
    }

    @SneakyThrows
    private Arbitrary<?> getPropertyArbitrary(String propertyName, Schema<?> propertySchema) {
        return switch (propertySchema.getType()) {
            case "object" -> {
                yield new ObjectSupplier<>(validArbitrarySupplier, propertySchema).get();
            }
            case "array" -> {
                var itemSchema = validArbitrarySupplier.getRef(propertySchema.getItems().get$ref());
                var itemArbitrary = getPropertyArbitrary(propertyName, itemSchema);
                var supplier = Objects.requireNonNullElse(propertySchema.getUniqueItems(), false) ?
                        new ArrayAsSetSupplier(propertySchema, itemArbitrary) :
                        new ArrayAsListSupplier(propertySchema, itemArbitrary);
                yield supplier.get();
            }
            case String s when propertySchema.getEnum() != null -> {
                String enumName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1) + "Enum";
                yield Arbitraries.defaultFor(Class.forName(modelClass.getName() + "$" + enumName));
            }
            case "string" -> new StringSupplier(propertySchema).get();
            case "integer" -> new IntegerSupplier(propertySchema).get();
            case null, default -> {
                if (propertySchema.get$ref() != null) {
                    System.out.println("$ref: " + propertySchema.get$ref());
                    yield new ObjectSupplier<>(validArbitrarySupplier, validArbitrarySupplier.getRef(propertySchema.get$ref())).get();
                }
                throw new IllegalStateException("Unexpected value: " + propertySchema.getType());
            }
        };
    }

    private BiFunction<Object, Object, Object> getBuilderMutator(String propertyName, Schema<?> propertySchema) {
        return switch (propertySchema.getType()) {
            case "object" -> getObjectWith(propertyName, propertySchema.getTitle());
            case "array" -> getWith(propertyName, List.class);
            case String s when propertySchema.getEnum() != null -> {
                String enumName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1) + "Enum";
                yield getObjectWith(propertyName, schema.getTitle() + "$" + enumName);
            }
            case "string" -> getWith(propertyName, String.class);
            case "integer" -> getWith(propertyName, Integer.class);
            case null, default -> {
                if (propertySchema.get$ref() != null) {
                    System.out.println("$ref: " + propertySchema.get$ref());
                    yield getObjectWith(propertyName, validArbitrarySupplier.getRef(propertySchema.get$ref()).getTitle());
                }
                throw new IllegalStateException("Unexpected value: " + propertySchema.getType());
            }
        };
    }
}
