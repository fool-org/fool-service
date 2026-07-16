package org.fool.framework.auth.security;

import org.fool.framework.auth.authorization.PolicyVersionService;
import org.fool.framework.auth.business.service.TokenService;
import org.fool.framework.common.authz.EffectiveSubject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class EffectiveSubjectResolver {
    private final TokenService tokenService;
    private final PolicyVersionService policyVersionService;
    private final JdbcTemplate jdbcTemplate;
    private final DepartmentTreeResolver departmentTreeResolver;

    @Value("${fool.auth.default-app-id:fool-service}")
    private String defaultAppId;

    @Value("${fool.auth.default-database-id:car_wash}")
    private String defaultDatabaseId;

    public EffectiveSubjectResolver(TokenService tokenService,
                                    PolicyVersionService policyVersionService,
                                    JdbcTemplate jdbcTemplate,
                                    DepartmentTreeResolver departmentTreeResolver) {
        this.tokenService = tokenService;
        this.policyVersionService = policyVersionService;
        this.jdbcTemplate = jdbcTemplate;
        this.departmentTreeResolver = departmentTreeResolver;
    }

    public EffectiveSubject resolve(String token) {
        TokenService.AuthenticatedToken authenticated = tokenService.authenticate(token);
        String appId = defaultValue(tokenService.getLegacyAppId(token), defaultAppId);
        String databaseId = defaultValue(tokenService.getLegacyDbId(token), defaultDatabaseId);
        List<String> roles = new ArrayList<>(jdbcTemplate.queryForList("""
                SELECT CONCAT('auth:', `role_id`)
                  FROM `auth_user_role`
                 WHERE `user_id` = ?
                """, String.class, authenticated.userId()));
        roles.addAll(jdbcTemplate.queryForList("""
                SELECT CONCAT('legacy:', ?, ':', relation.`SW_APP_AUTH_ROLE_ID`)
                  FROM `SW_APP_AUTH_ROLE_SW_APP_AUTH_USER` relation
                  JOIN `SW_APP_AUTH_USER` legacy_user
                    ON legacy_user.`APP_AUTH_ID` = relation.`SW_APP_AUTH_USER_ID`
                 WHERE legacy_user.`APP_AUTH_USERLOGINNAME` = ?
                    OR legacy_user.`APP_AUTH_USERID` = ?
                """, String.class, appId, authenticated.userId(), authenticated.userId()));
        roles.addAll(jdbcTemplate.queryForList("""
                SELECT CONCAT('legacy:', ?, ':', relation.`SW_APP_AUTH_ROLE_ID`)
                  FROM `SW_APP_AUTH_DEPARTMENT_SW_APP_AUTH_ROLE` relation
                  JOIN `SW_APP_AUTH_USER` legacy_user
                    ON legacy_user.`APP_AUTH_DEP` = relation.`SW_APP_AUTH_DEPARTMENT_ID`
                 WHERE legacy_user.`APP_AUTH_USERLOGINNAME` = ?
                    OR legacy_user.`APP_AUTH_USERID` = ?
                """, String.class, appId, authenticated.userId(), authenticated.userId()));

        List<String> departments = jdbcTemplate.queryForList("""
                SELECT CAST(`APP_AUTH_DEP` AS CHAR)
                  FROM `SW_APP_AUTH_USER`
                 WHERE (`APP_AUTH_USERLOGINNAME` = ? OR `APP_AUTH_USERID` = ?)
                   AND `APP_AUTH_DEP` IS NOT NULL
                """, String.class, authenticated.userId(), authenticated.userId());
        List<String> companies = jdbcTemplate.queryForList("""
                SELECT CAST(department.`SW_APP_AUTH_COMPANY_DepsAPP_COR_ID` AS CHAR)
                  FROM `SW_APP_AUTH_USER` legacy_user
                 JOIN `SW_APP_AUTH_DEPARTMENT` department
                    ON department.`APP_DEP_ID` = legacy_user.`APP_AUTH_DEP`
                 WHERE (legacy_user.`APP_AUTH_USERLOGINNAME` = ?
                    OR legacy_user.`APP_AUTH_USERID` = ?)
                   AND department.`SW_APP_AUTH_COMPANY_DepsAPP_COR_ID` IS NOT NULL
                 LIMIT 1
                """, String.class, authenticated.userId(), authenticated.userId());
        long policyVersion = policyVersionService.currentVersion(appId, databaseId);
        return new EffectiveSubject(
                authenticated.userId(),
                roles.stream().distinct().toList(),
                companies.stream().findFirst().orElse(""),
                departments.stream().distinct().toList(),
                departmentTreeResolver.expand(departments.stream().distinct().toList()),
                appId,
                databaseId,
                authenticated.sessionId(),
                authenticated.authenticatedAt(),
                tokenService.stepUpAt(authenticated.sessionId()),
                policyVersion);
    }

    private static String defaultValue(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
}
