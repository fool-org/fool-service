package org.fool.framework.auth.authorization;

import org.fool.framework.auth.foolframework.auth.MenuItem;
import org.fool.framework.common.authz.AuthorizationDecision;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.EffectiveSubjectContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class MenuAuthorizationServiceTest {
    @Before
    public void setSubject() {
        EffectiveSubjectContext.set(new EffectiveSubject(
                "user-1", List.of(), "company-1", List.of(), "fool-service", "car_wash",
                "session-1", Instant.parse("2026-07-15T00:00:00Z"), null, 1));
    }

    @After
    public void clearSubject() {
        EffectiveSubjectContext.clear();
    }

    @Test
    public void menuDiscoveryIsFilteredByViewAuthorization() {
        MenuAuthorizationService service = new MenuAuthorizationService(
                request -> request.resourceKey().endsWith(":100")
                        ? AuthorizationDecision.allow(1, null)
                        : AuthorizationDecision.deny("NO_MATCHING_ALLOW", 1),
                mock(JdbcTemplate.class),
                event -> { });
        MenuItem allowed = new MenuItem();
        allowed.setViewId(100L);
        MenuItem denied = new MenuItem();
        denied.setViewId(200L);
        MenuItem folder = new MenuItem();
        folder.setViewId(0L);

        List<MenuItem> visible = service.visibleLegacyMenus(List.of(allowed, denied, folder));

        assertEquals(List.of(allowed, folder), visible);
    }

    @Test
    public void auditFailureHidesProtectedMenu() {
        MenuAuthorizationService service = new MenuAuthorizationService(
                request -> AuthorizationDecision.allow(1, null),
                mock(JdbcTemplate.class),
                event -> { throw new IllegalStateException("audit down"); });
        MenuItem protectedMenu = new MenuItem();
        protectedMenu.setViewId(100L);

        assertEquals(List.of(), service.visibleLegacyMenus(List.of(protectedMenu)));
    }
}
