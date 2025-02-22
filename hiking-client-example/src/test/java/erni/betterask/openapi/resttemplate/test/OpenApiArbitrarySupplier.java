package erni.betterask.openapi.resttemplate.test;

import io.swagger.v3.oas.models.media.Schema;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;

import java.util.Map;

public class OpenApiArbitrarySupplier<T> implements ArbitrarySupplier<T> {

    private final Model model;

    public OpenApiArbitrarySupplier(String fname, String model) {
        this.model = new Model(fname, model);
    }

    @Override
    public Arbitrary<T> get() {
        return new Builder().build().get();
    }

    public class Builder {

        ArbitrarySupplier<T> build() {

            for (Map.Entry<String, Schema> entry : model.getSchema().getProperties().entrySet()) {
                final String name = entry.getKey();
                final Schema<?> schema = entry.getValue();

                var supplier = switch (schema.getType()) {
                    case "object" -> null;
                    case "array" -> null;
                    case "string" -> null;
                    case "integer" -> new IntegerSupplier(schema);
                    default -> throw new IllegalStateException("Unexpected value: " + schema.getType());
                };
            }

            return null;
        }
    }

    public static class ObjectSupplier<T> implements ArbitrarySupplier<T> {

        private final Schema<?> schema;

        public ObjectSupplier(Schema<?> schema) {
            this.schema = schema;
        }

        @Override
        public Arbitrary<T> get() {
            return null;
//            return Builders.withBuilder(MountainRideSegment::builder)
//                    .use(((ArbitrarySupplier<String>) null).get()).in(MountainRideSegment.Builder::from)
////                    .use(/* 1. Property */ map.get("from")).in(MountainRideSegment.Builder::from)
//                    .build(MountainRideSegment.Builder::build);
        }

    }

    public static class StringSupplier implements ArbitrarySupplier<String> {

        private final Schema<?> schema;

        public StringSupplier(Schema<?> schema) {
            this.schema = schema;
        }

        @Override
        public Arbitrary<String> get() {
            return Arbitraries.strings()
                    .ofMinLength(this.schema.getMinLength())
                    .ofMaxLength(this.schema.getMaxLength())
                    .injectNull(this.schema.getNullable() ? 0.1 : 0.0);
        }

    }

    public static class IntegerSupplier implements ArbitrarySupplier<Integer> {

        private final Schema<?> schema;

        public IntegerSupplier(Schema<?> schema) {
            this.schema = schema;
        }

        @Override
        public Arbitrary<Integer> get() {
            return Arbitraries.integers()
                    .lessOrEqual(this.schema.getExclusiveMaximum() ? this.schema.getMaximum().intValue() - 1 : this.schema.getMaximum().intValue())
                    .greaterOrEqual(this.schema.getExclusiveMinimum() ? this.schema.getMinimum().intValue() + 1 : this.schema.getMinimum().intValue())
                    .injectNull(this.schema.getNullable() ? 0.1 : 0.0);
        }
    }
}
