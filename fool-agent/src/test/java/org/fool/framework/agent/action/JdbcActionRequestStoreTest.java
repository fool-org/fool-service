package org.fool.framework.agent.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.common.authz.ControlledActionException;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JdbcActionRequestStoreTest {
    @Test
    public void staleConcurrentTransitionIsRejectedByCompareAndSetUpdate() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(0);
        JdbcActionRequestStore store = new JdbcActionRequestStore(jdbcTemplate, new ObjectMapper());

        ControlledActionException error = assertThrows(ControlledActionException.class,
                () -> store.transition("request-1", ActionRequestStatus.APPROVED,
                        ActionRequestStatus.EXECUTING, Instant.EPOCH));

        assertEquals("ACTION_STATE_CONFLICT", error.getMessage());
    }
}
