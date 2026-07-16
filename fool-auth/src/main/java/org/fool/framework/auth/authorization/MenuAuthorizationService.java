package org.fool.framework.auth.authorization;

import org.fool.framework.auth.business.model.Auth;
import org.fool.framework.auth.foolframework.auth.MenuItem;
import org.fool.framework.common.authz.AuthorizationRequest;
import org.fool.framework.common.authz.AuthorizationService;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.EffectiveSubjectContext;
import org.fool.framework.common.authz.RiskLevel;
import org.fool.framework.common.authz.SecurityAuditEvent;
import org.fool.framework.common.authz.SecurityAuditService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.time.Instant;
import java.util.UUID;

@Service
public class MenuAuthorizationService {
    private final AuthorizationService authorizationService;
    private final JdbcTemplate jdbcTemplate;
    private final SecurityAuditService auditService;

    public MenuAuthorizationService(AuthorizationService authorizationService,
                                    JdbcTemplate jdbcTemplate,
                                    SecurityAuditService auditService) {
        this.authorizationService = authorizationService;
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
    }

    public List<MenuItem> visibleLegacyMenus(List<MenuItem> menus) {
        return menus == null ? List.of() : menus.stream()
                .filter(menu -> menu.getViewId() == null || menu.getViewId() <= 0 || canDiscover(menu.getViewId()))
                .toList();
    }

    public List<Auth> visibleAuthItems(List<Auth> items) {
        return items == null ? List.of() : items.stream()
                .filter(item -> item.getAuthType() != 1 || canDiscover(viewId(item.getAuthName())))
                .toList();
    }

    private Long viewId(String viewName) {
        if (!StringUtils.hasText(viewName)) {
            return null;
        }
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT `id` FROM `fool_sys_view` WHERE `view_name` = ? LIMIT 1",
                Long.class,
                viewName.trim());
        return ids.stream().findFirst().orElse(null);
    }

    private boolean canDiscover(Long viewId) {
        if (viewId == null) {
            return false;
        }
        EffectiveSubject subject = EffectiveSubjectContext.require();
        String resource = "app:%s:db:%s:view:%s".formatted(
                subject.appId(), subject.databaseId(), viewId);
        try {
            var decision = authorizationService.decide(new AuthorizationRequest(
                    subject, "view.discover", "View", resource));
            auditService.record(new SecurityAuditEvent(
                    UUID.randomUUID().toString(), UUID.randomUUID().toString(), subject.userId(),
                    "SERVICE", null, null, "view.discover", resource,
                    decision.allowed() ? "ALLOW" : "DENY", decision.reasonCode(), RiskLevel.LOW,
                    decision.policyVersion(), null, null, Instant.now()));
            return decision.allowed();
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
