package org.fool.framework.view.service;

import org.fool.framework.common.authz.AuthorizationDecision;
import org.fool.framework.common.authz.AuthorizationDeniedException;
import org.fool.framework.common.authz.AuthorizationRequest;
import org.fool.framework.common.authz.AuthorizationService;
import org.fool.framework.common.authz.DataPolicy;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.EffectiveSubjectContext;
import org.fool.framework.common.authz.RiskLevel;
import org.fool.framework.common.authz.SecurityAuditEvent;
import org.fool.framework.common.authz.SecurityAuditService;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.query.CompareFilter;
import org.fool.framework.query.CompareOp;
import org.fool.framework.query.IQueryFilter;
import org.fool.framework.query.InFilter;
import org.fool.framework.view.dto.ListDataItem;
import org.fool.framework.view.dto.ListDataValue;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.dto.QueryDataDetailResult;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class ReadAuthorizationEnforcer {
    private final AuthorizationService authorizationService;
    private final SecurityAuditService auditService;

    public ReadAuthorizationEnforcer(AuthorizationService authorizationService,
                                     SecurityAuditService auditService) {
        this.authorizationService = authorizationService;
        this.auditService = auditService;
    }

    public DataPolicy requireView(String action, String viewId) {
        EffectiveSubject subject = EffectiveSubjectContext.require();
        String resource = viewResource(subject, viewId);
        return require(subject, action, "View", resource);
    }

    public DataPolicy requireModel(String action, String modelId) {
        EffectiveSubject subject = EffectiveSubjectContext.require();
        if (!StringUtils.hasText(modelId) || !modelId.chars().allMatch(Character::isDigit)) {
            throw new AuthorizationDeniedException("RESOURCE_OUT_OF_SCOPE");
        }
        String resource = "app:%s:db:%s:model:%s".formatted(
                subject.appId(), subject.databaseId(), modelId);
        return require(subject, action, "Model", resource);
    }

    private DataPolicy require(EffectiveSubject subject,
                               String action,
                               String resourceType,
                               String resource) {
        AuthorizationDecision decision;
        try {
            decision = authorizationService.decide(
                    new AuthorizationRequest(subject, action, resourceType, resource));
            audit(subject, action, resource, decision);
        } catch (RuntimeException ex) {
            throw new AuthorizationDeniedException("POLICY_OR_AUDIT_UNAVAILABLE");
        }
        if (!decision.allowed()) {
            throw new AuthorizationDeniedException(decision.reasonCode());
        }
        return decision.dataPolicy() == null ? DataPolicy.unrestricted() : decision.dataPolicy();
    }

    public View constrainView(View view, DataPolicy policy) {
        if (view == null) {
            return null;
        }
        List<ViewItem> readableItems = view.getListItems() == null
                ? List.of()
                : view.getListItems().stream().filter(item -> readable(policy, item)).toList();
        view.setListItems(readableItems);
        if (view.getOperations() != null) {
            view.setOperations(view.getOperations().stream()
                    .filter(operation -> operation.getOperation() != null
                            && operation.getOperation().getId() != null)
                    .filter(operation -> operationAllowed(operation.getOperation().getId()))
                    .toList());
        }
        return view;
    }

    public View constrainWritableView(View view, DataPolicy policy) {
        if (view == null) {
            return null;
        }
        List<ViewItem> writableItems = view.getListItems() == null
                ? List.of()
                : view.getListItems().stream().filter(item -> writable(policy, item)).toList();
        view.setListItems(writableItems);
        view.setOperations(List.of());
        return view;
    }

    public boolean readable(DataPolicy policy, ViewItem item) {
        Property property = item == null ? null : item.getProperty();
        return allowed(policy, item == null ? null : item.getModelProperty(),
                item == null ? null : item.getItemName(),
                item == null || item.getId() == null ? null : item.getId().toString(),
                property == null ? null : property.getName(),
                property == null ? null : property.getColumn(),
                property == null || property.getId() == null ? null : property.getId().toString());
    }

    public boolean readable(DataPolicy policy, Property property) {
        return property != null && allowed(policy,
                property.getName(),
                property.getColumn(),
                property.getId() == null ? null : property.getId().toString());
    }

    public boolean writable(DataPolicy policy, ViewItem item) {
        Property property = item == null ? null : item.getProperty();
        return allowedWrite(policy, item == null ? null : item.getModelProperty(),
                item == null ? null : item.getItemName(),
                item == null || item.getId() == null ? null : item.getId().toString(),
                property == null ? null : property.getName(),
                property == null ? null : property.getColumn(),
                property == null || property.getId() == null ? null : property.getId().toString());
    }

    public void requireFilterable(DataPolicy policy, Property property) {
        if (property == null || !allowedFilter(policy,
                property.getName(), property.getColumn(),
                property.getId() == null ? null : property.getId().toString())) {
            throw new AuthorizationDeniedException("FIELD_NOT_FILTERABLE");
        }
    }

    public void requireSortable(DataPolicy policy, Property property) {
        if (property == null || !allowedSort(policy,
                property.getName(), property.getColumn(),
                property.getId() == null ? null : property.getId().toString())) {
            throw new AuthorizationDeniedException("FIELD_NOT_SORTABLE");
        }
    }

    public IQueryFilter rowFilter(DataPolicy policy, Model model) {
        IQueryFilter result = null;
        for (DataPolicy.RowRule rowRule : policy.rowRules()) {
            if ("ALL".equals(rowRule.scopeType())) {
                return IQueryFilter.init();
            }
            IQueryFilter rule = compileRowRule(rowRule, model);
            result = result == null ? rule : result.or(rule);
        }
        if (result == null) {
            throw new AuthorizationDeniedException("ROW_POLICY_EMPTY");
        }
        return result;
    }

    public void constrainPage(DataPolicy policy, PageNavigator page) {
        if (page == null || policy.maxQueryRows() == null || policy.maxQueryRows() <= 0) {
            return;
        }
        if (page.getPageSize() <= 0 || page.getPageSize() > policy.maxQueryRows()) {
            page.setPageSize(policy.maxQueryRows());
        }
    }

    public ListViewResult mask(ListViewResult result, DataPolicy policy) {
        if (result == null || result.getItems() == null) {
            return result;
        }
        result.getItems().forEach(item -> mask(item, policy));
        return result;
    }

    public QueryDataDetailResult mask(QueryDataDetailResult result, DataPolicy policy) {
        if (result == null || result.getData() == null) {
            return result;
        }
        maskValues(result.getData().getSimpleData(), policy);
        if (result.getData().getItems() != null) {
            result.getData().getItems().forEach(group -> {
                maskValues(group.getProperties(), policy);
                if (group.getItems() != null) {
                    group.getItems().forEach(item -> maskValues(item.getValues(), policy));
                }
            });
        }
        return result;
    }

    private void mask(ListDataItem item, DataPolicy policy) {
        if (item == null) {
            return;
        }
        maskValues(item.getItems(), policy);
        if (item.getValues() != null) {
            item.getValues().replaceAll((field, value) -> maskedValue(policy, field, value));
        }
    }

    private void maskValues(List<ListDataValue> values, DataPolicy policy) {
        if (values == null) {
            return;
        }
        values.forEach(value -> {
            if (value != null) {
                Object formatted = maskedValue(policy, value.getPrpId(), value.getFmtValue());
                Object objectId = maskedValue(policy, value.getPrpId(), value.getObjId());
                value.setFmtValue(formatted == null ? null : String.valueOf(formatted));
                value.setObjId(objectId == null ? null : String.valueOf(objectId));
            }
        });
    }

    private Object maskedValue(DataPolicy policy, String field, Object value) {
        if (value == null) {
            return null;
        }
        return switch (policy.maskStrategy(field).toUpperCase()) {
            case "FULL" -> "****";
            case "LAST4" -> lastFour(value.toString());
            case "HASH" -> sha256(value.toString());
            default -> value;
        };
    }

    private IQueryFilter compileRowRule(DataPolicy.RowRule rule, Model model) {
        Map<String, Object> filter = rule.filter();
        return switch (rule.scopeType()) {
            case "OWN" -> equality(model, string(filter.getOrDefault("field", "owner_user_id")),
                    EffectiveSubjectContext.require().userId());
            case "COMPANY" -> equality(model, string(filter.getOrDefault("field", "company_id")),
                    EffectiveSubjectContext.require().companyId());
            case "DEPARTMENT" -> in(model,
                    string(filter.getOrDefault("field", "department_id")),
                    EffectiveSubjectContext.require().departmentIds());
            case "DEPARTMENT_TREE" -> in(model,
                    string(filter.getOrDefault("field", "department_id")),
                    EffectiveSubjectContext.require().departmentTreeIds());
            case "EXPLICIT", "CUSTOM" -> compileCustom(filter, model);
            default -> throw new AuthorizationDeniedException("ROW_POLICY_UNSUPPORTED");
        };
    }

    private IQueryFilter compileCustom(Map<String, Object> filter, Model model) {
        Object clauses = filter.get("all");
        if (!(clauses instanceof Collection<?> collection) || collection.isEmpty()) {
            throw new AuthorizationDeniedException("ROW_POLICY_INVALID");
        }
        IQueryFilter result = IQueryFilter.init();
        for (Object value : collection) {
            if (!(value instanceof Map<?, ?> clause)) {
                throw new AuthorizationDeniedException("ROW_POLICY_INVALID");
            }
            String field = string(clause.get("field"));
            String operator = string(clause.get("op"));
            Object expected = clause.get("value");
            IQueryFilter compiled = switch (operator) {
                case "eq" -> equality(model, field, string(expected));
                case "in" -> expected instanceof Collection<?> values
                        ? in(model, field, values.stream().map(ReadAuthorizationEnforcer::string).toList())
                        : throwInvalidPolicy();
                case "eqSubject" -> equality(model, field, subjectValue(string(expected)));
                default -> throwInvalidPolicy();
            };
            result = result.and(compiled);
        }
        return result;
    }

    private IQueryFilter throwInvalidPolicy() {
        throw new AuthorizationDeniedException("ROW_POLICY_INVALID");
    }

    private IQueryFilter equality(Model model, String field, String value) {
        Property property = policyProperty(model, field);
        return new CompareFilter(property.getColumn(), CompareOp.EQUAL, value);
    }

    private IQueryFilter in(Model model, String field, Collection<String> values) {
        if (values == null || values.isEmpty()) {
            throw new AuthorizationDeniedException("ROW_POLICY_EMPTY");
        }
        Property property = policyProperty(model, field);
        return new InFilter(property.getColumn(), values.toArray(String[]::new));
    }

    private Property policyProperty(Model model, String field) {
        if (model == null || model.getProperties() == null || !StringUtils.hasText(field)) {
            throw new AuthorizationDeniedException("ROW_POLICY_FIELD_INVALID");
        }
        return model.getProperties().stream()
                .filter(property -> StringUtils.hasText(property.getColumn()))
                .filter(property -> Objects.equals(property.getName(), field)
                        || Objects.equals(property.getColumn(), field)
                        || (property.getId() != null && Objects.equals(property.getId().toString(), field)))
                .findFirst()
                .orElseThrow(() -> new AuthorizationDeniedException("ROW_POLICY_FIELD_INVALID"));
    }

    private String subjectValue(String name) {
        EffectiveSubject subject = EffectiveSubjectContext.require();
        return switch (name) {
            case "userId" -> subject.userId();
            case "companyId" -> subject.companyId();
            default -> throw new AuthorizationDeniedException("ROW_POLICY_SUBJECT_INVALID");
        };
    }

    private boolean operationAllowed(Long operationId) {
        EffectiveSubject subject = EffectiveSubjectContext.require();
        String resource = "app:%s:db:%s:operation:%s".formatted(
                subject.appId(), subject.databaseId(), operationId);
        try {
            AuthorizationDecision decision = authorizationService.decide(new AuthorizationRequest(
                    subject, "operation.execute", "Operation", resource));
            audit(subject, "operation.execute", resource, decision);
            return decision.allowed();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private void audit(EffectiveSubject subject,
                       String action,
                       String resource,
                       AuthorizationDecision decision) {
        auditService.record(new SecurityAuditEvent(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                subject.userId(),
                "SERVICE",
                null,
                null,
                action,
                resource,
                decision.allowed() ? "ALLOW" : "DENY",
                decision.reasonCode(),
                RiskLevel.LOW,
                decision.policyVersion(),
                null,
                null,
                Instant.now()));
    }

    private static boolean allowed(DataPolicy policy, String... candidates) {
        return allowed(policy, policy::readable, candidates);
    }

    private static boolean allowedFilter(DataPolicy policy, String... candidates) {
        return allowed(policy, policy::filterable, candidates);
    }

    private static boolean allowedSort(DataPolicy policy, String... candidates) {
        return allowed(policy, policy::sortable, candidates);
    }

    private static boolean allowedWrite(DataPolicy policy, String... candidates) {
        return allowed(policy, policy::writable, candidates);
    }

    private static boolean allowed(DataPolicy policy,
                                   java.util.function.Predicate<String> permission,
                                   String... candidates) {
        if (candidates == null) {
            return false;
        }
        List<String> identities = java.util.Arrays.stream(candidates)
                .filter(StringUtils::hasText)
                .toList();
        return identities.stream().noneMatch(policy::denied)
                && identities.stream().anyMatch(permission);
    }

    private static String viewResource(EffectiveSubject subject, String viewId) {
        if (!StringUtils.hasText(viewId) || !viewId.chars().allMatch(Character::isDigit)) {
            throw new AuthorizationDeniedException("RESOURCE_OUT_OF_SCOPE");
        }
        return "app:%s:db:%s:view:%s".formatted(subject.appId(), subject.databaseId(), viewId);
    }

    private static String lastFour(String value) {
        if (value.length() <= 4) {
            return "****";
        }
        return "****" + value.substring(value.length() - 4);
    }

    private static String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }

    private static String string(Object value) {
        return value == null ? "" : value.toString();
    }
}
