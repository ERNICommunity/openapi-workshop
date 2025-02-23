package erni.betterask.openapi.resttemplate.test.datatype;

import erni.betterask.openapi.resttemplate.test.BuilderHelper;
import erni.betterask.openapi.resttemplate.test.OpenApi;
import io.swagger.v3.oas.models.media.Schema;
import lombok.SneakyThrows;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;
import net.jqwik.api.Builders;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

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

        Set<Arbitrary<T>> allSubtypeArbitraries = new HashSet<>();

        // process all strict subtypes, excluding self
        if (openApi.getDiscriminatorProperty(schema).isPresent()) {
            openApi.getSubTypes(schema).stream()
                    .filter(Predicate.not(schema::equals))
                    // FIXME: grundlegendes API Design- bzw. Implementations-Problem
                    //        -> Serialisierung-Identity Test bricht, wenn man den Filter raus nimmt,
                    //           weil beim "TourSegment" Discriminator Mapping, Zeile 149 im api.yml, "HikingSegment"
                    //           zugelassen ist, obwohl "HikingSegment" kein "TourSegment" sein _kann_ weil es schon, siehe
                    //           Zeile 183, schon "allOf" "RouteSegment" hat und somit ein "RouteSegment" ist,
                    //           und Java keine Mehrfach-Klassenvererbung zulässt.
                    //        -> Der Fehler ist:
                    //              com.fasterxml.jackson.databind.exc.InvalidTypeIdException:
                    //              Could not resolve type id 'HIKING_SEGMENT' as a subtype of `erni.betterask.hiking.client.model.TourSegment`:
                    //              Class `erni.betterask.hiking.client.model.HikingSegment` not subtype of `erni.betterask.hiking.client.model.TourSegment`
                    .filter(sts -> !(schema.getTitle().equals("TourSegment") && sts.getTitle().equals("HikingSegment")))
                    .map(subtypeSchema -> new ObjectSupplier<T>(openApi, subtypeSchema.getTitle()).get())
                    .forEach(allSubtypeArbitraries::add);
        }

        // process self, if simple type or included in discriminator mapping
        if (openApi.getDiscriminatorProperty(schema).isEmpty()
                || openApi.getDiscriminatorProperty(schema).isPresent() && openApi.getSubTypes(schema).contains(schema)) {

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

            allSubtypeArbitraries.add(builderCombinator.build(builderHelper.getBuildFunction()));
        }

        return Arbitraries.oneOf(allSubtypeArbitraries);
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
