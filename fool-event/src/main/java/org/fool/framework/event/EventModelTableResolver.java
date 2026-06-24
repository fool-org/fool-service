package org.fool.framework.event;

public interface EventModelTableResolver {
    EventModelQueryMetadata resolve(String modelId);

    default String resolveTableName(String modelId) {
        return resolve(modelId).tableName();
    }
}
