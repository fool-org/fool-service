import { computed, ref, type Ref } from "vue";
import type { CommonResponse, ListViewInfo, ListViewResult, ReadItemViewInfo, TableColumnInfo } from "./api";
import { postApi } from "./api";
import {
  buildLegacyListViewRequest,
  buildLegacyQueryDataRequest,
  buildLegacyReadItemViewRequest
} from "./payload";
import {
  listFreshTime,
  listPageIndex,
  listRenderColumns,
  listRows,
  listTotalItems,
  listTotalPages,
  readViewForId,
  rememberReadView,
  viewDetailViewId,
  viewDisplayName,
  viewDisplayTitle,
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
  queryViewId: Ref<number>;
  queryPageIndex: Ref<number>;
  queryPageSize: Ref<number>;
  pageIndex: Ref<number>;
  pageSize: Ref<number>;
  queryFilter: Ref<string>;
  reportViewId: Ref<number>;
  detailViewId: Ref<number>;
  initNewViewId: Ref<number>;
  operationViewId: Ref<number>;
  saveViewId: Ref<string>;
  saveNewViewId: Ref<string>;
  runAction: WorkflowActionRunner;
}

export function useViewDataWorkflow(options: ViewDataWorkflowRefs) {
  const viewResponse = ref<CommonResponse<ListViewInfo> | null>(null);
  const readItemViewResponse = ref<CommonResponse<ReadItemViewInfo> | null>(null);
  const readItemViews = ref<Record<number, ReadItemViewInfo>>({});
  const dataResponse = ref<CommonResponse<ListViewResult> | null>(null);

  const currentViewId = computed(() => viewId(viewResponse.value?.data));
  const loadedViewName = computed(() => viewDisplayName(viewResponse.value?.data));
  const viewTitle = computed(() => viewDisplayTitle(viewResponse.value?.data, "Load a View"));
  const resultColumns = computed<TableColumnInfo[]>(() => listRenderColumns(viewResponse.value?.data));
  const resultRows = computed(() => listRows(dataResponse.value?.data));
  const resultPageIndex = computed(() => listPageIndex(dataResponse.value?.data, Number(options.pageIndex.value)));
  const resultTotalItems = computed(() => listTotalItems(dataResponse.value?.data));
  const resultTotalPages = computed(() => listTotalPages(dataResponse.value?.data));
  const resultFreshTime = computed(() => listFreshTime(dataResponse.value?.data));

  function applyLoadedView(view?: ListViewInfo) {
    const loadedViewId = viewId(view, options.listViewId.value);
    if (!loadedViewId) {
      return;
    }
    const loadedDetailViewId = viewDetailViewId(view, loadedViewId);
    options.listViewId.value = loadedViewId;
    options.readItemViewId.value = loadedDetailViewId;
    options.queryViewId.value = loadedViewId;
    options.reportViewId.value = loadedViewId;
    options.detailViewId.value = loadedDetailViewId;
    options.initNewViewId.value = loadedDetailViewId;
    options.operationViewId.value = loadedDetailViewId;
    options.saveViewId.value = String(loadedDetailViewId);
    options.saveNewViewId.value = String(loadedDetailViewId);
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

  async function queryLoadedViewData(label: string, pageIndex: number, pageSize: number) {
    const loadedViewId = Number(currentViewId.value);
    if (loadedViewId <= 0) {
      return null;
    }
    const request = buildLegacyQueryDataRequest({
      token: options.token.value,
      viewId: loadedViewId,
      pageIndex,
      pageSize,
      queryFilter: options.queryFilter.value
    });

    const response = await options.runAction(label, () =>
      postApi<ListViewResult>("/api/v1/data/querydata", request)
    );
    if (response) {
      dataResponse.value = response;
    }
    return response;
  }

  async function queryLegacyData() {
    options.listViewId.value = Number(options.queryViewId.value);
    if (!(await loadLegacyListView())) {
      return null;
    }
    return queryLoadedViewData(
      "legacy-query",
      Number(options.queryPageIndex.value),
      Number(options.queryPageSize.value)
    );
  }

  async function queryCurrentViewData() {
    const requestedViewId = Number(options.listViewId.value);
    if (!viewResponse.value?.data || viewId(viewResponse.value.data) !== requestedViewId) {
      const loadedView = await loadLegacyListView();
      if (!loadedView) {
        return null;
      }
    }

    const loadedViewId = Number(currentViewId.value);
    options.queryViewId.value = loadedViewId;
    options.queryPageIndex.value = Number(options.pageIndex.value);
    options.queryPageSize.value = Number(options.pageSize.value);
    return queryLoadedViewData(
      "workflow-query",
      Number(options.pageIndex.value),
      Number(options.pageSize.value)
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
    loadedViewName,
    viewTitle,
    resultColumns,
    resultRows,
    resultPageIndex,
    resultTotalItems,
    resultTotalPages,
    resultFreshTime,
    readItemViewFor,
    loadLegacyListView,
    loadReadItemView,
    queryLegacyData,
    queryCurrentViewData
  };
}
