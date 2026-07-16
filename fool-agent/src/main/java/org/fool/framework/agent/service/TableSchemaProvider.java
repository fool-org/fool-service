package org.fool.framework.agent.service;

@FunctionalInterface
public interface TableSchemaProvider {
    TableSchemaSnapshot load(String tableName);

    static TableSchemaProvider unavailable() {
        return tableName -> TableSchemaSnapshot.unavailable("Target table schema provider is unavailable.");
    }
}
