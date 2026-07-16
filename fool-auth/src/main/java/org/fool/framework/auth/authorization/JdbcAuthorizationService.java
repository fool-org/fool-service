package org.fool.framework.auth.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.common.authz.AuthorizationDecision;
import org.fool.framework.common.authz.AuthorizationRequest;
import org.fool.framework.common.authz.AuthorizationService;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.RiskLevel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class JdbcAuthorizationService implements AuthorizationService {
    private final JdbcTemplate jdbcTemplate;
    private final JdbcDataPolicyLoader dataPolicyLoader;

    @Autowired
    public JdbcAuthorizationService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataPolicyLoader = new JdbcDataPolicyLoader(jdbcTemplate, objectMapper);
    }

    JdbcAuthorizationService(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, new ObjectMapper());
    }

    @Override
    public AuthorizationDecision decide(AuthorizationRequest request) {
        try {
            List<Map<String, Object>> bindings = jdbcTemplate.queryForList("""
                    SELECT binding.`SUBJECT_TYPE`, binding.`SUBJECT_ID`, binding.`EFFECT`,
                           binding.`INCLUDE_CHILDREN`, binding.`DATA_POLICY_ID`,
                           permission.`RESOURCE_PATTERN`, permission.`MIN_RISK`
                      FROM `FOOL_AUTHZ_PERMISSION` permission
                      JOIN `FOOL_AUTHZ_BINDING` binding
                        ON binding.`PERMISSION_ID` = permission.`PERMISSION_ID`
                     WHERE permission.`ENABLED` = 1
                       AND (permission.`ACTION_ID` = ? OR permission.`ACTION_ID` = '*')
                       AND (permission.`RESOURCE_TYPE` = ? OR permission.`RESOURCE_TYPE` = '*')
                       AND (binding.`APP_ID` = ? OR binding.`APP_ID` = '*')
                       AND (binding.`DATABASE_ID` = ? OR binding.`DATABASE_ID` = '*')
                       AND (binding.`VALID_FROM` IS NULL OR binding.`VALID_FROM` <= CURRENT_TIMESTAMP(6))
                       AND (binding.`VALID_UNTIL` IS NULL OR binding.`VALID_UNTIL` > CURRENT_TIMESTAMP(6))
                     ORDER BY binding.`BINDING_ID`
                    """,
                    request.action(),
                    request.resourceType(),
                    request.subject().appId(),
                    request.subject().databaseId());

            List<Map<String, Object>> matches = bindings.stream()
                    .filter(row -> subjectMatches(request.subject(), row))
                    .filter(row -> resourceMatches(request.resourceKey(), row))
                    .toList();
            if (matches.stream().anyMatch(row -> "DENY".equals(value(row, "EFFECT")))) {
                return AuthorizationDecision.deny("EXPLICIT_DENY", request.subject().policyVersion());
            }
            List<Map<String, Object>> allows = matches.stream()
                    .filter(row -> "ALLOW".equals(value(row, "EFFECT")))
                    .toList();
            if (allows.isEmpty()) {
                return AuthorizationDecision.deny("NO_MATCHING_ALLOW", request.subject().policyVersion());
            }
            List<String> dataPolicyIds = allows.stream()
                    .map(row -> nullableValue(row, "DATA_POLICY_ID"))
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
            RiskLevel minimumRisk = allows.stream()
                    .map(row -> risk(value(row, "MIN_RISK")))
                    .reduce(RiskLevel.LOW, RiskLevel::max);
            return AuthorizationDecision.allow(
                    request.subject().policyVersion(),
                    String.join(",", dataPolicyIds),
                    dataPolicyLoader.load(dataPolicyIds),
                    minimumRisk);
        } catch (RuntimeException ex) {
            return AuthorizationDecision.deny("POLICY_UNAVAILABLE", request.subject().policyVersion());
        }
    }

    private static boolean subjectMatches(EffectiveSubject subject, Map<String, Object> row) {
        String type = value(row, "SUBJECT_TYPE");
        String id = value(row, "SUBJECT_ID");
        return switch (type) {
            case "USER" -> subject.userId().equals(id);
            case "ROLE" -> subject.roleIds().contains(id);
            case "DEPARTMENT" -> subject.departmentIds().contains(id);
            case "COMPANY" -> subject.companyId().equals(id);
            default -> false;
        };
    }

    private static boolean resourceMatches(String resourceKey, Map<String, Object> row) {
        String pattern = value(row, "RESOURCE_PATTERN");
        if ("*".equals(pattern) || pattern.equals(resourceKey)) {
            return true;
        }
        if (pattern.endsWith("*")) {
            return resourceKey.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        Object includeChildren = row.get("INCLUDE_CHILDREN");
        boolean includesChildren = includeChildren instanceof Number number
                ? number.intValue() != 0
                : Boolean.parseBoolean(String.valueOf(includeChildren));
        return includesChildren && resourceKey.startsWith(pattern + ":");
    }

    private static String value(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? "" : value.toString();
    }

    private static String nullableValue(Map<String, Object> row, String key) {
        String value = value(row, key);
        return value.isBlank() ? null : value;
    }

    private static RiskLevel risk(String value) {
        return value.isBlank() ? RiskLevel.LOW : RiskLevel.valueOf(value.trim().toUpperCase());
    }
}
