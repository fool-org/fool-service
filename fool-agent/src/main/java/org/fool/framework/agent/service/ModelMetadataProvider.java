package org.fool.framework.agent.service;

public interface ModelMetadataProvider {
    AgentModelMetadataSnapshot load(Long modelId, String modelName);

    static ModelMetadataProvider unavailable() {
        return (modelId, modelName) -> AgentModelMetadataSnapshot.unavailable(
                modelId,
                modelName,
                "metadata-provider-unavailable",
                "JDBC model metadata provider is unavailable.");
    }
}
