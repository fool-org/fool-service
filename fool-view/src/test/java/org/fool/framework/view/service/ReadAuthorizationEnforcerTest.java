package org.fool.framework.view.service;

import org.fool.framework.common.authz.AuthorizationDecision;
import org.fool.framework.common.authz.AuthorizationDeniedException;
import org.fool.framework.common.authz.DataPolicy;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.EffectiveSubjectContext;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.QueryAndArgs;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.view.dto.ListDataItem;
import org.fool.framework.view.dto.ListDataValue;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ReadAuthorizationEnforcerTest {
    private final DataPolicy policy = new DataPolicy(
            List.of(new DataPolicy.RowRule("CUSTOM", Map.of("all", List.of(
                    Map.of("field", "companyId", "op", "eqSubject", "value", "companyId"),
                    Map.of("field", "state", "op", "in", "value", List.of("OPEN", "CLOSED")))))),
            Set.of("symbol", "phone"),
            Set.of("symbol"),
            Set.of("symbol"),
            Set.of("symbol"),
            Set.of(),
            Set.of("symbol"),
            Map.of("phone", "LAST4"),
            Map.of(),
            Set.of("local"),
            25,
            100);

    @Before
    public void setSubject() {
        EffectiveSubjectContext.set(new EffectiveSubject(
                "user-1", List.of("role-1"), "company-1", List.of("department-1"),
                "fool-service", "car_wash", "session-1",
                Instant.parse("2026-07-15T00:00:00Z"), null, 7));
    }

    @After
    public void clearSubject() {
        EffectiveSubjectContext.clear();
    }

    @Test
    public void compilesStructuredRowsAndEnforcesFieldsAndQuota() {
        ReadAuthorizationEnforcer enforcer = enforcer(AuthorizationDecision.allow(7, "limited", policy));
        Model model = model();

        QueryAndArgs query = enforcer.rowFilter(enforcer.requireView("view.query", "100"), model).generateSql();
        PageNavigator page = new PageNavigator();
        page.setPageSize(500);
        enforcer.constrainPage(policy, page);

        assertTrue(query.getSql().contains("`company_id`= ?"));
        assertTrue(query.getSql().contains("`state` IN (?, ?)"));
        assertArrayEquals(new Object[]{"company-1", "OPEN", "CLOSED"}, query.getArgs());
        assertEquals(25, page.getPageSize());
        assertTrue(enforcer.readable(policy, model.getProperties().get(2)));
        assertFalse(enforcer.readable(policy, model.getProperties().get(1)));
        assertThrows(AuthorizationDeniedException.class,
                () -> enforcer.requireFilterable(policy, model.getProperties().get(1)));
    }

    @Test
    public void departmentTreeUsesExpandedSubjectScope() {
        EffectiveSubjectContext.set(new EffectiveSubject(
                "u1", List.of(), "c1", List.of("10"), List.of("10", "11", "12"),
                "app", "db", "session", Instant.now(), null, 7));
        DataPolicy policy = new DataPolicy(
                List.of(new DataPolicy.RowRule("DEPARTMENT_TREE", Map.of("field", "department_id"))),
                Set.of("*"), Set.of(), Set.of(), Set.of(), Set.of(), Set.of(),
                Map.of(), Map.of(), Set.of(), 100, 100);

        QueryAndArgs query = enforcer(AuthorizationDecision.allow(7, "tree", policy))
                .rowFilter(policy, model()).generateSql();
        assertTrue(query.getSql().contains("`department_id` IN (?, ?, ?)"));
        assertArrayEquals(new Object[]{"10", "11", "12"}, query.getArgs());
    }

    @Test
    public void masksValuesWithoutConvertingNullToText() {
        ReadAuthorizationEnforcer enforcer = enforcer(AuthorizationDecision.allow(7, "limited", policy));
        ListDataValue value = new ListDataValue();
        value.setPrpId("phone");
        value.setFmtValue("13800138000");
        value.setObjId(null);
        ListDataItem item = new ListDataItem();
        item.setItems(List.of(value));
        ListViewResult result = new ListViewResult();
        result.setItems(List.of(item));

        enforcer.mask(result, policy);

        assertEquals("****8000", value.getFmtValue());
        assertEquals(null, value.getObjId());
    }

    @Test
    public void initViewKeepsOnlyWritableFieldsAndNoOperations() {
        DataPolicy writable = new DataPolicy(
                List.of(new DataPolicy.RowRule("ALL", Map.of())),
                Set.of("symbol", "phone"), Set.of(), Set.of(), Set.of(), Set.of("symbol"), Set.of(),
                Map.of(), Map.of(), Set.of(), 25, 100);
        ViewItem symbol = new ViewItem();
        symbol.setProperty(property(3, "symbol", "symbol"));
        ViewItem phone = new ViewItem();
        phone.setProperty(property(4, "phone", "phone"));
        View view = new View();
        view.setListItems(List.of(symbol, phone));

        enforcer(AuthorizationDecision.allow(7, "write", writable))
                .constrainWritableView(view, writable);

        assertEquals(List.of(symbol), view.getListItems());
        assertTrue(view.getOperations().isEmpty());
    }

    @Test
    public void fieldDenyAppliesToEveryAliasEvenWhenAnotherRoleAllowsWildcard() {
        DataPolicy denied = new DataPolicy(
                List.of(new DataPolicy.RowRule("ALL", Map.of())),
                Set.of("*"), Set.of("*"), Set.of("*"), Set.of("*"), Set.of("*"), Set.of("*"),
                Map.of(), Map.of(), Set.of(), 100, 100, Set.of("symbol"));
        ReadAuthorizationEnforcer enforcer = enforcer(AuthorizationDecision.allow(7, "merged", denied));
        Property symbol = property(3, "symbol", "symbol_column");

        assertFalse(enforcer.readable(denied, symbol));
        assertThrows(AuthorizationDeniedException.class,
                () -> enforcer.requireFilterable(denied, symbol));
        assertThrows(AuthorizationDeniedException.class,
                () -> enforcer.requireSortable(denied, symbol));
    }

    @Test
    public void denyAndAuditFailureAreFailClosed() {
        assertThrows(AuthorizationDeniedException.class,
                () -> enforcer(AuthorizationDecision.deny("NO_MATCHING_ALLOW", 7))
                        .requireView("view.query", "100"));
        ReadAuthorizationEnforcer auditFailure = new ReadAuthorizationEnforcer(
                request -> AuthorizationDecision.allow(7, "limited", policy),
                event -> { throw new IllegalStateException("audit down"); });
        assertEquals("POLICY_OR_AUDIT_UNAVAILABLE",
                assertThrows(AuthorizationDeniedException.class,
                        () -> auditFailure.requireView("view.query", "100")).reasonCode());
    }

    private ReadAuthorizationEnforcer enforcer(AuthorizationDecision decision) {
        return new ReadAuthorizationEnforcer(request -> decision, event -> { });
    }

    private Model model() {
        Model model = new Model();
        model.setName("Order");
        model.setProperties(List.of(
                property(1, "companyId", "company_id"),
                property(2, "state", "state"),
                property(3, "symbol", "symbol"),
                property(4, "phone", "phone"),
                property(5, "departmentId", "department_id")));
        return model;
    }

    private Property property(long id, String name, String column) {
        Property property = new Property();
        property.setId(id);
        property.setName(name);
        property.setColumn(column);
        return property;
    }
}
