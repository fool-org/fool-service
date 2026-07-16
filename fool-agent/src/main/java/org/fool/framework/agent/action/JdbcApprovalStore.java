package org.fool.framework.agent.action;

import org.fool.framework.common.authz.ControlledActionException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class JdbcApprovalStore {
    private final JdbcTemplate jdbcTemplate;

    public JdbcApprovalStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(ApprovalRecord approval) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO `FOOL_AGENT_APPROVAL`
                      (`APPROVAL_ID`, `ACTION_REQUEST_ID`, `APPROVER_USER_ID`, `DECISION`,
                       `PAYLOAD_HASH`, `PREVIEW_HASH`, `COMMENT`, `APPROVER_POLICY_VERSION`,
                       `DECIDED_AT`, `EXPIRES_AT`)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, approval.id(), approval.actionRequestId(), approval.approverUserId(),
                    approval.decision(), approval.payloadHash(), approval.previewHash(), approval.comment(),
                    approval.approverPolicyVersion(), Timestamp.from(approval.decidedAt()),
                    Timestamp.from(approval.expiresAt()));
        } catch (DuplicateKeyException ex) {
            throw new ControlledActionException(409, "APPROVAL_REPLAY");
        }
    }

    public List<ApprovalRecord> list(String requestId) {
        return jdbcTemplate.query("""
                SELECT * FROM `FOOL_AGENT_APPROVAL`
                 WHERE `ACTION_REQUEST_ID` = ? ORDER BY `DECIDED_AT`, `APPROVAL_ID`
                """, (rs, rowNum) -> new ApprovalRecord(
                rs.getString("APPROVAL_ID"), rs.getString("ACTION_REQUEST_ID"),
                rs.getString("APPROVER_USER_ID"), rs.getString("DECISION"),
                rs.getString("PAYLOAD_HASH"), rs.getString("PREVIEW_HASH"),
                rs.getString("COMMENT"), rs.getLong("APPROVER_POLICY_VERSION"),
                rs.getTimestamp("DECIDED_AT").toInstant(), rs.getTimestamp("EXPIRES_AT").toInstant()),
                requestId);
    }

    public long validApprovalCount(String requestId, Instant now) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(DISTINCT `APPROVER_USER_ID`) FROM `FOOL_AGENT_APPROVAL`
                 WHERE `ACTION_REQUEST_ID` = ? AND `DECISION` = 'APPROVE' AND `EXPIRES_AT` > ?
                """, Long.class, requestId, Timestamp.from(now));
        return count == null ? 0 : count;
    }
}
