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
            "appkey", "connection", "connectionstring", "syscon",
            "checkcode", "checkcodekey", "chk", "chkid",
            "chkkey", "chkcodeimg", "chkimg");
    private static final Set<String> CAPTCHA_CONTEXT_KEYS = Set.of(
            "checkcodekey", "chk", "chkid", "chkkey", "chkcodeimg", "chkimg");
    private static final Set<String> CAPTCHA_VALUE_KEYS = Set.of("code", "key");

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
            redact(root, false);
            return objectMapper.writeValueAsString(root);
        } catch (Exception ignored) {
            return "[REDACTED_NON_JSON_BODY]";
        }
    }

    private void redact(JsonNode node, boolean captchaContext) {
        if (node instanceof ObjectNode object) {
            boolean redactCaptchaValues = captchaContext || isCaptchaObject(object);
            Iterator<Map.Entry<String, JsonNode>> fields = object.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String normalized = normalize(field.getKey());
                if ("checkcode".equals(normalized) && field.getValue().isContainerNode()) {
                    redact(field.getValue(), true);
                } else if (isSensitive(normalized)
                        || (redactCaptchaValues && CAPTCHA_VALUE_KEYS.contains(normalized))) {
                    object.put(field.getKey(), REDACTED);
                } else {
                    redact(field.getValue(), captchaContext);
                }
            }
        } else if (node instanceof ArrayNode array) {
            array.forEach(item -> redact(item, captchaContext));
        }
    }

    private static boolean isCaptchaObject(ObjectNode object) {
        boolean hasCode = false;
        boolean hasKey = false;
        Iterator<String> fields = object.fieldNames();
        while (fields.hasNext()) {
            String normalized = normalize(fields.next());
            if (CAPTCHA_CONTEXT_KEYS.contains(normalized)) {
                return true;
            }
            hasCode |= "code".equals(normalized);
            hasKey |= "key".equals(normalized);
        }
        return hasCode && hasKey;
    }

    private static boolean isSensitive(String normalized) {
        return SENSITIVE_KEYS.contains(normalized);
    }

    private static String normalize(String key) {
        return key == null
                ? ""
                : key.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
    }
}
