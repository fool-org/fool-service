package org.fool.framework.auth.authorization;

import org.fool.framework.common.authz.AuthorizationDecision;
import org.fool.framework.common.authz.AuthorizationRequest;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.RiskLevel;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JdbcAuthorizationServiceTest {
    @Test
    public void explicitDenyWinsOverAllow() {
        JdbcAuthorizationService service = service(List.of(
                row("ROLE", "auth:1", "ALLOW", "agent:capability:*", 0),
                row("USER", "admin", "DENY", "agent:capability:*", 0)));

        AuthorizationDecision decision = service.decide(request("agent:capability:report-query"));

        assertFalse(decision.allowed());
        assertEquals("EXPLICIT_DENY", decision.reasonCode());
    }

    @Test
    public void noMatchingAllowIsDenied() {
        JdbcAuthorizationService service = service(List.of());

        AuthorizationDecision decision = service.decide(request("agent:capability:report-query"));

        assertFalse(decision.allowed());
        assertEquals("NO_MATCHING_ALLOW", decision.reasonCode());
    }

    @Test
    public void numericIncludeChildrenAllowsOnlyDescendants() {
        JdbcAuthorizationService service = service(List.of(
                row("ROLE", "auth:1", "ALLOW", "agent:capability", 1)));

        assertTrue(service.decide(request("agent:capability:report-query")).allowed());
        assertFalse(service.decide(request("other:capability:report-query")).allowed());
    }

    @Test
    public void permissionRiskFloorsCanOnlyRaiseTheDecision() {
        JdbcAuthorizationService service = service(List.of(
                row("ROLE", "auth:1", "ALLOW", "agent:capability:*", 0, "MEDIUM"),
                row("USER", "admin", "ALLOW", "agent:capability:*", 0, "HIGH")));

        assertEquals(RiskLevel.HIGH,
                service.decide(request("agent:capability:report-query")).minimumRisk());
    }

    private static JdbcAuthorizationService service(List<Map<String, Object>> rows) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public List<Map<String, Object>> queryForList(String sql, Object... args) {
                return rows;
            }
        };
        return new JdbcAuthorizationService(jdbcTemplate);
    }

    private static AuthorizationRequest request(String resourceKey) {
        EffectiveSubject subject = new EffectiveSubject(
                "admin",
                List.of("auth:1"),
                "company-1",
                List.of("department-1"),
                "fool-service",
                "car_wash",
                "auth-session",
                Instant.parse("2026-07-15T00:00:00Z"),
                null,
                7L);
        return new AuthorizationRequest(subject, "agent.use", "AgentCapability", resourceKey);
    }

    private static Map<String, Object> row(String type,
                                           String id,
                                           String effect,
                                           String resourcePattern,
                                           int includeChildren) {
        return row(type, id, effect, resourcePattern, includeChildren, "LOW");
    }

    private static Map<String, Object> row(String type,
                                           String id,
                                           String effect,
                                           String resourcePattern,
                                           int includeChildren,
                                           String minimumRisk) {
        return Map.of(
                "SUBJECT_TYPE", type,
                "SUBJECT_ID", id,
                "EFFECT", effect,
                "RESOURCE_PATTERN", resourcePattern,
                "INCLUDE_CHILDREN", includeChildren,
                "MIN_RISK", minimumRisk);
    }
}
