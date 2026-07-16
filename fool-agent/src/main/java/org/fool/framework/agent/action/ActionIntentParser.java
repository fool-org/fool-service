package org.fool.framework.agent.action;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ActionIntentParser {
    private static final Set<String> ACTIONS = Set.of(
            "view.discover", "view.read", "view.query", "report.preview", "report.save",
            "report.export", "data.create", "data.update", "data.delete", "operation.execute",
            "model.configure", "model.ddl.preview", "model.ddl.execute", "datasource.test",
            "datasource.route.update", "datasource.credential.update", "event.preview", "event.enable",
            "message.send", "agent.use", "auth.permission.change", "action.approve");
    private static final Set<String> RESOURCE_TYPES = Set.of(
            "app", "view", "report", "model", "field", "operation", "datasource", "event",
            "agent-capability", "action-request");
    private static final Set<String> ROOT_FIELDS = Set.of(
            "schemaVersion", "action", "resource", "arguments", "rationale");
    private static final Set<String> FORBIDDEN_FIELDS = Set.of(
            "actor", "actorUserId", "effectiveRoles", "risk", "riskLevel", "approval",
            "approvalPolicy", "policyVersion", "executor", "executorId", "executionStatus",
            "sql", "url", "class", "className", "method", "methodName", "token", "password",
            "secret", "credential", "connectionString");
    private static final Pattern SQL = Pattern.compile(
            "(?is)\\b(?:select\\s+.+?\\s+from|insert\\s+into|update\\s+[a-z0-9_.`]+\\s+set|"
                    + "delete\\s+from|drop\\s+(?:table|database)|alter\\s+table|truncate\\s+table|"
                    + "grant\\s+.+?\\s+on|revoke\\s+.+?\\s+on|call\\s+[a-z0-9_.`]+\\s*\\(|"
                    + "exec(?:ute)?\\s+[a-z0-9_.`]+)\\b");
    private static final Pattern URL = Pattern.compile("(?i)(?:\\b(?:https?|jdbc|file)://|\\bjdbc:)");
    private static final Pattern BEARER = Pattern.compile("(?i)bearer\\s+[a-z0-9._~+/-]{16,}");
    private static final Pattern SECRET_ASSIGNMENT = Pattern.compile(
            "(?i)(password|passwd|pwd|secret|token|api[_-]?key)\\s*[:=]\\s*\\S+");

    private final ObjectMapper objectMapper;

    public ActionIntentParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ActionIntent parse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root == null || !root.isObject()) {
                throw invalid("ACTION_INTENT_OBJECT_REQUIRED");
            }
            root.fieldNames().forEachRemaining(field -> {
                if (!ROOT_FIELDS.contains(field)) {
                    throw invalid("ACTION_INTENT_FIELD_FORBIDDEN");
                }
            });
            if (root.path("schemaVersion").asInt(-1) != 1) {
                throw invalid("ACTION_INTENT_SCHEMA_UNSUPPORTED");
            }
            String action = root.path("action").asText("");
            if (!ACTIONS.contains(action)) {
                throw invalid("ACTION_INTENT_ACTION_UNKNOWN");
            }
            JsonNode resourceNode = root.path("resource");
            String resourceType = resourceNode.path("type").asText("");
            String resourceId = resourceNode.path("id").asText("");
            if (!resourceNode.isObject()
                    || !RESOURCE_TYPES.contains(resourceType)
                    || !safeIdentifier(resourceId)) {
                throw invalid("ACTION_INTENT_RESOURCE_INVALID");
            }
            JsonNode argumentsNode = root.path("arguments");
            if (!argumentsNode.isObject() || argumentsNode.size() > 100) {
                throw invalid("ACTION_INTENT_ARGUMENTS_INVALID");
            }
            validate(argumentsNode, 0);
            String rationale = root.path("rationale").asText("");
            if (rationale.length() > 2000) {
                throw invalid("ACTION_INTENT_RATIONALE_TOO_LONG");
            }
            if (forbiddenText(rationale)) {
                throw invalid("ACTION_INTENT_VALUE_FORBIDDEN");
            }
            Map<String, Object> arguments = objectMapper.convertValue(
                    argumentsNode, new TypeReference<>() {
                    });
            return new ActionIntent(
                    1,
                    action,
                    new ActionIntent.Resource(resourceType, resourceId),
                    arguments,
                    rationale);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw invalid("ACTION_INTENT_JSON_INVALID");
        }
    }

    private void validate(JsonNode node, int depth) {
        if (depth > 8) {
            throw invalid("ACTION_INTENT_TOO_DEEP");
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (forbiddenField(field.getKey())) {
                    throw invalid("ACTION_INTENT_FIELD_FORBIDDEN");
                }
                validate(field.getValue(), depth + 1);
            }
        } else if (node.isArray()) {
            if (node.size() > 500) {
                throw invalid("ACTION_INTENT_ARRAY_TOO_LARGE");
            }
            node.forEach(item -> validate(item, depth + 1));
        } else if (node.isTextual()) {
            String value = node.asText();
            if (value.length() > 4000 || forbiddenText(value)) {
                throw invalid("ACTION_INTENT_VALUE_FORBIDDEN");
            }
        }
    }

    private static boolean forbiddenField(String field) {
        String normalized = field == null ? "" : field.replace("_", "").replace("-", "").toLowerCase();
        if ("credentialref".equals(normalized) || "credentialreference".equals(normalized)) {
            return false;
        }
        return FORBIDDEN_FIELDS.stream().anyMatch(name -> name.equalsIgnoreCase(field))
                || normalized.contains("password")
                || normalized.contains("passwd")
                || normalized.startsWith("pwd")
                || normalized.contains("secret")
                || normalized.contains("token")
                || normalized.contains("apikey")
                || normalized.contains("connectionstring")
                || normalized.contains("credential");
    }

    private static boolean forbiddenText(String value) {
        return SQL.matcher(value).find()
                || URL.matcher(value).find()
                || BEARER.matcher(value).find()
                || SECRET_ASSIGNMENT.matcher(value).find();
    }

    private static boolean safeIdentifier(String value) {
        return StringUtils.hasText(value) && value.matches("[A-Za-z0-9._:-]{1,256}");
    }

    private static IllegalArgumentException invalid(String reason) {
        return new IllegalArgumentException(reason);
    }
}
