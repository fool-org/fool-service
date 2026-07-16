package org.fool.framework.common.authz;

import java.util.function.Supplier;

/** Binds a domain write to the immutable Action Request currently executing. */
public class ControlledActionExecutionGuard {
    private final ThreadLocal<Execution> current = new ThreadLocal<>();

    public <T> T execute(String actionRequestId,
                         String action,
                         String resourceId,
                         Supplier<T> operation) {
        Execution previous = current.get();
        current.set(new Execution(actionRequestId, action, resourceId));
        try {
            return operation.get();
        } finally {
            if (previous == null) {
                current.remove();
            } else {
                current.set(previous);
            }
        }
    }

    public void require(String action, String resourceId) {
        Execution execution = current.get();
        if (execution == null
                || !execution.action().equals(action)
                || !execution.resourceId().equals(resourceId)) {
            throw new ControlledActionException(403, "ACTION_WORKFLOW_REQUIRED");
        }
    }

    private record Execution(String actionRequestId, String action, String resourceId) {
        private Execution {
            if (actionRequestId == null || actionRequestId.isBlank()
                    || action == null || action.isBlank()
                    || resourceId == null || resourceId.isBlank()) {
                throw new ControlledActionException(403, "ACTION_WORKFLOW_REQUIRED");
            }
        }
    }
}
