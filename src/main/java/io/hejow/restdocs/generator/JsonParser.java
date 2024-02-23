package io.hejow.restdocs.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.Nullable;

import java.io.UnsupportedEncodingException;

class JsonParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonParser() {
        throw new AssertionError("can't be initialize!");
    }

    @FunctionalInterface
    interface ResultContentSupplier {
        String get() throws UnsupportedEncodingException;
    }

    @Nullable
    public static JsonNode readTree(ResultContentSupplier contentSupplier) {
        try {
            String content = contentSupplier.get();
            return content != null ? mapper.readTree(content) : null;
        } catch (UnsupportedEncodingException | JsonProcessingException exception) {
            throw new IllegalArgumentException(exception);
        }
    }
}
