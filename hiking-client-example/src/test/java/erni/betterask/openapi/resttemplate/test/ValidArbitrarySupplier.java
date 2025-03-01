package erni.betterask.openapi.resttemplate.test;

import erni.betterask.openapi.resttemplate.test.datatype.ObjectSupplier;
import lombok.SneakyThrows;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;

public class ValidArbitrarySupplier<T> implements ArbitrarySupplier<T> {

    private final ArbitrarySupplier<T> validObjectSupplier;

    @SneakyThrows
    public ValidArbitrarySupplier(String openApiSpec, Class<T> modelClass) {
        var openApi = new OpenApiModel(openApiSpec, modelClass.getPackageName());
        validObjectSupplier = new ObjectSupplier<T>(openApi, modelClass.getSimpleName());
    }

    @Override
    public Arbitrary<T> get() {

        return validObjectSupplier.get();
    }
}
