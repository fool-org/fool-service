package org.fool.framework.application.api;

import org.fool.framework.agent.action.ActionCatalog;
import org.fool.framework.common.authz.AuthorizationDecision;
import org.fool.framework.common.authz.AuthorizationRequest;
import org.fool.framework.common.authz.AuthorizationService;
import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.EffectiveSubjectContext;
import org.fool.framework.common.authz.RiskLevel;
import org.fool.framework.common.authz.SecurityAuditEvent;
import org.fool.framework.common.authz.SecurityAuditService;
import org.fool.framework.dto.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authz")
public class EffectiveActionsController {
    private final ActionCatalog catalog;
    private final AuthorizationService authorizationService;
    private final SecurityAuditService auditService;

    public EffectiveActionsController(ActionCatalog catalog,
                                      AuthorizationService authorizationService,
                                      SecurityAuditService auditService) {
        this.catalog = catalog;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
    }

    @GetMapping("/effective-actions")
    public CommonResponse<EffectiveActionsResult> effectiveActions(
            @RequestParam String resourceType,
            @RequestParam String resourceId) {
        EffectiveSubject subject = EffectiveSubjectContext.require();
        String canonicalType = canonicalType(resourceType);
        String resourceKey = resourceKey(subject, canonicalType, resourceId);
        List<EffectiveAction> actions = catalog.definitions().entrySet().stream()
                .filter(entry -> entry.getValue().resourceType().equals(canonicalType))
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(entry -> decision(subject, resourceKey, entry.getKey(), entry.getValue()))
                .filter(java.util.Objects::nonNull)
                .toList();
        return new CommonResponse<>(new EffectiveActionsResult(
                resourceKey, subject.policyVersion(), actions));
    }

    private EffectiveAction decision(EffectiveSubject subject,
                                     String resourceKey,
                                     String action,
                                     ActionCatalog.Definition definition) {
        AuthorizationDecision decision = authorizationService.decide(new AuthorizationRequest(
                subject, action, definition.resourceType(), resourceKey));
        RiskLevel risk = RiskLevel.max(definition.minimumRisk(), decision.minimumRisk());
        auditService.record(new SecurityAuditEvent(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), subject.userId(),
                "HTTP", null, null, action, resourceKey,
                decision.allowed() ? "ALLOW" : "DENY", decision.reasonCode(), risk,
                decision.policyVersion(), null, null, Instant.now()));
        return decision.allowed() ? new EffectiveAction(action, risk) : null;
    }

    private String canonicalType(String resourceType) {
        if (resourceType == null) {
            throw new ControlledActionException(400, "RESOURCE_TYPE_INVALID");
        }
        return catalog.definitions().values().stream()
                .map(ActionCatalog.Definition::resourceType)
                .filter(type -> type.equalsIgnoreCase(resourceType.trim()))
                .findFirst()
                .orElseThrow(() -> new ControlledActionException(400, "RESOURCE_TYPE_INVALID"));
    }

    private static String resourceKey(EffectiveSubject subject, String resourceType, String resourceId) {
        String id = resourceId == null ? "" : resourceId.trim();
        if (!id.matches("[A-Za-z0-9._*-]{1,256}")) {
            throw new ControlledActionException(400, "RESOURCE_INVALID");
        }
        if ("AgentCapability".equals(resourceType)) {
            return "agent:capability:" + id;
        }
        if ("Auth".equals(resourceType)) {
            return "auth:" + id;
        }
        String segment = switch (resourceType.toLowerCase(Locale.ROOT)) {
            case "view" -> "view";
            case "model" -> "model";
            case "operation" -> "operation";
            case "datasource" -> "datasource";
            case "event" -> "event";
            case "actionrequest" -> "action-request";
            default -> throw new ControlledActionException(400, "RESOURCE_TYPE_INVALID");
        };
        return "app:" + subject.appId() + ":db:" + subject.databaseId() + ":" + segment + ":" + id;
    }

    public record EffectiveAction(String action, RiskLevel minimumRisk) {
    }

    public record EffectiveActionsResult(String resourceKey,
                                         long policyVersion,
                                         List<EffectiveAction> actions) {
    }
}
