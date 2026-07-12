import { ref, type Ref } from "vue";
import type { CommonResponse, ListViewInfo, ListViewResult, QueryDataDetailResult, TableColumnInfo } from "./api";
import { postApi } from "./api";
import { buildQueryDataDetailRequest } from "./payload";
import type { WorkflowActionRunner } from "./useViewDataWorkflow";
import {
  listAutoFreshTime,
  sudokuPanelKind,
  sudokuPanelListViewType,
  sudokuPanelViewId,
  viewColumns,
  viewId
} from "./viewWorkflow";

export type SudokuPanelResult = {
  view: ListViewInfo;
  data: ListViewResult | null;
  detail?: QueryDataDetailResult | null;
};

interface SudokuPanelWorkflowOptions {
  enabled: Readonly<Ref<boolean>>;
  loadViewById: (viewId: number, label?: string) => Promise<CommonResponse<ListViewInfo> | null>;
  loadViewDataById: (viewId: number, label?: string, pageSize?: number) => Promise<SudokuPanelResult | null>;
  panels: Readonly<Ref<TableColumnInfo[]>>;
  pendingAction: Ref<string>;
  runAction: WorkflowActionRunner;
  token: Ref<string>;
}

export function useSudokuPanels(options: SudokuPanelWorkflowOptions) {
  const panelData = ref<Record<number, SudokuPanelResult>>({});
  const refreshTimers = new Map<string, number>();

  async function loadPanels() {
    stopRefresh();
    panelData.value = {};
    if (!options.enabled.value) return;
    for (const panel of options.panels.value) await refreshPanel(panel);
  }

  async function refreshPanel(panel: TableColumnInfo) {
    const panelViewId = sudokuPanelViewId(panel);
    if (!panelViewId) return;
    const response = await loadPanel(panel);
    if (!response) return;
    mergePanelResult(panelViewId, response);
    scheduleRefresh(panel, response);
    if (sudokuPanelKind(panel) !== "group") return;
    for (const childPanel of viewColumns(response.view)) {
      const childViewId = sudokuPanelViewId(childPanel);
      if (!childViewId || sudokuPanelListViewType(childPanel) !== 0) continue;
      const childResponse = await options.loadViewDataById(childViewId, "sudoku-panel", 5);
      if (!childResponse) continue;
      mergePanelResult(childViewId, childResponse);
      scheduleRefresh(childPanel, childResponse);
    }
  }

  async function loadPanel(panel: TableColumnInfo) {
    const panelViewId = sudokuPanelViewId(panel);
    if (sudokuPanelKind(panel) !== "item") {
      return options.loadViewDataById(panelViewId, "sudoku-panel", 5);
    }
    const panelViewResponse = await options.loadViewById(panelViewId, "sudoku-item");
    if (!panelViewResponse) return null;
    const loadedViewId = viewId(panelViewResponse.data, panelViewId);
    if (!loadedViewId) return { view: panelViewResponse.data, data: null, detail: null };
    const detailResponse = await options.runAction("sudoku-item-detail", () =>
      postApi<QueryDataDetailResult>("/api/v1/data/querydatadetail", buildQueryDataDetailRequest({
        token: options.token.value,
        viewId: loadedViewId,
        objId: ""
      }))
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
        detail: next.detail ?? current?.detail
      }
    };
  }

  function scheduleRefresh(panel: TableColumnInfo, result: SudokuPanelResult) {
    const key = refreshKey(panel);
    const activeTimer = refreshTimers.get(key);
    if (activeTimer !== undefined) window.clearInterval(activeTimer);
    const seconds = listAutoFreshTime(result.data || undefined);
    if (seconds <= 0) {
      refreshTimers.delete(key);
      return;
    }
    refreshTimers.set(key, window.setInterval(() => {
      if (!options.pendingAction.value) void refreshPanel(panel);
    }, seconds * 1000));
  }

  function refreshKey(panel: TableColumnInfo) {
    return [
      sudokuPanelViewId(panel),
      panel.id ?? panel.ID ?? panel.propertyName ?? panel.PropertyName ?? "panel",
      sudokuPanelKind(panel)
    ].join(":");
  }

  function stopRefresh() {
    for (const timer of refreshTimers.values()) window.clearInterval(timer);
    refreshTimers.clear();
  }

  return { panelData, loadPanels, refreshPanel, stopRefresh };
}
