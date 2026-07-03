<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import {
  type AuthItem,
  type CheckCodeRequest,
  type CheckCodeResult,
  type CommonResponse,
  type GetEnumResult,
  type GetMessageResult,
  type GetNotifyResult,
  type InputQueryResult,
  type LegacyAppResult,
  type LegacyInitAppResult,
  type LegacyLoginResult,
  type LegacyMainResult,
  type LegacyRunOperationResult,
  type LegacySubMenuResult,
  type LegacyUserInfoResult,
  type QueryDataDetailResult,
  type QueryDataDetailDataItem,
  type QueryDataDetailItemGroup,
  type ListDataItem,
  type ListViewInfo,
  type ListViewResult,
  type LoginVo,
  type OperationInfo,
  type ReadItemViewInfo,
  type ReportGridResult,
  type ReportModelResult,
  type SaveItemProperty,
  type TableColumnInfo,
  type TreeNode,
  type UserDTO,
  postApi
} from "./api";
import ListDataTable from "./ListDataTable.vue";
import MetadataFieldEditor from "./MetadataFieldEditor.vue";
import { useChildCandidates } from "./useChildCandidates";
import {
  buildAddedItemProperty,
  buildDeletedItemProperty,
  buildFieldDrafts,
  buildItemDrafts,
  buildReportColsFromModel,
  buildSavePropertyies,
  buildSelectedExistingItemProperty,
  buildUpdatedItemProperty,
  columnKey,
  columnTitle,
  columnsFromRowItems,
  createOperations,
  displayValue,
  emptyGroupDraft,
  fieldKey,
  fieldModelId,
  fieldTitle,
  groupKey,
  groupColumns,
  isEnumField,
  itemKey,
  itemValue,
  listPageIndex,
  listTotalItems,
  listTotalPages,
  recordColumns,
  recordRowKey,
  reportRowsFromCells,
  rowObjectId,
  rowOperations,
  rowRenderKey,
  rowValue,
  selectedChildViewId,
  viewDetailViewId
} from "./viewWorkflow";
import {
  buildGetEnumRequest,
  buildInitNewRequest,
  buildInputQueryRequest,
  buildLegacyListViewRequest,
  buildLegacyQueryDataRequest,
  buildLegacyReadItemViewRequest,
  buildMakeReportRequest,
  buildQueryDataDetailRequest,
  buildRunOperationRequest,
  buildSaveObjRequest,
  buildSaveNewObjRequest,
  buildTokenRequest
} from "./payload";

const token = ref(localStorage.getItem("fool-service-token") || "");
const userId = ref("admin");
const password = ref("");
const legacyAppId = ref("fool-service");
const legacyAppKey = ref("fool-service");
const legacyDbId = ref("car_wash");
const viewName = ref("");
const legacyListViewId = ref(100);
const readItemViewId = ref(100);
const pageIndex = ref(1);
const pageSize = ref(20);
const legacyQueryViewId = ref(100);
const legacyQueryPageIndex = ref(1);
const legacyQueryPageSize = ref(10);
const legacyQueryFilter = ref("");
const reportViewId = ref(100);
const reportCurrentPage = ref(1);
const reportPageSize = ref(10);
const reportQueryFilter = ref("");
const reportColsJson = ref("[]");
const reportName = ref("Saved View Report");
const inputQueryViewItemId = ref("");
const inputQueryText = ref("");
const inputQueryObjId = ref("");
const inputQueryOwnerId = ref("");
const inputQueryIsAdded = ref(false);
const detailViewId = ref(100);
const detailObjId = ref("1001");
const detailIdExp = ref("");
const initNewViewId = ref(100);
const initNewParentObjId = ref("");
const enumModelId = ref("102");
const saveViewId = ref("100");
const saveObjId = ref("1001");
const savePropertyiesJson = ref("[]");
const saveItempropertiesJson = ref("");
const saveNewViewId = ref("100");
const saveNewObjId = ref("9001");
const saveNewPropertyiesJson = ref("[]");
const saveNewOwnerViewId = ref("");
const saveNewOwnerId = ref("");
const saveNewProperty = ref("");
const operationObjectId = ref("1001");
const operationViewId = ref(100);
const operationId = ref(7001);
const checkCodeKey = ref("");
const checkCodeValue = ref("");
const subMenuParentAuthCode = ref("");
const activeSection = ref("views");
const selectedObjectId = ref("");
const isCreatingObject = ref(false);
const detailDrafts = ref<Record<string, string>>({});
const childDrafts = ref<Record<string, Record<string, string>>>({});
const newChildDrafts = ref<Record<string, Record<string, string>>>({});
const enumOptions = ref<Record<string, { label: string; value: string }[]>>({});
const {
  candidateColumns,
  candidateRows,
  candidateState,
  setCandidateResults,
  setCandidateState,
  updateCandidateKeyword,
  updateCandidatePage,
  updateCandidatePageSize
} = useChildCandidates(groupKey);

const loginResponse = ref<CommonResponse<LoginVo> | null>(null);
const initAppResponse = ref<CommonResponse<LegacyInitAppResult> | null>(null);
const legacyLoginResponse = ref<CommonResponse<LegacyLoginResult> | null>(null);
const profileResponse = ref<CommonResponse<UserDTO> | null>(null);
const legacyUserInfoResponse = ref<CommonResponse<LegacyUserInfoResult> | null>(null);
const mainInfoResponse = ref<CommonResponse<LegacyMainResult> | null>(null);
const appInfoResponse = ref<CommonResponse<LegacyAppResult> | null>(null);
const checkCodeResponse = ref<CommonResponse<CheckCodeResult> | null>(null);
const checkCodeValidationResponse = ref<CommonResponse<boolean> | null>(null);
const subMenuResponse = ref<CommonResponse<LegacySubMenuResult> | null>(null);
const logoutResponse = ref<CommonResponse<void> | null>(null);
const menuResponse = ref<CommonResponse<TreeNode<AuthItem>[]> | null>(null);
const viewResponse = ref<CommonResponse<ListViewInfo> | null>(null);
const readItemViewResponse = ref<CommonResponse<ReadItemViewInfo> | null>(null);
const dataResponse = ref<CommonResponse<ListViewResult> | null>(null);
const detailResponse = ref<CommonResponse<QueryDataDetailResult> | null>(null);
const initNewResponse = ref<CommonResponse<QueryDataDetailResult> | null>(null);
const enumResponse = ref<CommonResponse<GetEnumResult> | null>(null);
const inputQueryResponse = ref<CommonResponse<InputQueryResult> | null>(null);
const saveObjResponse = ref<CommonResponse<void> | null>(null);
const saveNewObjResponse = ref<CommonResponse<void> | null>(null);
const runOperationResponse = ref<CommonResponse<LegacyRunOperationResult> | null>(null);
const reportResponse = ref<CommonResponse<ReportGridResult> | null>(null);
const reportModelResponse = ref<CommonResponse<ReportModelResult> | null>(null);
const saveReportResponse = ref<CommonResponse<void> | null>(null);
const messageResponse = ref<CommonResponse<GetMessageResult> | null>(null);
const notifyResponse = ref<CommonResponse<GetNotifyResult> | null>(null);
const backendSmokeResponse = ref<CommonResponse<Record<string, unknown>[]> | null>(null);
const errorMessage = ref("");
const pendingAction = ref("");

