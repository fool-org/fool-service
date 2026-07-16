package org.fool.framework.agent.action;

public enum ActionRequestStatus {
    DRAFT,
    PREFLIGHT_DENIED,
    PREVIEW_READY,
    AWAITING_CONFIRMATION,
    AWAITING_APPROVAL,
    APPROVED,
    EXECUTING,
    SUCCEEDED,
    FAILED,
    ROLLED_BACK,
    PARTIALLY_SUCCEEDED,
    CANCELLED,
    EXPIRED;

    public boolean terminal() {
        return this == PREFLIGHT_DENIED || this == SUCCEEDED || this == FAILED
                || this == ROLLED_BACK || this == PARTIALLY_SUCCEEDED
                || this == CANCELLED || this == EXPIRED;
    }
}
