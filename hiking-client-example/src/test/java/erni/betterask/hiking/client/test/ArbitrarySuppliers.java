package erni.betterask.hiking.client.test;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;
import org.apache.logging.log4j.util.Strings;

import javax.validation.constraints.NotNull;

public class ArbitrarySuppliers<T> {

    public ArbitrarySuppliers<T> create(@NotNull final String name, @NotNull final String type, )

    public static <U extends Integer> IntegerSupplier<U> integerSupplier() {
        return new IntegerSupplier<U>(0.0);
    }

    public static <U extends String> IntegerSupplier<U> string() {
        return new StringBuilder<U>();
    }

    public static class StringBuilder<U extends String> implements ArbitrarySupplier<String> {

    }

    public static class IntegerSupplier<U extends Integer> implements ArbitrarySupplier<Integer> {

        private Double nullability = 0.0;
        private Integer maximum = Integer.MAX_VALUE;
        private Integer minimum = Integer.MIN_VALUE;
        private Boolean exclusiveMinimum = false;
        private Boolean exclusiveMaximum = false;

        public IntegerSupplier(@NotNull final Double nullability) {
            this(nullability, Integer.MIN_VALUE, Integer.MAX_VALUE, false, false);
        }

        public IntegerSupplier(@NotNull final Double nullability, @NotNull final Integer minimum, @NotNull final Integer maximum, @NotNull final Boolean exclusiveMinimum, @NotNull final Boolean exclusiveMaximum) {
            this.minimum(minimum).maximum(maximum).exclusiveMinimum(exclusiveMinimum).exclusiveMaximum(exclusiveMaximum).nullability(nullability);
        }

        public IntegerSupplier(final String nullability, final String minimum, final String maximum, final String exclusiveMinimum, final String exclusiveMaximum) {
            this.minimumFromString(minimum).maximumFromString(maximum).exclusiveMinimumFromString(exclusiveMinimum).exclusiveMaximumFromString(exclusiveMaximum).nullabilityFromString(nullability);
        }

        public ArbitrarySupplier<Integer> build() {
            return this;
        }

        @Override
        public Arbitrary<Integer> get() {
            return Arbitraries.integers().lessOrEqual(this.exclusiveMaximum ? --this.maximum : this.maximum).greaterOrEqual(this.exclusiveMinimum ? ++this.minimum : this.minimum).injectNull(this.nullability);
        }

        public IntegerSupplier<U> minimum(@NotNull final Integer minimum) {
            this.minimum = minimum;
            return this;
        }

        public IntegerSupplier<U> minimumFromString(@NotNull final String minimum) {
            if (minimum == null || minimum.trim().isEmpty()) {
                return this;
            }
            return this.minimum(Integer.valueOf(minimum));
        }

        public IntegerSupplier<U> exclusiveMinimum(@NotNull final Boolean exclusiveMinimum) {
            this.exclusiveMinimum = exclusiveMinimum;
            return this;
        }

        public IntegerSupplier<U> exclusiveMinimumFromString(@NotNull final String exclusiveMinimum) {
            if (exclusiveMinimum == null || exclusiveMinimum.trim().isEmpty()) {
                return this;
            }
            return this.exclusiveMinimum(Boolean.valueOf(exclusiveMinimum));
        }

        public IntegerSupplier<U> maximum(@NotNull final Integer maximum) {
            this.maximum = maximum;
            return this;
        }

        public IntegerSupplier<U> maximumFromString(@NotNull final String maximum) {
            if (maximum == null || maximum.trim().isEmpty()) {
                return this;
            }
            return this.maximum(Integer.valueOf(maximum));
        }

        public IntegerSupplier<U> exclusiveMaximum(@NotNull final Boolean exclusiveMaximum) {
            this.exclusiveMaximum = exclusiveMaximum;
            return this;
        }

        public IntegerSupplier<U> exclusiveMaximumFromString(@NotNull final String exclusiveMaximum) {
            if (exclusiveMaximum == null || exclusiveMaximum.trim().isEmpty()) {
                return this;
            }
            return this.exclusiveMaximum(Boolean.valueOf(exclusiveMaximum));
        }

        public IntegerSupplier<U> nullability(@NotNull final Double nullability) {
            this.nullability = nullability;
            return this;
        }

        public IntegerSupplier<U> nullabilityFromString(@NotNull final String nullability) {
            if (nullability == null || nullability.trim().isEmpty()) {
                return this;
            }
            return this.nullability(Double.valueOf(nullability));
        }
    }

}
