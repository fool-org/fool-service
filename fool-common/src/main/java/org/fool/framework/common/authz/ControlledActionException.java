package org.fool.framework.common.authz;

public class ControlledActionException extends RuntimeException {
    private final int status;

    public ControlledActionException(int status, String reasonCode) {
        super(reasonCode == null || reasonCode.isBlank() ? "ACTION_REQUEST_REJECTED" : reasonCode);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
