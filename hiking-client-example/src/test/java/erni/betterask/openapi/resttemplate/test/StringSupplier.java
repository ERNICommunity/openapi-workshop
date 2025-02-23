package erni.betterask.openapi.resttemplate.test;

import io.swagger.v3.oas.models.media.Schema;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;

import java.util.Objects;

public class StringSupplier implements ArbitrarySupplier<String> {

    private final Schema<?> schema;

    public StringSupplier(Schema<?> schema) {
        this.schema = schema;
    }

    @Override
    public Arbitrary<String> get() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(Objects.requireNonNullElse(this.schema.getMinLength(), 0))
                .ofMaxLength(Math.min(Objects.requireNonNullElse(this.schema.getMaxLength(), Integer.MAX_VALUE), 80))
                .injectNull(Boolean.TRUE.equals(this.schema.getNullable()) ? 0.1 : 0.0);
    }
}
