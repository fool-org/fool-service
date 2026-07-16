package org.fool.framework.common.authz;

import java.time.Instant;
import java.util.List;

public record EffectiveSubject(String userId,
                               List<String> roleIds,
                               String companyId,
                               List<String> departmentIds,
                               List<String> departmentTreeIds,
                               String appId,
                               String databaseId,
                               String sessionId,
                               Instant authenticatedAt,
                               Instant stepUpAt,
                               long policyVersion) {
    public EffectiveSubject(String userId,
                            List<String> roleIds,
                            String companyId,
                            List<String> departmentIds,
                            String appId,
                            String databaseId,
                            String sessionId,
                            Instant authenticatedAt,
                            Instant stepUpAt,
                            long policyVersion) {
        this(userId, roleIds, companyId, departmentIds, departmentIds, appId,
                databaseId, sessionId, authenticatedAt, stepUpAt, policyVersion);
    }

    public EffectiveSubject {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required.");
        }
        roleIds = roleIds == null ? List.of() : List.copyOf(roleIds);
        departmentIds = departmentIds == null ? List.of() : List.copyOf(departmentIds);
        departmentTreeIds = departmentTreeIds == null
                ? departmentIds : List.copyOf(departmentTreeIds);
        companyId = value(companyId);
        appId = value(appId);
        databaseId = value(databaseId);
        sessionId = value(sessionId);
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }
}
