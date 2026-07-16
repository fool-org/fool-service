package org.fool.framework.common.authz;

import java.time.Instant;

public record SecurityAuditEvent(String auditEventId,
                                 String traceId,
                                 String actorUserId,
                                 String source,
                                 String agentSessionId,
                                 String actionRequestId,
                                 String action,
                                 String resourceKey,
                                 String decision,
                                 String reasonCode,
                                 RiskLevel riskLevel,
                                 long policyVersion,
                                 String remoteAddressHash,
                                 String userAgent,
                                 Instant createdAt) {
}