const services = computed(() => [
  { label: "Docker Backend", value: "8080", state: "ready" },
  { label: "MySQL", value: "car_wash", state: "ready" },
  { label: "Redis", value: "6379", state: "ready" }
]);

const navItems = [
  { id: "views", label: "Views" },
  { id: "tools", label: "API Tools" },
  { id: "migration", label: "Migration" }
];

const currentViewId = computed(() => viewResponse.value?.data?.id || legacyListViewId.value);
const viewTitle = computed(() => viewResponse.value?.data?.viewTitle || viewResponse.value?.data?.name || viewResponse.value?.data?.viewName || viewName.value || `View ${legacyListViewId.value}`);

const resultColumns = computed<TableColumnInfo[]>(() => {
  const declared = viewResponse.value?.data?.tableColumn || [];
  if (declared.length > 0) {
    return declared;
  }

  const first = dataResponse.value?.data?.items?.[0] || dataResponse.value?.data?.data?.[0];
  return columnsFromRowItems(first);
});

const resultRows = computed<ListDataItem[]>(() => dataResponse.value?.data?.items || dataResponse.value?.data?.data || []);
const resultPageIndex = computed(() => listPageIndex(dataResponse.value?.data, Number(pageIndex.value)));
const resultTotalItems = computed(() => listTotalItems(dataResponse.value?.data));
const resultTotalPages = computed(() => listTotalPages(dataResponse.value?.data));
const selectedObject = computed(() => resultRows.value.find((row) => rowObjectId(row, resultColumns.value) === selectedObjectId.value));
const detailRows = computed(() => detailResponse.value?.data?.data?.simpleData || []);
const detailItemGroups = computed<QueryDataDetailItemGroup[]>(() => detailResponse.value?.data?.data?.items || []);
const listCreateOperations = computed(() => createOperations(viewResponse.value?.data?.operations));
const listRowOperations = computed(() => rowOperations(viewResponse.value?.data?.operations));
const backendSmokeColumns = computed(() => recordColumns(backendSmokeResponse.value?.data || []));
const viewCanEdit = computed(() => Boolean(selectedObject.value || isCreatingObject.value));
const fieldEditorContext = computed(() => ({
  isAdded: isCreatingObject.value,
  lookupDisabled: Boolean(pendingAction.value),
  objectId: selectedObjectId.value,
  token: token.value,
  viewId: Number(detailViewId.value)
}));

const reportRows = computed(() => reportRowsFromCells(reportResponse.value?.data?.cells || []));

async function runAction<T>(label: string, action: () => Promise<CommonResponse<T>>) {
  pendingAction.value = label;
  errorMessage.value = "";

  try {
    return await action();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : String(error);
    return null;
  } finally {
    pendingAction.value = "";
  }
}

async function login() {
  const response = await runAction("login", () =>
    postApi<LoginVo>("/api/v1/auth/login", {
      userId: userId.value,
      password: password.value
    })
  );

  if (response) {
    loginResponse.value = response;
    token.value = response.data?.token || token.value;
    if (token.value) {
      localStorage.setItem("fool-service-token", token.value);
    }
  }
}

async function initApp() {
  const response = await runAction("initapp", () =>
    postApi<LegacyInitAppResult>("/api/v1/auth/initapp", {
      AppId: legacyAppId.value,
      AppKey: legacyAppKey.value
    })
  );

  if (response) {
    initAppResponse.value = response;
    checkCodeKey.value = response.data?.checkCode?.key || checkCodeKey.value;
    checkCodeValue.value = response.data?.checkCode?.code || checkCodeValue.value;
    legacyDbId.value = response.data?.dbs?.[0]?.dbId || legacyDbId.value;
  }
}

async function loginV2() {
  const response = await runAction("loginv2", () =>
    postApi<LegacyLoginResult>("/api/v1/auth/loginv2", {
      UserId: userId.value,
      PassWord: password.value,
      DbId: legacyDbId.value,
      CheckCode: checkCodeValue.value,
      AppId: legacyAppId.value,
      AppKey: legacyAppKey.value,
      CheckCodeKey: checkCodeKey.value
    })
  );

  if (response) {
    legacyLoginResponse.value = response;
    token.value = response.data?.token || token.value;
    if (token.value) {
      localStorage.setItem("fool-service-token", token.value);
    }
  }
}

async function loadProfile() {
  const response = await runAction("profile", () =>
    postApi<UserDTO>("/api/v1/auth/profile", {
      token: token.value
    })
  );
  if (response) {
    profileResponse.value = response;
  }
}

async function loadLegacyUserInfo() {
  const response = await runAction("getuserinfo", () =>
    postApi<LegacyUserInfoResult>("/api/v1/auth/getuserinfo", buildTokenRequest(token.value))
  );
  if (response) {
    legacyUserInfoResponse.value = response;
  }
}

async function loadMainInfo() {
  const response = await runAction("getmain", () => postApi<LegacyMainResult>("/api/v1/auth/getmain", token.value));
  if (response) {
    mainInfoResponse.value = response;
  }
}

async function loadAppInfo() {
  const response = await runAction("getapp", () =>
    postApi<LegacyAppResult>("/api/v1/auth/getapp", buildTokenRequest(token.value))
  );
  if (response) {
    appInfoResponse.value = response;
  }
}

async function loadCheckCode() {
  const response = await runAction("getcheckcode", () => postApi<CheckCodeResult>("/api/v1/auth/getcheckcode", {}));
  if (response) {
    checkCodeResponse.value = response;
    checkCodeKey.value = response.data?.key || "";
    checkCodeValue.value = response.data?.code || "";
  }
}

async function validateCheckCode() {
  const request: CheckCodeRequest = {
    key: checkCodeKey.value,
    code: checkCodeValue.value
  };
  const response = await runAction("checkcode", () => postApi<boolean>("/api/v1/auth/checkcode", request));
  if (response) {
    checkCodeValidationResponse.value = response;
  }
}

async function loadMenus() {
  const response = await runAction("menus", () =>
    postApi<TreeNode<AuthItem>[]>("/api/v1/auth/auth-menus", {
      token: token.value
    })
  );
  if (response) {
    menuResponse.value = response;
  }
}

async function loadSubMenu() {
  const response = await runAction("getsubmenu", () =>
    postApi<LegacySubMenuResult>("/api/v1/auth/getsubmenu", {
      token: token.value,
      ParentAuthCode: subMenuParentAuthCode.value.trim()
    })
  );
  if (response) {
    subMenuResponse.value = response;
  }
}

async function loadMessages() {
  const response = await runAction("messages", () =>
    postApi<GetMessageResult>("/api/v1/message/getmsg", buildTokenRequest(token.value))
  );
  if (response) {
    messageResponse.value = response;
  }
}

async function loadNotify() {
  const response = await runAction("notify", () =>
    postApi<GetNotifyResult>("/api/v1/message/getnotify", buildTokenRequest(token.value))
  );
  if (response) {
    notifyResponse.value = response;
  }
}

async function logout() {
  const response = await runAction("logout", () => postApi<void>("/api/v1/auth/logout", buildTokenRequest(token.value)));
  if (response) {
    logoutResponse.value = response;
    token.value = "";
    localStorage.removeItem("fool-service-token");
  }
}

