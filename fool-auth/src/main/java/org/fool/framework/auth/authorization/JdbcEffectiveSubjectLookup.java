package org.fool.framework.auth.authorization;

import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.EffectiveSubjectLookup;
import org.fool.framework.auth.security.DepartmentTreeResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class JdbcEffectiveSubjectLookup implements EffectiveSubjectLookup {
    private final JdbcTemplate jdbcTemplate;
    private final PolicyVersionService policyVersionService;
    private final DepartmentTreeResolver departmentTreeResolver;

    public JdbcEffectiveSubjectLookup(JdbcTemplate jdbcTemplate,
                                      PolicyVersionService policyVersionService,
                                      DepartmentTreeResolver departmentTreeResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.policyVersionService = policyVersionService;
        this.departmentTreeResolver = departmentTreeResolver;
    }

    @Override
    public EffectiveSubject resolve(String userId, String appId, String databaseId) {
        List<String> roles = new ArrayList<>(jdbcTemplate.queryForList("""
                SELECT CONCAT('auth:', `role_id`) FROM `auth_user_role` WHERE `user_id` = ?
                """, String.class, userId));
        roles.addAll(jdbcTemplate.queryForList("""
                SELECT CONCAT('legacy:', ?, ':', relation.`SW_APP_AUTH_ROLE_ID`)
                  FROM `SW_APP_AUTH_ROLE_SW_APP_AUTH_USER` relation
                  JOIN `SW_APP_AUTH_USER` legacy_user
                    ON legacy_user.`APP_AUTH_ID` = relation.`SW_APP_AUTH_USER_ID`
                 WHERE legacy_user.`APP_AUTH_USERLOGINNAME` = ? OR legacy_user.`APP_AUTH_USERID` = ?
                """, String.class, appId, userId, userId));
        roles.addAll(jdbcTemplate.queryForList("""
                SELECT CONCAT('legacy:', ?, ':', relation.`SW_APP_AUTH_ROLE_ID`)
                  FROM `SW_APP_AUTH_DEPARTMENT_SW_APP_AUTH_ROLE` relation
                  JOIN `SW_APP_AUTH_USER` legacy_user
                    ON legacy_user.`APP_AUTH_DEP` = relation.`SW_APP_AUTH_DEPARTMENT_ID`
                 WHERE legacy_user.`APP_AUTH_USERLOGINNAME` = ? OR legacy_user.`APP_AUTH_USERID` = ?
                """, String.class, appId, userId, userId));
        List<String> departments = jdbcTemplate.queryForList("""
                SELECT CAST(`APP_AUTH_DEP` AS CHAR) FROM `SW_APP_AUTH_USER`
                 WHERE (`APP_AUTH_USERLOGINNAME` = ? OR `APP_AUTH_USERID` = ?)
                   AND `APP_AUTH_DEP` IS NOT NULL
                """, String.class, userId, userId);
        List<String> companies = jdbcTemplate.queryForList("""
                SELECT CAST(department.`SW_APP_AUTH_COMPANY_DepsAPP_COR_ID` AS CHAR)
                  FROM `SW_APP_AUTH_USER` legacy_user
                  JOIN `SW_APP_AUTH_DEPARTMENT` department
                    ON department.`APP_DEP_ID` = legacy_user.`APP_AUTH_DEP`
                 WHERE (legacy_user.`APP_AUTH_USERLOGINNAME` = ? OR legacy_user.`APP_AUTH_USERID` = ?)
                   AND department.`SW_APP_AUTH_COMPANY_DepsAPP_COR_ID` IS NOT NULL
                 LIMIT 1
                """, String.class, userId, userId);
        return new EffectiveSubject(userId, roles.stream().distinct().toList(),
                companies.stream().findFirst().orElse(""), departments.stream().distinct().toList(),
                departmentTreeResolver.expand(departments.stream().distinct().toList()),
                appId, databaseId, "approval-recheck", Instant.now(), null,
                policyVersionService.currentVersion(appId, databaseId));
    }
}
