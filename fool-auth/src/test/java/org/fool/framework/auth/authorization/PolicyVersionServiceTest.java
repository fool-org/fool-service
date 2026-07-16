package org.fool.framework.auth.authorization;

import org.fool.framework.common.authz.PolicyVersionQuery;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PolicyVersionServiceTest {
    @Test
    public void everyLookupObservesTheLatestPolicyFingerprintWithoutTtl() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(
                PolicyVersionQuery.SQL, Long.class,
                "app", "db", "app", "db")).thenReturn(7L, 8L);
        PolicyVersionService service = new PolicyVersionService(jdbcTemplate);

        assertEquals(7L, service.currentVersion("app", "db"));
        assertEquals(8L, service.currentVersion("app", "db"));
        verify(jdbcTemplate, times(2)).queryForObject(
                PolicyVersionQuery.SQL, Long.class,
                "app", "db", "app", "db");
    }
}
