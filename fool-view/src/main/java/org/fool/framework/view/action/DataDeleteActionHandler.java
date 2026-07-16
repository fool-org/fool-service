package org.fool.framework.view.action;

import org.fool.framework.common.authz.ControlledActionContext;
import org.fool.framework.common.authz.ControlledActionHandler;
import org.fool.framework.common.authz.ControlledActionPreview;
import org.fool.framework.common.authz.ControlledActionResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class DataDeleteActionHandler implements ControlledActionHandler {
    private final ViewActionSupport support;

    public DataDeleteActionHandler(ViewActionSupport support) { this.support = support; }

    public String action() { return "data.delete"; }
    public String resourceType() { return "View"; }
    public void preflight(ControlledActionContext context) { support.deleteObjectId(context); }
    public String currentSnapshotVersion(ControlledActionContext context) { return support.deleteSnapshot(context); }

    public ControlledActionPreview preview(ControlledActionContext context) {
        return new ControlledActionPreview(currentSnapshotVersion(context), 1,
                Map.of("operation", "delete", "objectId", support.deleteObjectId(context)),
                List.of("object remains in authorized row scope"),
                "restore the captured business record from backup through a new approved create request",
                List.of("deletion is not automatically reversible"),
                List.of("DESTRUCTIVE_WRITE"));
    }

    @Transactional
    public ControlledActionResult execute(ControlledActionContext context) {
        support.deleteObject(context);
        return new ControlledActionResult("DATA_DELETED", Map.of("deleted", true), Map.of("count", 1));
    }
}
