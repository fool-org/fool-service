package org.fool.framework.agent.service;

import org.fool.framework.common.authz.DataClassification;
import org.fool.framework.common.authz.DataPolicy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class AgentOutboundPolicy {
    private static final String REDACTED = "[REDACTED]";
    private static final Set<String> RESTRICTED_KEYS = Set.of(
            "token", "authorization", "password", "passwd", "pwd", "secret", "apikey",
            "connection", "connectionstring", "rawconnectionstring", "credentialcolumns",
            "serverip", "username");
    private static final Set<String> EXECUTABLE_KEYS = Set.of(
            "classname", "invokedll", "invokeclass", "invokemethod", "filter", "argfilter",
            "sourceexpression", "querypreview");
    private static final Set<String> FIELD_VALUE_KEYS = Set.of(
            "value", "fmtvalue", "rawvalue", "defaultvalue", "sample", "content", "text");
    private static final Pattern BEARER = Pattern.compile("(?i)bearer\\s+[a-z0-9._~+/-]{16,}");
    private static final Pattern JDBC = Pattern.compile("(?i)jdbc:[^\\s,;]+(?:[;,][^\\s]+)*");
    private static final Pattern SECRET_ASSIGNMENT = Pattern.compile(
            "(?i)(password|passwd|pwd|secret|token|api[_-]?key)\\s*[:=]\\s*[^\\s,;]+");

    public AgentDraft sanitize(AgentDraft draft, DataPolicy policy) {
        Object sanitized = sanitizeValue(draft.getDraftPayload(), policy == null ? DataPolicy.unrestricted() : policy);
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = sanitized instanceof Map<?, ?>
                ? (Map<String, Object>) sanitized
                : Map.of();
        return new AgentDraft(
                draft.getCapability(),
                sanitizeText(draft.getSummary()),
                draft.getOwnerModules(),
                draft.getRiskLevel(),
                payload,
                draft.getValidationSteps().stream().map(this::sanitizeText).toList());
    }

    public String sanitizeText(String value) {
        if (value == null) {
            return "";
        }
        String sanitized = BEARER.matcher(value).replaceAll(REDACTED);
        sanitized = JDBC.matcher(sanitized).replaceAll(REDACTED);
        return SECRET_ASSIGNMENT.matcher(sanitized).replaceAll(match -> match.group(1) + "=" + REDACTED);
    }

    public DataClassification highestClassification(AgentDraft draft, DataPolicy policy) {
        List<String> fields = fieldIdentities(draft.getDraftPayload());
        DataClassification result = fields.isEmpty() ? DataClassification.INTERNAL : DataClassification.PUBLIC;
        for (String field : fields) {
            result = DataClassification.max(result, policy.classification(field));
        }
        return result;
    }

    private Object sanitizeValue(Object value, DataPolicy policy) {
        if (value instanceof Map<?, ?> source) {
            if (!fieldMapVisible(source, policy)) {
                return null;
            }
            List<String> directFields = directFieldIdentities(source);
            DataClassification classification = directFields.stream()
                    .map(policy::classification)
                    .reduce(DataClassification.PUBLIC, DataClassification::max);
            if (classification == DataClassification.RESTRICTED) {
                return null;
            }
            String mask = directFields.stream()
                    .map(policy::maskStrategy)
                    .reduce("NONE", AgentOutboundPolicy::stricterMask);
            if (classification == DataClassification.CONFIDENTIAL && "NONE".equalsIgnoreCase(mask)) {
                mask = "FULL";
            }
            String effectiveMask = mask;
            Map<String, Object> result = new LinkedHashMap<>();
            source.forEach((rawKey, rawValue) -> {
                String key = String.valueOf(rawKey);
                String normalized = normalize(key);
                if (restrictedKey(normalized) || EXECUTABLE_KEYS.contains(normalized)) {
                    return;
                }
                Object sanitized = !directFields.isEmpty() && FIELD_VALUE_KEYS.contains(normalized)
                        ? mask(rawValue, effectiveMask)
                        : sanitizeValue(rawValue, policy);
                if (sanitized != null) {
                    result.put(key, sanitized);
                }
            });
            return result;
        }
        if (value instanceof Collection<?> source) {
            List<Object> result = new ArrayList<>();
            source.forEach(item -> {
                Object sanitized = sanitizeValue(item, policy);
                if (sanitized != null) {
                    result.add(sanitized);
                }
            });
            return result;
        }
        return value instanceof String text ? sanitizeText(text) : value;
    }

    private boolean fieldMapVisible(Map<?, ?> map, DataPolicy policy) {
        List<String> direct = directFieldIdentities(map);
        if (!direct.isEmpty()) {
            return direct.stream().noneMatch(policy::denied)
                    && direct.stream().anyMatch(policy::llmVisible);
        }
        List<String> nested = fieldIdentities(map);
        return nested.isEmpty() || nested.stream().anyMatch(policy::llmVisible);
    }

    private List<String> fieldIdentities(Object value) {
        List<String> fields = new ArrayList<>();
        collectFieldIdentities(value, fields);
        return fields;
    }

    private List<String> directFieldIdentities(Map<?, ?> map) {
        List<String> fields = new ArrayList<>();
        for (String key : List.of("PropertyName", "ModelProperty", "DbColumn", "PrpId")) {
            Object identity = map.get(key);
            if (identity != null && !identity.toString().isBlank()) {
                fields.add(identity.toString());
            }
        }
        return fields;
    }

    private void collectFieldIdentities(Object value, List<String> fields) {
        if (value instanceof Map<?, ?> map) {
            for (String key : List.of("PropertyName", "ModelProperty", "DbColumn", "PrpId")) {
                Object identity = map.get(key);
                if (identity != null && !identity.toString().isBlank()) {
                    fields.add(identity.toString());
                }
            }
            map.values().forEach(item -> collectFieldIdentities(item, fields));
        } else if (value instanceof Collection<?> collection) {
            collection.forEach(item -> collectFieldIdentities(item, fields));
        }
    }

    private static boolean restrictedKey(String normalized) {
        return RESTRICTED_KEYS.contains(normalized)
                || normalized.endsWith("token")
                || normalized.endsWith("password")
                || normalized.endsWith("secret")
                || normalized.startsWith("pwd");
    }

    private static Object mask(Object value, String strategy) {
        if (value == null || "NONE".equalsIgnoreCase(strategy)) {
            return value;
        }
        String text = String.valueOf(value);
        return switch (strategy.toUpperCase(Locale.ROOT)) {
            case "LAST4" -> text.length() <= 4 ? "****" : "****" + text.substring(text.length() - 4);
            case "HASH" -> "[HASHED]";
            default -> REDACTED;
        };
    }

    private static String stricterMask(String left, String right) {
        return maskRank(left) >= maskRank(right) ? left : right;
    }

    private static int maskRank(String value) {
        return switch (value == null ? "NONE" : value.toUpperCase(Locale.ROOT)) {
            case "FULL" -> 3;
            case "HASH" -> 2;
            case "LAST4" -> 1;
            default -> 0;
        };
    }

    private static String normalize(String value) {
        return value.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
    }
}
