package org.fool.framework.agent.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.fool.framework.common.authz.ControlledActionException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class CanonicalJson {
    private final ObjectMapper objectMapper;

    public CanonicalJson(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy()
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public String write(Object value) {
        try {
            return objectMapper.writer().with(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                    .writeValueAsString(normalize(objectMapper.convertValue(value, Object.class)));
        } catch (RuntimeException | JsonProcessingException ex) {
            throw new ControlledActionException(400, "ACTION_PAYLOAD_INVALID");
        }
    }

    public String write(JsonNode value) {
        try {
            return objectMapper.writer().with(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                    .writeValueAsString(normalize(objectMapper.convertValue(value, Object.class)));
        } catch (RuntimeException | JsonProcessingException ex) {
            throw new ControlledActionException(400, "ACTION_PAYLOAD_INVALID");
        }
    }

    public String hash(Object value) {
        return sha256(write(value));
    }

    public String hashJson(String json) {
        try {
            return hash(objectMapper.readTree(json));
        } catch (JsonProcessingException ex) {
            throw new ControlledActionException(409, "ACTION_PAYLOAD_INVALID");
        }
    }

    public JsonNode read(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new ControlledActionException(409, "ACTION_PAYLOAD_INVALID");
        }
    }

    public static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static Object normalize(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sorted = new TreeMap<>();
            map.forEach((key, item) -> sorted.put(String.valueOf(key), normalize(item)));
            return sorted;
        }
        if (value instanceof List<?> list) {
            List<Object> normalized = new ArrayList<>(list.size());
            list.forEach(item -> normalized.add(normalize(item)));
            return normalized;
        }
        return value;
    }
}
