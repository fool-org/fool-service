package org.fool.framework.common.authz;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public record DataPolicy(List<RowRule> rowRules,
                         Set<String> readableFields,
                         Set<String> filterableFields,
                         Set<String> sortableFields,
                         Set<String> exportableFields,
                         Set<String> writableFields,
                         Set<String> llmVisibleFields,
                         Map<String, String> maskStrategies,
                         Map<String, DataClassification> classifications,
                         Set<String> allowedProviders,
                         Integer maxQueryRows,
                         Integer maxExportRows,
                         Set<String> deniedFields) {
    public static final String DENY_ALL_PROVIDERS = "__DENY_ALL__";

    public DataPolicy {
        rowRules = rowRules == null || rowRules.isEmpty()
                ? List.of(new RowRule("ALL", Map.of()))
                : List.copyOf(rowRules);
        readableFields = copy(readableFields);
        filterableFields = copy(filterableFields);
        sortableFields = copy(sortableFields);
        exportableFields = copy(exportableFields);
        writableFields = copy(writableFields);
        llmVisibleFields = copy(llmVisibleFields);
        maskStrategies = maskStrategies == null ? Map.of() : Map.copyOf(maskStrategies);
        classifications = classifications == null ? Map.of() : Map.copyOf(classifications);
        allowedProviders = copy(allowedProviders);
        deniedFields = copy(deniedFields);
    }

    public DataPolicy(List<RowRule> rowRules,
                      Set<String> readableFields,
                      Set<String> filterableFields,
                      Set<String> sortableFields,
                      Set<String> exportableFields,
                      Set<String> writableFields,
                      Set<String> llmVisibleFields,
                      Map<String, String> maskStrategies,
                      Map<String, DataClassification> classifications,
                      Set<String> allowedProviders,
                      Integer maxQueryRows,
                      Integer maxExportRows) {
        this(rowRules, readableFields, filterableFields, sortableFields, exportableFields,
                writableFields, llmVisibleFields, maskStrategies, classifications,
                allowedProviders, maxQueryRows, maxExportRows, Set.of());
    }

    public static DataPolicy unrestricted() {
        return new DataPolicy(
                List.of(new RowRule("ALL", Map.of())),
                Set.of("*"), Set.of(), Set.of(), Set.of(), Set.of("*"), Set.of(),
                Map.of(), Map.of(), Set.of(), null, null);
    }

    public boolean readable(String field) {
        return fieldAllowed(readableFields, field);
    }

    public boolean filterable(String field) {
        return fieldAllowed(filterableFields.isEmpty() ? readableFields : filterableFields, field);
    }

    public boolean sortable(String field) {
        return fieldAllowed(sortableFields.isEmpty() ? readableFields : sortableFields, field);
    }

    public boolean exportable(String field) {
        return fieldAllowed(exportableFields.isEmpty() ? readableFields : exportableFields, field);
    }

    public boolean writable(String field) {
        return fieldAllowed(writableFields, field);
    }

    public boolean llmVisible(String field) {
        return readable(field)
                && fieldAllowed(llmVisibleFields.isEmpty() ? readableFields : llmVisibleFields, field)
                && classification(field) != DataClassification.RESTRICTED;
    }

    public boolean denied(String field) {
        return allowed(deniedFields, field);
    }

    public DataClassification classification(String field) {
        DataClassification configured = lookup(classifications, field, DataClassification.INTERNAL);
        return sensitiveName(field) ? DataClassification.RESTRICTED : configured;
    }

    public String maskStrategy(String field) {
        return lookup(maskStrategies, field, "NONE");
    }

    public boolean providerAllowed(String provider, DataClassification highestClassification) {
        if (highestClassification == DataClassification.RESTRICTED) {
            return false;
        }
        if (highestClassification == DataClassification.CONFIDENTIAL && allowedProviders.isEmpty()) {
            return false;
        }
        return !allowedProviders.contains(DENY_ALL_PROVIDERS)
                && (allowedProviders.isEmpty() || containsIgnoreCase(allowedProviders, provider));
    }

    private boolean fieldAllowed(Set<String> configured, String field) {
        return !denied(field) && allowed(configured, field);
    }

    private static boolean sensitiveName(String field) {
        String normalized = field == null ? "" : field.toLowerCase(Locale.ROOT);
        return normalized.contains("password")
                || normalized.contains("passwd")
                || normalized.contains("secret")
                || normalized.contains("token")
                || normalized.contains("connection")
                || normalized.equals("key")
                || normalized.equals("pwd")
                || normalized.startsWith("pwd_")
                || normalized.endsWith("_pwd")
                || normalized.endsWith("_key")
                || normalized.contains("apikey")
                || normalized.contains("api_key");
    }

    private static boolean allowed(Set<String> configured, String field) {
        return configured.contains("*") || containsIgnoreCase(configured, field);
    }

    private static boolean containsIgnoreCase(Set<String> values, String candidate) {
        return candidate != null && values.stream().anyMatch(value -> value.equalsIgnoreCase(candidate));
    }

    private static <T> T lookup(Map<String, T> values, String field, T fallback) {
        if (field == null) {
            return fallback;
        }
        return values.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(field))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(fallback);
    }

    private static Set<String> copy(Set<String> values) {
        return values == null ? Set.of() : Set.copyOf(values);
    }

    public record RowRule(String scopeType, Map<String, Object> filter) {
        public RowRule {
            scopeType = scopeType == null || scopeType.isBlank()
                    ? "ALL"
                    : scopeType.trim().toUpperCase(Locale.ROOT);
            filter = filter == null ? Map.of() : Map.copyOf(filter);
        }
    }
}
