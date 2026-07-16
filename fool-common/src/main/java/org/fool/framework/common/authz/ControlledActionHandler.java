package org.fool.framework.common.authz;

public interface ControlledActionHandler {
    String action();

    String resourceType();

    void preflight(ControlledActionContext context);

    ControlledActionPreview preview(ControlledActionContext context);

    String currentSnapshotVersion(ControlledActionContext context);

    ControlledActionResult execute(ControlledActionContext context);

    default boolean supports(String candidateAction, String candidateResourceType) {
        return action().equals(candidateAction) && resourceType().equalsIgnoreCase(candidateResourceType);
    }
}
