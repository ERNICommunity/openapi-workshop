package erni.betterask.openapi.resttemplate.test;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Schema;
import lombok.SneakyThrows;

import java.util.*;
import java.util.stream.Collectors;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

public class OpenApiModel {

    private final OpenAPI openApi;
    private final String packageName;

    public OpenApiModel(String openApiSpec, String packageName) {
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

    public Optional<String> getDiscriminatorProperty(Schema<?> schema) {
        return Optional.ofNullable(schema.getDiscriminator())
                .map(Discriminator::getPropertyName);
    }

    public Set<Schema<?>> getSubTypes(Schema<?> schema) {
        return schema.getDiscriminator().getMapping().values().stream()
                .map(this::getRef)
                .collect(Collectors.toSet());
    }

    public Map<String, Schema<?>> getAllProperties(Schema<?> schema) {

        if (schema.getProperties() != null) {
            return getProperties(schema);
        }
        if (schema.getAllOf() != null) {
            return schema.getAllOf().stream()
                    .map(Schema::get$ref)
                    .map(this::getRef)
                    .map(this::getProperties)
                    .map(Map::entrySet)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return Collections.emptyMap();
    }

    private boolean isPrimitive(Schema<?> schema) {

        return true;
    }



    private Map<String, Schema<?>> getProperties(Schema<?> schema) {
        var map = new HashMap<>(Objects.requireNonNullElseGet(getProperProperties(schema), Collections::emptyMap));
        getDiscriminatorProperty(schema).ifPresent(map::remove);
        return map;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Schema<?>> getProperProperties(Schema<?> schema) {
        return (Map<String, Schema<?>>) (Map) schema.getProperties();
    }
}
