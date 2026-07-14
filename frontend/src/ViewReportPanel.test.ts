import { describe, expect, it } from "vitest";
import appSource from "./App.vue?raw";
import reportOutputSelectorSource from "./ReportOutputSelector.vue?raw";
import viewReportPanelSource from "./ViewReportPanel.vue?raw";

describe("ViewReportPanel legacy interactions", () => {
  it("keeps setup and result commands active during requests", () => {
    expect(viewReportPanelSource).not.toContain("pending");
    expect(reportOutputSelectorSource).not.toContain("disabled");
    expect(appSource).not.toContain(':pending="Boolean(pendingAction)"\n          :run-action');
    expect(viewReportPanelSource).toContain(':closable="!showingResults"');
    expect(viewReportPanelSource).toContain("dismissable-mask");
    expect(viewReportPanelSource).toContain("const revealResults = !showingResults.value");
    expect(viewReportPanelSource).toContain("reportRunning.value = revealResults");
    expect(viewReportPanelSource).toContain('v-if="!reportSetupLoading && !reportRunning"');
    expect(viewReportPanelSource).toContain("if (revealResults) showingResults.value = true");
    expect(viewReportPanelSource).toContain('label="取消" severity="secondary" outlined @click="emit(\'close\')"');
    expect(viewReportPanelSource).toContain('label="返回" @click="backToReportSetup"');
  });

  it("keeps the report closed when its metadata request has a transport failure", () => {
    expect(viewReportPanelSource).toContain("async function runSuccessOnlyAction<T>");
    expect(viewReportPanelSource).toContain("transportFailed = isTransportError(error)");
    expect(viewReportPanelSource).toContain("{ silentTransport: true }");
    expect(viewReportPanelSource).toContain('if (transportFailed) return emit("close")');
  });

  it("keeps initial and paging report transport failures on their legacy surfaces", () => {
    expect(viewReportPanelSource).toContain('runSuccessOnlyAction("mkrpt"');
    expect(viewReportPanelSource).toMatch(/if \(transportFailed\) \{\s+if \(revealResults\) emit\("close"\);\s+return;/);
  });

  it("keeps the old inert report-save command visible", () => {
    expect(viewReportPanelSource).toContain('label="保存报表定义" severity="info"');
    expect(viewReportPanelSource).not.toContain("saveReport");
    expect(viewReportPanelSource).not.toContain("/api/v1/report/saverpt");
    expect(viewReportPanelSource).not.toContain("报表定义已提交。");
  });
});
