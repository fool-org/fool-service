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
    expect(viewReportPanelSource).toContain('v-if="!reportSetupLoading && (!reportRunning || showingResults)"');
    expect(viewReportPanelSource).toContain("const revealResults = !showingResults.value");
    expect(viewReportPanelSource).toContain("if (revealResults) showingResults.value = true");
    expect(viewReportPanelSource).toContain('label="取消" severity="secondary" outlined @click="emit(\'close\')"');
    expect(viewReportPanelSource).toContain('label="返回" @click="backToReportSetup"');
  });
});
