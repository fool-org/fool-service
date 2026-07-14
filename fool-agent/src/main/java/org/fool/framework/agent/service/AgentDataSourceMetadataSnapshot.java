package org.fool.framework.agent.service;

import java.util.List;

public class AgentDataSourceMetadataSnapshot {
    private final String status;
    private final String reason;
    private final List<AgentWorkingDatabaseMetadata> workingDatabases;
    private final List<AgentApplicationRouteMetadata> applicationRoutes;
    private final List<AgentDataSourceRouteMetadata> dataSourceRoutes;

    public AgentDataSourceMetadataSnapshot(String status,
                                           String reason,
                                           List<AgentWorkingDatabaseMetadata> workingDatabases,
                                           List<AgentApplicationRouteMetadata> applicationRoutes,
                                           List<AgentDataSourceRouteMetadata> dataSourceRoutes) {
        this.status = status;
        this.reason = reason;
        this.workingDatabases = workingDatabases == null ? List.of() : List.copyOf(workingDatabases);
        this.applicationRoutes = applicationRoutes == null ? List.of() : List.copyOf(applicationRoutes);
        this.dataSourceRoutes = dataSourceRoutes == null ? List.of() : List.copyOf(dataSourceRoutes);
    }

    public static AgentDataSourceMetadataSnapshot hydrated(
            List<AgentWorkingDatabaseMetadata> workingDatabases,
            List<AgentApplicationRouteMetadata> applicationRoutes,
            List<AgentDataSourceRouteMetadata> dataSourceRoutes) {
        return new AgentDataSourceMetadataSnapshot(
                "hydrated",
                null,
                workingDatabases,
                applicationRoutes,
                dataSourceRoutes);
    }

    public static AgentDataSourceMetadataSnapshot unavailable(String status, String reason) {
        return new AgentDataSourceMetadataSnapshot(status, reason, List.of(), List.of(), List.of());
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public List<AgentWorkingDatabaseMetadata> getWorkingDatabases() {
        return workingDatabases;
    }

    public List<AgentApplicationRouteMetadata> getApplicationRoutes() {
        return applicationRoutes;
    }

    public List<AgentDataSourceRouteMetadata> getDataSourceRoutes() {
        return dataSourceRoutes;
    }

    public boolean isHydrated() {
        return "hydrated".equals(status);
    }
}
