import { describe, expect, it } from "vitest";
import appSource from "./App.vue?raw";
import metadataFieldEditorSource from "./MetadataFieldEditor.vue?raw";
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
    expect(viewReportPanelSource).toContain('v-if="visible && !reportSetupLoading && !reportRunning"');
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
    expect(viewReportPanelSource).toContain("const resultPage = computed(() => currentPage.value);");
    expect(viewReportPanelSource).not.toContain("reportGridPage");
  });

  it("keeps the old inert report-save command visible", () => {
    expect(viewReportPanelSource).toContain('label="保存报表定义" severity="info"');
    expect(viewReportPanelSource).not.toContain("saveReport");
    expect(viewReportPanelSource).not.toContain("/api/v1/report/saverpt");
    expect(viewReportPanelSource).not.toContain("报表定义已提交。");
  });

  it("keeps the old single-type shortcut but requires a real output type", () => {
    expect(reportOutputSelectorSource).toContain('@change="chooseCandidate"');
    expect(reportOutputSelectorSource).toContain("if (!queryTypeOptions.value.length) return;");
    expect(reportOutputSelectorSource).toContain("if (queryTypeOptions.value.length === 1) addOutput(candidate);");
    expect(reportOutputSelectorSource).not.toContain('{ label: "原值", value: "" }');
  });

  it("keeps output types empty until the old candidate change event", () => {
    expect(reportOutputSelectorSource).toContain('defineModel<{ label: string; value: string }[]>("queryTypeOptions"');
    expect(reportOutputSelectorSource).toContain("queryTypeOptions.value = candidate");
  });

  it("rebuilds candidates but retains output methods when report metadata reloads", () => {
    expect(reportOutputSelectorSource).toContain('defineModel<string>("candidateKey"');
    expect(reportOutputSelectorSource).not.toContain("watch(() => props.columns");
    expect(viewReportPanelSource).toContain('reportCandidateKey.value = modelColumns.value[0] ? columnKey(modelColumns.value[0]) : "";');
    expect(viewReportPanelSource).toContain('v-model:candidate-key="reportCandidateKey"');
    expect(reportOutputSelectorSource).toContain("selectedTypeName");
    expect(viewReportPanelSource).toContain('v-model:query-type-options="reportQueryTypeOptions"');
    expect(viewReportPanelSource).toContain('v-model:selected-type-id="reportSelectedTypeId"');
    expect(viewReportPanelSource).toContain('v-model:selected-output-index="reportSelectedOutputIndex"');
  });

  it("keeps the selected candidate when returning from report results", () => {
    const backToSetup = viewReportPanelSource.slice(
      viewReportPanelSource.indexOf("function backToReportSetup"),
      viewReportPanelSource.indexOf("watch(() => props.visible")
    );
    expect(backToSetup).not.toContain("reportCandidateKey");
  });

  it("reopens setup after dismissing results without resetting its page", () => {
    const dismissResult = viewReportPanelSource.slice(
      viewReportPanelSource.indexOf("function dismissReportDialog"),
      viewReportPanelSource.indexOf("watch(() => props.visible")
    );
    expect(dismissResult).toContain("showingResults.value = false;");
    expect(dismissResult).toContain('emit("close")');
    expect(dismissResult).not.toContain("currentPage.value");
    expect(viewReportPanelSource).toContain("if (!visible) dismissReportDialog()");
  });

  it("uses the changed candidate before its parent model round trip", () => {
    expect(reportOutputSelectorSource).toContain("(event.currentTarget as HTMLSelectElement).value");
    expect(reportOutputSelectorSource).toContain("addOutput(candidate)");
    expect(reportOutputSelectorSource).toContain('@click="addOutput()"');
  });

  it("reuses the View metadata field editor for report condition values", () => {
    expect(viewReportPanelSource).toContain("<MetadataFieldEditor");
    expect(viewReportPanelSource).toContain(':field="conditionEditorField(condition)"');
    expect(viewReportPanelSource).toContain('@update:formatted-value="condition.formattedValue = $event"');
    expect(metadataFieldEditorSource).toContain('emit("update:formattedValue", choice.label)');
    expect(metadataFieldEditorSource).toContain('emit("update:formattedValue", "")');
    expect(viewReportPanelSource).not.toContain('v-else-if="condition.columnId && condition.compareId"');
  });

  it("keeps the old report draft mounted across cancel and reopen", () => {
    expect(appSource).toContain('v-if="isListView && !isMetadataOnlyView && !isStandaloneDetail && currentViewId"');
    expect(appSource).toContain(':visible="showViewReport"');
    expect(viewReportPanelSource).toContain("watch(() => props.visible");
    expect(viewReportPanelSource).toContain('v-if="visible && !reportSetupLoading && !reportRunning"');
    expect(viewReportPanelSource).not.toContain("reportCols.value = [];");
  });
});
