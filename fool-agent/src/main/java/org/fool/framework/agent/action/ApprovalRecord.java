package org.fool.framework.agent.action;

import java.time.Instant;

public record ApprovalRecord(String id,
                             String actionRequestId,
                             String approverUserId,
                             String decision,
                             String payloadHash,
                             String previewHash,
                             String comment,
                             long approverPolicyVersion,
                             Instant decidedAt,
                             Instant expiresAt) {
}
