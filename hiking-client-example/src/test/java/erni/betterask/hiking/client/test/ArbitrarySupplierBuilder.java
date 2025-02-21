package erni.betterask.hiking.client.test;

import io.swagger.v3.oas.models.media.Schema;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;
import net.jqwik.api.Builders;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class ArbitrarySupplierBuilder<T> {

    private final Model model;

    public ArbitrarySupplierBuilder(@NotNull final String fname, @NotNull final String model) {
        this.model = new Model(fname, model);
    }

    public ArbitrarySupplierBuilder<T> toBuilder() {
        return new ArbitrarySupplierBuilder<>(this);
    }

    static class Supplier<U> {

        public ArbitrarySupplier<U> build() {

            for (Map.Entry<String, Schema> entry : this.model.getSchema().getProperties().entrySet()) {
                final String name = entry.getKey();
                final Schema<?> schema = entry.getValue();
                switch (schema.getType()) {

                }
            }

            return  null;

//            switch (schema.getType()) {
//
//            }
        }
    }

    public static class ObjectSupplierBuilder<U> implements ArbitrarySupplier<U> {

        private final Schema<?> schema;

        public ObjectSupplierBuilder(@NotNull final Schema<?> schema) {
            this.schema = schema;
        }

        @Override
        public Arbitrary<U> get() {
            return Builders.withBuilder().build();
        }

    }

    public static class StringSupplierBuilder implements ArbitrarySupplier<String> {

        private final Schema<?> schema;

        public StringSupplierBuilder(@NotNull final Schema<?> schema) {
            this.schema = schema;
        }

        public ArbitrarySupplier<String> build() {
            return this;
        }

        @Override
        public Arbitrary<String> get() {
            return Arbitraries.strings()
                    .ofMinLength(this.schema.getMinLength())
                    .ofMaxLength(this.schema.getMaxLength())
                    .injectNull(this.schema.getNullable() ? 0.1 : 0.0);
        }

    }

    public static class IntegerSupplierBuilder implements ArbitrarySupplier<Integer> {

        private final Schema<?> schema;

        public IntegerSupplierBuilder(@NotNull final Schema<?> schema) {
            this.schema = schema;
        }

        public ArbitrarySupplier<Integer> build() {
            return this;
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
