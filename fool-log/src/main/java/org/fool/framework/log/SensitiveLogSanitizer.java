package org.fool.framework.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class SensitiveLogSanitizer {
    private static final String REDACTED = "[REDACTED]";
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "token", "accesstoken", "refreshtoken", "authorization",
            "password", "passwd", "pwd", "secret", "clientsecret",
            "appkey", "connection", "connectionstring", "syscon");

    private final ObjectMapper objectMapper;

    public SensitiveLogSanitizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String sanitize(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root == null) {
                return "";
            }
            if (root.isTextual()) {
                return objectMapper.writeValueAsString(REDACTED);
            }
            redact(root);
            return objectMapper.writeValueAsString(root);
        } catch (Exception ignored) {
            return "[REDACTED_NON_JSON_BODY]";
        }
    }

    private void redact(JsonNode node) {
        if (node instanceof ObjectNode object) {
            Iterator<Map.Entry<String, JsonNode>> fields = object.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (isSensitive(field.getKey())) {
                    object.put(field.getKey(), REDACTED);
                } else {
                    redact(field.getValue());
                }
            }
        } else if (node instanceof ArrayNode array) {
            array.forEach(this::redact);
        }
    }

    private static boolean isSensitive(String key) {
        String normalized = key == null
                ? ""
                : key.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYS.contains(normalized);
    }
}