async function loadLegacyListView() {
  const request = buildLegacyListViewRequest({
    token: token.value,
    viewId: Number(legacyListViewId.value)
  });

  const response = await runAction("legacy-list-view", () => postApi<ListViewInfo>("/api/v1/view/getlistview", request));
  if (response) {
    viewResponse.value = response;
    applyLoadedView(response.data);
  }
  return response;
}

async function loadReadItemView() {
  const request = buildLegacyReadItemViewRequest({
    token: token.value,
    viewId: Number(readItemViewId.value)
  });

  const response = await runAction("read-item-view", () =>
    postApi<ReadItemViewInfo>("/api/v1/view/getreaditemview", request)
  );
  if (response) {
    readItemViewResponse.value = response;
  }
}

async function queryLegacyData() {
  legacyListViewId.value = Number(legacyQueryViewId.value);
  if (!(await loadLegacyListView())) return;
  const request = buildLegacyQueryDataRequest({
    token: token.value,
    viewId: Number(currentViewId.value),
    pageIndex: Number(legacyQueryPageIndex.value),
    pageSize: Number(legacyQueryPageSize.value),
    queryFilter: legacyQueryFilter.value
  });

  const response = await runAction("legacy-query", () => postApi<ListViewResult>("/api/v1/data/querydata", request));
  if (response) {
    dataResponse.value = response;
  }
}

async function queryCurrentViewData() {
  const viewId = Number(currentViewId.value);
  legacyQueryViewId.value = viewId;
  legacyQueryPageIndex.value = Number(pageIndex.value);
  legacyQueryPageSize.value = Number(pageSize.value);
  const request = buildLegacyQueryDataRequest({
    token: token.value,
    viewId,
    pageIndex: Number(pageIndex.value),
    pageSize: Number(pageSize.value),
    queryFilter: legacyQueryFilter.value
  });

  const response = await runAction("workflow-query", () => postApi<ListViewResult>("/api/v1/data/querydata", request));
  if (response) {
    dataResponse.value = response;
  }
}

async function loadResultPage(nextPage: number) {
  pageIndex.value = Math.max(1, nextPage);
  await queryCurrentViewData();
}

async function runReport(action: string, path: string) {
  const request = buildMakeReportRequest({
    token: token.value,
    viewId: Number(reportViewId.value),
    currentPage: Number(reportCurrentPage.value),
    pageSize: Number(reportPageSize.value),
    queryFilter: reportQueryFilter.value,
    reportColsJson: reportColsJson.value
  });

  const response = await runAction(action, () => postApi<ReportGridResult>(path, request));
  if (response) {
    reportResponse.value = response;
  }
}

async function makeReport() {
  await runReport("makereport", "/api/v1/report/makereport");
}

async function getReport() {
  await runReport("getrpt", "/api/v1/report/getrpt");
}

async function loadReportColumns() {
  const request = buildLegacyListViewRequest({
    token: token.value,
    viewId: Number(reportViewId.value)
  });

  const response = await runAction("report-columns", () =>
    postApi<ReportModelResult>("/api/v1/report/getmkqview", request)
  );
  if (response) {
    reportModelResponse.value = response;
    reportColsJson.value = JSON.stringify(buildReportColsFromModel(response.data?.cols || []));
  }
}

async function saveReport() {
  const request = buildMakeReportRequest({
    token: token.value,
    viewId: Number(reportViewId.value),
    currentPage: Number(reportCurrentPage.value),
    pageSize: Number(reportPageSize.value),
    queryFilter: reportQueryFilter.value,
    reportColsJson: reportColsJson.value,
    reportName: reportName.value
  });

  const response = await runAction("saverpt", () => postApi<void>("/api/v1/report/saverpt", request));
  if (response) {
    saveReportResponse.value = response;
  }
}

async function inputQuery() {
  const request = buildInputQueryRequest({
    token: token.value,
    viewId: Number(currentViewId.value),
    viewItemId: inputQueryViewItemId.value,
    text: inputQueryText.value,
    objID: inputQueryObjId.value,
    ownerId: inputQueryOwnerId.value,
    isAdded: inputQueryIsAdded.value
  });

  const response = await runAction("inputquery", () =>
    postApi<InputQueryResult>("/api/v1/data/inputquery", request)
  );
  if (response) {
    inputQueryResponse.value = response;
  }
}

async function queryDetail(viewId = Number(detailViewId.value)) {
  const request = buildQueryDataDetailRequest({
    token: token.value,
    viewId,
    objId: detailObjId.value,
    idExp: detailIdExp.value
  });

  const response = await runAction("detail", () =>
    postApi<QueryDataDetailResult>("/api/v1/data/querydatadetail", request)
  );
  if (response) {
    detailResponse.value = response;
    syncDetailDrafts();
    await loadFieldEnums();
  }
  return response;
}

async function initNew() {
  const request = buildInitNewRequest({
    token: token.value,
    viewId: Number(initNewViewId.value),
    parentObjId: initNewParentObjId.value
  });

  const response = await runAction("initnew", () =>
    postApi<QueryDataDetailResult>("/api/v1/data/initnew", request)
  );
  if (response) {
    initNewResponse.value = response;
  }
  return response;
}

async function loadEnums() {
  const request = buildGetEnumRequest({
    token: token.value,
    modelId: enumModelId.value
  });

  const response = await runAction("getenums", () => postApi<GetEnumResult>("/api/v1/data/getenums", request));
  if (response) {
    enumResponse.value = response;
  }
}

async function saveObj() {
  const request = buildSaveObjRequest({
    token: token.value,
    id: saveObjId.value,
    viewID: saveViewId.value,
    propertyiesJson: savePropertyiesJson.value,
    itempropertiesJson: saveItempropertiesJson.value
  });

  const response = await runAction("saveobj", () => postApi<void>("/api/v1/data/saveobj", request));
  if (response) {
    saveObjResponse.value = response;
    return true;
  }
  return false;
}

async function saveNewObj() {
  const request = buildSaveNewObjRequest({
    token: token.value,
    id: saveNewObjId.value,
    viewID: saveNewViewId.value,
    propertyiesJson: saveNewPropertyiesJson.value,
    ownerViewId: saveNewOwnerViewId.value,
    ownerId: saveNewOwnerId.value,
    property: saveNewProperty.value
  });

  const response = await runAction("savenewobj", () => postApi<void>("/api/v1/data/savenewobj", request));
  if (response) {
    saveNewObjResponse.value = response;
    return true;
  }
  return false;
}

async function runOperation() {
  const request = buildRunOperationRequest({
    token: token.value,
    objectId: operationObjectId.value,
    viewId: Number(operationViewId.value),
    operationId: Number(operationId.value)
  });

  const response = await runAction("runoperation", () =>
    postApi<LegacyRunOperationResult>("/api/v1/data/runoperation", request)
  );
  if (response) {
    runOperationResponse.value = response;
  }
}

async function runViewOperation(operation: OperationInfo) {
  if (!selectedObjectId.value) {
    errorMessage.value = "Select an object first.";
    return;
  }
  if (!operation.id) {
    return;
  }
  operationObjectId.value = selectedObjectId.value;
  operationViewId.value = Number(detailViewId.value);
  operationId.value = operation.id;
  await runOperation();
  if (runOperationResponse.value?.data?.success) {
    await queryCurrentViewData();
    detailObjId.value = selectedObjectId.value;
    await queryDetail(Number(detailViewId.value));
  }
}

