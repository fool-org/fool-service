package org.fool.framework.auth.authorization;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.fool.framework.common.authz.PolicyVersionQuery;

@Service
public class PolicyVersionService {
    private final JdbcTemplate jdbcTemplate;

    public PolicyVersionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long currentVersion(String appId, String databaseId) {
        Long version = jdbcTemplate.queryForObject(
                PolicyVersionQuery.SQL, Long.class, appId, databaseId, appId, databaseId);
        return version == null ? 0L : version;
    }
}
