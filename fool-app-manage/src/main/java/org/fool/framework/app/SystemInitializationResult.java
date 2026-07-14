package org.fool.framework.app;

import java.util.List;

public record SystemInitializationResult(
        int discoveredModelCount,
        List<String> installedMetadataItems,
        List<String> schemaStatements,
        List<String> defaultViews) {
    public SystemInitializationResult {
        installedMetadataItems = immutable(installedMetadataItems);
        schemaStatements = immutable(schemaStatements);
        defaultViews = immutable(defaultViews);
    }

    private static List<String> immutable(List<String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
