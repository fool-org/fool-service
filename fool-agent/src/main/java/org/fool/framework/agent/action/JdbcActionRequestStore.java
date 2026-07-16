package org.fool.framework.agent.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.PolicyVersionQuery;
import org.fool.framework.common.authz.RiskLevel;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcActionRequestStore {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcActionRequestStore(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void insert(ActionRequestRecord request) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO `FOOL_AGENT_ACTION_REQUEST`
                      (`ACTION_REQUEST_ID`, `OWNER_USER_ID`, `AGENT_SESSION_ID`, `SOURCE`,
                       `APP_ID`, `DATABASE_ID`, `ACTION_ID`, `RESOURCE_KEY`, `PAYLOAD_JSON`,
                       `PAYLOAD_HASH`, `PREVIEW_JSON`, `PREVIEW_HASH`, `RISK_LEVEL`,
                       `RISK_REASONS_JSON`, `POLICY_VERSION`, `STATUS`, `IDEMPOTENCY_KEY`,
                       `EXPIRES_AT`, `CREATED_AT`, `UPDATED_AT`)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    request.id(), request.ownerUserId(), request.agentSessionId(), request.source(),
                    request.appId(), request.databaseId(), request.action(), request.resourceKey(),
                    request.payloadJson(), request.payloadHash(), request.previewJson(), request.previewHash(),
                    request.risk().name(), json(request.riskReasons()), request.policyVersion(),
                    request.status().name(), request.idempotencyKey(), Timestamp.from(request.expiresAt()),
                    Timestamp.from(request.createdAt()), Timestamp.from(request.updatedAt()));
        } catch (DuplicateKeyException ex) {
            throw new ControlledActionException(409, "IDEMPOTENCY_CONFLICT");
        }
    }

    public Optional<ActionRequestRecord> find(String id) {
        List<ActionRequestRecord> rows = jdbcTemplate.query("""
                SELECT * FROM `FOOL_AGENT_ACTION_REQUEST` WHERE `ACTION_REQUEST_ID` = ?
                """, (rs, rowNum) -> new ActionRequestRecord(
                rs.getString("ACTION_REQUEST_ID"),
                rs.getString("OWNER_USER_ID"),
                rs.getString("AGENT_SESSION_ID"),
                rs.getString("SOURCE"),
                rs.getString("APP_ID"),
                rs.getString("DATABASE_ID"),
                rs.getString("ACTION_ID"),
                rs.getString("RESOURCE_KEY"),
                rs.getString("PAYLOAD_JSON"),
                rs.getString("PAYLOAD_HASH"),
                rs.getString("PREVIEW_JSON"),
                rs.getString("PREVIEW_HASH"),
                RiskLevel.valueOf(rs.getString("RISK_LEVEL")),
                readReasons(rs.getString("RISK_REASONS_JSON")),
                rs.getLong("POLICY_VERSION"),
                ActionRequestStatus.valueOf(rs.getString("STATUS")),
                rs.getString("IDEMPOTENCY_KEY"),
                rs.getTimestamp("EXPIRES_AT").toInstant(),
                rs.getTimestamp("CREATED_AT").toInstant(),
                rs.getTimestamp("UPDATED_AT").toInstant()), id);
        return rows.stream().findFirst();
    }

    public void savePreview(String id,
                            ActionRequestStatus expected,
                            String payloadJson,
                            String payloadHash,
                            String previewJson,
                            String previewHash,
                            RiskLevel risk,
                            List<String> reasons,
                            long policyVersion,
                            ActionRequestStatus status,
                            Instant expiresAt,
                            Instant now) {
        int updated = jdbcTemplate.update("""
                UPDATE `FOOL_AGENT_ACTION_REQUEST`
                   SET `PAYLOAD_JSON` = ?, `PAYLOAD_HASH` = ?, `PREVIEW_JSON` = ?,
                       `PREVIEW_HASH` = ?, `RISK_LEVEL` = ?, `RISK_REASONS_JSON` = ?,
                       `POLICY_VERSION` = ?, `STATUS` = ?, `EXPIRES_AT` = ?, `UPDATED_AT` = ?
                 WHERE `ACTION_REQUEST_ID` = ? AND `STATUS` = ?
                """, payloadJson, payloadHash, previewJson, previewHash, risk.name(), json(reasons),
                policyVersion, status.name(), Timestamp.from(expiresAt), Timestamp.from(now), id, expected.name());
        requireOne(updated, "ACTION_STATE_CONFLICT");
    }

    public void transition(String id, ActionRequestStatus from, ActionRequestStatus to, Instant now) {
        int updated = jdbcTemplate.update("""
                UPDATE `FOOL_AGENT_ACTION_REQUEST`
                   SET `STATUS` = ?, `UPDATED_AT` = ?
                 WHERE `ACTION_REQUEST_ID` = ? AND `STATUS` = ?
                """, to.name(), Timestamp.from(now), id, from.name());
        requireOne(updated, "ACTION_STATE_CONFLICT");
    }

    public long currentPolicyVersion(String appId, String databaseId) {
        Long version = jdbcTemplate.queryForObject(
                PolicyVersionQuery.SQL, Long.class, appId, databaseId, appId, databaseId);
        return version == null ? 0L : version;
    }

    private static void requireOne(int updated, String reason) {
        if (updated != 1) {
            throw new ControlledActionException(409, reason);
        }
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private List<String> readReasons(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            throw new ControlledActionException(409, "ACTION_PAYLOAD_INVALID");
        }
    }
}
