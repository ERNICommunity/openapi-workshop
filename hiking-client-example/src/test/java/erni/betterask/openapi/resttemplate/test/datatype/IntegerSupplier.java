package erni.betterask.openapi.resttemplate.test.datatype;

import io.swagger.v3.oas.models.media.Schema;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;

public class IntegerSupplier implements ArbitrarySupplier<Integer> {

    private final Schema<?> schema;

    public IntegerSupplier(Schema<?> schema) {
        this.schema = schema;
    }

    @Override
    public Arbitrary<Integer> get() {
        return Arbitraries.integers()
                .lessOrEqual(Boolean.TRUE.equals(this.schema.getExclusiveMaximum()) ? this.schema.getMaximum().intValue() - 1 : this.schema.getMaximum().intValue())
                .greaterOrEqual(Boolean.TRUE.equals(this.schema.getExclusiveMinimum()) ? this.schema.getMinimum().intValue() + 1 : this.schema.getMinimum().intValue())
                .injectNull(Boolean.TRUE.equals(this.schema.getNullable()) ? 0.1 : 0.0);
    }
}
