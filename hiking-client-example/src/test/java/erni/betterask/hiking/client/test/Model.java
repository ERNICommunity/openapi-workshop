package erni.betterask.hiking.client.test;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.openapitools.codegen.InlineModelResolver;
import org.openapitools.codegen.utils.ModelUtils;

import javax.validation.constraints.NotNull;

public class Model {

    // ggf. weitere memeber Variable
    private final OpenAPI openAPI;

    private final Schema<?> schema;

    public Model(@NotNull final String openApiSpec, @NotNull final String model) {
        this.openAPI = this.parseOpenApi(openApiSpec);
        this.schema = openAPI.getComponents().getSchemas().get(model);
    }

    public Schema<?> getSchema() {
        return this.schema;
    }

    public boolean isArray() {
        return this.matchesTypeName("array");
    }

    public boolean isObject() {
        return this.matchesTypeName("object");
    }

    public boolean isInteger() {
        return this.matchesTypeName("integer");
    }

    public boolean isString() {
        return this.matchesTypeName("string");
    }

    private boolean matchesTypeName(final String typeName) {
        final String type = this.schema.getType();
        if (type == null || type.trim().isEmpty()) {
            return false;
        }
        return typeName.equals(type);
    }

    private OpenAPI parseOpenApi(@NotNull final String openApiSpec) {
        final OpenAPI openAPI = new OpenAPIParser().readLocation(openApiSpec, null, new ParseOptions(/*...date type, ...*/)).getOpenAPI();
        ModelUtils.getOpenApiVersion(openAPI, openApiSpec, null);
        InlineModelResolver resolver = new InlineModelResolver();
        return openAPI;
    }
}
