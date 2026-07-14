package org.fool.framework.agent.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AgentSession {
    private final String id;
    private final String token;
    private final String title;
    private AgentCapabilityType currentCapability;
    private AgentSessionStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<AgentMessage> messages = new ArrayList<>();

    public AgentSession(String id, String token, String title, AgentCapabilityType currentCapability, Instant createdAt) {
        this.id = id;
        this.token = token;
        this.title = title;
        this.currentCapability = currentCapability;
        this.status = AgentSessionStatus.ACTIVE;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getTitle() {
        return title;
    }

    public AgentCapabilityType getCurrentCapability() {
        return currentCapability;
    }

    public void setCurrentCapability(AgentCapabilityType currentCapability) {
        this.currentCapability = currentCapability;
    }

    public AgentSessionStatus getStatus() {
        return status;
    }

    public void setStatus(AgentSessionStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<AgentMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void addMessage(AgentMessage message) {
        messages.add(message);
        updatedAt = message.getCreatedAt();
    }
}
