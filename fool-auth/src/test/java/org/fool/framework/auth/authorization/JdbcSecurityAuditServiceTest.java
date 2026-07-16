package org.fool.framework.auth.authorization;

import org.fool.framework.common.authz.RiskLevel;
import org.fool.framework.common.authz.SecurityAuditEvent;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class JdbcSecurityAuditServiceTest {
    @Test
    public void hashIsDeterministicAndBindsEveryAuditField() {
        Timestamp createdAt = Timestamp.from(Instant.parse("2026-07-15T11:00:00.123456Z"));
        SecurityAuditEvent original = event("ALLOW");

        String first = JdbcSecurityAuditService.eventHash("0".repeat(64), original, createdAt);
        String second = JdbcSecurityAuditService.eventHash("0".repeat(64), original, createdAt);
        String tampered = JdbcSecurityAuditService.eventHash(
                "0".repeat(64), event("DENY"), createdAt);

        assertEquals(first, second);
        assertEquals(64, first.length());
        assertNotEquals(first, tampered);
    }

    private static SecurityAuditEvent event(String decision) {
        return new SecurityAuditEvent(
                "event-1", "trace-1", "admin", "HTTP", "session-1", "request-1",
                "data.update", "app:fool-service:db:car_wash:view:100", decision,
                "EXECUTION_RECHECK_PASSED", RiskLevel.MEDIUM, 7L,
                "remote-hash", "test-agent", Instant.parse("2026-07-15T11:00:00.123456Z"));
    }
}
