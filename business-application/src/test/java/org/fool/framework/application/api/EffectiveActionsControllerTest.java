package org.fool.framework.application.api;

import org.fool.framework.agent.action.ActionCatalog;
import org.fool.framework.common.authz.AuthorizationDecision;
import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.DataPolicy;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.EffectiveSubjectContext;
import org.fool.framework.common.authz.RiskLevel;
import org.fool.framework.common.authz.SecurityAuditEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class EffectiveActionsControllerTest {
    private final List<SecurityAuditEvent> auditEvents = new ArrayList<>();

    @Before
    public void setSubject() {
        EffectiveSubjectContext.set(new EffectiveSubject(
                "reader", List.of("auth:1"), "", List.of(), "fool-service", "car_wash",
                "session-1", Instant.EPOCH, null, 9L));
    }

    @After
    public void clearSubject() {
        EffectiveSubjectContext.clear();
    }

    @Test
    public void returnsOnlyServerAuthorizedActionsWithEffectiveRisk() {
        Set<String> allowed = Set.of("view.read", "view.query", "data.update");
        EffectiveActionsController controller = new EffectiveActionsController(
                new ActionCatalog(),
                request -> allowed.contains(request.action())
                        ? AuthorizationDecision.allow(9, "test", DataPolicy.unrestricted(),
                        "data.update".equals(request.action()) ? RiskLevel.HIGH : RiskLevel.LOW)
                        : AuthorizationDecision.deny("NO_MATCHING_ALLOW", 9),
                auditEvents::add);

        var result = controller.effectiveActions("view", "100").getData();

        assertEquals("app:fool-service:db:car_wash:view:100", result.resourceKey());
        assertEquals(9L, result.policyVersion());
        assertEquals(List.of("data.update", "view.query", "view.read"),
                result.actions().stream().map(EffectiveActionsController.EffectiveAction::action).toList());
        assertEquals(RiskLevel.HIGH, result.actions().get(0).minimumRisk());
        assertEquals(new ActionCatalog().definitions().values().stream()
                .filter(definition -> "View".equals(definition.resourceType())).count(), auditEvents.size());
    }

    @Test
    public void rejectsUnknownResourceTypesAndUnsafeIds() {
        EffectiveActionsController controller = new EffectiveActionsController(
                new ActionCatalog(),
                request -> AuthorizationDecision.deny("NO_MATCHING_ALLOW", 9),
                event -> {
                });

        assertEquals("RESOURCE_TYPE_INVALID", assertThrows(
                ControlledActionException.class,
                () -> controller.effectiveActions("Unknown", "100")).getMessage());
        assertEquals("RESOURCE_INVALID", assertThrows(
                ControlledActionException.class,
                () -> controller.effectiveActions("View", "100:other")).getMessage());
    }
}
