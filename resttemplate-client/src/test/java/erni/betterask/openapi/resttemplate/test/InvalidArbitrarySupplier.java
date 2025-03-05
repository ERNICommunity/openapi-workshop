package erni.betterask.openapi.resttemplate.test;

import erni.betterask.openapi.resttemplate.test.datatype.ObjectSupplier;
import lombok.SneakyThrows;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;

import static erni.betterask.openapi.resttemplate.test.ValueGenerationMode.INVALID_INSTANCES;

public class InvalidArbitrarySupplier<T> implements ArbitrarySupplier<T> {

    private final ArbitrarySupplier<T> invalidObjectSupplier;

    @SneakyThrows
    public InvalidArbitrarySupplier(String openApiSpec, Class<T> modelClass) {
        var openApi = new OpenApiModel(openApiSpec, modelClass.getPackageName());
        // TODO: implementation
        invalidObjectSupplier = new ObjectSupplier<T>(openApi, modelClass.getSimpleName(), INVALID_INSTANCES);
    }

    @Override
    public Arbitrary<T> get() {
        return invalidObjectSupplier.get();
    }
}
