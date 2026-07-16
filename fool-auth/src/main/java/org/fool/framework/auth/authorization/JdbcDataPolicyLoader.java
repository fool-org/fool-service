package org.fool.framework.auth.authorization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.common.authz.DataClassification;
import org.fool.framework.common.authz.DataPolicy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class JdbcDataPolicyLoader {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    JdbcDataPolicyLoader(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    DataPolicy load(List<String> policyIds) {
        if (policyIds == null || policyIds.isEmpty()) {
            return DataPolicy.unrestricted();
        }
        List<DataPolicy> policies = policyIds.stream().distinct().map(this::loadOne).toList();
        return merge(policies);
    }

    private DataPolicy loadOne(String policyId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT `SCOPE_TYPE`, `FILTER_JSON`, `READABLE_FIELDS_JSON`,
                       `WRITABLE_FIELDS_JSON`, `MASK_FIELDS_JSON`,
                       `MAX_QUERY_ROWS`, `MAX_EXPORT_ROWS`, `LLM_POLICY_JSON`
                  FROM `FOOL_AUTHZ_DATA_POLICY`
                 WHERE `DATA_POLICY_ID` = ?
                """, policyId);
        if (rows.size() != 1) {
            throw new IllegalStateException("Data policy is missing or ambiguous: " + policyId);
        }
        Map<String, Object> row = rows.get(0);
        Map<String, Object> filter = jsonMap(row.get("FILTER_JSON"));
        Map<String, Object> llm = jsonMap(row.get("LLM_POLICY_JSON"));
        Set<String> readable = jsonSet(row.get("READABLE_FIELDS_JSON"));
        Set<String> writable = jsonSet(row.get("WRITABLE_FIELDS_JSON"));
        Set<String> filterable = stringSet(llm.get("filterableFields"));
        Set<String> sortable = stringSet(llm.get("sortableFields"));
        Set<String> exportable = stringSet(llm.get("exportableFields"));
        Set<String> llmVisible = stringSet(llm.get("llmVisibleFields"));
        Set<String> deniedFields = stringSet(llm.get("deniedFields"));
        Set<String> allowedProviders = stringSet(llm.get("allowedProviders"));
        Map<String, String> masks = stringMap(row.get("MASK_FIELDS_JSON"));
        Map<String, DataClassification> classifications = classifications(llm.get("classifications"));
        return new DataPolicy(
                List.of(new DataPolicy.RowRule(value(row.get("SCOPE_TYPE")), filter)),
                readable,
                filterable,
                sortable,
                exportable,
                writable,
                llmVisible,
                masks,
                classifications,
                allowedProviders,
                integer(row.get("MAX_QUERY_ROWS")),
                integer(row.get("MAX_EXPORT_ROWS")),
                deniedFields);
    }

    private DataPolicy merge(List<DataPolicy> policies) {
        List<DataPolicy.RowRule> rowRules = new ArrayList<>();
        Set<String> readable = new LinkedHashSet<>();
        Set<String> filterable = new LinkedHashSet<>();
        Set<String> sortable = new LinkedHashSet<>();
        Set<String> exportable = new LinkedHashSet<>();
        Set<String> writable = new LinkedHashSet<>();
        Set<String> llmVisible = new LinkedHashSet<>();
        Set<String> deniedFields = new LinkedHashSet<>();
        Set<String> providers = null;
        Map<String, String> masks = new LinkedHashMap<>();
        Map<String, DataClassification> classifications = new LinkedHashMap<>();
        Integer maxQueryRows = null;
        Integer maxExportRows = null;
        for (DataPolicy policy : policies) {
            rowRules.addAll(policy.rowRules());
            readable.addAll(policy.readableFields());
            filterable.addAll(policy.filterableFields().isEmpty()
                    ? policy.readableFields() : policy.filterableFields());
            sortable.addAll(policy.sortableFields().isEmpty()
                    ? policy.readableFields() : policy.sortableFields());
            exportable.addAll(policy.exportableFields().isEmpty()
                    ? policy.readableFields() : policy.exportableFields());
            writable.addAll(policy.writableFields());
            llmVisible.addAll(policy.llmVisibleFields());
            deniedFields.addAll(policy.deniedFields());
            if (!policy.allowedProviders().isEmpty()) {
                if (providers == null) {
                    providers = new LinkedHashSet<>(policy.allowedProviders());
                } else {
                    providers.removeIf(provider -> policy.allowedProviders().stream()
                            .noneMatch(provider::equalsIgnoreCase));
                }
            }
            policy.maskStrategies().forEach((field, strategy) -> masks.merge(
                    field, strategy, JdbcDataPolicyLoader::stricterMask));
            policy.classifications().forEach((field, level) -> classifications.merge(
                    field, level, DataClassification::max));
            maxQueryRows = minimum(maxQueryRows, policy.maxQueryRows());
            maxExportRows = minimum(maxExportRows, policy.maxExportRows());
        }
        return new DataPolicy(
                rowRules,
                readable,
                filterable,
                sortable,
                exportable,
                writable,
                llmVisible,
                masks,
                classifications,
                providers == null ? Set.of()
                        : providers.isEmpty() ? Set.of(DataPolicy.DENY_ALL_PROVIDERS) : providers,
                maxQueryRows,
                maxExportRows,
                deniedFields);
    }

    private Map<String, Object> jsonMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        try {
            if (value instanceof Map<?, ?> map) {
                return objectMapper.convertValue(map, new TypeReference<>() {
                });
            }
            String json = value.toString();
            return StringUtils.hasText(json)
                    ? objectMapper.readValue(json, new TypeReference<>() {
                    })
                    : Map.of();
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid data policy JSON.", ex);
        }
    }

    private Set<String> jsonSet(Object value) {
        if (value == null) {
            return Set.of();
        }
        try {
            if (value instanceof Collection<?> collection) {
                return stringSet(collection);
            }
            String json = value.toString();
            return StringUtils.hasText(json)
                    ? stringSet(objectMapper.readValue(json, new TypeReference<List<Object>>() {
                    }))
                    : Set.of();
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid data policy field list.", ex);
        }
    }

    private Map<String, String> stringMap(Object value) {
        Map<String, Object> source = value instanceof Map<?, ?>
                ? objectMapper.convertValue(value, new TypeReference<>() {
                })
                : jsonMap(value);
        Map<String, String> result = new LinkedHashMap<>();
        source.forEach((key, item) -> result.put(key, item == null ? "NONE" : item.toString()));
        return result;
    }

    private Map<String, DataClassification> classifications(Object value) {
        if (!(value instanceof Map<?, ?>)) {
            return Map.of();
        }
        Map<String, Object> source = objectMapper.convertValue(value, new TypeReference<>() {
        });
        Map<String, DataClassification> result = new LinkedHashMap<>();
        source.forEach((field, level) -> result.put(
                field,
                DataClassification.valueOf(level.toString().toUpperCase(Locale.ROOT))));
        return result;
    }

    private Set<String> stringSet(Object value) {
        if (!(value instanceof Collection<?> values)) {
            return Set.of();
        }
        Set<String> result = new LinkedHashSet<>();
        values.stream().filter(item -> item != null && StringUtils.hasText(item.toString()))
                .forEach(item -> result.add(item.toString().trim()));
        return result;
    }

    private static String value(Object value) {
        return value == null ? "" : value.toString();
    }

    private static Integer integer(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private static Integer minimum(Integer left, Integer right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return Math.min(left, right);
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
}
