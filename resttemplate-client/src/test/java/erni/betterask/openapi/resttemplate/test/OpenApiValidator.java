package erni.betterask.openapi.resttemplate.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.openapi4j.core.exception.EncodeException;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.schema.validator.ValidationContext;
import org.openapi4j.schema.validator.ValidationData;
import org.openapi4j.schema.validator.v3.SchemaValidator;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

public class OpenApiValidator {

    private final OpenApi3 api;
    private final ObjectMapper om;
    private final Logger logger;

    public OpenApiValidator(String fnameApi, Class<?> clazz) {
        this.api = createOpenApi3(fnameApi);
        this.om = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(new JsonNullableModule())
                .build();
        this.om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        this.om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.logger = clazz == null ? null : LoggerFactory.getLogger(clazz);
    }

    private OpenApi3 createOpenApi3(String apiFname) {
        try {
            var url = Path.of(apiFname).toUri().toURL();
            return new OpenApi3Parser().parse(url, true);
        } catch(ResolutionException
                | ValidationException
                | MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String toJson(Object any) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            this.om.writeValue(out, any);
            return out.toString();
        } catch(IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return this.om.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public <T> boolean validate(T value) {
        try {
            String schemaName =
                    value.getClass().getSimpleName();
            JsonNode schemaNode =
                    this.api.getComponents().getSchema(schemaName).toNode();
            JsonNode contentNode =
                    this.om.convertValue(value, JsonNode.class);
            SchemaValidator validator =
                    new SchemaValidator(new ValidationContext<>(
                            this.api.getContext()), null, schemaNode
                    );
            ValidationData<Void> validation = new ValidationData<>();
            validator.validate(contentNode, validation);
            if (this.logger != null && !validation.isValid()) {
                this.logger.info("validation of node `{}` failed with results `{}`.",
                        contentNode, validation.results());
            }
            return validation.isValid();
        } catch(EncodeException ex) {
            throw new RuntimeException(ex);
        }
    }
}
