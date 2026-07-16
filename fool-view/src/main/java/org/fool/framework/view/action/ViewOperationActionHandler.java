package org.fool.framework.view.action;

import org.fool.framework.common.authz.ControlledActionContext;
import org.fool.framework.common.authz.ControlledActionHandler;
import org.fool.framework.common.authz.ControlledActionPreview;
import org.fool.framework.common.authz.ControlledActionResult;
import org.fool.framework.view.dto.LegacyRunOperationRequest;
import org.fool.framework.view.dto.LegacyRunOperationResult;
import org.fool.framework.model.model.OperationBaseType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class ViewOperationActionHandler implements ControlledActionHandler {
    private final ViewActionSupport support;

    public ViewOperationActionHandler(ViewActionSupport support) { this.support = support; }

    public String action() { return "operation.execute"; }
    public String resourceType() { return "Operation"; }
    public void preflight(ControlledActionContext context) { support.operationRequest(context); }
    public String currentSnapshotVersion(ControlledActionContext context) { return support.operationSnapshot(context); }

    public ControlledActionPreview preview(ControlledActionContext context) {
        LegacyRunOperationRequest request = support.operationRequest(context);
        OperationBaseType type = support.operationType(context);
        return new ControlledActionPreview(currentSnapshotVersion(context), 1,
                Map.of("operationId", request.getOperationId(), "viewId", request.getViewId(),
                        "objectId", request.getObjectId(), "operationType", type.name()),
                List.of("operation belongs to the selected View", "object remains readable in row scope"),
                "use the operation-specific business recovery path or restore the captured object version",
                List.of("only fixed CREATE, UPDATE, or DELETE metadata operations are allowed"),
                type == OperationBaseType.DELETE ? List.of("DESTRUCTIVE_WRITE") : List.of());
    }

    @Transactional
    public ControlledActionResult execute(ControlledActionContext context) {
        LegacyRunOperationResult result = support.runOperation(context);
        return new ControlledActionResult("OPERATION_EXECUTED",
                Map.of("success", result.isSuccess(), "returnObjectId", result.getReturnObjId() == null ? "" : result.getReturnObjId()),
                Map.of("count", 1));
    }
}
