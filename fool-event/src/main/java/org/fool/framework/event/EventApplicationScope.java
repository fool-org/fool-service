package org.fool.framework.event;

import java.util.List;

public record EventApplicationScope(
        String applicationId,
        String systemConnection,
        List<String> databaseConnections) {
    public EventApplicationScope {
        databaseConnections = databaseConnections == null ? List.of() : List.copyOf(databaseConnections);
    }
}
