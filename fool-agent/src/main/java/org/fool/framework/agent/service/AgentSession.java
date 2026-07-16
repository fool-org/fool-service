package org.fool.framework.agent.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AgentSession {
    private final String id;
    private final String ownerUserId;
    private final String appId;
    private final String databaseId;
    private final String authSessionId;
    private final String title;
    private AgentCapabilityType currentCapability;
    private AgentSessionStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<AgentMessage> messages = new ArrayList<>();

    public AgentSession(String id,
                        String ownerUserId,
                        String appId,
                        String databaseId,
                        String authSessionId,
                        String title,
                        AgentCapabilityType currentCapability,
                        Instant createdAt) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.appId = appId;
        this.databaseId = databaseId;
        this.authSessionId = authSessionId;
        this.title = title;
        this.currentCapability = currentCapability;
        this.status = AgentSessionStatus.ACTIVE;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getAppId() {
        return appId;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public String getAuthSessionId() {
        return authSessionId;
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