async function loadBackendSmoke() {
  const response = await runAction("backend-smoke", async () => {
    const backendResponse = await fetch("/test");
    const data = (await backendResponse.json().catch(() => null)) as Record<string, unknown>[] | null;
    if (!backendResponse.ok) {
      throw new Error(`GET /test failed with HTTP ${backendResponse.status}`);
    }
    if (!Array.isArray(data)) {
      throw new Error("GET /test returned an unexpected payload.");
    }
    return {
      code: 0,
      message: "OK",
      data
    };
  });
  if (response) {
    backendSmokeResponse.value = response;
  }
}

function applyLoadedView(view?: ListViewInfo) {
  if (view?.viewName) {
    viewName.value = view.viewName;
  }
  const loadedViewId = view?.id;
  if (loadedViewId) {
    const loadedDetailViewId = viewDetailViewId(view, loadedViewId);
    legacyListViewId.value = loadedViewId;
    readItemViewId.value = loadedViewId;
    legacyQueryViewId.value = loadedViewId;
    reportViewId.value = loadedViewId;
    detailViewId.value = loadedDetailViewId;
    initNewViewId.value = loadedDetailViewId;
    operationViewId.value = loadedDetailViewId;
    saveViewId.value = String(loadedDetailViewId);
    saveNewViewId.value = String(loadedDetailViewId);
  }
}

async function loadViewWorkflow(resetPage = false) {
  if (resetPage) {
    pageIndex.value = 1;
  }
  const loadedView = await loadLegacyListView();
  if (!loadedView) {
    return;
  }
  await queryCurrentViewData();
  const firstRow = resultRows.value[0];
  if (firstRow) {
    await selectObject(firstRow);
  }
}

onMounted(() => {
  void loadViewWorkflow();
});

async function selectObject(row: ListDataItem, viewId = Number(detailViewId.value)) {
  const objectId = rowObjectId(row, resultColumns.value);
  if (!objectId) {
    return;
  }
  isCreatingObject.value = false;
  selectedObjectId.value = objectId;
  detailViewId.value = viewId;
  detailObjId.value = objectId;
  saveObjId.value = objectId;
  operationObjectId.value = objectId;
  await queryDetail(viewId);
}

async function startNewObject(viewId = Number(detailViewId.value)) {
  initNewViewId.value = viewId;
  initNewParentObjId.value = "";
  const initialized = await initNew();
  if (!initialized) {
    return;
  }
  if (!resultRows.value.length) {
    await queryCurrentViewData();
  }
  detailResponse.value = initialized;
  selectedObjectId.value = nextObjectId();
  detailObjId.value = selectedObjectId.value;
  saveObjId.value = selectedObjectId.value;
  operationObjectId.value = selectedObjectId.value;
  isCreatingObject.value = true;
  syncDetailDrafts();
  await loadFieldEnums();
}

async function saveSelectedObject() {
  if (!selectedObjectId.value) {
    errorMessage.value = "Select an object first.";
    return;
  }
  const propertyiesJson = JSON.stringify(buildSavePropertyies(detailRows.value, detailDrafts.value));
  let saved = false;
  if (isCreatingObject.value) {
    saveNewObjId.value = selectedObjectId.value;
    saveNewViewId.value = String(initNewViewId.value);
    saveNewPropertyiesJson.value = propertyiesJson;
    saveNewOwnerViewId.value = "";
    saveNewOwnerId.value = "";
    saveNewProperty.value = "";
    saved = await saveNewObj();
  } else {
    saveObjId.value = selectedObjectId.value;
    saveViewId.value = String(detailViewId.value);
    savePropertyiesJson.value = propertyiesJson;
    saveItempropertiesJson.value = "";
    saved = await saveObj();
  }
  if (!saved) {
    return;
  }
  isCreatingObject.value = false;
  detailObjId.value = selectedObjectId.value;
  await queryCurrentViewData();
  await queryDetail(Number(detailViewId.value));
}

async function addDetailItem(group: QueryDataDetailItemGroup) {
  if (!selectedObject.value || isCreatingObject.value) {
    errorMessage.value = "Save the object before adding items.";
    return;
  }
  const key = groupKey(group);
  const drafts = { ...(newChildDrafts.value[key] || {}) };
  const firstField = groupColumns(group)[0];
  const firstFieldKey = firstField ? fieldKey(firstField) : "";
  const itemId = firstFieldKey && drafts[firstFieldKey] ? drafts[firstFieldKey] : nextObjectId();
  if (firstFieldKey && !drafts[firstFieldKey]) {
    drafts[firstFieldKey] = itemId;
  }
  setDetailItemSavePayload([buildAddedItemProperty(group, itemId, drafts)]);
  const saved = await saveObj();
  saveItempropertiesJson.value = "";
  if (!saved) {
    return;
  }
  newChildDrafts.value = {
    ...newChildDrafts.value,
    [key]: emptyGroupDraft(group)
  };
  await queryDetail(Number(detailViewId.value));
}

async function loadExistingDetailItems(group: QueryDataDetailItemGroup) {
  const viewId = selectedChildViewId(group);
  if (!viewId) {
    errorMessage.value = "No selectable view configured.";
    return;
  }
  const viewRequest = buildLegacyListViewRequest({
    token: token.value,
    viewId
  });
  const view = await runAction("child-select-view", () => postApi<ListViewInfo>("/api/v1/view/getlistview", viewRequest));
  if (!view) {
    return;
  }
  const columns = view.data?.tableColumn || [];
  const state = candidateState(group);
  const dataRequest = buildLegacyQueryDataRequest({
    token: token.value,
    viewId,
    pageIndex: state.pageIndex,
    pageSize: state.pageSize,
    keyword: state.keyword
  });
  const data = await runAction("child-select-data", () => postApi<ListViewResult>("/api/v1/data/querydata", dataRequest));
  if (!data) {
    return;
  }
  setCandidateResults(group, columns, data.data?.items || data.data?.data || [], {
    totalItem: data.data?.totalItem || data.data?.pageInfo?.total || 0,
    totalPage: data.data?.totalPage || data.data?.pageInfo?.pageCount || 0
  });
}

async function addExistingDetailItem(group: QueryDataDetailItemGroup, row: ListDataItem) {
  if (!selectedObject.value || isCreatingObject.value) {
    errorMessage.value = "Save the object before adding items.";
    return;
  }
  setDetailItemSavePayload([buildSelectedExistingItemProperty(group, row, candidateColumns(group))]);
  const saved = await saveObj();
  saveItempropertiesJson.value = "";
  if (saved) {
    await queryDetail(Number(detailViewId.value));
  }
}

async function updateDetailItem(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
  if (!selectedObject.value || isCreatingObject.value || !item.dataId) {
    errorMessage.value = "Select a saved item first.";
    return;
  }
  const drafts = childDrafts.value[itemKey(group, item)] || buildFieldDrafts(item.values || []);
  setDetailItemSavePayload([buildUpdatedItemProperty(group, item, drafts)]);
  const saved = await saveObj();
  saveItempropertiesJson.value = "";
  if (saved) {
    await queryDetail(Number(detailViewId.value));
  }
}

