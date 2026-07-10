<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from "vue";
import Button from "primevue/button";
import Drawer from "primevue/drawer";
import Tag from "primevue/tag";
import {
  type CheckCodeResult,
  type CommonResponse,
  type GetMessageResult,
  type GetNotifyResult,
  type LegacyAuthItem,
  type LegacyInitAppResult,
  type LegacyLoginResult,
  type LegacyMainResult,
  type LegacyRunOperationResult,
  type LegacySubMenuResult,
  type LegacyUserInfoResult,
  type QueryDataDetailResult,
  type QueryDataDetailDataItem,
  type QueryDataDetailItemGroup,
  type ListDataValue,
  type ListDataItem,
  type ListViewInfo,
  type ListViewResult,
  type MessageInfo,
  type OperationInfo,
  type SaveItemProperty,
  type TableColumnInfo,
  postApi
} from "./api";
import LoginPanel from "./LoginPanel.vue";
import ShellActions from "./ShellActions.vue";
import ViewDetailPanel from "./ViewDetailPanel.vue";
import ViewListPanel from "./ViewListPanel.vue";
import ViewReportPanel from "./ViewReportPanel.vue";
import { useChildCandidates } from "./useChildCandidates";
import { useChildDrafts } from "./useChildDrafts";
import { useFieldEnums } from "./useFieldEnums";
import { useViewDataWorkflow } from "./useViewDataWorkflow";
import { enumFieldOptions, nextObjectId, services } from "./viewShell";
import {
  buildAddedItemProperty,
  buildDeletedItemProperty,
  buildFieldDrafts,
  buildGroupItemDrafts,
  buildSavePropertyies,
  buildSelectedExistingItemProperty,
  buildUpdatedItemProperty,
  dataOperations,
  detailResultItems,
  detailResultSimpleData,
  emptyGroupDraft,
  fieldKey,
  groupKey,
  groupColumns,
  itemDataId,
  itemKey,
  legacyAppDefaultViewId,
  legacyAuthNo,
  legacyAuthText,
  legacyAuthViewId,
  legacyCheckCodeKey,
  legacyInitAppCheckCode,
  legacyInitAppDbId,
  legacyLoginErrorMessage,
  legacyDetailPath,
  legacyItemViewPathId,
  legacyMainMenuItems,
  legacyMessageResultKey,
  legacyMessageResultView,
  legacyMessages,
  legacyNotifies,
  legacyNotifyCountForAuth,
  legacyRunOperationSuccess,
  legacySubMenuItems,
  legacyUserName,
  listAutoFreshTime,
  listRows,
  listTotalItems,
  listTotalPages,
  legacyNewPath,
  legacyViewPathId,
  operationId as operationInfoId,
  rowObjectId,
  selectedChildViewId,
  sudokuPanelKind,
  sudokuPanelListViewType,
  sudokuPanelViewId,
  viewDisplayTitle,
  viewId,
  viewUsesChartTemplate,
  viewUsesSudokuTemplate,
  renderedDetailFields,
  renderedDetailGroups,
  viewColumns
} from "./viewWorkflow";
import {
  buildInitNewRequest,
  buildLegacyListViewRequest,
  buildLegacyQueryDataRequest,
  buildQueryDataDetailRequest,
  buildRunOperationRequest,
  buildSaveObjRequest,
  buildSaveNewObjRequest,
  buildTokenRequest
} from "./payload";

const token = ref(localStorage.getItem("fool-service-token") || "");
const mobileMenuOpen = ref(false);
const userId = ref("");
const password = ref("");
const legacyAppId = "fool-service";
const legacyAppKey = "fool-service";
const legacyDbId = ref("car_wash");
const legacyListViewId = ref(0);
const readItemViewId = ref(0);
const pageIndex = ref(1);
const pageSize = ref(20);
const viewKeyword = ref("");
const detailViewId = ref(0);
const checkCodeKey = ref("");
const checkCodeValue = ref("");
const subMenuParentAuthCode = ref("");
const isMetadataOnlyView = ref(false);
const isStandaloneDetail = ref(false);
const showViewReport = ref(false);
const selectedObjectId = ref("");
const isCreatingObject = ref(false);
const detailDrafts = ref<Record<string, string>>({});
let newObjectOwner = { ownerViewId: "", ownerId: "", property: "" };
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
const {
  childDrafts,
  newChildDrafts,
  childDraftValue,
  newChildDraftValue,
  setChildDraftValue,
  setNewChildDraftValue,
  syncChildDrafts
} = useChildDrafts();

