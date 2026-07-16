package org.fool.framework.agent.action;

import org.fool.framework.common.authz.RiskLevel;

import java.time.Instant;
import java.util.List;

public record ActionRequestRecord(String id,
                                  String ownerUserId,
                                  String agentSessionId,
                                  String source,
                                  String appId,
                                  String databaseId,
                                  String action,
                                  String resourceKey,
                                  String payloadJson,
                                  String payloadHash,
                                  String previewJson,
                                  String previewHash,
                                  RiskLevel risk,
                                  List<String> riskReasons,
                                  long policyVersion,
                                  ActionRequestStatus status,
                                  String idempotencyKey,
                                  Instant expiresAt,
                                  Instant createdAt,
                                  Instant updatedAt) {
}
