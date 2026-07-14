package org.fool.framework.agent.service;

import java.util.List;

public class AgentEventAutomationMetadataSnapshot {
    private final String status;
    private final String reason;
    private final List<AgentEventDefinitionMetadata> definitions;

    public AgentEventAutomationMetadataSnapshot(String status,
                                                String reason,
                                                List<AgentEventDefinitionMetadata> definitions) {
        this.status = status;
        this.reason = reason;
        this.definitions = definitions == null ? List.of() : List.copyOf(definitions);
    }

    public static AgentEventAutomationMetadataSnapshot hydrated(List<AgentEventDefinitionMetadata> definitions) {
        return new AgentEventAutomationMetadataSnapshot("hydrated", null, definitions);
    }

    public static AgentEventAutomationMetadataSnapshot unavailable(String status, String reason) {
        return new AgentEventAutomationMetadataSnapshot(status, reason, List.of());
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public List<AgentEventDefinitionMetadata> getDefinitions() {
        return definitions;
    }

    public boolean isHydrated() {
        return "hydrated".equals(status);
    }
}
