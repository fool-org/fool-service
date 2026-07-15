import { ref, type Ref } from "vue";
import type { CommonResponse, ListViewInfo, ListViewResult, QueryDataDetailResult, TableColumnInfo } from "./api";
import { postApi } from "./api";
import { buildQueryDataDetailRequest } from "./payload";
import type { WorkflowActionOptions, WorkflowActionRunner } from "./useViewDataWorkflow";
import {
  appendLegacyChartSample,
  listAutoFreshTime,
  sudokuPanelKind,
  sudokuPanelListViewType,
  sudokuPanelViewId,
  viewColumns,
  viewId
} from "./viewWorkflow";
import type { LegacyChartData } from "./viewWorkflow";

export type SudokuPanelResult = {
  view: ListViewInfo;
  data: ListViewResult | null;
  detail?: QueryDataDetailResult | null;
  chart?: LegacyChartData;
};

export function sudokuPanelStateKey(panel: TableColumnInfo) {
  return [
    sudokuPanelViewId(panel),
    panel.id ?? panel.ID ?? panel.propertyName ?? panel.PropertyName ?? "panel",
    sudokuPanelKind(panel)
  ].join(":");
}

const silentTransport: WorkflowActionOptions = { silentTransport: true };

interface SudokuPanelWorkflowOptions {
  enabled: Readonly<Ref<boolean>>;
  loadViewById: (
    viewId: number,
    label?: string,
    actionOptions?: WorkflowActionOptions
  ) => Promise<CommonResponse<ListViewInfo> | null>;
  loadViewDataById: (
    viewId: number,
    label?: string,
    pageSize?: number,
    actionOptions?: WorkflowActionOptions
  ) => Promise<SudokuPanelResult | null>;
  panels: Readonly<Ref<TableColumnInfo[]>>;
  runAction: WorkflowActionRunner;
  token: Ref<string>;
}

export function useSudokuPanels(options: SudokuPanelWorkflowOptions) {
  const panelData = ref<Record<number, SudokuPanelResult>>({});
  const panelUpdating = ref<Record<string, boolean>>({});
  const refreshTimers = new Map<string, number>();

  async function loadPanels() {
    stopRefresh();
    panelData.value = {};
    panelUpdating.value = {};
    if (!options.enabled.value) return;
    for (const panel of options.panels.value) await refreshPanel(panel);
  }

  async function refreshPanel(panel: TableColumnInfo) {
    const panelViewId = sudokuPanelViewId(panel);
    if (!panelViewId) return;
    const response = await loadPanel(panel);
    if (!response) return;
    const next = sudokuPanelKind(panel) === "linechart" && response.detail
      ? { ...response, chart: appendLegacyChartSample(panelData.value[panelViewId]?.chart, response.detail) }
      : response;
    mergePanelResult(panelViewId, next);
    scheduleRefresh(panel, next);
    if (sudokuPanelKind(panel) !== "group") return;
    for (const childPanel of viewColumns(response.view)) {
      const childViewId = sudokuPanelViewId(childPanel);
      if (!childViewId || sudokuPanelListViewType(childPanel) !== 0) continue;
      const childResponse = await loadListPanel(childPanel);
      if (!childResponse) continue;
      mergePanelResult(childViewId, childResponse);
      scheduleRefresh(childPanel, childResponse);
    }
  }

  async function loadPanel(panel: TableColumnInfo) {
    const panelViewId = sudokuPanelViewId(panel);
    const kind = sudokuPanelKind(panel);
    if (kind === "item" || kind === "linechart") return loadDetailPanel(panelViewId, `sudoku-${kind}`);
    if (kind === "group") {
      const response = await options.loadViewById(panelViewId, "sudoku-group", silentTransport);
      return response ? { view: response.data, data: null } : null;
    }
    if (kind === "list") return loadListPanel(panel);
    return options.loadViewDataById(panelViewId, "sudoku-panel", 5, silentTransport);
  }

  async function loadListPanel(panel: TableColumnInfo) {
    const panelViewId = sudokuPanelViewId(panel);
    const panelKey = sudokuPanelStateKey(panel);
    panelUpdating.value = { ...panelUpdating.value, [panelKey]: true };
    const response = await options.loadViewDataById(panelViewId, "sudoku-panel", 5, silentTransport);
    if (response?.data) panelUpdating.value = { ...panelUpdating.value, [panelKey]: false };
    return response;
  }

  async function loadDetailPanel(panelViewId: number, label: string) {
    const panelViewResponse = await options.loadViewById(panelViewId, label, silentTransport);
    if (!panelViewResponse) return null;
    const loadedViewId = viewId(panelViewResponse.data, panelViewId);
    if (!loadedViewId) return { view: panelViewResponse.data, data: null, detail: null };
    const detailResponse = await options.runAction(
      `${label}-detail`,
      () => postApi<QueryDataDetailResult>("/api/v1/data/querydatadetail", buildQueryDataDetailRequest({
        token: options.token.value,
        viewId: loadedViewId,
        objId: ""
      })),
      silentTransport
    );
    return { view: panelViewResponse.data, data: null, detail: detailResponse?.data ?? null };
  }

  function mergePanelResult(panelViewId: number, next: SudokuPanelResult) {
    const current = panelData.value[panelViewId];
    panelData.value = {
      ...panelData.value,
      [panelViewId]: {
        ...next,
        data: next.data ?? current?.data ?? null,
        detail: next.detail ?? current?.detail,
        chart: next.chart ?? current?.chart
      }
    };
  }

  function scheduleRefresh(panel: TableColumnInfo, result: SudokuPanelResult) {
    const key = refreshKey(panel);
    const activeTimer = refreshTimers.get(key);
    if (activeTimer !== undefined) window.clearInterval(activeTimer);
    const kind = sudokuPanelKind(panel);
    if (kind === "map") {
      refreshTimers.delete(key);
      return;
    }
    const refreshSource = kind === "linechart" ? result.detail : result.data;
    const seconds = listAutoFreshTime(refreshSource || undefined);
    if (seconds <= 0) {
      refreshTimers.delete(key);
      return;
    }
    refreshTimers.set(key, window.setInterval(() => {
      void refreshPanel(panel);
    }, seconds * 1000));
  }

  function refreshKey(panel: TableColumnInfo) {
    return sudokuPanelStateKey(panel);
  }

  function stopRefresh() {
    for (const timer of refreshTimers.values()) window.clearInterval(timer);
    refreshTimers.clear();
  }

  return { panelData, panelUpdating, loadPanels, refreshPanel, stopRefresh };
}
