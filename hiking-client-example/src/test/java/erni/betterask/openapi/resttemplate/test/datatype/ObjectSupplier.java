package erni.betterask.openapi.resttemplate.test.datatype;

import erni.betterask.openapi.resttemplate.test.BuilderHelper;
import erni.betterask.openapi.resttemplate.test.OpenApiModel;
import erni.betterask.openapi.resttemplate.test.ValueGenerationMode;
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

    private final OpenApiModel openApiModel;
    private final ValueGenerationMode valueGenerationMode;
    private final Schema<?> schema;
    private final Class<?> modelClass;
    private final BuilderHelper<T> builderHelper;

    @SneakyThrows
    public ObjectSupplier(OpenApiModel openApiModel, String modelName, ValueGenerationMode valueGenerationMode) {
        this.openApiModel = openApiModel;
        this.valueGenerationMode = valueGenerationMode;
        this.schema = openApiModel.getSchema(modelName);
        this.modelClass = openApiModel.getModelClass(modelName);
        this.builderHelper = new BuilderHelper<>(this.modelClass);
    }

    class A {}
    class B extends A {}
//    class C extends B {}

    private boolean isBaseClass() {
        if (this.schema.getAllOf() != null) {

            if (schema.getAllOf().get(0).getName().equals(schema.getName())) {

            }
        }

        return true;
    }

    private boolean isValid() {
        return true;
    }


    /**
     * Prädikat Klassen
     *
     * isBaseClass
     * hasDiscriminator
     * isSubclass
     *
     * Fall, wenn isBaseClass() und hasDiscriminator()
     *  --> Model ist eine Route Klasse eines algebraischen Datentype ---> isInvalid Supplier instanziieren
     *
     * Valid Supplier: isBaseClass() und hasDiscriminator() ausfiltern
     *
     * sprich, supplier = isBaseClass() && hasDiscriminator() ? invalidSupplier : validSupplier
     *
     * -> und vice-versa beim invalid supplier
     *
     */

    @Override
    public Arbitrary<T> get() {

        Set<Arbitrary<T>> allSubtypeArbitraries = new HashSet<>();

        // process all strict subtypes, excluding self
        if (openApiModel.getDiscriminatorProperty(schema).isPresent() && valueGenerationMode != ValueGenerationMode.INVALID_INSTANCES) {
            openApiModel.getSubTypes(schema).stream()
                    .filter(Predicate.not(schema::equals))
                    .map(subtypeSchema -> new ObjectSupplier<T>(openApiModel, subtypeSchema.getTitle(), valueGenerationMode).get())
                    .forEach(allSubtypeArbitraries::add);
        }

        // process self, if simple type or included in discriminator mapping
        if (openApiModel.getDiscriminatorProperty(schema).isEmpty()
                || openApiModel.getDiscriminatorProperty(schema).isPresent()
                && (openApiModel.getSubTypes(schema).contains(schema) || valueGenerationMode == ValueGenerationMode.INVALID_INSTANCES)) {

            Builders.BuilderCombinator<Object> builderCombinator = Builders.withBuilder(builderHelper.getBuilderSupplier());

            builderCombinator = openApiModel.getAllProperties(schema).entrySet().stream().reduce(
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
                yield new ObjectSupplier<>(openApiModel, propertySchema.getTitle(), valueGenerationMode).get();
            }
            case "array" -> {
                var itemSchema = openApiModel.getRef(propertySchema.getItems().get$ref());
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
                    yield new ObjectSupplier<>(openApiModel, openApiModel.getRef(propertySchema.get$ref()).getTitle(), valueGenerationMode).get();
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
                    yield builderHelper.getObjectWith(propertyName, openApiModel.getRef(propertySchema.get$ref()).getTitle());
                }
                throw new IllegalStateException("Unexpected value: " + propertySchema.getType());
            }
        };
    }
}