const initAppResponse = ref<CommonResponse<LegacyInitAppResult> | null>(null);
const legacyUserInfoResponse = ref<CommonResponse<LegacyUserInfoResult> | null>(null);
const mainInfoResponse = ref<CommonResponse<LegacyMainResult> | null>(null);
const checkCodeResponse = ref<CommonResponse<CheckCodeResult> | null>(null);
const subMenuResponse = ref<CommonResponse<LegacySubMenuResult> | null>(null);
const detailResponse = ref<CommonResponse<QueryDataDetailResult> | null>(null);
const messageResponse = ref<CommonResponse<GetMessageResult> | null>(null);
const notifyResponse = ref<CommonResponse<GetNotifyResult> | null>(null);
const errorMessage = ref("");
const pendingAction = ref("");
const shellErrorMessage = ref("");
const shellPending = ref(false);

const {
  viewResponse,
  dataResponse,
  currentViewId,
  loadedViewName,
  viewTitle,
  resultColumns,
  resultRows,
  readItemViewFor,
  loadLegacyListView,
  loadReadItemView: loadReadItemViewBase,
  loadViewById,
  loadViewDataById,
  queryCurrentViewData: queryCurrentViewDataBase
} = useViewDataWorkflow({
  token,
  listViewId: legacyListViewId,
  readItemViewId,
  pageIndex,
  pageSize,
  keyword: viewKeyword,
  detailViewId,
  runAction
});
const { enumOptions, loadFieldEnums: loadFieldEnumsFor } = useFieldEnums(token, runAction);

const selectedObject = computed(() => resultRows.value.find((row) => rowObjectId(row, resultColumns.value) === selectedObjectId.value));
const detailDataRows = computed(() => detailResultSimpleData(detailResponse.value?.data));
const currentReadItemView = computed(() => readItemViewFor(Number(detailViewId.value)));
const detailTitle = computed(() => viewDisplayTitle(currentReadItemView.value, "Detail"));
const pageViewTitle = computed(() => isMetadataOnlyView.value || isStandaloneDetail.value ? detailTitle.value : viewTitle.value);
const pageViewName = computed(() => isMetadataOnlyView.value || isStandaloneDetail.value ? "" : loadedViewName.value);
const detailRows = computed(() => renderedDetailFields(currentReadItemView.value, detailDataRows.value));
const detailItemGroups = computed<QueryDataDetailItemGroup[]>(() =>
  renderedDetailGroups(currentReadItemView.value, detailResultItems(detailResponse.value?.data))
);
const detailViewOperations = computed(() => dataOperations(detailResponse.value?.data));
const topMenuItems = computed(() => legacyMainMenuItems(mainInfoResponse.value?.data));
const subMenuItems = computed(() => legacySubMenuItems(subMenuResponse.value?.data));
const shellMenuItems = computed(() => (subMenuItems.value.length ? subMenuItems.value : topMenuItems.value));
const messageItems = computed(() => legacyMessages(messageResponse.value?.data));
const notifyItems = computed(() => legacyNotifies(notifyResponse.value?.data));
const shellUserName = computed(() => legacyUserName(legacyUserInfoResponse.value?.data));
const viewCanEdit = computed(() => Boolean(selectedObject.value || isCreatingObject.value));
const fieldEditorContext = computed(() => ({
  isAdded: isCreatingObject.value,
  lookupDisabled: Boolean(pendingAction.value),
  objectId: selectedObjectId.value,
  token: token.value,
  viewId: Number(detailViewId.value)
}));

function fieldEnumOptions(field: ListDataValue) {
  return enumFieldOptions(enumOptions.value, field);
}

const isChartView = computed(() => viewUsesChartTemplate(viewResponse.value?.data));
const isSudokuView = computed(() => viewUsesSudokuTemplate(viewResponse.value?.data));
const sudokuPanels = computed(() => viewColumns(viewResponse.value?.data));
type SudokuPanelResult = { view: ListViewInfo; data: ListViewResult | null; detail?: QueryDataDetailResult | null };

