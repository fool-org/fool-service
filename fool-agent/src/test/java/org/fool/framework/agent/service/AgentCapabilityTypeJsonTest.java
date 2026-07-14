package org.fool.framework.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AgentCapabilityTypeJsonTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void serializesStableCapabilityId() throws Exception {
        assertEquals("\"report-query\"", objectMapper.writeValueAsString(AgentCapabilityType.REPORT_QUERY));
    }

    @Test
    public void readsStableCapabilityIdAndEnumName() throws Exception {
        assertEquals(AgentCapabilityType.REPORT_QUERY,
                objectMapper.readValue("\"report-query\"", AgentCapabilityType.class));
        assertEquals(AgentCapabilityType.REPORT_QUERY,
                objectMapper.readValue("\"REPORT_QUERY\"", AgentCapabilityType.class));
    }
}
