package erni.betterask.openapi.resttemplate.test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import lombok.SneakyThrows;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;

import static erni.betterask.openapi.resttemplate.test.OpenApiHelper.parseOpenApi;
import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

public class InvalidArbitrarySupplier<T> implements ArbitrarySupplier<T> {

    private final OpenAPI openAPI;
    final Class<T> modelClass;

    @SneakyThrows
    public InvalidArbitrarySupplier(String openApiSpec, Class<T> modelClass) {
        openAPI = parseOpenApi(openApiSpec);
        this.modelClass = modelClass;
    }

    @Override
    public Arbitrary<T> get() {
        return new InvalidObjectSupplier<T>(this, getSchema(modelClass.getSimpleName())).get();
    }

    public Schema<?> getRef(String ref) {
        return getSchema(ref.substring(COMPONENTS_SCHEMAS_REF.length()));
    }

    private Schema<?> getSchema(String title) {
        return openAPI.getComponents().getSchemas().get(title);
    }
}
