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
    expect(viewReportPanelSource).toContain("transportFailed = isTransportError(error)");
    expect(viewReportPanelSource).toContain("{ silentTransport: true }");
    expect(viewReportPanelSource).toMatch(/if \(transportFailed\) \{\s+emit\("close"\);\s+return;/);
  });
});
