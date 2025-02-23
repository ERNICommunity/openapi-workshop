package erni.betterask.openapi.resttemplate.test;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import lombok.SneakyThrows;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

public class OpenApi {

    private final OpenAPI openApi;
    private final String packageName;

    public OpenApi(String openApiSpec, String packageName) {
        openApi = new OpenAPIParser().readLocation(openApiSpec, null, null).getOpenAPI();
        this.packageName = packageName;
    }

    @SneakyThrows
    public Class<?> getModelClass(String modelName) {
        return Class.forName(packageName + "." + modelName);
    }

    public Schema<?> getSchema(String title) {
        return openApi.getComponents().getSchemas().get(title);
    }

    public Schema<?> getRef(String ref) {
        return getSchema(ref.substring(COMPONENTS_SCHEMAS_REF.length()));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Schema<?>> getAllProperties(Schema<?> schema) {

        Stream<Schema<?>> schemaStream = Stream.of(
                        Stream.of(schema),
                        Optional.ofNullable(schema.getAllOf()).stream()
                                .flatMap(Collection::stream)
                                .map(Schema::get$ref)
                                .map(this::getRef)
                )
                .flatMap(Function.identity());

        return schemaStream
                .map(Schema::getProperties)
                .filter(Objects::nonNull)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .filter(e -> !"discriminator".equals(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
