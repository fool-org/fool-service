package org.fool.framework.agent.service;

import java.time.Instant;

public class AgentMessage {
    private final String id;
    private final AgentMessageRole role;
    private final AgentCapabilityType capability;
    private final String content;
    private final Instant createdAt;

    public AgentMessage(String id, AgentMessageRole role, AgentCapabilityType capability, String content, Instant createdAt) {
        this.id = id;
        this.role = role;
        this.capability = capability;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public AgentMessageRole getRole() {
        return role;
    }

    public AgentCapabilityType getCapability() {
        return capability;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
