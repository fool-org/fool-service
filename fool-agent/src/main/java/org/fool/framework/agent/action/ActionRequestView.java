package org.fool.framework.agent.action;

import org.fool.framework.common.authz.RiskLevel;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ActionRequestView(String actionRequestId,
                                String action,
                                String resourceKey,
                                String source,
                                RiskLevel riskLevel,
                                List<String> riskReasons,
                                ActionRequestStatus status,
                                Map<String, Object> preview,
                                Instant expiresAt,
                                boolean owned,
                                boolean confirmable,
                                boolean executable,
                                boolean approvable,
                                boolean cancellable,
                                int requiredApprovals,
                                long approvalCount,
                                Map<String, Object> result) {
    public ActionRequestView {
        riskReasons = riskReasons == null ? List.of() : List.copyOf(riskReasons);
        preview = preview == null ? Map.of() : Map.copyOf(preview);
        result = result == null ? Map.of() : Map.copyOf(result);
    }
}
