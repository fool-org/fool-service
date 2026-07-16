package org.fool.framework.common.authz;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ControlledActionExecutionGuardTest {
    @Test
    public void acceptsOnlyTheBoundActionAndResourceDuringExecution() {
        ControlledActionExecutionGuard guard = new ControlledActionExecutionGuard();

        assertEquals("ACTION_WORKFLOW_REQUIRED", assertThrows(ControlledActionException.class,
                () -> guard.require("data.update", "100")).getMessage());

        String result = guard.execute("request-1", "data.update", "100", () -> {
            guard.require("data.update", "100");
            assertEquals("ACTION_WORKFLOW_REQUIRED", assertThrows(ControlledActionException.class,
                    () -> guard.require("data.update", "101")).getMessage());
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals("ACTION_WORKFLOW_REQUIRED", assertThrows(ControlledActionException.class,
                () -> guard.require("data.update", "100")).getMessage());
    }
}
