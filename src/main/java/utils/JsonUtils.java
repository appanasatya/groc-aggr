package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public enum JsonUtils {
    DEFAULT(new ObjectMapper()),

    EVENT(new ObjectMapper() {
        {
            configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        }
    });

    public final ObjectMapper mapper;

    private JsonUtils(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> String toJson(T object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> byte[] toJsonBinary(T object) {
        try {
            return mapper.writeValueAsBytes(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T fromJson(String jsonString, Class<T> clazz) {
        try {
            return mapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T fromJson(byte[] json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> asMap(String jsonString) {
        try {
            final TypeReference<LinkedHashMap<String, Object>> typeRef = new TypeReference<LinkedHashMap<String, Object>>() {
            };
            return mapper.readValue(jsonString, typeRef);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> asMap(byte[] jsonString) {
        try {
            final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
            };
            return mapper.readValue(jsonString, typeRef);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
