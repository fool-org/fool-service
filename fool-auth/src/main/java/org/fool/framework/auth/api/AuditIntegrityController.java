package org.fool.framework.auth.api;

import org.fool.framework.auth.authorization.JdbcSecurityAuditService;
import org.fool.framework.common.authz.AuthorizationDecision;
import org.fool.framework.common.authz.AuthorizationDeniedException;
import org.fool.framework.common.authz.AuthorizationRequest;
import org.fool.framework.common.authz.AuthorizationService;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.EffectiveSubjectContext;
import org.fool.framework.dto.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authz")
public class AuditIntegrityController {
    private final AuthorizationService authorizationService;
    private final JdbcSecurityAuditService auditService;

    public AuditIntegrityController(AuthorizationService authorizationService,
                                    JdbcSecurityAuditService auditService) {
        this.authorizationService = authorizationService;
        this.auditService = auditService;
    }

    @GetMapping("/audit-integrity")
    public CommonResponse<JdbcSecurityAuditService.IntegrityReport> verify() {
        EffectiveSubject subject = EffectiveSubjectContext.require();
        AuthorizationDecision decision = authorizationService.decide(new AuthorizationRequest(
                subject, "audit.verify", "Auth", "auth:audit-integrity"));
        if (!decision.allowed()) {
            throw new AuthorizationDeniedException(decision.reasonCode());
        }
        return new CommonResponse<>(auditService.verifyAndAlert());
    }
}
