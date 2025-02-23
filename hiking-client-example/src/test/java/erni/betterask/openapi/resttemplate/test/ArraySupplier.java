package erni.betterask.openapi.resttemplate.test;

import io.swagger.v3.oas.models.media.Schema;
import lombok.SneakyThrows;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;

import java.util.List;
import java.util.Objects;

public class ArraySupplier implements ArbitrarySupplier<List<Object>> {

    private final Schema<?> schema;
    private final Arbitrary<Object> itemArbitrary;
    @SuppressWarnings("unchecked")

    @SneakyThrows
    public ArraySupplier(Schema<?> schema, Arbitrary<?> itemArbitrary) {
        this.schema = schema;
        this.itemArbitrary = (Arbitrary<Object>) itemArbitrary;
    }


    @Override
    public Arbitrary<List<Object>> get() {
        return itemArbitrary.list()
                .ofMinSize(Objects.requireNonNullElse(schema.getMinItems(), 0))
                .ofMaxSize(Objects.requireNonNullElse(schema.getMaxItems(), 99));
    }
}
