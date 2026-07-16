package org.fool.framework.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.common.authz.AuthorizationDecision;
import org.fool.framework.common.authz.AuthorizationRequest;
import org.fool.framework.common.authz.AuthorizationService;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.EffectiveSubjectContext;
import org.fool.framework.common.authz.RiskLevel;
import org.fool.framework.common.authz.SecurityAuditEvent;
import org.fool.framework.common.authz.SecurityAuditService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class BearerAuthenticationFilter extends OncePerRequestFilter {
    public static final String SUBJECT_ATTRIBUTE = EffectiveSubject.class.getName();
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/loginv2",
            "/api/v1/auth/initapp",
            "/api/v1/auth/getcheckcode",
            "/api/v1/auth/getchk",
            "/api/v1/auth/checkcode");
    private static final Set<String> CONTROLLED_ACTION_PATHS = Set.of(
            "/api/v1/data/saveobj",
            "/api/v1/data/save",
            "/api/v1/data/savenewobj",
            "/api/v1/data/new",
            "/api/v1/data/runoperation",
            "/api/v1/data/exoperation",
            "/api/v1/report/saverpt");

    private final EffectiveSubjectResolver subjectResolver;
    private final AuthorizationService authorizationService;
    private final SecurityAuditService auditService;
    private final ObjectMapper objectMapper;

    public BearerAuthenticationFilter(EffectiveSubjectResolver subjectResolver,
                                      AuthorizationService authorizationService,
                                      SecurityAuditService auditService,
                                      ObjectMapper objectMapper) {
        this.subjectResolver = subjectResolver;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !path.startsWith("/api/v1/")
                || PUBLIC_PATHS.contains(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = traceId(request);
        String authorization = request.getHeader("Authorization");
        String bearerToken = bearerToken(authorization);
        if (StringUtils.hasText(authorization) && !StringUtils.hasText(bearerToken)) {
            if (!auditAuthenticationDeny(request, response, traceId, "AUTHENTICATION_SCHEME_INVALID")) {
                return;
            }
            deny(response, traceId, "AUTHENTICATION_SCHEME_INVALID", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (!StringUtils.hasText(bearerToken)) {
            if (!auditAuthenticationDeny(request, response, traceId, "AUTHENTICATION_REQUIRED")) {
                return;
            }
            deny(response, traceId, "AUTHENTICATION_REQUIRED", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        EffectiveSubject subject;
        try {
            subject = subjectResolver.resolve(bearerToken);
            audit(request, traceId, subject.userId(), "request.authenticate", request.getRequestURI(),
                    "ALLOW", "AUTHENTICATED", subject.policyVersion());
        } catch (RuntimeException ex) {
            try {
                audit(request, traceId, null, "request.authenticate", request.getRequestURI(),
                        "DENY", "AUTHENTICATION_INVALID", 0);
            } catch (RuntimeException ignored) {
                deny(response, traceId, "AUDIT_UNAVAILABLE", HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }
            deny(response, traceId, "AUTHENTICATION_INVALID", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (request.getRequestURI().startsWith("/api/v1/agent/")) {
            AuthorizationDecision decision = authorizationService.decide(new AuthorizationRequest(
                    subject, "agent.use", "AgentCapability", "agent:capability:*"));
            try {
                audit(request, traceId, subject.userId(), "agent.use", "agent:capability:*",
                        decision.allowed() ? "ALLOW" : "DENY", decision.reasonCode(), decision.policyVersion());
            } catch (RuntimeException ex) {
                deny(response, traceId, "AUDIT_UNAVAILABLE", HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }
            if (!decision.allowed()) {
                deny(response, traceId, decision.reasonCode(), HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        if (CONTROLLED_ACTION_PATHS.contains(request.getRequestURI())) {
            try {
                audit(request, traceId, subject.userId(), "controlled-action.required", request.getRequestURI(),
                        "DENY", "ACTION_WORKFLOW_REQUIRED", subject.policyVersion());
            } catch (RuntimeException ex) {
                deny(response, traceId, "AUDIT_UNAVAILABLE", HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }
            deny(response, traceId, "ACTION_WORKFLOW_REQUIRED", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        request.setAttribute(SUBJECT_ATTRIBUTE, subject);
        EffectiveSubjectContext.set(subject);
        try {
            filterChain.doFilter(request, response);
        } finally {
            EffectiveSubjectContext.clear();
        }
    }

    private boolean auditAuthenticationDeny(HttpServletRequest request,
                                            HttpServletResponse response,
                                            String traceId,
                                            String reason) throws IOException {
        try {
            audit(request, traceId, null, "request.authenticate", request.getRequestURI(),
                    "DENY", reason, 0);
            return true;
        } catch (RuntimeException ex) {
            deny(response, traceId, "AUDIT_UNAVAILABLE", HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return false;
        }
    }

    private static String bearerToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        if (!authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        return normalized(authorization.substring(7));
    }

    private void audit(HttpServletRequest request,
                       String traceId,
                       String userId,
                       String action,
                       String resource,
                       String decision,
                       String reason,
                       long policyVersion) {
        auditService.record(new SecurityAuditEvent(
                UUID.randomUUID().toString(),
                traceId,
                userId,
                "HTTP",
                null,
                null,
                action,
                resource,
                decision,
                reason,
                RiskLevel.LOW,
                policyVersion,
                sha256(request.getRemoteAddr()),
                sanitized(request.getHeader("User-Agent"), 500),
                Instant.now()));
    }

    private void deny(HttpServletResponse response, String traceId, String reason, int status) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "code", status,
                "message", reason,
                "traceId", traceId));
    }

    private static String traceId(HttpServletRequest request) {
        String supplied = request.getHeader("X-Trace-Id");
        if (StringUtils.hasText(supplied) && supplied.matches("[A-Za-z0-9._:-]{1,128}")) {
            return supplied;
        }
        return UUID.randomUUID().toString();
    }

    private static String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(normalized(value).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }

    private static String sanitized(String value, int limit) {
        String normalized = normalized(value).replace('\r', ' ').replace('\n', ' ');
        return normalized.length() <= limit ? normalized : normalized.substring(0, limit);
    }

    private static String normalized(String value) {
        return value == null ? "" : value.trim();
    }

}
