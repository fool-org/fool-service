package org.fool.framework.agent.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAgentSessionStore implements AgentSessionStore {
    private final Map<String, AgentSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void save(AgentSession session) {
        sessions.put(session.getId(), session);
    }

    @Override
    public Optional<AgentSession> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}