async function deleteDetailItem(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
  if (!selectedObject.value || isCreatingObject.value || !item.dataId) {
    errorMessage.value = "Select a saved item first.";
    return;
  }
  if (!window.confirm(`Delete ${item.dataId}?`)) {
    return;
  }
  setDetailItemSavePayload([buildDeletedItemProperty(group, item)]);
  const saved = await saveObj();
  saveItempropertiesJson.value = "";
  if (saved) {
    await queryDetail(Number(detailViewId.value));
  }
}

function setDetailItemSavePayload(itemproperties: SaveItemProperty[]) {
  saveObjId.value = selectedObjectId.value;
  saveViewId.value = String(detailViewId.value);
  savePropertyiesJson.value = JSON.stringify(buildSavePropertyies(detailRows.value, detailDrafts.value));
  saveItempropertiesJson.value = JSON.stringify(itemproperties);
}

function nextObjectId() {
  return String(Date.now());
}

function enumFieldOptions(field: { prpModelId?: number }) {
  return enumOptions.value[String(field.prpModelId || "")] || [];
}

async function loadFieldEnums() {
  const fields = [
    ...detailRows.value,
    ...detailItemGroups.value.flatMap((group) => groupColumns(group))
  ].filter(isEnumField);
  for (const field of fields) {
    const modelId = String(fieldModelId(field));
    if (enumOptions.value[modelId]) {
      continue;
    }
    const response = await runAction("field-enums", () =>
      postApi<GetEnumResult>("/api/v1/data/getenums", buildGetEnumRequest({ token: token.value, modelId }))
    );
    if (response) {
      enumOptions.value = {
        ...enumOptions.value,
        [modelId]: (response.data?.enumValues || []).map((item) => ({
          label: item.name || displayValue(item.value),
          value: displayValue(item.value)
        }))
      };
    }
  }
}

async function loadCandidatePage(group: QueryDataDetailItemGroup, pageIndex: number) {
  setCandidateState(group, { pageIndex: Math.max(1, pageIndex) });
  await loadExistingDetailItems(group);
}

