package org.fool.framework.view.action;

import org.fool.framework.common.authz.ControlledActionContext;
import org.fool.framework.common.authz.ControlledActionHandler;
import org.fool.framework.common.authz.ControlledActionPreview;
import org.fool.framework.common.authz.ControlledActionResult;
import org.fool.framework.report.ReportGridResult;
import org.fool.framework.view.dto.MakeReportRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ReportExportActionHandler implements ControlledActionHandler {
    private final ViewActionSupport support;

    public ReportExportActionHandler(ViewActionSupport support) { this.support = support; }

    public String action() { return "report.export"; }
    public String resourceType() { return "View"; }
    public void preflight(ControlledActionContext context) { support.exportRequest(context); }

    public ControlledActionPreview preview(ControlledActionContext context) {
        ViewActionSupport.ExportSnapshot snapshot = support.exportSnapshot(context);
        int count = snapshot.report() instanceof ReportGridResult report
                ? (int) Math.min(report.getTotalRecords(), report.getPageSize()) : 0;
        MakeReportRequest request = support.exportRequest(context);
        return new ControlledActionPreview(snapshot.version(), count,
                Map.of("format", "json", "bounded", true),
                List.of("row and field policy reapplied at execution"),
                "no business mutation; discard generated export", List.of(),
                support.sensitiveFieldRisk(context, request.getReportCols().stream()
                        .map(MakeReportRequest.ReportCol::getColId).toList()));
    }

    public String currentSnapshotVersion(ControlledActionContext context) {
        return support.exportSnapshot(context).version();
    }

    public ControlledActionResult execute(ControlledActionContext context) {
        ViewActionSupport.ExportSnapshot snapshot = support.exportSnapshot(context);
        support.requireApprovedSnapshot(context, snapshot.version());
        Object report = snapshot.report();
        return new ControlledActionResult("REPORT_EXPORTED",
                Map.of("format", "json", "report", report), Map.of("bounded", true));
    }
}
