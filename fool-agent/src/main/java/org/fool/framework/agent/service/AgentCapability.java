package org.fool.framework.agent.service;

public class AgentCapability {
    private final String id;
    private final int order;
    private final String displayName;
    private final String ownerModules;
    private final String intent;

    public AgentCapability(AgentCapabilityType type) {
        this.id = type.getId();
        this.order = type.getOrder();
        this.displayName = type.getDisplayName();
        this.ownerModules = type.getOwnerModules();
        this.intent = type.getIntent();
    }

    public String getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getOwnerModules() {
        return ownerModules;
    }

    public String getIntent() {
        return intent;
    }
}
