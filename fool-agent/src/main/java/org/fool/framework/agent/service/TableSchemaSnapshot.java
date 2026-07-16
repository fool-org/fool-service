package org.fool.framework.agent.service;

import java.util.Set;

public record TableSchemaSnapshot(String status, String reason, Set<String> columns) {
    public TableSchemaSnapshot {
        columns = columns == null ? Set.of() : Set.copyOf(columns);
    }

    public static TableSchemaSnapshot hydrated(Set<String> columns) {
        return new TableSchemaSnapshot("hydrated", null, columns);
    }

    public static TableSchemaSnapshot unavailable(String reason) {
        return new TableSchemaSnapshot("unavailable", reason, Set.of());
    }

    public boolean isHydrated() {
        return "hydrated".equals(status);
    }
}
