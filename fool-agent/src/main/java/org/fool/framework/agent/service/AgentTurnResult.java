package org.fool.framework.agent.service;

public class AgentTurnResult {
    private final AgentSession session;
    private final AgentMessage agentMessage;
    private final AgentDraft draft;
    private final boolean readyToAdvance;
    private final String provider;
    private final String model;

    public AgentTurnResult(AgentSession session, AgentMessage agentMessage, AgentDraft draft, boolean readyToAdvance) {
        this(session, agentMessage, draft, readyToAdvance, "local", "deterministic");
    }

    public AgentTurnResult(AgentSession session,
                           AgentMessage agentMessage,
                           AgentDraft draft,
                           boolean readyToAdvance,
                           String provider,
                           String model) {
        this.session = session;
        this.agentMessage = agentMessage;
        this.draft = draft;
        this.readyToAdvance = readyToAdvance;
        this.provider = provider;
        this.model = model;
    }

    public AgentSession getSession() {
        return session;
    }

    public AgentMessage getAgentMessage() {
        return agentMessage;
    }

    public AgentDraft getDraft() {
        return draft;
    }

    public boolean isReadyToAdvance() {
        return readyToAdvance;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }
}
