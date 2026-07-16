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
public class ReportSaveActionHandler implements ControlledActionHandler {
    private final ViewActionSupport support;

    public ReportSaveActionHandler(ViewActionSupport support) { this.support = support; }

    public String action() { return "report.save"; }
    public String resourceType() { return "View"; }
    public void preflight(ControlledActionContext context) { support.reportRequest(context, true); }

    public ControlledActionPreview preview(ControlledActionContext context) {
        return new ControlledActionPreview(currentSnapshotVersion(context), 1,
                Map.of("definition", support.reportDefinitionSummary(context)),
                List.of("report fields remain readable"),
                "restore the previous saved definition version", List.of());
    }

    public String currentSnapshotVersion(ControlledActionContext context) {
        return support.savedReportSnapshot(context);
    }

    @Transactional
    public ControlledActionResult execute(ControlledActionContext context) {
        support.saveReport(context);
        return new ControlledActionResult("REPORT_SAVED", Map.of("saved", true), Map.of("count", 1));
    }
}
