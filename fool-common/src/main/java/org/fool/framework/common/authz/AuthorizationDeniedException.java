package org.fool.framework.common.authz;

public class AuthorizationDeniedException extends RuntimeException {
    private final String reasonCode;

    public AuthorizationDeniedException(String reasonCode) {
        super(reasonCode == null || reasonCode.isBlank() ? "AUTHORIZATION_DENIED" : reasonCode);
        this.reasonCode = getMessage();
    }

    public String reasonCode() {
        return reasonCode;
    }
}
