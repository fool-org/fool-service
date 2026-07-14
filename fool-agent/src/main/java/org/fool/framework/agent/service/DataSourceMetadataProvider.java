package org.fool.framework.agent.service;

public interface DataSourceMetadataProvider {
    AgentDataSourceMetadataSnapshot load();

    static DataSourceMetadataProvider unavailable() {
        return () -> AgentDataSourceMetadataSnapshot.unavailable(
                "metadata-provider-unavailable",
                "JDBC data-source metadata provider is unavailable.");
    }
}
