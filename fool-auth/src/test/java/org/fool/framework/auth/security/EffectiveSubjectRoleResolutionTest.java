package org.fool.framework.auth.security;

import org.fool.framework.auth.authorization.JdbcEffectiveSubjectLookup;
import org.fool.framework.auth.authorization.PolicyVersionService;
import org.fool.framework.auth.business.service.TokenService;
import org.fool.framework.common.authz.EffectiveSubject;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EffectiveSubjectRoleResolutionTest {
    @Test
    public void requestSubjectIncludesDirectAndDepartmentAssignedLegacyRoles() {
        TokenService tokenService = mock(TokenService.class);
        when(tokenService.authenticate("token")).thenReturn(new TokenService.AuthenticatedToken(
                "alice", "auth:session", Instant.EPOCH, "hash"));
        when(tokenService.getLegacyAppId("token")).thenReturn("app-1");
        when(tokenService.getLegacyDbId("token")).thenReturn("main");
        PolicyVersionService policyVersionService = policyVersionService();
        DepartmentTreeResolver departmentTreeResolver = departmentTreeResolver();

        EffectiveSubject subject = new EffectiveSubjectResolver(
                tokenService,
                policyVersionService,
                new SubjectJdbcTemplate(),
                departmentTreeResolver).resolve("token");

        assertEquals(List.of("auth:2", "legacy:app-1:3", "legacy:app-1:4"), subject.roleIds());
        assertEquals(List.of("10"), subject.departmentIds());
        assertEquals(List.of("10", "11"), subject.departmentTreeIds());
        assertEquals("1", subject.companyId());
    }

    @Test
    public void approvalRecheckIncludesDepartmentAssignedLegacyRoles() {
        EffectiveSubject subject = new JdbcEffectiveSubjectLookup(
                new SubjectJdbcTemplate(),
                policyVersionService(),
                departmentTreeResolver()).resolve("alice", "app-1", "main");

        assertEquals(List.of("auth:2", "legacy:app-1:3", "legacy:app-1:4"), subject.roleIds());
        assertEquals(List.of("10"), subject.departmentIds());
        assertEquals(List.of("10", "11"), subject.departmentTreeIds());
        assertEquals("1", subject.companyId());
    }

    @Test
    public void subjectsAllowDepartmentWithoutCompanyAssignment() {
        TokenService tokenService = mock(TokenService.class);
        when(tokenService.authenticate("token")).thenReturn(new TokenService.AuthenticatedToken(
                "alice", "auth:session", Instant.EPOCH, "hash"));
        when(tokenService.getLegacyAppId("token")).thenReturn("app-1");
        when(tokenService.getLegacyDbId("token")).thenReturn("main");
        SubjectJdbcTemplate jdbcTemplate = new SubjectJdbcTemplate("");

        EffectiveSubject requestSubject = new EffectiveSubjectResolver(
                tokenService,
                policyVersionService(),
                jdbcTemplate,
                departmentTreeResolver()).resolve("token");
        EffectiveSubject approvalSubject = new JdbcEffectiveSubjectLookup(
                jdbcTemplate,
                policyVersionService(),
                departmentTreeResolver()).resolve("alice", "app-1", "main");

        assertEquals("", requestSubject.companyId());
        assertEquals("", approvalSubject.companyId());
    }

    private static PolicyVersionService policyVersionService() {
        PolicyVersionService service = mock(PolicyVersionService.class);
        when(service.currentVersion("app-1", "main")).thenReturn(7L);
        return service;
    }

    private static DepartmentTreeResolver departmentTreeResolver() {
        DepartmentTreeResolver resolver = mock(DepartmentTreeResolver.class);
        when(resolver.expand(List.of("10"))).thenReturn(List.of("10", "11"));
        return resolver;
    }

    private static final class SubjectJdbcTemplate extends JdbcTemplate {
        private final String companyId;

        private SubjectJdbcTemplate() {
            this("1");
        }

        private SubjectJdbcTemplate(String companyId) {
            this.companyId = companyId;
        }

        @Override
        public <T> List<T> queryForList(String sql, Class<T> elementType, Object... args) {
            List<String> values;
            if (sql.contains("`auth_user_role`")) {
                values = List.of("auth:2");
            } else if (sql.contains("`SW_APP_AUTH_ROLE_SW_APP_AUTH_USER`")) {
                values = List.of("legacy:" + args[0] + ":3");
            } else if (sql.contains("`SW_APP_AUTH_DEPARTMENT_SW_APP_AUTH_ROLE`")) {
                values = List.of("legacy:" + args[0] + ":4");
            } else if (sql.contains("`APP_AUTH_DEP` AS CHAR")) {
                values = List.of("10");
            } else if (sql.contains("`SW_APP_AUTH_COMPANY_DepsAPP_COR_ID`")) {
                assertTrue(sql.contains("IS NOT NULL"));
                values = companyId.isBlank() ? List.of() : List.of(companyId);
            } else {
                throw new AssertionError("Unexpected subject query: " + sql);
            }
            return values.stream().map(elementType::cast).toList();
        }
    }
}
