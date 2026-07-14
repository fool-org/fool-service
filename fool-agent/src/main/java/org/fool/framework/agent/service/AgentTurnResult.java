package org.fool.framework.agent.service;

public class AgentTurnResult {
    private final AgentSession session;
    private final AgentMessage agentMessage;
    private final AgentDraft draft;
    private final boolean readyToAdvance;

    public AgentTurnResult(AgentSession session, AgentMessage agentMessage, AgentDraft draft, boolean readyToAdvance) {
        this.session = session;
        this.agentMessage = agentMessage;
        this.draft = draft;
        this.readyToAdvance = readyToAdvance;
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
}
