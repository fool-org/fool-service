package org.fool.framework.common.authz;

import java.util.Map;

public record AuthorizationRequest(EffectiveSubject subject,
                                   String action,
                                   String resourceType,
                                   String resourceKey,
                                   Map<String, Object> environment) {
    public AuthorizationRequest {
        if (subject == null) {
            throw new IllegalArgumentException("subject is required.");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("action is required.");
        }
        if (resourceType == null || resourceType.isBlank()) {
            throw new IllegalArgumentException("resourceType is required.");
        }
        if (resourceKey == null || resourceKey.isBlank()) {
            throw new IllegalArgumentException("resourceKey is required.");
        }
        environment = environment == null ? Map.of() : Map.copyOf(environment);
    }

    public AuthorizationRequest(EffectiveSubject subject,
                                String action,
                                String resourceType,
                                String resourceKey) {
        this(subject, action, resourceType, resourceKey, Map.of());
    }
}
