package org.fool.framework.agent.action;

import org.fool.framework.common.authz.ControlledActionHandler;
import org.fool.framework.common.authz.ControlledActionContext;
import org.fool.framework.common.authz.ControlledActionPreview;
import org.fool.framework.common.authz.ControlledActionResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertThrows;

public class ActionCatalogCoverageValidatorTest {
    @Test
    public void missingRuntimeHandlerFailsClosed() {
        ControlledActionRegistry registry = new ControlledActionRegistry(List.of(new StubHandler()));

        assertThrows(IllegalStateException.class,
                () -> new ActionCatalogCoverageValidator(new ActionCatalog(), registry));
    }

    private static class StubHandler implements ControlledActionHandler {
        @Override public String action() { return "report.save"; }
        @Override public String resourceType() { return "View"; }
        @Override public void preflight(ControlledActionContext context) { }
        @Override public ControlledActionPreview preview(ControlledActionContext context) { return null; }
        @Override public String currentSnapshotVersion(ControlledActionContext context) { return "1"; }
        @Override public ControlledActionResult execute(ControlledActionContext context) { return null; }
    }
}
