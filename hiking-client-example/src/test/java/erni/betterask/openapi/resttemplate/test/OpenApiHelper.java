package erni.betterask.openapi.resttemplate.test;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.openapitools.codegen.utils.ModelUtils;

public class OpenApiHelper {

    static OpenAPI parseOpenApi(String openApiSpec) {
        // from org.openapitools.codegen.TestUtils#parseFlattenSpec()
        final OpenAPI openAPI = new OpenAPIParser().readLocation(openApiSpec, null, new ParseOptions(/*...date type, ...*/)).getOpenAPI();
        ModelUtils.getOpenApiVersion(openAPI, openApiSpec, null);
//        InlineModelResolver resolver = new InlineModelResolver();
//        resolver.flatten(openAPI);
        return openAPI;
    }
}
