package erni.betterask.openapi.resttemplate.test.datatype;

import io.swagger.v3.oas.models.media.Schema;
import lombok.SneakyThrows;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;

import java.util.Objects;
import java.util.Set;

public class UniqueItemsArraySupplier implements ArbitrarySupplier<Set<Object>> {

    private final Schema<?> schema;
    private final Arbitrary<Object> itemArbitrary;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public UniqueItemsArraySupplier(Schema<?> schema, Arbitrary<?> itemArbitrary) {
        this.schema = schema;
        this.itemArbitrary = (Arbitrary<Object>) itemArbitrary;
    }

    @Override
    public Arbitrary<Set<Object>> get() {
        return itemArbitrary.set()
                .ofMinSize(Objects.requireNonNullElse(schema.getMinItems(), 0))
                .ofMaxSize(Math.min(Objects.requireNonNullElse(schema.getMaxItems(), Integer.MAX_VALUE), 99))
                .injectNull(Objects.requireNonNullElse(schema.getNullable(), false) ? 0.1 : 0.0);
    }
}
