import { computed, ref, type Ref } from "vue";
import type { CommonResponse, ListViewInfo, ListViewResult, ReadItemViewInfo, TableColumnInfo } from "./api";
import { postApi } from "./api";
import {
  buildLegacyListViewRequest,
  buildLegacyQueryDataRequest,
  buildLegacyReadItemViewRequest
} from "./payload";
import {
  listPageIndex,
  listRenderColumns,
  listRows,
  listTotalItems,
  listTotalPages,
  readViewForId,
  rememberReadView,
  viewDetailViewId,
  viewId
} from "./viewWorkflow";

export type WorkflowActionRunner = <T>(
  label: string,
  action: () => Promise<CommonResponse<T>>
) => Promise<CommonResponse<T> | null>;

export interface ViewDataWorkflowRefs {
  token: Ref<string>;
  listViewId: Ref<number>;
  readItemViewId: Ref<number>;
  pageIndex: Ref<number>;
  pageSize: Ref<number>;
  keyword: Ref<string>;
  detailViewId: Ref<number>;
  runAction: WorkflowActionRunner;
}

export function useViewDataWorkflow(options: ViewDataWorkflowRefs) {
  const viewResponse = ref<CommonResponse<ListViewInfo> | null>(null);
  const readItemViewResponse = ref<CommonResponse<ReadItemViewInfo> | null>(null);
  const readItemViews = ref<Record<number, ReadItemViewInfo>>({});
  const dataResponse = ref<CommonResponse<ListViewResult> | null>(null);

  const currentViewId = computed(() => viewId(viewResponse.value?.data));
  const resultColumns = computed<TableColumnInfo[]>(() => listRenderColumns(viewResponse.value?.data));
  const resultRows = computed(() => listRows(dataResponse.value?.data));
  const resultPageIndex = computed(() => listPageIndex(dataResponse.value?.data, Number(options.pageIndex.value)));
  const resultTotalItems = computed(() => listTotalItems(dataResponse.value?.data));
  const resultTotalPages = computed(() => listTotalPages(dataResponse.value?.data));

  function canRenderLoadedView() {
    return listRenderColumns(viewResponse.value?.data).length > 0;
  }

  function applyLoadedView(view?: ListViewInfo) {
    const loadedViewId = viewId(view, options.listViewId.value);
    if (!loadedViewId) {
      return;
    }
    const loadedDetailViewId = viewDetailViewId(view, loadedViewId);
    options.listViewId.value = loadedViewId;
    options.readItemViewId.value = loadedDetailViewId;
    options.detailViewId.value = loadedDetailViewId;
  }

  async function loadLegacyListView() {
    const requestedViewId = Number(options.listViewId.value) || 0;
    if (requestedViewId <= 0) {
      return null;
    }
    const request = buildLegacyListViewRequest({
      token: options.token.value,
      viewId: requestedViewId
    });

    const response = await options.runAction("legacy-list-view", () =>
      postApi<ListViewInfo>("/api/v1/view/getlistview", request)
    );
    if (response) {
      viewResponse.value = response;
      dataResponse.value = null;
      applyLoadedView(response.data);
    }
    return response;
  }

  async function loadReadItemView(viewId = Number(options.readItemViewId.value)) {
    options.readItemViewId.value = viewId;
    if (viewId <= 0) {
      return null;
    }
    const request = buildLegacyReadItemViewRequest({
      token: options.token.value,
      viewId
    });

    const response = await options.runAction("read-item-view", () =>
      postApi<ReadItemViewInfo>("/api/v1/view/getreaditemview", request)
    );
    if (response) {
      readItemViewResponse.value = response;
      readItemViews.value = rememberReadView(readItemViews.value, viewId, response.data);
    }
    return response;
  }

  async function queryLoadedViewData(
    label: string,
    pageIndex: number,
    pageSize: number,
    filters: { keyword?: string } = {}
  ) {
    const loadedViewId = Number(currentViewId.value);
    if (loadedViewId <= 0 || !canRenderLoadedView()) {
      return null;
    }
    const request = buildLegacyQueryDataRequest({
      token: options.token.value,
      viewId: loadedViewId,
      pageIndex,
      pageSize,
      keyword: filters.keyword
    });

    const response = await options.runAction(label, () =>
      postApi<ListViewResult>("/api/v1/data/querydata", request)
    );
    if (response) {
      dataResponse.value = response;
    }
    return response;
  }

  async function loadViewById(requestedViewId: number, label = "view") {
    if (requestedViewId <= 0) {
      return null;
    }
    return options.runAction(`${label}-view`, () =>
      postApi<ListViewInfo>("/api/v1/view/getlistview", buildLegacyListViewRequest({
        token: options.token.value,
        viewId: requestedViewId
      }))
    );
  }

  async function loadViewDataById(requestedViewId: number, label = "view-data", pageSize = 10) {
    const viewResponse = await loadViewById(requestedViewId, label);
    if (!viewResponse) {
      return null;
    }
    const loadedViewId = viewId(viewResponse.data, requestedViewId);
    if (!loadedViewId || !listRenderColumns(viewResponse.data).length) {
      return { view: viewResponse.data, data: null };
    }
    const dataResponse = await options.runAction(`${label}-data`, () =>
      postApi<ListViewResult>("/api/v1/data/querydata", buildLegacyQueryDataRequest({
        token: options.token.value,
        viewId: loadedViewId,
        pageIndex: 1,
        pageSize
      }))
    );
    return { view: viewResponse.data, data: dataResponse?.data ?? null };
  }

  async function queryCurrentViewData() {
    const requestedViewId = Number(options.listViewId.value);
    if (!viewResponse.value?.data || viewId(viewResponse.value.data) !== requestedViewId) {
      const loadedView = await loadLegacyListView();
      if (!loadedView) {
        return null;
      }
    }

    return queryLoadedViewData(
      "workflow-query",
      Number(options.pageIndex.value),
      Number(options.pageSize.value),
      { keyword: options.keyword.value }
    );
  }

  function readItemViewFor(viewId: number) {
    return readViewForId(readItemViews.value, Number(viewId));
  }

  return {
    viewResponse,
    readItemViewResponse,
    dataResponse,
    currentViewId,
    resultColumns,
    resultRows,
    resultPageIndex,
    resultTotalItems,
    resultTotalPages,
    readItemViewFor,
    loadLegacyListView,
    loadReadItemView,
    loadViewById,
    loadViewDataById,
    queryCurrentViewData
  };
}
