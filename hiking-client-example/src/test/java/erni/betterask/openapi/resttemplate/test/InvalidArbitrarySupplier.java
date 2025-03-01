package erni.betterask.openapi.resttemplate.test;

import lombok.SneakyThrows;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;

public class InvalidArbitrarySupplier<T> implements ArbitrarySupplier<T> {

    private final ArbitrarySupplier<T> invalidObjectSupplier;

    @SneakyThrows
    public InvalidArbitrarySupplier(String openApiSpec, Class<T> modelClass) {
        var openApi = new OpenApiModel(openApiSpec, modelClass.getPackageName());
        // TODO: implementation
        invalidObjectSupplier = null;
    }

    @Override
    public Arbitrary<T> get() {
        return invalidObjectSupplier.get();
    }
}
