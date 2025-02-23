package erni.betterask.openapi.resttemplate.test.datatype;

import erni.betterask.openapi.resttemplate.test.BuilderHelper;
import erni.betterask.openapi.resttemplate.test.OpenApi;
import io.swagger.v3.oas.models.media.Schema;
import lombok.SneakyThrows;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;
import net.jqwik.api.Builders;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class ObjectSupplier<T> implements ArbitrarySupplier<T> {

    private final OpenApi openApi;
    private final Schema<?> schema;
    private final Class<?> modelClass;
    private final BuilderHelper<T> builderHelper;

    @SneakyThrows
    public ObjectSupplier(OpenApi openApi, String modelName) {
        this.openApi = openApi;
        this.schema = openApi.getSchema(modelName);
        this.modelClass = openApi.getModelClass(modelName);
        this.builderHelper = new BuilderHelper<>(this.modelClass);
    }

    @Override
    public Arbitrary<T> get() {

        // TODO: avoid hardcoding and generalize
        //       --> how to detect abstract types and all of their concrete "child" types ?
        if (schema.getTitle().equals("RouteSegment")) {
            return Arbitraries.oneOf(
                    (Arbitrary<T>) new ObjectSupplier<>(openApi, "HikingSegment").get(),
                    (Arbitrary<T>) new ObjectSupplier<>(openApi, "MountainRideSegment").get()
            );
        }
        if (schema.getTitle().equals("TourSegment")) {
            return Arbitraries.oneOf(
                    // FIXME: grundlegendes API Design- bzw. Implementation-Problem:
                    //        "public class HikingSegment extends RouteSegment", da mit Java Klassen keine Mehrfachvererbung möglich
                    //        --> "Could not resolve type id 'HIKING_SEGMENT' as a subtype of `erni.betterask.hiking.client.model.TourSegment`"
//                (Arbitrary<U>) new ObjectSupplier<>(openApi, "HikingSegment").get(),
                    (Arbitrary<T>) new ObjectSupplier<>(openApi, "ClimbingSegment").get()
            );
        }

        Builders.BuilderCombinator<Object> builderCombinator = Builders.withBuilder(builderHelper.getBuilderSupplier());

        builderCombinator = openApi.getAllProperties(schema).entrySet().stream().reduce(
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

        return builderCombinator.build(builderHelper.getBuildFunction());
    }

    @SneakyThrows
    private Arbitrary<?> getPropertyArbitrary(String propertyName, Schema<?> propertySchema) {
        return switch (propertySchema.getType()) {
            case "object" -> {
                yield new ObjectSupplier<>(openApi, propertySchema.getTitle()).get();
            }
            case "array" -> {
                var itemSchema = openApi.getRef(propertySchema.getItems().get$ref());
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
                    yield new ObjectSupplier<>(openApi, openApi.getRef(propertySchema.get$ref()).getTitle()).get();
                }
                throw new IllegalStateException("Unexpected value: " + propertySchema.getType());
            }
        };
    }

    private BiFunction<Object, Object, Object> getBuilderMutator(String propertyName, Schema<?> propertySchema) {
        return switch (propertySchema.getType()) {
            case "object" -> builderHelper.getObjectWith(propertyName, propertySchema.getTitle());
            case "array" -> builderHelper.getWith(propertyName, List.class);
            case String s when propertySchema.getEnum() != null -> {
                String enumName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1) + "Enum";
                yield builderHelper.getObjectWith(propertyName, schema.getTitle() + "$" + enumName);
            }
            case "string" -> builderHelper.getWith(propertyName, String.class);
            case "integer" -> builderHelper.getWith(propertyName, Integer.class);
            case null, default -> {
                if (propertySchema.get$ref() != null) {
                    System.out.println("$ref: " + propertySchema.get$ref());
                    yield builderHelper.getObjectWith(propertyName, openApi.getRef(propertySchema.get$ref()).getTitle());
                }
                throw new IllegalStateException("Unexpected value: " + propertySchema.getType());
            }
        };
    }
}
