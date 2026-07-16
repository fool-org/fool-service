package org.fool.framework.agent.action;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.fool.framework.common.authz.AuthorizationDecision;
import org.fool.framework.common.authz.AuthorizationRequest;
import org.fool.framework.common.authz.AuthorizationService;
import org.fool.framework.common.authz.ControlledActionContext;
import org.fool.framework.common.authz.ControlledActionExecutionGuard;
import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.ControlledActionHandler;
import org.fool.framework.common.authz.ControlledActionPreview;
import org.fool.framework.common.authz.ControlledActionResult;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.EffectiveSubjectContext;
import org.fool.framework.common.authz.EffectiveSubjectLookup;
import org.fool.framework.common.authz.RiskLevel;
import org.fool.framework.common.authz.SecurityAuditEvent;
import org.fool.framework.common.authz.SecurityAuditService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ActionRequestService {
    private static final Duration REQUEST_TTL = Duration.ofMinutes(15);
    private static final Duration CONFIRMATION_TTL = Duration.ofMinutes(5);
    private static final Set<String> SOURCES = Set.of("UI", "CHAT", "API", "SCHEDULER");

    private final JdbcActionRequestStore store;
    private final ActionIntentParser intentParser;
    private final ActionCatalog catalog;
    private final ActionRiskEngine riskEngine;
    private final ControlledActionRegistry registry;
    private final AuthorizationService authorizationService;
    private final SecurityAuditService auditService;
    private final CanonicalJson canonicalJson;
    private final ObjectMapper objectMapper;
    private final ControlledActionExecutionGuard executionGuard;
    private final Clock clock;
    @Autowired(required = false)
    private JdbcApprovalStore approvalStore;
    @Autowired(required = false)
    private EffectiveSubjectLookup subjectLookup;

    @Autowired
    public ActionRequestService(JdbcActionRequestStore store,
                                ActionIntentParser intentParser,
                                ActionCatalog catalog,
                                ActionRiskEngine riskEngine,
                                ControlledActionRegistry registry,
                                AuthorizationService authorizationService,
                                SecurityAuditService auditService,
                                CanonicalJson canonicalJson,
                                ObjectMapper objectMapper,
                                ControlledActionExecutionGuard executionGuard) {
        this(store, intentParser, catalog, riskEngine, registry, authorizationService,
                auditService, canonicalJson, objectMapper, executionGuard, Clock.systemUTC());
    }

    ActionRequestService(JdbcActionRequestStore store,
                         ActionIntentParser intentParser,
                         ActionCatalog catalog,
                         ActionRiskEngine riskEngine,
                         ControlledActionRegistry registry,
                         AuthorizationService authorizationService,
                         SecurityAuditService auditService,
                         CanonicalJson canonicalJson,
                         ObjectMapper objectMapper,
                         ControlledActionExecutionGuard executionGuard,
                         Clock clock) {
        this.store = store;
        this.intentParser = intentParser;
        this.catalog = catalog;
        this.riskEngine = riskEngine;
        this.registry = registry;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
        this.canonicalJson = canonicalJson;
        this.objectMapper = objectMapper;
        this.executionGuard = executionGuard;
        this.clock = clock;
    }

    public ActionRequestView create(EffectiveSubject subject,
                                    String intentJson,
                                    String source,
                                    String agentSessionId,
                                    String idempotencyKey) {
        requireCurrentPolicy(subject);
        if (!StringUtils.hasText(idempotencyKey) || idempotencyKey.length() > 128) {
            throw new ControlledActionException(400, "IDEMPOTENCY_KEY_REQUIRED");
        }
        ActionIntent intent = intentParser.parse(intentJson);
        String normalizedSource = normalizeSource(source);
        String resourceType = catalog.require(intent.action(), intent.resource().type()).resourceType();
        ActionCatalog.Definition definition = catalog.require(intent.action(), resourceType);
        if (!definition.executable()) {
            throw new ControlledActionException(403, "ACTION_NOT_EXECUTABLE");
        }
        String resourceKey = canonicalResource(subject, resourceType, intent.resource().id());
        AuthorizationDecision decision = authorize(subject, intent.action(), resourceType, resourceKey);
        ControlledActionHandler handler = registry.require(intent.action(), resourceType);
        RiskLevel initialRisk = RiskLevel.max(definition.minimumRisk(), decision.minimumRisk());
        List<String> initialRiskReasons = decision.minimumRisk().ordinal() > definition.minimumRisk().ordinal()
                ? List.of("ACTION_CATALOG_FLOOR", "POLICY_RISK_FLOOR")
                : List.of("ACTION_CATALOG_FLOOR");

        String id = UUID.randomUUID().toString();
        Map<String, Object> basePayload = Map.of(
                "schemaVersion", 1,
                "actorUserId", subject.userId(),
                "appId", subject.appId(),
                "databaseId", subject.databaseId(),
                "action", intent.action(),
                "resource", Map.of(
                        "type", resourceType,
                        "id", intent.resource().id(),
                        "key", resourceKey),
                "arguments", intent.arguments(),
                "rationale", intent.rationale(),
                "source", normalizedSource);
        ControlledActionContext context = context(id, subject, basePayload, decision);
        try {
            handler.preflight(context);
        } catch (RuntimeException ex) {
            audit(subject, normalizedSource, agentSessionId, id, intent.action(), resourceKey,
                    "DENY", reason(ex), initialRisk, subject.policyVersion());
            throw ex;
        }

        Instant now = clock.instant();
        Instant expiresAt = now.plus(REQUEST_TTL);
        String payloadJson = canonicalJson.write(basePayload);
        ActionRequestRecord request = new ActionRequestRecord(
                id, subject.userId(), blankToNull(agentSessionId), normalizedSource,
                subject.appId(), subject.databaseId(), intent.action(), resourceKey,
                payloadJson, canonicalJson.hashJson(payloadJson), null, null,
                initialRisk, initialRiskReasons,
                subject.policyVersion(), ActionRequestStatus.DRAFT, idempotencyKey,
                expiresAt, now, now);
        store.insert(request);
        audit(subject, normalizedSource, agentSessionId, id, intent.action(), resourceKey,
                "DEFER", "PREFLIGHT_PASSED", initialRisk, subject.policyVersion());
        return view(subject, request, Map.of());
    }

    public ActionRequestView preview(EffectiveSubject subject, String id) {
        ActionRequestRecord request = owned(subject, id);
        requireNotExpired(request);
        requireIntegrity(request, false);
        if (request.status() != ActionRequestStatus.DRAFT
                && request.status() != ActionRequestStatus.PREVIEW_READY) {
            throw new ControlledActionException(409, "ACTION_STATE_CONFLICT");
        }
        requireCurrentPolicy(subject);
        ParsedPayload payload = parsed(request);
        ActionCatalog.Definition definition = catalog.require(request.action(), payload.resourceType());
        ControlledActionHandler handler = registry.require(request.action(), payload.resourceType());
        AuthorizationDecision decision = authorize(subject, request.action(), payload.resourceType(), request.resourceKey());
        ControlledActionContext context = context(id, subject, payload.base(), decision);
        handler.preflight(context);
        ControlledActionPreview domainPreview = handler.preview(context);
        ActionRiskEngine.Assessment assessment = riskEngine.assess(
                request.action(), definition, domainPreview, decision.minimumRisk());
        if (assessment.risk() == RiskLevel.CRITICAL) {
            throw new ControlledActionException(403, "CRITICAL_AGENT_EXECUTION_FORBIDDEN");
        }
        if (assessment.risk() == RiskLevel.HIGH) {
            requireRecentStepUp(subject);
        }

        Instant now = clock.instant();
        Instant expiresAt = now.plus(REQUEST_TTL);
        Map<String, Object> preview = Map.of(
                "previewVersion", 1,
                "snapshotVersion", domainPreview.snapshotVersion(),
                "effectiveScope", Map.of("appId", subject.appId(), "databaseId", subject.databaseId()),
                "affectedObjectCount", domainPreview.affectedObjectCount(),
                "fieldDiff", domainPreview.fieldDiff(),
                "preconditions", domainPreview.preconditions(),
                "rollbackStrategy", domainPreview.rollbackStrategy(),
                "warnings", domainPreview.warnings(),
                "previewExpiresAt", expiresAt.toString());
        String previewJson = canonicalJson.write(preview);
        Map<String, Object> boundPayload = Map.of(
                "base", payload.base(),
                "affectedObjectSnapshot", domainPreview.snapshotVersion(),
                "policyVersion", subject.policyVersion(),
                "riskLevel", assessment.risk().name(),
                "previewVersion", 1,
                "expiresAt", expiresAt.toString());
        String payloadJson = canonicalJson.write(boundPayload);
        String payloadHash = canonicalJson.hashJson(payloadJson);
        String previewHash = canonicalJson.hashJson(previewJson);
        store.savePreview(id, request.status(), payloadJson, payloadHash, previewJson, previewHash,
                assessment.risk(), assessment.reasons(), subject.policyVersion(),
                assessment.risk() == RiskLevel.HIGH
                        ? ActionRequestStatus.AWAITING_APPROVAL
                        : ActionRequestStatus.AWAITING_CONFIRMATION,
                expiresAt, now);
        ActionRequestRecord updated = required(id);
        audit(subject, request.source(), request.agentSessionId(), id, request.action(), request.resourceKey(),
                "DEFER", updated.status().name(), updated.risk(), updated.policyVersion());
        return view(subject, updated, Map.of());
    }

    public ActionRequestView get(EffectiveSubject subject, String id) {
        ActionRequestRecord request = scoped(subject, id);
        if (!request.ownerUserId().equals(subject.userId())) {
            requireNotExpired(request);
            requireIntegrity(request, true);
            if (request.risk() != RiskLevel.HIGH) {
                throw new ControlledActionException(403, "ACTION_REQUEST_NOT_OWNED");
            }
            requireCurrentPolicy(subject);
            authorizeApproval(subject, request);
        }
        if (!request.status().terminal() && isExpired(request)) {
            expire(request);
            request = required(id);
        }
        requireIntegrity(request, request.previewJson() != null);
        return view(subject, request, Map.of());
    }

    public ActionRequestView confirm(EffectiveSubject subject, String id) {
        ActionRequestRecord request = owned(subject, id);
        requireStatus(request, ActionRequestStatus.AWAITING_CONFIRMATION);
        revalidate(subject, request);
        if (request.risk() != RiskLevel.MEDIUM) {
            throw new ControlledActionException(403, "APPROVAL_REQUIRED");
        }
        Instant now = clock.instant();
        store.transition(id, ActionRequestStatus.AWAITING_CONFIRMATION, ActionRequestStatus.APPROVED, now);
        request = required(id);
        audit(subject, request.source(), request.agentSessionId(), id, request.action(), request.resourceKey(),
                "ALLOW", "ACTOR_CONFIRMED", request.risk(), request.policyVersion());
        return view(subject, request, Map.of());
    }

    public ActionRequestView execute(EffectiveSubject subject, String id) {
        ActionRequestRecord request = owned(subject, id);
        requireStatus(request, ActionRequestStatus.APPROVED);
        if (request.risk() == RiskLevel.MEDIUM
                && request.updatedAt().plus(CONFIRMATION_TTL).isBefore(clock.instant())) {
            store.transition(id, ActionRequestStatus.APPROVED, ActionRequestStatus.EXPIRED, clock.instant());
            throw new ControlledActionException(409, "CONFIRMATION_EXPIRED");
        }
        if (request.risk() == RiskLevel.HIGH) {
            try {
                requireRecentStepUp(subject);
                requireValidApprovals(request);
            } catch (RuntimeException ex) {
                audit(subject, request.source(), request.agentSessionId(), id, request.action(),
                        request.resourceKey(), "DENY", reason(ex), request.risk(), request.policyVersion());
                throw ex;
            }
        }
        Revalidated validated = revalidate(subject, request);
        audit(subject, request.source(), request.agentSessionId(), id, request.action(), request.resourceKey(),
                "ALLOW", "EXECUTION_RECHECK_PASSED", request.risk(), request.policyVersion());
        store.transition(id, ActionRequestStatus.APPROVED, ActionRequestStatus.EXECUTING, clock.instant());
        try {
            ControlledActionResult result = executionGuard.execute(
                    request.id(), request.action(), validated.context().resourceId(),
                    () -> validated.handler().execute(validated.context()));
            store.transition(id, ActionRequestStatus.EXECUTING, ActionRequestStatus.SUCCEEDED, clock.instant());
            ActionRequestRecord updated = required(id);
            audit(subject, request.source(), request.agentSessionId(), id, request.action(), request.resourceKey(),
                    "SUCCESS", result.resultRef().isBlank() ? "EXECUTION_SUCCEEDED" : result.resultRef(),
                    request.risk(), request.policyVersion());
            return view(subject, updated, result.response());
        } catch (RuntimeException ex) {
            try {
                store.transition(id, ActionRequestStatus.EXECUTING, ActionRequestStatus.FAILED, clock.instant());
                audit(subject, request.source(), request.agentSessionId(), id, request.action(), request.resourceKey(),
                        "FAIL", reason(ex), request.risk(), request.policyVersion());
            } catch (RuntimeException ignored) {
                // Preserve the domain failure; the conditional EXECUTING state still blocks replay.
            }
            throw ex;
        }
    }

    public ActionRequestView cancel(EffectiveSubject subject, String id) {
        ActionRequestRecord request = owned(subject, id);
        if (request.status().terminal() || request.status() == ActionRequestStatus.EXECUTING) {
            throw new ControlledActionException(409, "ACTION_STATE_CONFLICT");
        }
        store.transition(id, request.status(), ActionRequestStatus.CANCELLED, clock.instant());
        request = required(id);
        audit(subject, request.source(), request.agentSessionId(), id, request.action(), request.resourceKey(),
                "DENY", "ACTOR_CANCELLED", request.risk(), request.policyVersion());
        return view(subject, request, Map.of());
    }

    public ActionRequestView approve(EffectiveSubject approver,
                                     String id,
                                     String decisionValue,
                                     String commentValue) {
        ActionRequestRecord request = scoped(approver, id);
        requireStatus(request, ActionRequestStatus.AWAITING_APPROVAL);
        requireNotExpired(request);
        requireIntegrity(request, true);
        if (request.risk() != RiskLevel.HIGH) {
            throw new ControlledActionException(409, "APPROVAL_NOT_REQUIRED");
        }
        if (request.ownerUserId().equals(approver.userId())) {
            audit(approver, request.source(), request.agentSessionId(), id, request.action(),
                    request.resourceKey(), "DENY", "SELF_APPROVAL_FORBIDDEN",
                    request.risk(), request.policyVersion());
            throw new ControlledActionException(403, "SELF_APPROVAL_FORBIDDEN");
        }
        requireCurrentPolicy(approver);
        authorizeApproval(approver, request);
        EffectiveSubject owner = effectiveSubjectLookup().resolve(
                request.ownerUserId(), request.appId(), request.databaseId());
        revalidateAs(owner, request);

        String decision = decisionValue == null ? "" : decisionValue.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("APPROVE", "REJECT").contains(decision)) {
            throw new ControlledActionException(400, "APPROVAL_DECISION_INVALID");
        }
        String comment = sanitizeComment(commentValue);
        Instant now = clock.instant();
        approvalStore().insert(new ApprovalRecord(UUID.randomUUID().toString(), id, approver.userId(),
                decision, request.payloadHash(), request.previewHash(), comment,
                approver.policyVersion(), now, request.expiresAt()));
        if ("REJECT".equals(decision)) {
            store.transition(id, ActionRequestStatus.AWAITING_APPROVAL, ActionRequestStatus.CANCELLED, now);
            audit(approver, request.source(), request.agentSessionId(), id, request.action(),
                    request.resourceKey(), "DENY", "APPROVAL_REJECTED", request.risk(), request.policyVersion());
        } else {
            long count = approvalStore().validApprovalCount(id, now);
            if (count >= requiredApprovals(request.action(), request.risk())) {
                store.transition(id, ActionRequestStatus.AWAITING_APPROVAL, ActionRequestStatus.APPROVED, now);
            }
            audit(approver, request.source(), request.agentSessionId(), id, request.action(),
                    request.resourceKey(), "ALLOW", "APPROVAL_GRANTED", request.risk(), request.policyVersion());
        }
        return view(approver, required(id), Map.of());
    }

    private Revalidated revalidate(EffectiveSubject subject, ActionRequestRecord request) {
        requireNotExpired(request);
        requireIntegrity(request, true);
        long currentPolicy = store.currentPolicyVersion(subject.appId(), subject.databaseId());
        if (currentPolicy != subject.policyVersion() || request.policyVersion() != subject.policyVersion()) {
            invalidatePreview(request, "POLICY_CHANGED");
        }
        ParsedPayload payload = parsed(request);
        AuthorizationDecision decision = authorize(subject, request.action(), payload.resourceType(), request.resourceKey());
        ControlledActionHandler handler = registry.require(request.action(), payload.resourceType());
        ControlledActionContext context = context(request.id(), subject, payload.base(), decision);
        handler.preflight(context);
        String currentSnapshot = handler.currentSnapshotVersion(context);
        if (!payload.snapshotVersion().equals(currentSnapshot)) {
            invalidatePreview(request, "OBJECT_CHANGED");
        }
        Map<String, Object> executionArguments = new java.util.LinkedHashMap<>(context.arguments());
        executionArguments.put("_approvedSnapshotVersion", payload.snapshotVersion());
        ControlledActionContext executionContext = new ControlledActionContext(
                context.actionRequestId(), context.subject(), context.action(), context.resourceType(),
                context.resourceId(), context.resourceKey(), executionArguments, context.dataPolicy());
        return new Revalidated(handler, executionContext);
    }

    private Revalidated revalidateAs(EffectiveSubject subject, ActionRequestRecord request) {
        EffectiveSubject previous = EffectiveSubjectContext.get();
        EffectiveSubjectContext.set(subject);
        try {
            return revalidate(subject, request);
        } finally {
            if (previous == null) EffectiveSubjectContext.clear(); else EffectiveSubjectContext.set(previous);
        }
    }

    private void invalidatePreview(ActionRequestRecord request, String reason) {
        if (request.status() == ActionRequestStatus.AWAITING_CONFIRMATION
                || request.status() == ActionRequestStatus.AWAITING_APPROVAL
                || request.status() == ActionRequestStatus.APPROVED) {
            store.transition(request.id(), request.status(), ActionRequestStatus.PREVIEW_READY, clock.instant());
        }
        throw new ControlledActionException(409, reason);
    }

    private AuthorizationDecision authorize(EffectiveSubject subject,
                                            String action,
                                            String resourceType,
                                            String resourceKey) {
        AuthorizationDecision decision = authorizationService.decide(
                new AuthorizationRequest(subject, action, resourceType, resourceKey));
        if (!decision.allowed()) {
            throw new ControlledActionException(403, decision.reasonCode());
        }
        return decision;
    }

    private void requireCurrentPolicy(EffectiveSubject subject) {
        long current = store.currentPolicyVersion(subject.appId(), subject.databaseId());
        if (current != subject.policyVersion()) {
            throw new ControlledActionException(409, "POLICY_CHANGED");
        }
    }

    private ActionRequestRecord owned(EffectiveSubject subject, String id) {
        ActionRequestRecord request = scoped(subject, id);
        if (!request.ownerUserId().equals(subject.userId())) {
            throw new ControlledActionException(403, "ACTION_REQUEST_NOT_OWNED");
        }
        return request;
    }

    private ActionRequestRecord scoped(EffectiveSubject subject, String id) {
        ActionRequestRecord request = required(id);
        if (!request.appId().equals(subject.appId())
                || !request.databaseId().equals(subject.databaseId())) {
            throw new ControlledActionException(403, "ACTION_REQUEST_OUT_OF_SCOPE");
        }
        return request;
    }

    private ActionRequestRecord required(String id) {
        return store.find(id).orElseThrow(() -> new ControlledActionException(404, "ACTION_REQUEST_NOT_FOUND"));
    }

    private void requireNotExpired(ActionRequestRecord request) {
        if (isExpired(request)) {
            expire(request);
            throw new ControlledActionException(409, "PREVIEW_EXPIRED");
        }
    }

    private boolean isExpired(ActionRequestRecord request) {
        return request.expiresAt().isBefore(clock.instant());
    }

    private void expire(ActionRequestRecord request) {
        if (!request.status().terminal() && request.status() != ActionRequestStatus.EXECUTING) {
            store.transition(request.id(), request.status(), ActionRequestStatus.EXPIRED, clock.instant());
        }
    }

    private void requireStatus(ActionRequestRecord request, ActionRequestStatus expected) {
        if (request.status() != expected) {
            throw new ControlledActionException(409, "ACTION_STATE_CONFLICT");
        }
    }

    private void requireIntegrity(ActionRequestRecord request, boolean previewRequired) {
        if (!canonicalJson.hashJson(request.payloadJson()).equals(request.payloadHash())) {
            throw new ControlledActionException(409, "ACTION_PAYLOAD_MISMATCH");
        }
        if (previewRequired && (request.previewJson() == null || request.previewHash() == null
                || !canonicalJson.hashJson(request.previewJson()).equals(request.previewHash()))) {
            throw new ControlledActionException(409, "PREVIEW_PAYLOAD_MISMATCH");
        }
    }

    private ParsedPayload parsed(ActionRequestRecord request) {
        JsonNode payload = canonicalJson.read(request.payloadJson());
        JsonNode baseNode = payload.has("base") ? payload.path("base") : payload;
        Map<String, Object> base = objectMapper.convertValue(baseNode, new TypeReference<>() {
        });
        JsonNode resource = baseNode.path("resource");
        return new ParsedPayload(base, resource.path("type").asText(),
                resource.path("id").asText(), payload.path("affectedObjectSnapshot").asText(""));
    }

    @SuppressWarnings("unchecked")
    private ControlledActionContext context(String id,
                                            EffectiveSubject subject,
                                            Map<String, Object> base,
                                            AuthorizationDecision decision) {
        Map<String, Object> resource = (Map<String, Object>) base.get("resource");
        Map<String, Object> arguments = (Map<String, Object>) base.getOrDefault("arguments", Map.of());
        return new ControlledActionContext(id, subject, String.valueOf(base.get("action")),
                String.valueOf(resource.get("type")), String.valueOf(resource.get("id")),
                String.valueOf(resource.get("key")), arguments, decision.dataPolicy());
    }

    private ActionRequestView view(EffectiveSubject subject,
                                   ActionRequestRecord request,
                                   Map<String, Object> result) {
        Map<String, Object> preview = request.previewJson() == null
                ? Map.of()
                : objectMapper.convertValue(canonicalJson.read(request.previewJson()), new TypeReference<>() {
                });
        int requiredApprovals = requiredApprovals(request.action(), request.risk());
        long approvalCount = requiredApprovals == 0 || approvalStore == null
                ? 0 : approvalStore.validApprovalCount(request.id(), clock.instant());
        boolean owned = request.ownerUserId().equals(subject.userId());
        return new ActionRequestView(request.id(), request.action(), request.resourceKey(), request.source(),
                request.risk(), request.riskReasons(), request.status(), preview, request.expiresAt(),
                owned,
                owned && request.status() == ActionRequestStatus.AWAITING_CONFIRMATION,
                owned && request.status() == ActionRequestStatus.APPROVED,
                !owned && request.status() == ActionRequestStatus.AWAITING_APPROVAL,
                owned && !request.status().terminal() && request.status() != ActionRequestStatus.EXECUTING,
                requiredApprovals, approvalCount, result);
    }

    private void requireValidApprovals(ActionRequestRecord request) {
        List<ApprovalRecord> approvals = approvalStore().list(request.id()).stream()
                .filter(approval -> "APPROVE".equals(approval.decision()))
                .filter(approval -> approval.expiresAt().isAfter(clock.instant()))
                .toList();
        int required = requiredApprovals(request.action(), request.risk());
        if (approvals.stream().map(ApprovalRecord::approverUserId).distinct().count() < required) {
            throw new ControlledActionException(403, "APPROVAL_REQUIRED");
        }
        for (ApprovalRecord approval : approvals) {
            if (approval.approverUserId().equals(request.ownerUserId())
                    || !request.payloadHash().equals(approval.payloadHash())
                    || !request.previewHash().equals(approval.previewHash())) {
                throw new ControlledActionException(409, "APPROVAL_PAYLOAD_MISMATCH");
            }
            EffectiveSubject currentApprover = effectiveSubjectLookup().resolve(
                    approval.approverUserId(), request.appId(), request.databaseId());
            ParsedPayload payload = parsed(request);
            AuthorizationDecision decision = authorizationService.decide(new AuthorizationRequest(
                    currentApprover, "action.approve", payload.resourceType(), request.resourceKey()));
            if (!decision.allowed()) {
                throw new ControlledActionException(403, "APPROVER_PERMISSION_REVOKED");
            }
        }
    }

    private void requireRecentStepUp(EffectiveSubject subject) {
        Instant stepUpAt = subject.stepUpAt();
        if (stepUpAt == null || stepUpAt.plus(Duration.ofMinutes(5)).isBefore(clock.instant())) {
            throw new ControlledActionException(403, "STEP_UP_REQUIRED");
        }
    }

    private JdbcApprovalStore approvalStore() {
        if (approvalStore == null) {
            throw new ControlledActionException(503, "APPROVAL_SERVICE_UNAVAILABLE");
        }
        return approvalStore;
    }

    private EffectiveSubjectLookup effectiveSubjectLookup() {
        return subjectLookup == null ? EffectiveSubjectLookup.unavailable() : subjectLookup;
    }

    private AuthorizationDecision authorizeApproval(EffectiveSubject subject,
                                                     ActionRequestRecord request) {
        ParsedPayload payload = parsed(request);
        return authorize(subject, "action.approve", payload.resourceType(), request.resourceKey());
    }

    private static int requiredApprovals(String action, RiskLevel risk) {
        if (risk != RiskLevel.HIGH) {
            return 0;
        }
        return Set.of("model.ddl.execute", "datasource.route.update",
                        "datasource.credential.update", "message.send").contains(action) ? 2 : 1;
    }

    private static String sanitizeComment(String comment) {
        String value = comment == null ? "" : comment.replace('\r', ' ').replace('\n', ' ').trim();
        if (value.length() > 1000) {
            throw new ControlledActionException(400, "APPROVAL_COMMENT_TOO_LONG");
        }
        return value;
    }

    private void audit(EffectiveSubject subject,
                       String source,
                       String agentSessionId,
                       String requestId,
                       String action,
                       String resourceKey,
                       String decision,
                       String reason,
                       RiskLevel risk,
                       long policyVersion) {
        auditService.record(new SecurityAuditEvent(
                UUID.randomUUID().toString(), requestId, subject.userId(), source,
                blankToNull(agentSessionId), requestId, action, resourceKey, decision,
                reason, risk, policyVersion, null, null, clock.instant()));
    }

    private static String canonicalResource(EffectiveSubject subject, String type, String id) {
        if (!StringUtils.hasText(id) || !id.matches("[A-Za-z0-9._:-]{1,256}")) {
            throw new ControlledActionException(400, "RESOURCE_INVALID");
        }
        String segment = switch (type.toLowerCase(Locale.ROOT)) {
            case "view" -> "view";
            case "model" -> "model";
            case "operation" -> "operation";
            case "datasource" -> "datasource";
            case "event" -> "event";
            default -> throw new ControlledActionException(400, "RESOURCE_TYPE_INVALID");
        };
        return "app:" + subject.appId() + ":db:" + subject.databaseId() + ":" + segment + ":" + id;
    }

    private static String normalizeSource(String source) {
        String normalized = source == null ? "UI" : source.trim().toUpperCase(Locale.ROOT);
        if (!SOURCES.contains(normalized)) {
            throw new ControlledActionException(400, "ACTION_SOURCE_INVALID");
        }
        return normalized;
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }

    private static String reason(RuntimeException ex) {
        String value = ex.getMessage();
        return StringUtils.hasText(value) && value.matches("[A-Z0-9_:-]{1,128}")
                ? value : "ACTION_REJECTED";
    }

    private record ParsedPayload(Map<String, Object> base,
                                 String resourceType,
                                 String resourceId,
                                 String snapshotVersion) {
    }

    private record Revalidated(ControlledActionHandler handler, ControlledActionContext context) {
    }
}