const sudokuPanelData = ref<Record<number, SudokuPanelResult>>({});
let autoRefreshTimer: number | undefined;
let shellPollTimer: number | undefined;
let shellRefreshInFlight = false;

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

async function initApp() {
  const response = await runAction("initapp", () =>
    postApi<LegacyInitAppResult>("/api/v1/auth/initapp", {
      AppId: legacyAppId,
      AppKey: legacyAppKey
    })
  );

  if (response) {
    initAppResponse.value = response;
    const checkCode = legacyInitAppCheckCode(response.data);
    checkCodeKey.value = legacyCheckCodeKey(checkCode) || checkCodeKey.value;
    legacyDbId.value = legacyInitAppDbId(response.data) || legacyDbId.value;
  }
}

async function loginV2() {
  const response = await runAction("loginv2", () =>
    postApi<LegacyLoginResult>("/api/v1/auth/loginv2", {
      UserId: userId.value,
      PassWord: password.value,
      DbId: legacyDbId.value,
      CheckCode: checkCodeValue.value,
      AppId: legacyAppId,
      AppKey: legacyAppKey,
      CheckCodeKey: checkCodeKey.value
    })
  );

  if (!response) return false;
  applyDefaultAppView(response.data);
  token.value = response.data?.token || response.data?.Token || "";
  if (!token.value) {
    errorMessage.value = legacyLoginErrorMessage(response.data) || "Sign in failed.";
    return false;
  }
  localStorage.setItem("fool-service-token", token.value);
  return true;
}

async function submitLegacyLogin(user: string, secret: string, database: string, checkCode: string) {
  userId.value = user;
  password.value = secret;
  legacyDbId.value = database;
  checkCodeValue.value = checkCode;
  if (await loginV2()) {
    password.value = "";
    checkCodeValue.value = "";
    await enterAuthenticatedShell();
    return;
  }
  const loginError = errorMessage.value || "Sign in failed.";
  await refreshLoginCheckCode();
  errorMessage.value = loginError;
}

async function loadMainInfo() {
  const response = await runAction("getmain", () => postApi<LegacyMainResult>("/api/v1/auth/getmain", token.value));
  if (response) {
    mainInfoResponse.value = response;
    applyDefaultAppView(response.data);
  }
  return response;
}

async function loadCheckCode() {
  const response = await runAction("getcheckcode", () => postApi<CheckCodeResult>("/api/v1/auth/getcheckcode", {}));
  if (response) {
    checkCodeResponse.value = response;
    checkCodeKey.value = legacyCheckCodeKey(response.data);
  }
}

async function refreshLoginCheckCode() {
  checkCodeValue.value = "";
  await loadCheckCode();
}

