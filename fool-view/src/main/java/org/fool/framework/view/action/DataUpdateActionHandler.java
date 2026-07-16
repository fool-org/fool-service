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
public class DataUpdateActionHandler implements ControlledActionHandler {
    private final ViewActionSupport support;

    public DataUpdateActionHandler(ViewActionSupport support) { this.support = support; }

    public String action() { return "data.update"; }
    public String resourceType() { return "View"; }
    public void preflight(ControlledActionContext context) {
        if (support.bulkUpdate(context)) support.bulkUpdateRequests(context); else support.updateRequest(context);
    }

    public ControlledActionPreview preview(ControlledActionContext context) {
        int count = support.bulkUpdate(context) ? support.bulkUpdateRequests(context).size() : 1;
        List<String> fields = support.updateFields(context);
        return new ControlledActionPreview(currentSnapshotVersion(context), count,
                Map.of("operation", count > 1 ? "bounded-bulk-update" : "update", "fields", fields),
                List.of("object remains in authorized row scope", "authorized writable fields"),
                "restore the captured object version through a new approved update", List.of(),
                support.updateRiskFactors(context, fields));
    }

    public String currentSnapshotVersion(ControlledActionContext context) {
        return support.bulkUpdate(context) ? support.bulkUpdateSnapshot(context) : support.updateSnapshot(context);
    }

    @Transactional
    public ControlledActionResult execute(ControlledActionContext context) {
        int count;
        if (support.bulkUpdate(context)) {
            count = support.bulkUpdateRequests(context).size();
            support.saveBulkUpdate(context);
        } else {
            count = 1;
            support.saveUpdate(context);
        }
        return new ControlledActionResult("DATA_UPDATED", Map.of("updated", true), Map.of("count", count));
    }
}
