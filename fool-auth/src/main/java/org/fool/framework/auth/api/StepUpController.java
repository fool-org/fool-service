package org.fool.framework.auth.api;

import org.fool.framework.auth.business.service.CredentialService;
import org.fool.framework.auth.business.service.TokenService;
import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.EffectiveSubjectContext;
import org.fool.framework.common.authz.RiskLevel;
import org.fool.framework.common.authz.SecurityAuditEvent;
import org.fool.framework.common.authz.SecurityAuditService;
import org.fool.framework.dto.CommonResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class StepUpController {
    private final CredentialService credentialService;
    private final TokenService tokenService;
    private final SecurityAuditService auditService;

    public StepUpController(CredentialService credentialService,
                            TokenService tokenService,
                            SecurityAuditService auditService) {
        this.credentialService = credentialService;
        this.tokenService = tokenService;
        this.auditService = auditService;
    }

    @PostMapping("/step-up")
    public CommonResponse<Map<String, Object>> stepUp(@RequestBody StepUpRequest request) {
        EffectiveSubject subject = EffectiveSubjectContext.require();
        String hash = credentialService.passwordHash(subject.userId())
                .orElseThrow(() -> denied(subject, "STEP_UP_CREDENTIAL_MIGRATION_REQUIRED"));
        if (request == null || !credentialService.matches(request.password, hash)) {
            throw denied(subject, "STEP_UP_FAILED");
        }
        Instant now = Instant.now();
        audit(subject, "ALLOW", "STEP_UP_SUCCEEDED", now);
        tokenService.recordStepUp(subject.sessionId());
        return new CommonResponse<>(Map.of("stepUpAt", now, "expiresAt", now.plusSeconds(300)));
    }

    private ControlledActionException denied(EffectiveSubject subject, String reason) {
        audit(subject, "DENY", reason, Instant.now());
        return new ControlledActionException(403, reason);
    }

    private void audit(EffectiveSubject subject, String decision, String reason, Instant now) {
        auditService.record(new SecurityAuditEvent(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                subject.userId(), "HTTP", null, null, "auth.step-up", "auth-session:" + subject.sessionId(),
                decision, reason, RiskLevel.HIGH, subject.policyVersion(), null, null, now));
    }

    public static class StepUpRequest {
        public String password;
    }
}
