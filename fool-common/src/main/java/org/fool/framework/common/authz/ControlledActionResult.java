package org.fool.framework.common.authz;

import java.util.Map;

public record ControlledActionResult(String resultRef,
                                     Map<String, Object> response,
                                     Map<String, Object> auditSummary) {
    public ControlledActionResult {
        resultRef = resultRef == null ? "" : resultRef;
        response = response == null ? Map.of() : Map.copyOf(response);
        auditSummary = auditSummary == null ? Map.of() : Map.copyOf(auditSummary);
    }
}