function syncDetailDrafts() {
  detailDrafts.value = buildFieldDrafts(detailRows.value);
  childDrafts.value = buildItemDrafts(detailItemGroups.value);
  newChildDrafts.value = detailItemGroups.value.reduce<Record<string, Record<string, string>>>((drafts, group) => {
    const key = groupKey(group);
    drafts[key] = newChildDrafts.value[key] || emptyGroupDraft(group);
    return drafts;
  }, {});
}
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">F</span>
        <div>
          <strong>Fool Service</strong>
          <small>FoolFrame migration</small>
        </div>
      </div>

      <nav class="nav-list" aria-label="Main">
        <button
          v-for="item in navItems"
          :key="item.id"
          type="button"
          :class="{ active: activeSection === item.id }"
          @click="activeSection = item.id"
        >
          {{ item.label }}
        </button>
      </nav>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div>
          <h1>{{ viewTitle }}</h1>
          <p>{{ viewResponse?.data?.viewName || viewName }}</p>
        </div>
        <div class="status-strip">
          <div v-for="service in services" :key="service.label" class="status-item">
            <span class="status-dot" :class="service.state"></span>
            <span>{{ service.label }}</span>
            <strong>{{ service.value }}</strong>
          </div>
        </div>
      </header>

      <section v-if="activeSection === 'views'" class="view-workflow" aria-label="View workflow">
        <article class="panel view-list-panel">
          <div class="panel-heading">
            <h2>{{ viewTitle }}</h2>
            <span>{{ viewName }}</span>
          </div>
          <div class="workflow-toolbar">
            <label>
              View ID
              <input v-model.number="legacyListViewId" min="1" type="number" />
            </label>
            <label>
              QueryFilter
              <input v-model="legacyQueryFilter" />
            </label>
            <label>
              Page size
              <input v-model.number="pageSize" min="1" type="number" />
            </label>
            <button class="primary" type="button" :disabled="Boolean(pendingAction)" @click="loadViewWorkflow(true)">
              Load View
            </button>
            <button v-for="operation in listCreateOperations" :key="operation.id || operation.name" type="button" :disabled="Boolean(pendingAction)" @click="startNewObject(operation.viewId || currentViewId)">{{ operation.text || operation.name || `New ${operation.viewId}` }}</button>
            <button v-if="!listCreateOperations.length" type="button" :disabled="Boolean(pendingAction)" @click="startNewObject()">New Row</button>
          </div>

          <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

          <div class="table-wrap view-table">
            <ListDataTable
              :columns="resultColumns"
              :disabled="Boolean(pendingAction)"
              :row-operations="listRowOperations"
              :rows="resultRows"
              :selected-object-id="selectedObjectId"
              @select="selectObject"
            />
          </div>
          <div v-if="resultRows.length || resultTotalItems" class="button-row">
            <button
              type="button"
              :disabled="Boolean(pendingAction) || resultPageIndex <= 1"
              @click="loadResultPage(resultPageIndex - 1)"
            >
              Previous
            </button>
            <span>Page {{ resultPageIndex }} / {{ resultTotalPages || 1 }} · {{ resultTotalItems }} rows</span>
            <button
              type="button"
              :disabled="Boolean(pendingAction) || resultTotalPages === 0 || resultPageIndex >= resultTotalPages"
              @click="loadResultPage(resultPageIndex + 1)"
            >
              Next
            </button>
          </div>
        </article>

        <article class="panel view-detail-panel">
          <div class="panel-heading">
            <h2>Detail</h2>
            <span>{{ selectedObjectId || "No row selected" }}</span>
          </div>

          <div v-if="viewCanEdit" class="view-edit-grid">
            <label v-for="field in detailRows" :key="fieldKey(field)">
              {{ fieldTitle(field) }}
              <MetadataFieldEditor
                v-model="detailDrafts[fieldKey(field)]"
                :field="field"
                :options="enumFieldOptions(field)"
                :readonly-value="field.fmtValue"
                v-bind="fieldEditorContext"
              />
            </label>
            <button class="primary" type="button" :disabled="Boolean(pendingAction)" @click="saveSelectedObject">
              {{ isCreatingObject ? "Create Row" : "Save Row" }}
            </button>
          </div>
          <div v-else class="empty-state compact">Select a row from the list.</div>

          <div class="detail-fields">
            <div v-for="item in detailRows" :key="item.prpId || item.prpShowName">
              <span>{{ fieldTitle(item) }}</span>
              <strong>{{ item.fmtValue }}</strong>
            </div>
          </div>

          <div v-if="selectedObject && !isCreatingObject && detailResponse?.data?.operations?.length" class="view-operations">
            <h3>View Operations</h3>
            <div class="button-row">
              <button
                v-for="operation in detailResponse?.data?.operations || []"
                :key="operation.id || operation.name"
                type="button"
                :disabled="Boolean(pendingAction)"
                @click="runViewOperation(operation)"
              >
                {{ operation.text || operation.name || `Operation ${operation.id}` }}
              </button>
            </div>
            <div v-for="operation in detailResponse?.data?.operations || []" :key="`params-${operation.id}`">
              <span v-for="param in operation.params || []" :key="param.id || param.paramId" class="operation-param">
                {{ param.paramName || param.name }}
              </span>
            </div>
          </div>

          <div v-if="selectedObject && !isCreatingObject" class="view-items-panel">
            <div v-if="detailItemGroups.length" class="detail-fields">
              <template v-for="group in detailItemGroups" :key="group.prpId || group.name">
                <div>
                  <span>{{ group.itemName || group.name || group.prpId }}</span>
                  <strong>{{ group.items?.length || 0 }} rows</strong>
                </div>
                <div class="item-add-row">
                  <label v-for="field in groupColumns(group)" :key="fieldKey(field)">
                    {{ fieldTitle(field) }}
                    <MetadataFieldEditor
                      v-model="newChildDrafts[groupKey(group)][fieldKey(field)]"
                      :field="field"
                      :options="enumFieldOptions(field)"
                      v-bind="fieldEditorContext"
                    />
                  </label>
                  <button type="button" :disabled="Boolean(pendingAction)" @click="addDetailItem(group)">Add</button>
                </div>
                <div v-if="group.selectFromExists" class="table-wrap">
                  <div class="inline-fields">
                    <label>
                      Search
                      <input :value="candidateState(group).keyword" @input="updateCandidateKeyword(group, $event)" />
                    </label>
                    <label>
                      Page
                      <input
                        min="1"
                        type="number"
                        :value="candidateState(group).pageIndex"
                        @input="updateCandidatePage(group, $event)"
                      />
                    </label>
                    <label>
                      Page size
                      <input
                        min="1"
                        type="number"
                        :value="candidateState(group).pageSize"
                        @input="updateCandidatePageSize(group, $event)"
                      />
                    </label>
                    <button type="button" :disabled="Boolean(pendingAction)" @click="loadExistingDetailItems(group)">
                      Load Existing
                    </button>
                  </div>
                  <div v-if="candidateRows(group).length || candidateState(group).totalPage" class="button-row">
                    <button
                      type="button"
                      :disabled="Boolean(pendingAction) || candidateState(group).pageIndex <= 1"
                      @click="loadCandidatePage(group, candidateState(group).pageIndex - 1)"
                    >
                      Previous
                    </button>
                    <span>
                      Page {{ candidateState(group).pageIndex }} / {{ candidateState(group).totalPage || 1 }}
                    </span>
                    <button
                      type="button"
                      :disabled="
                        Boolean(pendingAction) ||
                        candidateState(group).totalPage === 0 ||
                        candidateState(group).pageIndex >= candidateState(group).totalPage
                      "
                      @click="loadCandidatePage(group, candidateState(group).pageIndex + 1)"
                    >
                      Next
                    </button>
                  </div>
                  <table v-if="candidateRows(group).length">
                    <thead>
                      <tr>
                        <th v-for="column in candidateColumns(group)" :key="columnKey(column)">
                          {{ columnTitle(column) }}
                        </th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(row, rowIndex) in candidateRows(group)" :key="rowRenderKey(row, rowIndex)">
                        <td v-for="column in candidateColumns(group)" :key="columnKey(column)">
                          {{ rowValue(row, column) }}
                        </td>
                        <td>
                          <button
                            type="button"
                            :disabled="Boolean(pendingAction)"
                            @click="addExistingDetailItem(group, row)"
                          >
                            Select
                          </button>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <div
                  v-for="item in group.items || []"
                  :key="`${group.prpId || group.name}-${item.dataId}`"
                  class="detail-item-row"
                >
                  <span>{{ item.dataId }}</span>
                  <label v-for="field in groupColumns(group)" :key="fieldKey(field)">
                    {{ fieldTitle(field) }}
                    <MetadataFieldEditor
                      v-model="childDrafts[itemKey(group, item)][fieldKey(field)]"
                      :field="field"
                      :options="enumFieldOptions(field)"
                      :readonly-value="itemValue(item, field)"
                      v-bind="fieldEditorContext"
                    />
                  </label>
                  <button type="button" :disabled="Boolean(pendingAction)" @click="updateDetailItem(group, item)">
                    Save
                  </button>
                  <button type="button" :disabled="Boolean(pendingAction)" @click="deleteDetailItem(group, item)">
                    Delete
                  </button>
                </div>
              </template>
            </div>
            <div v-else class="empty-state compact">No child rows loaded.</div>
          </div>
        </article>
      </section>

      <section v-if="activeSection === 'tools'" class="grid auth-grid" aria-labelledby="auth-heading">
        <article class="panel">
          <div class="panel-heading">
            <h2 id="auth-heading">Auth Session</h2>
            <span>POST /api/v1/auth/login</span>
          </div>
          <label>
            User ID
            <input v-model="userId" autocomplete="username" />
          </label>
          <label>
            Password
            <input v-model="password" type="password" autocomplete="current-password" />
          </label>
          <div class="inline-fields">
            <label>
              App ID
              <input v-model="legacyAppId" />
            </label>
            <label>
              App Key
              <input v-model="legacyAppKey" />
            </label>
            <label>
              DB ID
              <input v-model="legacyDbId" />
            </label>
          </div>
          <button class="primary" type="button" :disabled="pendingAction === 'login'" @click="login">
            Login
          </button>
          <button type="button" :disabled="pendingAction === 'initapp'" @click="initApp">Init App</button>
          <button type="button" :disabled="pendingAction === 'loginv2'" @click="loginV2">Legacy Login V2</button>
        </article>

        <article class="panel">
          <div class="panel-heading">
            <h2>Token & Profile</h2>
            <span>profile / menus</span>
          </div>
          <label>
            Token
            <textarea v-model="token" rows="3" spellcheck="false"></textarea>
          </label>
          <div class="button-row">
            <button type="button" :disabled="pendingAction === 'profile'" @click="loadProfile">Profile</button>
            <button type="button" :disabled="pendingAction === 'getuserinfo'" @click="loadLegacyUserInfo">
              Legacy User Info
            </button>
            <button type="button" :disabled="pendingAction === 'getmain'" @click="loadMainInfo">Main Info</button>
            <button type="button" :disabled="pendingAction === 'getapp'" @click="loadAppInfo">App Info</button>
            <button type="button" :disabled="pendingAction === 'menus'" @click="loadMenus">Menus</button>
            <button type="button" :disabled="pendingAction === 'logout'" @click="logout">Logout</button>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Check Code</h2>
            <span>POST /api/v1/auth/getcheckcode</span>
          </div>
          <div class="inline-fields">
            <label>
              Key
              <input v-model="checkCodeKey" />
            </label>
            <label>
              Code
              <input v-model="checkCodeValue" />
            </label>
          </div>
          <div class="button-row">
            <button type="button" :disabled="pendingAction === 'getcheckcode'" @click="loadCheckCode">
              Load Check Code
            </button>
            <button type="button" :disabled="pendingAction === 'checkcode'" @click="validateCheckCode">
              Validate Code
            </button>
          </div>

          <div v-if="checkCodeResponse?.data" class="summary-list">
            <div><span>Image bytes</span><strong>{{ checkCodeResponse.data.chkCodeImg?.length || 0 }}</strong></div>
            <div><span>Valid</span><strong>{{ checkCodeValidationResponse?.data ?? "-" }}</strong></div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Sub Menu</h2>
            <span>POST /api/v1/auth/getsubmenu</span>
          </div>
          <label>
            Parent Auth Code
            <input v-model="subMenuParentAuthCode" placeholder="blank for top level" />
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'getsubmenu'" @click="loadSubMenu">
            Load Sub Menu
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="subMenuResponse?.data?.items?.length">
              <thead>
                <tr>
                  <th>Auth</th>
                  <th>Text</th>
                  <th>View</th>
                  <th>Index</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in subMenuResponse.data.items" :key="item.authNo || item.text">
                  <td>{{ item.authNo }}</td>
                  <td>{{ item.text }}</td>
                  <td>{{ item.viewId }}</td>
                  <td>{{ item.index }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No submenu items loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Messages</h2>
            <span>POST /api/v1/message/getmsg</span>
          </div>
          <button class="primary" type="button" :disabled="pendingAction === 'messages'" @click="loadMessages">
            Load Messages
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="messageResponse?.data?.messages?.length">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Content</th>
                  <th>View</th>
                  <th>Result</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="message in messageResponse.data.messages" :key="message.messageID">
                  <td>{{ message.messageID }}</td>
                  <td>{{ message.messageContent }}</td>
                  <td>{{ message.resultView }}</td>
                  <td>{{ message.resultKey }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No generated messages loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Notify Counts</h2>
            <span>POST /api/v1/message/getnotify</span>
          </div>
          <button class="primary" type="button" :disabled="pendingAction === 'notify'" @click="loadNotify">
            Load Notify Counts
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="notifyResponse?.data?.notifies?.length">
              <thead>
                <tr>
                  <th>Auth</th>
                  <th>Count</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in notifyResponse.data.notifies" :key="item.authNo || item.count">
                  <td>{{ item.authNo }}</td>
                  <td>{{ item.count }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No notify counts loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Backend Smoke</h2>
            <span>GET /test</span>
          </div>
          <button class="primary" type="button" :disabled="pendingAction === 'backend-smoke'" @click="loadBackendSmoke">
            Load Seed Data
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="backendSmokeResponse?.data?.length">
              <thead>
                <tr>
                  <th v-for="column in backendSmokeColumns" :key="column">{{ column }}</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(row, rowIndex) in backendSmokeResponse.data"
                  :key="recordRowKey(row, backendSmokeColumns, rowIndex)"
                >
                  <td v-for="column in backendSmokeColumns" :key="column">{{ displayValue(row[column]) }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No backend smoke rows loaded.</div>
          </div>
        </article>
      </section>

      <section v-if="activeSection === 'tools'" class="grid work-grid" aria-label="View and data tools">
        <article class="panel">
          <div class="panel-heading">
            <h2>View Definition</h2>
            <span>POST /api/v1/view/getlistview</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model.number="legacyListViewId" min="1" type="number" />
            </label>
          </div>
          <div class="button-row">
            <button type="button" :disabled="pendingAction === 'legacy-list-view'" @click="loadLegacyListView">
              Load View
            </button>
          </div>

          <div v-if="viewResponse?.data" class="summary-list">
            <div><span>Title</span><strong>{{ viewResponse.data.viewTitle || "-" }}</strong></div>
            <div><span>Type</span><strong>{{ viewResponse.data.viewType || "-" }}</strong></div>
            <div><span>Columns</span><strong>{{ viewResponse.data.tableColumn?.length || 0 }}</strong></div>
            <div><span>Inputs</span><strong>{{ viewResponse.data.inputInfo?.length || 0 }}</strong></div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Read Item View</h2>
            <span>POST /api/v1/view/getreaditemview</span>
          </div>
          <label>
            View ID
            <input v-model.number="readItemViewId" min="1" type="number" />
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'read-item-view'" @click="loadReadItemView">
            Load Read Items
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="readItemViewResponse?.data?.items?.length">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Property</th>
                  <th>Type</th>
                  <th>Edit</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in readItemViewResponse.data.items" :key="item.prpId || item.name">
                  <td>{{ item.name }}</td>
                  <td>{{ item.prpId }}</td>
                  <td>{{ item.prpType }}</td>
                  <td>{{ item.editType }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No read items loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Query Data</h2>
            <span>POST /api/v1/data/querydata</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model.number="legacyQueryViewId" min="1" type="number" />
            </label>
            <label>
              Page
              <input v-model.number="legacyQueryPageIndex" min="1" type="number" />
            </label>
            <label>
              Size
              <input v-model.number="legacyQueryPageSize" min="1" type="number" />
            </label>
          </div>
          <label>
            QueryFilter
            <input v-model="legacyQueryFilter" />
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'legacy-query'" @click="queryLegacyData">
            Query Data
          </button>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Report Columns</h2>
            <span>POST /api/v1/report/getmkqview</span>
          </div>
          <label>
            View ID
            <input v-model.number="reportViewId" min="1" type="number" />
          </label>
          <button
            class="primary"
            type="button"
            :disabled="pendingAction === 'report-columns'"
            @click="loadReportColumns"
          >
            Load Columns
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="reportModelResponse?.data?.cols?.length">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>ID</th>
                  <th>Type</th>
                  <th>Compare</th>
                  <th>Select</th>
                  <th>States</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="col in reportModelResponse.data.cols" :key="col.id || col.name">
                  <td>{{ col.name }}</td>
                  <td>{{ col.id }}</td>
                  <td>{{ col.prpType }}</td>
                  <td>{{ col.compareTypes?.map((item) => item.name || item.id).join(", ") }}</td>
                  <td>{{ col.queryTypes?.map((item) => item.name || item.id).join(", ") }}</td>
                  <td>{{ col.states?.map((item) => item.showName || item.dbName).join(", ") }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No report columns loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Report Grid</h2>
            <span>POST /api/v1/report/makereport | /api/v1/report/getrpt</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model.number="reportViewId" min="1" type="number" />
            </label>
            <label>
              Page
              <input v-model.number="reportCurrentPage" min="1" type="number" />
            </label>
            <label>
              Size
              <input v-model.number="reportPageSize" min="1" type="number" />
            </label>
          </div>
          <label>
            QueryFilter
            <input v-model="reportQueryFilter" />
          </label>
          <label>
            Report Columns JSON
            <textarea v-model="reportColsJson" rows="3" spellcheck="false"></textarea>
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'makereport'" @click="makeReport">
            Make Report
          </button>
          <button type="button" :disabled="pendingAction === 'getrpt'" @click="getReport">
            Get Report
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="reportRows.length">
              <thead>
                <tr>
                  <th v-for="(cell, index) in reportRows[0]" :key="`report-head-${index}`">
                    {{ cell }}
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, rowIndex) in reportRows.slice(1)" :key="`report-row-${rowIndex}`">
                  <td v-for="(cell, colIndex) in row" :key="`report-cell-${rowIndex}-${colIndex}`">
                    {{ cell }}
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No report cells loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Save Report Definition</h2>
            <span>POST /api/v1/report/saverpt</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model.number="reportViewId" min="1" type="number" />
            </label>
            <label>
              Name
              <input v-model="reportName" />
            </label>
          </div>
          <label>
            QueryFilter
            <input v-model="reportQueryFilter" />
          </label>
          <label>
            Report Columns JSON
            <textarea v-model="reportColsJson" rows="3" spellcheck="false"></textarea>
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'saverpt'" @click="saveReport">
            Save Report
          </button>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Detail Data</h2>
            <span>POST /api/v1/data/querydatadetail</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model.number="detailViewId" min="1" type="number" />
            </label>
            <label>
              Object ID
              <input v-model="detailObjId" />
            </label>
          </div>
          <label>
            ID Exp
            <input v-model="detailIdExp" />
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'detail'" @click="queryDetail()">
            Load Detail
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="detailResponse?.data?.data?.simpleData?.length">
              <thead>
                <tr>
                  <th>Property</th>
                  <th>Value</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in detailResponse.data.data.simpleData" :key="item.prpId || item.prpShowName">
                  <td>{{ item.prpShowName || item.prpId }}</td>
                  <td>{{ item.fmtValue }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No detail loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Init New Object</h2>
            <span>POST /api/v1/data/initnew</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model.number="initNewViewId" min="1" type="number" />
            </label>
            <label>
              Parent ID
              <input v-model="initNewParentObjId" />
            </label>
          </div>
          <button class="primary" type="button" :disabled="pendingAction === 'initnew'" @click="initNew">
            Init New
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="initNewResponse?.data?.data?.simpleData?.length">
              <thead>
                <tr>
                  <th>Property</th>
                  <th>Value</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in initNewResponse.data.data.simpleData" :key="item.prpId || item.prpShowName">
                  <td>{{ item.prpShowName || item.prpId }}</td>
                  <td>{{ item.fmtValue }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No new object initialized.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Enum Values</h2>
            <span>POST /api/v1/data/getenums</span>
          </div>
          <label>
            Model ID
            <input v-model="enumModelId" />
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'getenums'" @click="loadEnums">
            Load Enums
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="enumResponse?.data?.enumValues?.length">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Value</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in enumResponse.data.enumValues" :key="`${item.name}-${item.value}`">
                  <td>{{ item.name }}</td>
                  <td>{{ item.value }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No enums loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Input Query</h2>
            <span>POST /api/v1/data/inputquery</span>
          </div>
          <div class="inline-fields">
            <label>
              View Item
              <input v-model="inputQueryViewItemId" />
            </label>
            <label>
              Text
              <input v-model="inputQueryText" />
            </label>
          </div>
          <div class="inline-fields">
            <label>
              Obj ID
              <input v-model="inputQueryObjId" />
            </label>
            <label>
              Owner ID
              <input v-model="inputQueryOwnerId" />
            </label>
          </div>
          <label class="checkbox-row">
            <input v-model="inputQueryIsAdded" type="checkbox" />
            Added item
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'inputquery'" @click="inputQuery">
            Query Candidates
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="inputQueryResponse?.data?.items?.length">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Text</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in inputQueryResponse.data.items" :key="item.id || item.text">
                  <td>{{ item.id }}</td>
                  <td>{{ item.text }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No candidates loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Save Object</h2>
            <span>POST /api/v1/data/saveobj</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model="saveViewId" />
            </label>
            <label>
              Object ID
              <input v-model="saveObjId" />
            </label>
          </div>
          <label>
            Propertyies JSON
            <textarea v-model="savePropertyiesJson" rows="4" spellcheck="false"></textarea>
          </label>
          <label>
            Itemproperties JSON
            <textarea v-model="saveItempropertiesJson" rows="4" spellcheck="false"></textarea>
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'saveobj'" @click="saveObj">
            Save Object
          </button>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Save New Object</h2>
            <span>POST /api/v1/data/savenewobj</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model="saveNewViewId" />
            </label>
            <label>
              Object ID
              <input v-model="saveNewObjId" />
            </label>
          </div>
          <div class="inline-fields">
            <label>
              Owner View
              <input v-model="saveNewOwnerViewId" />
            </label>
            <label>
              Owner ID
              <input v-model="saveNewOwnerId" />
            </label>
            <label>
              Property
              <input v-model="saveNewProperty" />
            </label>
          </div>
          <label>
            Propertyies JSON
            <textarea v-model="saveNewPropertyiesJson" rows="4" spellcheck="false"></textarea>
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'savenewobj'" @click="saveNewObj">
            Save New
          </button>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Run Operation</h2>
            <span>POST /api/v1/data/runoperation</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model.number="operationViewId" min="1" type="number" />
            </label>
            <label>
              Operation ID
              <input v-model.number="operationId" min="1" type="number" />
            </label>
            <label>
              Object ID
              <input v-model="operationObjectId" />
            </label>
          </div>
          <button class="primary" type="button" :disabled="pendingAction === 'runoperation'" @click="runOperation">
            Run Operation
          </button>
        </article>
      </section>

      <section v-if="activeSection === 'tools'" class="panel results-panel" aria-label="Results">
        <div class="panel-heading">
          <h2>Response & Result Set</h2>
          <span v-if="pendingAction">Running {{ pendingAction }}...</span>
          <span v-else>Ready</span>
        </div>

        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <div class="result-layout">
          <div class="table-wrap">
            <table v-if="resultColumns.length > 0">
              <thead>
                <tr>
                  <th
                    v-for="column in resultColumns"
                    :key="column.property || column.title"
                    :style="{ width: column.width ? `${column.width}px` : undefined }"
                  >
                    {{ column.title || column.property }}
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, rowIndex) in resultRows" :key="rowRenderKey(row, rowIndex)">
                  <td
                    v-for="column in resultColumns"
                    :key="column.property || column.title"
                    :style="{ width: column.width ? `${column.width}px` : undefined }"
                  >
                    {{ rowValue(row, column) }}
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No query rows loaded.</div>
          </div>

          <pre class="json-output">{{
            JSON.stringify(
              {
                login: loginResponse,
                initApp: initAppResponse,
                legacyLogin: legacyLoginResponse,
                profile: profileResponse,
                legacyUserInfo: legacyUserInfoResponse,
                mainInfo: mainInfoResponse,
                appInfo: appInfoResponse,
                checkCode: checkCodeResponse,
                checkCodeValidation: checkCodeValidationResponse,
                subMenu: subMenuResponse,
                logout: logoutResponse,
                menus: menuResponse,
                view: viewResponse,
                readItemView: readItemViewResponse,
                data: dataResponse,
                detail: detailResponse,
                initNew: initNewResponse,
                enums: enumResponse,
                inputQuery: inputQueryResponse,
                saveObj: saveObjResponse,
                saveNewObj: saveNewObjResponse,
                runOperation: runOperationResponse,
                reportModel: reportModelResponse,
                report: reportResponse,
                saveReport: saveReportResponse,
                messages: messageResponse,
                notify: notifyResponse,
                backendSmoke: backendSmokeResponse
              },
              null,
              2
            )
          }}</pre>
        </div>
      </section>

      <section v-if="activeSection === 'migration'" class="migration-band" aria-label="Migration map">
        <div>
          <strong>SCPB01-Soway.Data</strong>
          <span>fool-common</span>
        </div>
        <div>
          <strong>SCPB02-Soway.DB</strong>
          <span>fool-dao</span>
        </div>
        <div>
          <strong>SCPB05-Soway.Model</strong>
          <span>fool-model</span>
        </div>
        <div>
          <strong>SWDQ01-Soway.Query</strong>
          <span>fool-query</span>
        </div>
        <div>
          <strong>Soway.Server</strong>
          <span>fool-view</span>
        </div>
        <div>
          <strong>SWUA Auth</strong>
          <span>fool-auth</span>
        </div>
      </section>
    </main>
  </div>
</template>
