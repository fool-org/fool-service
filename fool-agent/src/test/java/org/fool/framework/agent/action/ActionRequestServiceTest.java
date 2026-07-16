package org.fool.framework.agent.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.common.authz.AuthorizationDecision;
import org.fool.framework.common.authz.AuthorizationRequest;
import org.fool.framework.common.authz.AuthorizationService;
import org.fool.framework.common.authz.ControlledActionContext;
import org.fool.framework.common.authz.ControlledActionExecutionGuard;
import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.ControlledActionHandler;
import org.fool.framework.common.authz.ControlledActionPreview;
import org.fool.framework.common.authz.ControlledActionResult;
import org.fool.framework.common.authz.DataPolicy;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.SecurityAuditEvent;
import org.fool.framework.common.authz.SecurityAuditService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class ActionRequestServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final MutableClock clock = new MutableClock(Instant.parse("2026-07-15T10:00:00Z"));
    private final AtomicReference<ActionRequestRecord> stored = new AtomicReference<>();
    private final AtomicLong policyVersion = new AtomicLong(1);
    private final TestHandler handler = new TestHandler();
    private final List<SecurityAuditEvent> audits = new ArrayList<>();
    private JdbcActionRequestStore store;
    private ActionRequestService service;
    private EffectiveSubject subject;

    @Before
    public void setUp() {
        stored.set(null);
        policyVersion.set(1);
        handler.snapshot = "v1";
        audits.clear();
        store = Mockito.mock(JdbcActionRequestStore.class);
        when(store.currentPolicyVersion(any(), any())).thenAnswer(invocation -> policyVersion.get());
        when(store.find(any())).thenAnswer(invocation -> Optional.ofNullable(stored.get())
                .filter(value -> value.id().equals(invocation.getArgument(0))));
        doAnswer(invocation -> {
            stored.set(invocation.getArgument(0));
            return null;
        }).when(store).insert(any());
        doAnswer(invocation -> {
            ActionRequestRecord old = stored.get();
            ActionRequestStatus expected = invocation.getArgument(1);
            assertEquals(expected, old.status());
            stored.set(new ActionRequestRecord(old.id(), old.ownerUserId(), old.agentSessionId(), old.source(),
                    old.appId(), old.databaseId(), old.action(), old.resourceKey(),
                    invocation.getArgument(2), invocation.getArgument(3), invocation.getArgument(4),
                    invocation.getArgument(5), invocation.getArgument(6), invocation.getArgument(7),
                    invocation.getArgument(8), invocation.getArgument(9), old.idempotencyKey(),
                    invocation.getArgument(10), old.createdAt(), invocation.getArgument(11)));
            return null;
        }).when(store).savePreview(any(), any(), any(), any(), any(), any(), any(), any(),
                anyLong(), any(), any(), any());
        doAnswer(invocation -> {
            ActionRequestRecord old = stored.get();
            ActionRequestStatus from = invocation.getArgument(1);
            if (old.status() != from) {
                throw new ControlledActionException(409, "ACTION_STATE_CONFLICT");
            }
            stored.set(copyWithStatus(old, invocation.getArgument(2), invocation.getArgument(3)));
            return null;
        }).when(store).transition(any(), any(), any(), any());

        AuthorizationService authorization = request -> AuthorizationDecision.allow(
                request.subject().policyVersion(), "test", DataPolicy.unrestricted());
        SecurityAuditService audit = audits::add;
        CanonicalJson canonical = new CanonicalJson(mapper);
        service = new ActionRequestService(store, new ActionIntentParser(mapper), new ActionCatalog(),
                new ActionRiskEngine(), new ControlledActionRegistry(List.of(handler)), authorization,
                audit, canonical, mapper, new ControlledActionExecutionGuard(), clock);
        subject = new EffectiveSubject("u1", List.of(), "c1", List.of(),
                "app", "db", "session", clock.instant(), null, 1);
    }

    @Test
    public void mediumRequestExecutesOnlyAfterImmutablePreviewAndOneConfirmation() {
        ActionRequestView created = create();
        assertEquals(ActionRequestStatus.DRAFT, created.status());
        ActionRequestView preview = service.preview(subject, created.actionRequestId());
        assertTrue(preview.confirmable());
        ActionRequestView confirmed = service.confirm(subject, created.actionRequestId());
        assertTrue(confirmed.executable());
        ActionRequestView executed = service.execute(subject, created.actionRequestId());

        assertEquals(ActionRequestStatus.SUCCEEDED, executed.status());
        assertEquals(1, handler.executions.get());
        ControlledActionException replay = assertThrows(
                ControlledActionException.class,
                () -> service.execute(subject, created.actionRequestId()));
        assertEquals("ACTION_STATE_CONFLICT", replay.getMessage());
        assertEquals(1, handler.executions.get());
        assertTrue(audits.stream().anyMatch(event -> "EXECUTION_RECHECK_PASSED".equals(event.reasonCode())));
    }

    @Test
    public void payloadMutationPolicyChangeObjectChangeAndExpiryInvalidateExecution() {
        ActionRequestView first = createAndPreview();
        ActionRequestRecord record = stored.get();
        stored.set(new ActionRequestRecord(record.id(), record.ownerUserId(), record.agentSessionId(), record.source(),
                record.appId(), record.databaseId(), record.action(), record.resourceKey(),
                record.payloadJson().replace("report.save", "report.export"), record.payloadHash(),
                record.previewJson(), record.previewHash(), record.risk(), record.riskReasons(),
                record.policyVersion(), record.status(), record.idempotencyKey(), record.expiresAt(),
                record.createdAt(), record.updatedAt()));
        assertEquals("ACTION_PAYLOAD_MISMATCH", assertThrows(ControlledActionException.class,
                () -> service.confirm(subject, first.actionRequestId())).getMessage());

        setUp();
        ActionRequestView policy = createAndPreview();
        policyVersion.set(2);
        assertEquals("POLICY_CHANGED", assertThrows(ControlledActionException.class,
                () -> service.confirm(subject, policy.actionRequestId())).getMessage());
        assertEquals(ActionRequestStatus.PREVIEW_READY, stored.get().status());

        setUp();
        ActionRequestView changed = createAndPreview();
        handler.snapshot = "v2";
        assertEquals("OBJECT_CHANGED", assertThrows(ControlledActionException.class,
                () -> service.confirm(subject, changed.actionRequestId())).getMessage());
        assertEquals(ActionRequestStatus.PREVIEW_READY, stored.get().status());

        setUp();
        ActionRequestView expired = createAndPreview();
        clock.advance(Duration.ofMinutes(16));
        assertEquals("PREVIEW_EXPIRED", assertThrows(ControlledActionException.class,
                () -> service.confirm(subject, expired.actionRequestId())).getMessage());
        assertEquals(ActionRequestStatus.EXPIRED, stored.get().status());
    }

    @Test
    public void auditFailureBeforeExecutionLeavesApprovedRequestUnexecuted() {
        SecurityAuditService failingAudit = event -> {
            if ("EXECUTION_RECHECK_PASSED".equals(event.reasonCode())) {
                throw new IllegalStateException("audit unavailable");
            }
            audits.add(event);
        };
        service = new ActionRequestService(store, new ActionIntentParser(mapper), new ActionCatalog(),
                new ActionRiskEngine(), new ControlledActionRegistry(List.of(handler)),
                request -> AuthorizationDecision.allow(1, "test", DataPolicy.unrestricted()),
                failingAudit, new CanonicalJson(mapper), mapper,
                new ControlledActionExecutionGuard(), clock);
        ActionRequestView request = createAndPreview();
        service.confirm(subject, request.actionRequestId());

        assertThrows(IllegalStateException.class, () -> service.execute(subject, request.actionRequestId()));
        assertEquals(ActionRequestStatus.APPROVED, stored.get().status());
        assertEquals(0, handler.executions.get());
    }

    @Test
    public void highRequestRequiresRecentStepUpAndIndependentCurrentApproval() throws Exception {
        TestHandler highHandler = new TestHandler("data.delete", "View");
        List<ApprovalRecord> approvals = new ArrayList<>();
        JdbcApprovalStore approvalStore = Mockito.mock(JdbcApprovalStore.class);
        doAnswer(invocation -> {
            approvals.add(invocation.getArgument(0));
            return null;
        }).when(approvalStore).insert(any());
        when(approvalStore.list(any())).thenAnswer(invocation -> approvals.stream()
                .filter(approval -> approval.actionRequestId().equals(invocation.getArgument(0))).toList());
        when(approvalStore.validApprovalCount(any(), any())).thenAnswer(invocation -> approvals.stream()
                .filter(approval -> approval.actionRequestId().equals(invocation.getArgument(0)))
                .filter(approval -> "APPROVE".equals(approval.decision())).count());
        AtomicBoolean approverAllowed = new AtomicBoolean(true);
        List<AuthorizationRequest> approvalDecisions = new ArrayList<>();
        AuthorizationService authorization = request -> {
            if ("action.approve".equals(request.action())) {
                approvalDecisions.add(request);
                if (!approverAllowed.get()) {
                    return AuthorizationDecision.deny("NO_MATCHING_ALLOW", request.subject().policyVersion());
                }
            }
            return AuthorizationDecision.allow(
                    request.subject().policyVersion(), "test", DataPolicy.unrestricted());
        };
        service = new ActionRequestService(store, new ActionIntentParser(mapper), new ActionCatalog(),
                new ActionRiskEngine(), new ControlledActionRegistry(List.of(highHandler)), authorization,
                audits::add, new CanonicalJson(mapper), mapper,
                new ControlledActionExecutionGuard(), clock);
        setField(service, "approvalStore", approvalStore);
        setField(service, "subjectLookup", (org.fool.framework.common.authz.EffectiveSubjectLookup)
                (userId, appId, databaseId) -> new EffectiveSubject(userId,
                        "approver".equals(userId) ? List.of("auth:9001") : List.of(), "c1", List.of(),
                        appId, databaseId, "lookup", clock.instant(), null, policyVersion.get()));

        EffectiveSubject withoutStepUp = new EffectiveSubject("u1", List.of(), "c1", List.of(),
                "app", "db", "session", clock.instant(), null, 1);
        ActionRequestView created = service.create(withoutStepUp, highIntent(), "UI", null, "high-1");
        assertEquals("STEP_UP_REQUIRED", assertThrows(ControlledActionException.class,
                () -> service.preview(withoutStepUp, created.actionRequestId())).getMessage());

        EffectiveSubject owner = new EffectiveSubject("u1", List.of(), "c1", List.of(),
                "app", "db", "session", clock.instant(), clock.instant(), 1);
        ActionRequestView preview = service.preview(owner, created.actionRequestId());
        assertEquals(ActionRequestStatus.AWAITING_APPROVAL, preview.status());
        assertTrue(preview.owned());
        assertTrue(!preview.approvable());
        assertEquals(1, preview.requiredApprovals());
        assertEquals("SELF_APPROVAL_FORBIDDEN", assertThrows(ControlledActionException.class,
                () -> service.approve(owner, created.actionRequestId(), "APPROVE", "self")).getMessage());
        assertTrue(audits.stream().anyMatch(event ->
                "SELF_APPROVAL_FORBIDDEN".equals(event.reasonCode())
                        && created.actionRequestId().equals(event.actionRequestId())));

        EffectiveSubject approver = new EffectiveSubject("approver", List.of("auth:9001"), "c1", List.of(),
                "app", "db", "approver-session", clock.instant(), null, 1);
        ActionRequestView approverView = service.get(approver, created.actionRequestId());
        assertEquals(ActionRequestStatus.AWAITING_APPROVAL, approverView.status());
        assertTrue(!approverView.owned());
        assertTrue(approverView.approvable());
        assertTrue(!approverView.executable());
        ActionRequestView approved = service.approve(approver, created.actionRequestId(), "APPROVE", "reviewed");
        assertEquals(ActionRequestStatus.APPROVED, approved.status());
        assertTrue(!approved.executable());
        assertTrue(!approvalDecisions.isEmpty());
        assertTrue(approvalDecisions.stream().allMatch(request -> "View".equals(request.resourceType())
                && "app:app:db:db:view:100".equals(request.resourceKey())));
        assertEquals("STEP_UP_REQUIRED", assertThrows(ControlledActionException.class,
                () -> service.execute(withoutStepUp, created.actionRequestId())).getMessage());
        assertTrue(audits.stream().anyMatch(event ->
                "STEP_UP_REQUIRED".equals(event.reasonCode())
                        && created.actionRequestId().equals(event.actionRequestId())));
        approverAllowed.set(false);
        assertEquals("APPROVER_PERMISSION_REVOKED", assertThrows(ControlledActionException.class,
                () -> service.execute(owner, created.actionRequestId())).getMessage());
        assertEquals(0, highHandler.executions.get());

        approverAllowed.set(true);
        ActionRequestView executed = service.execute(owner, created.actionRequestId());
        assertEquals(ActionRequestStatus.SUCCEEDED, executed.status());
        assertEquals(1, highHandler.executions.get());
    }

    private ActionRequestView create() {
        return service.create(subject, """
                {"schemaVersion":1,"action":"report.save","resource":{"type":"view","id":"100"},
                 "arguments":{"request":{"ViewId":100,"ReportName":"Daily","ReportCols":[{"ColId":"amount"}]}},
                 "rationale":"save reviewed report"}
                """, "UI", null, "idem-" + System.nanoTime());
    }

    private ActionRequestView createAndPreview() {
        ActionRequestView request = create();
        return service.preview(subject, request.actionRequestId());
    }

    private static String highIntent() {
        return """
                {"schemaVersion":1,"action":"data.delete","resource":{"type":"view","id":"100"},
                 "arguments":{"objectId":"1"},"rationale":"delete reviewed object"}
                """;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static ActionRequestRecord copyWithStatus(
            ActionRequestRecord old, ActionRequestStatus status, Instant updatedAt) {
        return new ActionRequestRecord(old.id(), old.ownerUserId(), old.agentSessionId(), old.source(),
                old.appId(), old.databaseId(), old.action(), old.resourceKey(), old.payloadJson(),
                old.payloadHash(), old.previewJson(), old.previewHash(), old.risk(), old.riskReasons(),
                old.policyVersion(), status, old.idempotencyKey(), old.expiresAt(), old.createdAt(), updatedAt);
    }

    private static class TestHandler implements ControlledActionHandler {
        private final String action;
        private final String resourceType;
        private String snapshot = "v1";
        private final AtomicInteger executions = new AtomicInteger();
        private TestHandler() { this("report.save", "View"); }
        private TestHandler(String action, String resourceType) {
            this.action = action;
            this.resourceType = resourceType;
        }
        public String action() { return action; }
        public String resourceType() { return resourceType; }
        public void preflight(ControlledActionContext context) { }
        public ControlledActionPreview preview(ControlledActionContext context) {
            return new ControlledActionPreview(snapshot, 1, Map.of("name", "Daily"),
                    List.of("valid"), "restore", List.of());
        }
        public String currentSnapshotVersion(ControlledActionContext context) { return snapshot; }
        public ControlledActionResult execute(ControlledActionContext context) {
            executions.incrementAndGet();
            return new ControlledActionResult("REPORT_SAVED", Map.of("saved", true), Map.of());
        }
    }

    private static class MutableClock extends Clock {
        private Instant now;
        private MutableClock(Instant now) { this.now = now; }
        public ZoneId getZone() { return ZoneId.of("UTC"); }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return now; }
        private void advance(Duration duration) { now = now.plus(duration); }
    }
}
