package org.fool.framework.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.common.authz.AuthorizationDecision;
import org.fool.framework.common.authz.AuthorizationService;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.SecurityAuditService;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BearerAuthenticationFilterTest {
    @Test
    public void missingAuthenticationIsRejectedAndAudited() throws Exception {
        Fixture fixture = fixture();

        fixture.filter.doFilter(fixture.request("/api/v1/data/query", "{}"), fixture.response, fixture.chain);

        assertEquals(401, fixture.response.getStatus());
        verify(fixture.auditService).record(any());
        verify(fixture.subjectResolver, never()).resolve(any());
    }

    @Test
    public void bodyTokenCannotAuthenticate() throws Exception {
        Fixture fixture = fixture();

        MockHttpServletRequest request = fixture.request(
                "/api/v1/agent/sessions", "{\"token\":\"body-token\",\"title\":\"draft\"}");
        fixture.filter.doFilter(request, fixture.response, fixture.chain);

        assertEquals(401, fixture.response.getStatus());
        verify(fixture.subjectResolver, never()).resolve(any());
    }

    @Test
    public void bearerIsAuthoritativeWhenBodyContainsLegacyToken() throws Exception {
        Fixture fixture = fixture();
        when(fixture.subjectResolver.resolve("header-token")).thenReturn(subject());
        when(fixture.authorizationService.decide(any())).thenReturn(AuthorizationDecision.allow(7L, null));
        MockHttpServletRequest request = fixture.request(
                "/api/v1/agent/sessions", "{\"token\":\"body-token\"}");
        request.addHeader("Authorization", "Bearer header-token");

        fixture.filter.doFilter(request, fixture.response, fixture.chain);

        assertEquals(200, fixture.response.getStatus());
        verify(fixture.subjectResolver).resolve("header-token");
    }

    @Test
    public void agentUseIsDefaultDenied() throws Exception {
        Fixture fixture = fixture();
        when(fixture.subjectResolver.resolve("header-token")).thenReturn(subject());
        when(fixture.authorizationService.decide(any()))
                .thenReturn(AuthorizationDecision.deny("NO_MATCHING_ALLOW", 7L));
        MockHttpServletRequest request = fixture.request("/api/v1/agent/sessions", "{}");
        request.addHeader("Authorization", "Bearer header-token");

        fixture.filter.doFilter(request, fixture.response, fixture.chain);

        assertEquals(403, fixture.response.getStatus());
        assertEquals(null, fixture.chain.getRequest());
    }

    @Test
    public void auditFailureFailsClosed() throws Exception {
        Fixture fixture = fixture();
        doThrow(new IllegalStateException("database unavailable")).when(fixture.auditService).record(any());

        fixture.filter.doFilter(fixture.request("/api/v1/data/query", "{}"), fixture.response, fixture.chain);

        assertEquals(503, fixture.response.getStatus());
    }

    @Test
    public void legacyWriteEndpointsRequireControlledActionWorkflow() throws Exception {
        Fixture fixture = fixture();
        when(fixture.subjectResolver.resolve("header-token")).thenReturn(subject());
        MockHttpServletRequest request = fixture.request("/api/v1/data/save", "{}");
        request.addHeader("Authorization", "Bearer header-token");

        fixture.filter.doFilter(request, fixture.response, fixture.chain);

        assertEquals(403, fixture.response.getStatus());
        assertEquals(null, fixture.chain.getRequest());
    }

    private static Fixture fixture() {
        EffectiveSubjectResolver subjectResolver = mock(EffectiveSubjectResolver.class);
        AuthorizationService authorizationService = mock(AuthorizationService.class);
        SecurityAuditService auditService = mock(SecurityAuditService.class);
        return new Fixture(
                subjectResolver,
                authorizationService,
                auditService,
                new BearerAuthenticationFilter(
                        subjectResolver, authorizationService, auditService, new ObjectMapper()),
                new MockHttpServletResponse(),
                new MockFilterChain());
    }

    private static EffectiveSubject subject() {
        return new EffectiveSubject(
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
    }

    private record Fixture(EffectiveSubjectResolver subjectResolver,
                           AuthorizationService authorizationService,
                           SecurityAuditService auditService,
                           BearerAuthenticationFilter filter,
                           MockHttpServletResponse response,
                           MockFilterChain chain) {
        private MockHttpServletRequest request(String path, String json) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
            request.setContentType("application/json");
            request.setContent(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return request;
        }
    }
}
