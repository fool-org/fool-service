package org.fool.framework.auth.authorization;

import org.fool.framework.common.authz.SecurityAuditEvent;
import org.fool.framework.common.authz.SecurityAuditService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JdbcSecurityAuditService implements SecurityAuditService {
    private static final String CHAIN_ID = "primary";
    private static final String GENESIS_HASH = "0".repeat(64);

    private final JdbcTemplate jdbcTemplate;

    public JdbcSecurityAuditService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(SecurityAuditEvent event) {
        Head head = head(true);
        Timestamp createdAt = timestamp(event.createdAt());
        String eventHash = eventHash(head.lastHash(), event, createdAt, head.eventCount() + 1);
        jdbcTemplate.update("""
                INSERT INTO `FOOL_SECURITY_AUDIT_EVENT`
                    (`AUDIT_EVENT_ID`, `TRACE_ID`, `ACTOR_USER_ID`, `SOURCE`,
                     `AGENT_SESSION_ID`, `ACTION_REQUEST_ID`, `ACTION_ID`, `RESOURCE_KEY`,
                     `DECISION`, `REASON_CODE`, `RISK_LEVEL`, `POLICY_VERSION`,
                     `REMOTE_ADDRESS_HASH`, `USER_AGENT`, `CREATED_AT`,
                     `CHAIN_SEQUENCE`, `PREVIOUS_HASH`, `EVENT_HASH`)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                event.auditEventId(), event.traceId(), event.actorUserId(), event.source(),
                event.agentSessionId(), event.actionRequestId(), event.action(), event.resourceKey(),
                event.decision(), event.reasonCode(),
                event.riskLevel() == null ? null : event.riskLevel().name(), event.policyVersion(),
                event.remoteAddressHash(), event.userAgent(), createdAt, head.eventCount() + 1,
                head.lastHash(), eventHash);
        jdbcTemplate.update("""
                UPDATE `FOOL_SECURITY_AUDIT_HEAD`
                   SET `LAST_EVENT_ID` = ?,
                       `LAST_EVENT_HASH` = ?,
                       `EVENT_COUNT` = ?,
                       `UPDATED_AT` = CURRENT_TIMESTAMP(6)
                 WHERE `CHAIN_ID` = ?
                """, event.auditEventId(), eventHash, head.eventCount() + 1, CHAIN_ID);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IntegrityReport verifyAndAlert() {
        head(true);
        IntegrityReport report = verifyUnlocked();
        if (!report.valid()) {
            jdbcTemplate.update("""
                    INSERT INTO `FOOL_SECURITY_ALERT`
                        (`ALERT_ID`, `ALERT_TYPE`, `SEVERITY`, `REASON_CODE`, `DETAILS`, `CREATED_AT`)
                    SELECT ?, 'AUDIT_INTEGRITY', 'CRITICAL', ?, ?, CURRENT_TIMESTAMP(6)
                     WHERE NOT EXISTS (
                           SELECT 1 FROM `FOOL_SECURITY_ALERT`
                            WHERE `ALERT_TYPE` = 'AUDIT_INTEGRITY'
                              AND `REASON_CODE` = ?
                              AND `ACKNOWLEDGED_AT` IS NULL
                              AND `CREATED_AT` > CURRENT_TIMESTAMP(6) - INTERVAL 1 HOUR)
                    """, UUID.randomUUID().toString(), report.reasonCode(),
                    limited(report.details(), 1000), report.reasonCode());
        }
        return report;
    }

    IntegrityReport verifyUnlocked() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT `AUDIT_EVENT_ID`, `TRACE_ID`, `ACTOR_USER_ID`, `SOURCE`,
                       `AGENT_SESSION_ID`, `ACTION_REQUEST_ID`, `ACTION_ID`, `RESOURCE_KEY`,
                       `DECISION`, `REASON_CODE`, `RISK_LEVEL`, `POLICY_VERSION`,
                       `REMOTE_ADDRESS_HASH`, `USER_AGENT`, `CREATED_AT`,
                       `CHAIN_SEQUENCE`, `PREVIOUS_HASH`, `EVENT_HASH`
                  FROM `FOOL_SECURITY_AUDIT_EVENT`
                 WHERE `EVENT_HASH` IS NOT NULL
                 ORDER BY `CHAIN_SEQUENCE`
                """);
        String previous = GENESIS_HASH;
        String lastEventId = null;
        long count = 0;
        for (Map<String, Object> row : rows) {
            String eventId = value(row, "AUDIT_EVENT_ID");
            if (((Number) row.get("CHAIN_SEQUENCE")).longValue() != count + 1) {
                return IntegrityReport.failed("AUDIT_CHAIN_SEQUENCE_MISMATCH", eventId, count);
            }
            if (!MessageDigest.isEqual(previous.getBytes(StandardCharsets.UTF_8),
                    value(row, "PREVIOUS_HASH").getBytes(StandardCharsets.UTF_8))) {
                return IntegrityReport.failed("AUDIT_CHAIN_LINK_MISMATCH", eventId, count);
            }
            String expected = eventHash(previous, row);
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    value(row, "EVENT_HASH").getBytes(StandardCharsets.UTF_8))) {
                return IntegrityReport.failed("AUDIT_EVENT_HASH_MISMATCH", eventId, count);
            }
            previous = expected;
            lastEventId = eventId;
            count++;
        }
        Head head = head(false);
        if (head.eventCount() != count || !head.lastHash().equals(previous)
                || (count > 0 && !head.lastEventId().equals(lastEventId))) {
            return IntegrityReport.failed("AUDIT_HEAD_MISMATCH", lastEventId, count);
        }
        return new IntegrityReport(true, "AUDIT_CHAIN_VALID", "verified", count, lastEventId, previous);
    }

    private Head head(boolean forUpdate) {
        List<Head> rows = jdbcTemplate.query("""
                        SELECT `LAST_EVENT_ID`, `LAST_EVENT_HASH`, `EVENT_COUNT`
                          FROM `FOOL_SECURITY_AUDIT_HEAD`
                         WHERE `CHAIN_ID` = ?
                        """ + (forUpdate ? " FOR UPDATE" : ""),
                (rs, rowNum) -> new Head(
                        rs.getString("LAST_EVENT_ID"),
                        rs.getString("LAST_EVENT_HASH"),
                        rs.getLong("EVENT_COUNT")),
                CHAIN_ID);
        if (rows.isEmpty()) {
            throw new IllegalStateException("AUDIT_CHAIN_HEAD_MISSING");
        }
        return rows.get(0);
    }

    static String eventHash(String previousHash, SecurityAuditEvent event, Timestamp createdAt) {
        return eventHash(previousHash, event, createdAt, 1);
    }

    private static String eventHash(String previousHash, SecurityAuditEvent event,
                                    Timestamp createdAt, long chainSequence) {
        return sha256(canonical(Arrays.asList(
                previousHash, chainSequence, event.auditEventId(), event.traceId(), event.actorUserId(), event.source(),
                event.agentSessionId(), event.actionRequestId(), event.action(), event.resourceKey(),
                event.decision(), event.reasonCode(),
                event.riskLevel() == null ? null : event.riskLevel().name(), event.policyVersion(),
                event.remoteAddressHash(), event.userAgent(), instant(createdAt))));
    }

    private static String eventHash(String previousHash, Map<String, Object> row) {
        return sha256(canonical(Arrays.asList(
                previousHash, row.get("CHAIN_SEQUENCE"), row.get("AUDIT_EVENT_ID"),
                row.get("TRACE_ID"), row.get("ACTOR_USER_ID"),
                row.get("SOURCE"), row.get("AGENT_SESSION_ID"), row.get("ACTION_REQUEST_ID"),
                row.get("ACTION_ID"), row.get("RESOURCE_KEY"), row.get("DECISION"), row.get("REASON_CODE"),
                row.get("RISK_LEVEL"), row.get("POLICY_VERSION"), row.get("REMOTE_ADDRESS_HASH"),
                row.get("USER_AGENT"), instant((Timestamp) row.get("CREATED_AT")))));
    }

    private static String canonical(List<?> values) {
        StringBuilder result = new StringBuilder();
        for (Object value : values) {
            String text = value == null ? "" : String.valueOf(value);
            result.append(text.length()).append(':').append(text);
        }
        return result.toString();
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }

    private static Timestamp timestamp(Instant value) {
        Instant normalized = (value == null ? Instant.now() : value).truncatedTo(ChronoUnit.MICROS);
        return Timestamp.from(normalized);
    }

    private static String instant(Timestamp value) {
        return value == null ? "" : value.toInstant().truncatedTo(ChronoUnit.MICROS).toString();
    }

    private static String value(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private static String limited(String value, int max) {
        return value == null ? "" : value.substring(0, Math.min(max, value.length()));
    }

    private record Head(String lastEventId, String lastHash, long eventCount) {
        private Head {
            lastEventId = lastEventId == null ? "" : lastEventId;
            lastHash = lastHash == null || lastHash.isBlank() ? GENESIS_HASH : lastHash;
        }
    }

    public record IntegrityReport(boolean valid,
                                  String reasonCode,
                                  String details,
                                  long eventCount,
                                  String lastEventId,
                                  String lastEventHash) {
        private static IntegrityReport failed(String reason, String eventId, long count) {
            return new IntegrityReport(false, reason,
                    "event=" + (eventId == null ? "" : eventId), count,
                    eventId == null ? "" : eventId, "");
        }
    }
}
