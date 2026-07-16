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
public class DataCreateActionHandler implements ControlledActionHandler {
    private final ViewActionSupport support;

    public DataCreateActionHandler(ViewActionSupport support) {
        this.support = support;
    }

    public String action() { return "data.create"; }
    public String resourceType() { return "View"; }

    public void preflight(ControlledActionContext context) { support.createRequest(context); }

    public ControlledActionPreview preview(ControlledActionContext context) {
        List<String> fields = support.createFields(context);
        return new ControlledActionPreview(currentSnapshotVersion(context), 1,
                Map.of("operation", "create", "fields", fields),
                List.of("single object", "authorized writable fields"),
                "delete the newly created object through the approved delete workflow", List.of(),
                support.sensitiveFieldRisk(context, fields));
    }

    public String currentSnapshotVersion(ControlledActionContext context) {
        return support.createSnapshot(context);
    }

    @Transactional
    public ControlledActionResult execute(ControlledActionContext context) {
        support.saveCreate(context);
        return new ControlledActionResult("DATA_CREATED", Map.of("created", true), Map.of("count", 1));
    }
}