async function prepareLegacyLogin() {
  if (!initAppResponse.value) await initApp();
  if (!checkCodeKey.value) await loadCheckCode();
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

async function openShellMenu(item: LegacyAuthItem) {
  const itemViewId = legacyAuthViewId(item);
  if (itemViewId) {
    applyRequestedViewId(itemViewId);
    await loadViewWorkflow(true);
    return;
  }
  const authNo = legacyAuthNo(item);
  if (authNo) {
    subMenuParentAuthCode.value = authNo;
    await loadSubMenu();
  }
}

async function openPrimarySection() {
  if (isMetadataOnlyView.value || isStandaloneDetail.value) {
    await loadViewWorkflow();
  }
}

async function openMobilePrimarySection() {
  mobileMenuOpen.value = false;
  await openPrimarySection();
}

async function openMobileShellMenu(item: LegacyAuthItem) {
  mobileMenuOpen.value = false;
  await openShellMenu(item);
}

function shellNotifyCount(item: LegacyAuthItem) {
  return legacyNotifyCountForAuth(notifyItems.value, legacyAuthNo(item));
}

async function openShellMessage(message: MessageInfo) {
  const targetViewId = legacyMessageResultView(message);
  if (!targetViewId) return;
  const targetObjectId = legacyMessageResultKey(message);
  if (targetObjectId) {
    await loadLegacyDetailPath({ viewId: targetViewId, objectId: targetObjectId });
    return;
  }
  applyRequestedViewId(targetViewId);
  await loadViewWorkflow(true);
}

async function refreshShellStatus(interactive = true) {
  if (!token.value || shellRefreshInFlight) return;
  shellRefreshInFlight = true;
  shellErrorMessage.value = "";
  if (interactive) shellPending.value = true;
  try {
    const request = buildTokenRequest(token.value);
    const [user, messages, notifies] = await Promise.allSettled([
      postApi<LegacyUserInfoResult>("/api/v1/auth/getuserinfo", request),
      postApi<GetMessageResult>("/api/v1/message/getmsg", request),
      postApi<GetNotifyResult>("/api/v1/message/getnotify", request)
    ]);
    if (user.status === "fulfilled") legacyUserInfoResponse.value = user.value;
    if (messages.status === "fulfilled" && legacyMessages(messages.value.data).length) {
      messageResponse.value = messages.value;
    }
    if (notifies.status === "fulfilled") notifyResponse.value = notifies.value;
    const failed = [user, messages, notifies].find((result) => result.status === "rejected");
    if (failed?.status === "rejected") {
      shellErrorMessage.value = failed.reason instanceof Error ? failed.reason.message : String(failed.reason);
    }
  } finally {
    shellRefreshInFlight = false;
    if (interactive) shellPending.value = false;
  }
}

function startShellPolling() {
  stopShellPolling();
  shellPollTimer = window.setInterval(() => void refreshShellStatus(false), 15_000);
}

function stopShellPolling() {
  if (shellPollTimer !== undefined) {
    window.clearInterval(shellPollTimer);
    shellPollTimer = undefined;
  }
}

function clearLegacySession() {
  stopAutoRefresh();
  stopShellPolling();
  token.value = "";
  legacyUserInfoResponse.value = null;
  mainInfoResponse.value = null;
  subMenuResponse.value = null;
  messageResponse.value = null;
  notifyResponse.value = null;
  checkCodeResponse.value = null;
  checkCodeKey.value = "";
  checkCodeValue.value = "";
  shellErrorMessage.value = "";
  localStorage.removeItem("fool-service-token");
}

async function logout() {
  const response = await runAction("logout", () => postApi<void>("/api/v1/auth/logout", buildTokenRequest(token.value)));
  if (response) {
    clearLegacySession();
    await prepareLegacyLogin();
  }
}

async function loadReadItemView(viewId = Number(readItemViewId.value)) {
  const response = await loadReadItemViewBase(viewId);
  if (response) {
    syncDetailDrafts();
  }
  return response;
}

async function queryCurrentViewData() {
  if (isSudokuView.value) {
    await loadSudokuPanels();
    return null;
  }
  const response = await queryCurrentViewDataBase();
  if (response) {
    scheduleAutoRefresh(response.data);
  }
  return response;
}

async function loadSudokuPanels() {
  if (!isSudokuView.value) {
    sudokuPanelData.value = {};
    return;
  }
  const loaded: Record<number, SudokuPanelResult> = {};
  for (const panel of sudokuPanels.value) {
    const panelViewId = sudokuPanelViewId(panel);
    if (!panelViewId) continue;
    const response = await loadSudokuPanel(panel);
    if (response) {
      loaded[panelViewId] = mergeSudokuPanelResult(loaded[panelViewId], response);
      sudokuPanelData.value = { ...loaded };
      if (sudokuPanelKind(panel) === "group") {
        for (const childPanel of sudokuGroupPanels(panel)) {
          const childViewId = sudokuPanelViewId(childPanel);
          if (!childViewId || loaded[childViewId]?.data || sudokuPanelListViewType(childPanel) !== 0) continue;
          const childResponse = await loadViewDataById(childViewId, "sudoku-panel", 5);
          if (childResponse) loaded[childViewId] = mergeSudokuPanelResult(loaded[childViewId], childResponse);
        }
      }
    }
  }
  sudokuPanelData.value = { ...loaded };
}

function mergeSudokuPanelResult(current: SudokuPanelResult | undefined, next: SudokuPanelResult): SudokuPanelResult {
  return {
    ...next,
    data: next.data ?? current?.data ?? null,
    detail: next.detail ?? current?.detail
  };
}

async function loadSudokuPanel(panel: TableColumnInfo) {
  const panelViewId = sudokuPanelViewId(panel);
  if (sudokuPanelKind(panel) !== "item") {
    return loadViewDataById(panelViewId, "sudoku-panel", 5);
  }
  const panelViewResponse = await loadViewById(panelViewId, "sudoku-item");
  if (!panelViewResponse) {
    return null;
  }
  const loadedViewId = viewId(panelViewResponse.data, panelViewId);
  if (!loadedViewId) {
    return { view: panelViewResponse.data, data: null, detail: null };
  }
  const detailResponse = await runAction("sudoku-item-detail", () =>
    postApi<QueryDataDetailResult>("/api/v1/data/querydatadetail", buildQueryDataDetailRequest({
      token: token.value,
      viewId: loadedViewId,
      objId: ""
    }))
  );
  return { view: panelViewResponse.data, data: null, detail: detailResponse?.data ?? null };
}

function sudokuPanelResult(panel: TableColumnInfo) {
  return sudokuPanelData.value[sudokuPanelViewId(panel)];
}

function sudokuGroupPanels(panel: TableColumnInfo) {
  return viewColumns(sudokuPanelResult(panel)?.view);
}

async function loadResultPage(nextPage: number) {
  pageIndex.value = Math.max(1, nextPage);
  await queryCurrentViewData();
}

async function queryDetail(viewId = Number(detailViewId.value), objectId = selectedObjectId.value) {
  detailViewId.value = viewId;
  detailResponse.value = null;
  const readView = await loadReadItemView(viewId);
  if (!readView?.data) return null;
  const request = buildQueryDataDetailRequest({
    token: token.value,
    viewId,
    objId: objectId
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

async function initNew(viewId: number, parentObjId = "") {
  const readView = await loadReadItemView(viewId);
  if (!readView?.data) return null;
  const request = buildInitNewRequest({
    token: token.value,
    viewId,
    parentObjId
  });

  const response = await runAction("initnew", () =>
    postApi<QueryDataDetailResult>("/api/v1/data/initnew", request)
  );
  return response;
}

async function saveObj(itemproperties: SaveItemProperty[] = []) {
  const request = buildSaveObjRequest({
    token: token.value,
    id: selectedObjectId.value,
    viewID: String(detailViewId.value),
    propertyies: buildSavePropertyies(detailRows.value, detailDrafts.value),
    itemproperties
  });

  const response = await runAction("saveobj", () => postApi<void>("/api/v1/data/saveobj", request));
  return Boolean(response);
}

async function saveNewObj() {
  const request = buildSaveNewObjRequest({
    token: token.value,
    id: selectedObjectId.value,
    viewID: String(detailViewId.value),
    propertyies: buildSavePropertyies(detailRows.value, detailDrafts.value),
    ...newObjectOwner
  });

  const response = await runAction("savenewobj", () => postApi<void>("/api/v1/data/savenewobj", request));
  return Boolean(response);
}

async function runOperation(operationId: number) {
  const request = buildRunOperationRequest({
    token: token.value,
    objectId: selectedObjectId.value,
    viewId: Number(detailViewId.value),
    operationId
  });

  return runAction("runoperation", () =>
    postApi<LegacyRunOperationResult>("/api/v1/data/runoperation", request)
  );
}

async function runViewOperation(operation: OperationInfo) {
  if (!selectedObjectId.value) {
    errorMessage.value = "Select an object first.";
    return;
  }
  const id = operationInfoId(operation);
  if (!id) {
    return;
  }
  const response = await runOperation(id);
  if (legacyRunOperationSuccess(response?.data)) {
    await queryCurrentViewData();
    await queryDetail();
  }
}

function applyDefaultAppView(source?: unknown) {
  const defaultViewId = legacyAppDefaultViewId(source);
  if (defaultViewId && !legacyListViewId.value) applyRequestedViewId(defaultViewId);
}

function applyRequestedViewId(requestedViewId: number) {
  legacyListViewId.value = requestedViewId;
  viewKeyword.value = "";
}

async function ensureLegacyShell() {
  if (!token.value) return false;
  if (mainInfoResponse.value) return true;
  if (await loadMainInfo()) return true;
  clearLegacySession();
  await prepareLegacyLogin();
  return false;
}

async function loadViewWorkflow(resetPage = false) {
  stopAutoRefresh();
  isMetadataOnlyView.value = false;
  isStandaloneDetail.value = false;
  if (resetPage) {
    pageIndex.value = 1;
  }
  if (!(await ensureLegacyShell())) return;
  const loadedView = await loadLegacyListView();
  if (!loadedView) {
    return;
  }
  const response = await queryCurrentViewData();
  if (!response) return;
  const firstRow = resultRows.value[0];
  if (firstRow) {
    await selectObject(firstRow);
  }
}

async function searchCurrentView() {
  stopAutoRefresh();
  pageIndex.value = 1;
  const response = await queryCurrentViewData();
  if (!response) return;
  const firstRow = resultRows.value[0];
  if (firstRow) {
    await selectObject(firstRow);
    return;
  }
  selectedObjectId.value = "";
  detailResponse.value = null;
  isCreatingObject.value = false;
}

async function loadLegacyDetailPath(route: { viewId: number; objectId?: string }) {
  stopAutoRefresh();
  isMetadataOnlyView.value = false;
  isStandaloneDetail.value = true;
  applyRequestedViewId(route.viewId);
  if (!(await ensureLegacyShell())) return;
  const objectId = route.objectId || "";
  isCreatingObject.value = false;
  selectedObjectId.value = objectId;
  await queryDetail(route.viewId, objectId);
}

async function loadLegacyItemView(viewId: number) {
  stopAutoRefresh();
  isMetadataOnlyView.value = true;
  isStandaloneDetail.value = false;
  applyRequestedViewId(viewId);
  detailViewId.value = readItemViewId.value = viewId;
  selectedObjectId.value = "";
  detailResponse.value = null;
  if (!(await ensureLegacyShell())) return;
  await loadReadItemView(viewId);
}

async function loadLegacyNewPath(route: { viewId: number; parentObjId: string; ownerViewId: string; property: string }) {
  stopAutoRefresh();
  isMetadataOnlyView.value = false;
  isStandaloneDetail.value = true;
  applyRequestedViewId(route.viewId);
  if (!(await ensureLegacyShell())) return;
  await startNewObject(route.viewId, route.parentObjId, route.ownerViewId, route.property);
}

async function loadInitialRoute() {
  const detailRoute = legacyDetailPath(window.location.pathname);
  if (detailRoute) {
    await loadLegacyDetailPath(detailRoute);
    return;
  }
  const itemViewId = legacyItemViewPathId(window.location.pathname);
  if (itemViewId) {
    await loadLegacyItemView(itemViewId);
    return;
  }
  const newRoute = legacyNewPath(window.location.pathname);
  if (newRoute) {
    await loadLegacyNewPath(newRoute);
    return;
  }
  const routeViewId = legacyViewPathId(window.location.pathname);
  if (routeViewId) applyRequestedViewId(routeViewId);
  await loadViewWorkflow();
}

async function enterAuthenticatedShell() {
  await loadInitialRoute();
  if (!token.value) return;
  await refreshShellStatus(false);
  startShellPolling();
}

async function initializeApp() {
  if (!token.value) {
    await prepareLegacyLogin();
    return;
  }
  await enterAuthenticatedShell();
}

onMounted(() => void initializeApp());

onUnmounted(() => {
  stopAutoRefresh();
  stopShellPolling();
});

function stopAutoRefresh() {
  if (autoRefreshTimer !== undefined) {
    window.clearInterval(autoRefreshTimer);
    autoRefreshTimer = undefined;
  }
}

function scheduleAutoRefresh(result?: ListViewResult) {
  stopAutoRefresh();
  const seconds = listAutoFreshTime(result);
  if (seconds > 0) {
    autoRefreshTimer = window.setInterval(() => {
      if (!pendingAction.value) {
        pageIndex.value = 1;
        void queryCurrentViewData();
      }
    }, seconds * 1000);
  }
}

async function selectObject(row: ListDataItem, viewId = Number(detailViewId.value)) {
  const objectId = rowObjectId(row, resultColumns.value);
  if (!objectId) {
    return;
  }
  isCreatingObject.value = false;
  selectedObjectId.value = objectId;
  detailViewId.value = viewId;
  await queryDetail(viewId, objectId);
}

async function startNewObject(viewId = Number(detailViewId.value), parentObjId = "", ownerViewId = "", property = "") {
  detailViewId.value = viewId;
  detailResponse.value = null;
  newObjectOwner = { ownerViewId, ownerId: parentObjId, property };
  const initialized = await initNew(viewId, parentObjId);
  if (!initialized) {
    return;
  }
  if (!resultRows.value.length && currentViewId.value) {
    await queryCurrentViewData();
  }
  detailResponse.value = initialized;
  selectedObjectId.value = nextObjectId();
  isCreatingObject.value = true;
  syncDetailDrafts();
  await loadFieldEnums();
}

async function saveSelectedObject() {
  if (!selectedObjectId.value) {
    errorMessage.value = "Select an object first.";
    return;
  }
  const saved = isCreatingObject.value ? await saveNewObj() : await saveObj();
  if (!saved) {
    return;
  }
  isCreatingObject.value = false;
  await queryCurrentViewData();
  await queryDetail();
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
  const saved = await saveObj([buildAddedItemProperty(group, itemId, drafts)]);
  if (!saved) {
    return;
  }
  newChildDrafts.value = {
    ...newChildDrafts.value,
    [key]: emptyGroupDraft(group)
  };
  await queryDetail();
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
  setCandidateResults(group, viewColumns(view.data), listRows(data.data), {
    totalItem: listTotalItems(data.data),
    totalPage: listTotalPages(data.data)
  });
}

async function addExistingDetailItem(group: QueryDataDetailItemGroup, row: ListDataItem) {
  if (!selectedObject.value || isCreatingObject.value) {
    errorMessage.value = "Save the object before adding items.";
    return;
  }
  const saved = await saveObj([buildSelectedExistingItemProperty(group, row, candidateColumns(group))]);
  if (saved) {
    await queryDetail();
  }
}

async function updateDetailItem(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
  if (!selectedObject.value || isCreatingObject.value || !itemDataId(item)) {
    errorMessage.value = "Select a saved item first.";
    return;
  }
  const drafts = childDrafts.value[itemKey(group, item)] || buildGroupItemDrafts(group, item);
  const saved = await saveObj([buildUpdatedItemProperty(group, item, drafts)]);
  if (saved) {
    await queryDetail();
  }
}

async function deleteDetailItem(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
  const dataId = itemDataId(item);
  if (!selectedObject.value || isCreatingObject.value || !dataId) {
    errorMessage.value = "Select a saved item first.";
    return;
  }
  if (!window.confirm(`Delete ${dataId}?`)) {
    return;
  }
  const saved = await saveObj([buildDeletedItemProperty(group, item)]);
  if (saved) {
    await queryDetail();
  }
}

async function loadFieldEnums() {
  await loadFieldEnumsFor([
    ...detailRows.value,
    ...detailItemGroups.value.flatMap((group) => groupColumns(group))
  ]);
}

async function loadCandidatePage(group: QueryDataDetailItemGroup, pageIndex: number) {
  setCandidateState(group, { pageIndex: Math.max(1, pageIndex) });
  await loadExistingDetailItems(group);
}

function syncDetailDrafts() {
  detailDrafts.value = buildFieldDrafts(detailRows.value);
  syncChildDrafts(detailItemGroups.value);
}
</script>

<template>
  <LoginPanel
    v-if="!token"
    :app-info="initAppResponse?.data"
    :check-code="checkCodeResponse?.data"
    :error-message="errorMessage"
    :pending="Boolean(pendingAction)"
    @refresh="refreshLoginCheckCode"
    @submit="submitLegacyLogin"
  />
  <div v-else class="app-shell">
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
          class="active"
          type="button"
          @click="openPrimarySection"
        >
          Views
        </button>
      </nav>

      <nav v-if="shellMenuItems.length" class="nav-list" aria-label="FoolFrame menu">
        <button
          v-for="item in shellMenuItems"
          :key="legacyAuthNo(item) || legacyAuthText(item)"
          type="button"
          :class="{ active: legacyAuthViewId(item) === currentViewId }"
          :disabled="Boolean(pendingAction)"
          @click="openShellMenu(item)"
        >
          <span>{{ legacyAuthText(item) || legacyAuthNo(item) }}</span>
          <strong v-if="shellNotifyCount(item)" class="nav-count">{{ shellNotifyCount(item) }}</strong>
        </button>
      </nav>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div class="topbar-title">
          <Button class="mobile-menu-button" icon="pi pi-bars" severity="secondary" text aria-label="Open navigation" @click="mobileMenuOpen = true" />
          <div>
          <h1>{{ pageViewTitle }}</h1>
          <p>{{ pageViewName }}</p>
          </div>
        </div>
        <div class="topbar-side">
          <div class="status-strip">
            <Tag
              v-for="service in services"
              :key="service.label"
              :severity="service.state === 'ready' ? 'success' : 'warn'"
              :value="`${service.label} · ${service.value}`"
              rounded
            />
          </div>
          <ShellActions
            v-if="token"
            :error-message="shellErrorMessage"
            :messages="messageItems"
            :pending="shellPending || Boolean(pendingAction)"
            :user-name="shellUserName"
            @logout="logout"
            @open-message="openShellMessage"
            @refresh="refreshShellStatus"
          />
        </div>
      </header>

      <Drawer v-model:visible="mobileMenuOpen" position="left" class="mobile-navigation" header="Navigation">
        <div class="brand drawer-brand">
          <span class="brand-mark">F</span>
          <div>
            <strong>Fool Service</strong>
            <small>FoolFrame migration</small>
          </div>
        </div>
        <nav class="nav-list" aria-label="Mobile main">
          <button class="active" type="button" @click="openMobilePrimarySection">Views</button>
        </nav>
        <nav v-if="shellMenuItems.length" class="nav-list" aria-label="Mobile FoolFrame menu">
          <button
            v-for="item in shellMenuItems"
            :key="legacyAuthNo(item) || legacyAuthText(item)"
            type="button"
            :class="{ active: legacyAuthViewId(item) === currentViewId }"
            :disabled="Boolean(pendingAction)"
            @click="openMobileShellMenu(item)"
          >
            <span>{{ legacyAuthText(item) || legacyAuthNo(item) }}</span>
            <strong v-if="shellNotifyCount(item)" class="nav-count">{{ shellNotifyCount(item) }}</strong>
          </button>
        </nav>
      </Drawer>

      <section class="view-workflow" :class="{ 'metadata-only': isMetadataOnlyView || isStandaloneDetail }" aria-label="View workflow">
        <ViewListPanel
          v-if="!isMetadataOnlyView && !isStandaloneDetail"
          v-model:keyword="viewKeyword"
          v-model:page-size="pageSize"
          :data="dataResponse?.data"
          :disabled="Boolean(pendingAction)"
          :error-message="errorMessage"
          :page-index="pageIndex"
          :panel-data="sudokuPanelData"
          :selected-object-id="selectedObjectId"
          :view="viewResponse?.data"
          @new-object="startNewObject"
          @page="loadResultPage"
          @search="searchCurrentView"
          @select="selectObject"
          @toggle-report="showViewReport = !showViewReport"
        />

        <ViewDetailPanel
          :candidate-columns="candidateColumns"
          :candidate-rows="candidateRows"
          :candidate-state="candidateState"
          :child-draft-value="childDraftValue"
          :detail-drafts="detailDrafts"
          :detail-item-groups="detailItemGroups"
          :detail-rows="detailRows"
          :detail-view-operations="detailViewOperations"
          :enum-field-options="fieldEnumOptions"
          :field-editor-context="fieldEditorContext"
          :is-creating-object="isCreatingObject"
          :new-child-draft-value="newChildDraftValue"
          :pending="Boolean(pendingAction)"
          :schema-only="isMetadataOnlyView"
          :selected-object-id="selectedObjectId"
          :title="detailTitle"
          :view-can-edit="viewCanEdit"
          @add-detail-item="addDetailItem"
          @add-existing-detail-item="addExistingDetailItem"
          @delete-detail-item="deleteDetailItem"
          @load-candidate-page="loadCandidatePage"
          @load-existing-detail-items="loadExistingDetailItems"
          @run-view-operation="runViewOperation"
          @save-selected-object="saveSelectedObject"
          @set-child-draft-value="setChildDraftValue"
          @set-new-child-draft-value="setNewChildDraftValue"
          @update-candidate-keyword="updateCandidateKeyword"
          @update-candidate-page="updateCandidatePage"
          @update-candidate-page-size="updateCandidatePageSize"
          @update-detail-draft="(key, value) => detailDrafts[key] = value"
          @update-detail-item="updateDetailItem"
        />
        <ViewReportPanel
          v-if="showViewReport && !isMetadataOnlyView && !isStandaloneDetail && currentViewId"
          :key="currentViewId"
          :pending="Boolean(pendingAction)"
          :run-action="runAction"
          :token="token"
          :view-id="currentViewId"
          @close="showViewReport = false"
        />
      </section>

    </main>
  </div>
</template>
