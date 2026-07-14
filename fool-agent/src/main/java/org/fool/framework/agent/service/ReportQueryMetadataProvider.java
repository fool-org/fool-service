package org.fool.framework.agent.service;

public interface ReportQueryMetadataProvider {
    ReportQueryMetadataSnapshot load(Long viewId);

    static ReportQueryMetadataProvider unavailable() {
        return viewId -> ReportQueryMetadataSnapshot.unavailable(
                viewId,
                "metadata-provider-unavailable",
                "JDBC metadata provider is unavailable.");
    }
}
