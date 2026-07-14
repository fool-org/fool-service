package org.fool.framework.agent.service;

public interface EventAutomationMetadataProvider {
    AgentEventAutomationMetadataSnapshot load();

    static EventAutomationMetadataProvider unavailable() {
        return () -> AgentEventAutomationMetadataSnapshot.unavailable(
                "metadata-provider-unavailable",
                "JDBC event/automation metadata provider is unavailable.");
    }
}
