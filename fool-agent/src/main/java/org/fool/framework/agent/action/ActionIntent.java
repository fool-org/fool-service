package org.fool.framework.agent.action;

import java.util.Map;

public record ActionIntent(int schemaVersion,
                           String action,
                           Resource resource,
                           Map<String, Object> arguments,
                           String rationale) {
    public ActionIntent {
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
        rationale = rationale == null ? "" : rationale;
    }

    public record Resource(String type, String id) {
    }
}
