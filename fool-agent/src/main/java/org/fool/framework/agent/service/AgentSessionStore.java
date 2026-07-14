package org.fool.framework.agent.service;

import java.util.Optional;

public interface AgentSessionStore {
    void save(AgentSession session);

    Optional<AgentSession> findById(String sessionId);
}
