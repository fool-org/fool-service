package org.fool.framework.agent.service;

import java.util.List;
import java.util.Map;

public class AgentDraft {
    private final AgentCapabilityType capability;
    private final String summary;
    private final String ownerModules;
    private final String riskLevel;
    private final Map<String, Object> draftPayload;
    private final List<String> validationSteps;

    public AgentDraft(AgentCapabilityType capability,
                      String summary,
                      String ownerModules,
                      String riskLevel,
                      Map<String, Object> draftPayload,
                      List<String> validationSteps) {
        this.capability = capability;
        this.summary = summary;
        this.ownerModules = ownerModules;
        this.riskLevel = riskLevel;
        this.draftPayload = draftPayload;
        this.validationSteps = validationSteps;
    }

    public AgentCapabilityType getCapability() {
        return capability;
    }

    public String getSummary() {
        return summary;
    }

    public String getOwnerModules() {
        return ownerModules;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public Map<String, Object> getDraftPayload() {
        return draftPayload;
    }

    public List<String> getValidationSteps() {
        return validationSteps;
    }
}
