<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, onUnmounted, ref } from "vue";
import Button from "primevue/button";
import Drawer from "primevue/drawer";
import {
  type CheckCodeResult,
  type CommonResponse,
  type GetMessageResult,
  type LegacyAuthItem,
  type LegacyInitAppResult,
  type LegacyLoginResult,
  type LegacyMainResult,
  type LegacyRunOperationResult,
  type LegacySubMenuResult,
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
  postApi
} from "./api";
import LoginPanel from "./LoginPanel.vue";
import LegacyMenuNav from "./LegacyMenuNav.vue";
import { useChildCandidates } from "./useChildCandidates";
import { useChildDrafts } from "./useChildDrafts";
import { useFieldEnums } from "./useFieldEnums";
import { usePendingChildChanges } from "./usePendingChildChanges";
import { useSudokuPanels } from "./useSudokuPanels";
import { useViewDataWorkflow } from "./useViewDataWorkflow";
import { enumFieldOptions, nextObjectId } from "./viewShell";
import {
  buildAddedDetailItem,
  buildAddedItemProperty,
  buildDeletedItemProperty,
  buildDraftsFromRow,
  buildFieldDrafts,
  buildGroupItemDrafts,
  buildSavePropertyies,
  buildSelectedExistingItemProperty,
  buildUpdatedItemProperty,
  dataCanEdit,
  dataOperations,
  detailResultItems,
  detailResultObjectId,
  detailResultParentId,
  detailResultSimpleData,
  detailResultViewName,
  emptyGroupDraft,
  fieldKey,
  groupKey,
  groupColumns,
  groupListViewId,
  groupSelectedViewId,
  itemDataId,
  itemKey,
  legacyAppDefaultViewId,
  legacyAppName,
  legacyAppPowerBy,
  legacyAppVersion,
  legacyAuthNo,
  legacyAuthViewId,
  legacyCheckCodeKey,
  legacyInitAppCheckCode,
  legacyInitAppDbId,
  legacyLoginErrorCode,
  legacyLoginErrorMessage,
  legacyDetailHref,
  legacyDetailPath,
  legacyItemViewPathId,
  legacyMainMenuItems,
  legacyMessageResultKey,
  legacyMessageResultView,
  legacyMessages,
  legacyRunOperationMessage,
  legacyRunOperationSuccess,
  legacySubMenuItems,
  legacyUserAvatar,
  legacyUserName,
  listAutoFreshTime,
  listRows,
  listTotalItems,
  listTotalPages,
  legacyChildNewHref,
  legacyNewPath,
  legacyNewHref,
  legacyViewHref,
  legacyViewPathId,
  operationId as operationInfoId,
  rowObjectId,
  viewDisplayTitle,
  viewId,
  viewTemplateKind,
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

const ShellActions = defineAsyncComponent(() => import("./ShellActions.vue"));
const ViewDetailPanel = defineAsyncComponent(() => import("./ViewDetailPanel.vue"));
const ViewListPanel = defineAsyncComponent(() => import("./ViewListPanel.vue"));
const ViewReportPanel = defineAsyncComponent(() => import("./ViewReportPanel.vue"));

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
const pageSize = ref(10);
const viewKeyword = ref("");
const viewNavigationRevision = ref(0);
const detailViewId = ref(0);
const checkCodeKey = ref("");
const checkCodeValue = ref("");
const subMenuParentAuthCode = ref("");
const isMetadataOnlyView = ref(false);
const isStandaloneDetail = ref(false);
const showUnconfiguredHome = ref(false);
const unconfiguredHomeMessage = ref("");
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
  setCandidateView,
  updateCandidateKeyword
} = useChildCandidates(groupKey);
const {
  childDrafts,
  childDraftValue,
  setChildDraftValue,
  syncChildDrafts
} = useChildDrafts();

const initAppResponse = ref<CommonResponse<LegacyInitAppResult> | null>(null);
const mainInfoResponse = ref<CommonResponse<LegacyMainResult> | null>(null);
const checkCodeResponse = ref<CommonResponse<CheckCodeResult> | null>(null);
const subMenuResponse = ref<CommonResponse<LegacySubMenuResult> | null>(null);
const detailResponse = ref<CommonResponse<QueryDataDetailResult> | null>(null);
const activeShellMessage = ref<MessageInfo | null>(null);
const errorMessage = ref("");
const loginErrorCode = ref("");
const infoMessage = ref("");
const operationResult = ref<{ message: string; success: boolean } | null>(null);
const pendingAction = ref("");
const saveDialogVisible = ref(false);
const navigateAfterSave = ref(false);
const {
  add: addPendingDetailItem,
  clear: clearPendingDetailChanges,
  isAdded: isPendingAddedDetailItem,
  itemProperties: pendingItemProperties,
  removeAdded: removePendingAddedDetailItem,
  renderGroups: renderPendingDetailGroups,
  replaceAdded: replacePendingAddedDetailItem,
  stage: stageItemProperty
} = usePendingChildChanges();

const {
  viewResponse,
  dataResponse,
  currentViewId,
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

const detailDataRows = computed(() => detailResultSimpleData(detailResponse.value?.data));
const currentReadItemView = computed(() => readItemViewFor(Number(detailViewId.value)));
const detailTitle = computed(() => viewDisplayTitle(currentReadItemView.value, "详情"));
const detailSaveViewKey = computed(
  () => detailResultViewName(detailResponse.value?.data) || String(detailViewId.value)
);
const detailOwnerId = computed(
  () => detailResultParentId(detailResponse.value?.data) || newObjectOwner.ownerId
);
const detailRows = computed(() => renderedDetailFields(currentReadItemView.value, detailDataRows.value));
const detailItemGroups = computed<QueryDataDetailItemGroup[]>(() =>
  renderPendingDetailGroups(renderedDetailGroups(currentReadItemView.value, detailResultItems(detailResponse.value?.data)))
);
const detailViewOperations = computed(() => dataOperations(detailResponse.value?.data));
const topMenuItems = computed(() => legacyMainMenuItems(mainInfoResponse.value?.data));
const subMenuItems = computed(() => legacySubMenuItems(subMenuResponse.value?.data));
const shellUserAvatar = computed(() => legacyUserAvatar(mainInfoResponse.value?.data));
const shellUserName = computed(() => legacyUserName(mainInfoResponse.value?.data));
const shellAppName = computed(() => legacyAppName(mainInfoResponse.value?.data, "Fool Service"));
const shellAppVersion = computed(() => legacyAppVersion(mainInfoResponse.value?.data));
const shellAppPowerBy = computed(() => legacyAppPowerBy(mainInfoResponse.value?.data));
const viewCanEdit = computed(() => dataCanEdit(detailResponse.value?.data));
const candidateViewLoading = computed(() => pendingAction.value === "child-select-view");
const savingDetail = computed(() => pendingAction.value === "saveobj" || pendingAction.value === "savenewobj");
const fieldEditorContext = computed(() => ({
  isAdded: isCreatingObject.value,
  lookupDisabled: Boolean(pendingAction.value),
  objectId: selectedObjectId.value,
  ownerId: detailOwnerId.value,
  token: token.value,
  viewId: Number(detailViewId.value),
  viewName: detailResultViewName(detailResponse.value?.data)
}));

function fieldEnumOptions(field: ListDataValue) {
  return enumFieldOptions(enumOptions.value, field);
}

const isChartView = computed(() => viewUsesChartTemplate(viewResponse.value?.data));
const isSudokuView = computed(() => viewUsesSudokuTemplate(viewResponse.value?.data));
const isListView = computed(() => viewTemplateKind(viewResponse.value?.data) === "list");
const isUnsupportedView = computed(() => viewTemplateKind(viewResponse.value?.data) === "unsupported");
const sudokuPanels = computed(() => viewColumns(viewResponse.value?.data));
const {
  panelData: sudokuPanelData,
  loadPanels: loadSudokuPanels,
  refreshPanel: refreshSudokuPanel,
  stopRefresh: stopSudokuPanelRefresh
} = useSudokuPanels({
  enabled: isSudokuView,
  loadViewById,
  loadViewDataById,
  panels: sudokuPanels,
  pendingAction,
  runAction,
  token
});
let autoRefreshTimer: number | undefined;
let shellPollTimer: number | undefined;
let shellPollInFlight = false;

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

  if (!response) {
    errorMessage.value = "";
    return false;
  }
  applyDefaultAppView(response.data);
  token.value = response.data?.token || response.data?.Token || "";
  if (!token.value) {
    loginErrorCode.value = legacyLoginErrorCode(response.data);
    errorMessage.value = legacyLoginErrorMessage(response.data) || "登录失败。";
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
  loginErrorCode.value = "";
  if (await loginV2()) {
    password.value = "";
    checkCodeValue.value = "";
    replaceLegacyPath("/main");
    await enterAuthenticatedShell();
  }
}

async function dismissLoginError() {
  errorMessage.value = "";
  loginErrorCode.value = "";
  await refreshLoginCheckCode();
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
    closeShellNavigation();
    pushLegacyPath(legacyViewHref(itemViewId));
    applyRequestedViewId(itemViewId);
    await loadViewWorkflow(true);
    return;
  }
  const authNo = legacyAuthNo(item);
  if (authNo) {
    if (authNo === subMenuParentAuthCode.value) {
      subMenuParentAuthCode.value = "";
      subMenuResponse.value = null;
      return;
    }
    subMenuParentAuthCode.value = authNo;
    await loadSubMenu();
  }
}

async function openPrimarySection() {
  await loadPrimarySection(true);
}

async function loadPrimarySection(updatePath: boolean) {
  if (!(await ensureLegacyShell())) return;
  if (updatePath) pushLegacyPath("/");
  closeShellNavigation();
  const defaultViewId = legacyAppDefaultViewId(mainInfoResponse.value?.data);
  if (defaultViewId) {
    applyRequestedViewId(defaultViewId);
    await loadViewWorkflow(true);
    return;
  }
  stopAutoRefresh();
  showViewReport.value = false;
  showUnconfiguredHome.value = true;
  unconfiguredHomeMessage.value = window.location.pathname === "/main"
    ? "欢迎使用SOWAY无码系统，这是默认的首页，没有配置，请参考相关说明进行设定"
    : "默认首页 还没有配置";
  isMetadataOnlyView.value = false;
  isStandaloneDetail.value = false;
  selectedObjectId.value = "";
  detailResponse.value = null;
  errorMessage.value = "";
}

async function openMobilePrimarySection() {
  await openPrimarySection();
}

async function openMobileShellMenu(item: LegacyAuthItem) {
  await openShellMenu(item);
}

async function openShellMessage(message: MessageInfo) {
  const targetViewId = legacyMessageResultView(message);
  if (!targetViewId) return;
  closeShellNavigation();
  const targetObjectId = legacyMessageResultKey(message);
  if (targetObjectId) {
    pushLegacyPath(legacyDetailHref(targetViewId, targetObjectId));
    await loadLegacyDetailPath({ viewId: targetViewId, objectId: targetObjectId });
    return;
  }
  pushLegacyPath(legacyViewHref(targetViewId));
  applyRequestedViewId(targetViewId);
  await loadViewWorkflow(true);
}

async function pollShellMessages() {
  if (!token.value || shellPollInFlight) return;
  shellPollInFlight = true;
  try {
    const messages = await postApi<GetMessageResult>(
      "/api/v1/message/getmsg",
      buildTokenRequest(token.value)
    );
    const fetchedMessages = legacyMessages(messages.data);
    if (fetchedMessages.length) {
      activeShellMessage.value = fetchedMessages[0];
    }
  } catch {
    // Keep best-effort polling failures out of the active View error surface.
  } finally {
    shellPollInFlight = false;
  }
}

function startShellPolling() {
  stopShellPolling();
  shellPollTimer = window.setInterval(() => void pollShellMessages(), 15_000);
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
  closeShellNavigation();
  token.value = "";
  mainInfoResponse.value = null;
  activeShellMessage.value = null;
  loginErrorCode.value = "";
  infoMessage.value = "";
  clearPendingDetailChanges();
  checkCodeResponse.value = null;
  checkCodeKey.value = "";
  checkCodeValue.value = "";
  localStorage.removeItem("fool-service-token");
}

async function logout() {
  const response = await runAction("logout", () => postApi<void>("/api/v1/auth/logout", buildTokenRequest(token.value)));
  if (response) {
    replaceLegacyPath("/");
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
  if (isUnsupportedView.value) {
    errorMessage.value = "";
    return null;
  }
  const response = await queryCurrentViewDataBase();
  if (response) {
    scheduleAutoRefresh(response.data);
  }
  return response;
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
    viewID: detailSaveViewKey.value,
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
    viewID: detailSaveViewKey.value,
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

async function runViewOperation(operation: OperationInfo, editing = false) {
  if (editing) {
    errorMessage.value = "请先保存当前信息";
    return;
  }
  if (!selectedObjectId.value) {
    errorMessage.value = "请先选择记录。";
    return;
  }
  const id = operationInfoId(operation);
  if (!id) {
    return;
  }
  operationResult.value = null;
  const response = await runOperation(id);
  if (!response) return;
  const success = legacyRunOperationSuccess(response.data);
  const message = legacyRunOperationMessage(response.data) || (success ? "操作成功。" : "操作失败。");
  operationResult.value = { message, success };
}

function applyDefaultAppView(source?: unknown) {
  const defaultViewId = legacyAppDefaultViewId(source);
  if (defaultViewId && !legacyListViewId.value) applyRequestedViewId(defaultViewId);
}

function applyRequestedViewId(requestedViewId: number) {
  legacyListViewId.value = requestedViewId;
  viewKeyword.value = "";
}

function pushLegacyPath(path: string) {
  if (path && window.location.pathname !== path) window.history.pushState({}, "", path);
}

function replaceLegacyPath(path: string) {
  if (path && window.location.pathname !== path) window.history.replaceState({}, "", path);
}

function closeShellNavigation() {
  mobileMenuOpen.value = false;
  subMenuParentAuthCode.value = "";
  subMenuResponse.value = null;
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
  viewNavigationRevision.value += 1;
  stopAutoRefresh();
  showViewReport.value = false;
  showUnconfiguredHome.value = false;
  operationResult.value = null;
  infoMessage.value = "";
  clearPendingDetailChanges();
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
  selectedObjectId.value = "";
  detailResponse.value = null;
}

async function searchCurrentView() {
  stopAutoRefresh();
  pageIndex.value = 1;
  const response = await queryCurrentViewData();
  if (!response) return;
  selectedObjectId.value = "";
  detailResponse.value = null;
  isCreatingObject.value = false;
}

async function loadLegacyDetailPath(route: { viewId: number; objectId?: string }) {
  stopAutoRefresh();
  showUnconfiguredHome.value = false;
  operationResult.value = null;
  infoMessage.value = "";
  clearPendingDetailChanges();
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
  showUnconfiguredHome.value = false;
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
  showUnconfiguredHome.value = false;
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
  if (routeViewId) {
    applyRequestedViewId(routeViewId);
    await loadViewWorkflow();
    return;
  }
  await loadPrimarySection(false);
}

async function enterAuthenticatedShell() {
  await loadInitialRoute();
  if (!token.value) return;
  startShellPolling();
}

function handleHistoryNavigation() {
  closeShellNavigation();
  void loadInitialRoute();
}

async function initializeApp() {
  if (!token.value) {
    await prepareLegacyLogin();
    return;
  }
  await enterAuthenticatedShell();
}

onMounted(() => {
  window.addEventListener("popstate", handleHistoryNavigation);
  void initializeApp();
});

onUnmounted(() => {
  window.removeEventListener("popstate", handleHistoryNavigation);
  stopAutoRefresh();
  stopShellPolling();
});

function stopAutoRefresh() {
  stopSudokuPanelRefresh();
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

function openListObject(row: ListDataItem, targetViewId = 0) {
  const href = legacyDetailHref(targetViewId, rowObjectId(row, viewColumns(viewResponse.value?.data)));
  if (href) window.location.assign(href);
}

function openNewObject(targetViewId: number) {
  const href = legacyNewHref(targetViewId);
  if (href) window.location.assign(href);
}

async function startNewObject(viewId = Number(detailViewId.value), parentObjId = "", ownerViewId = "", property = "") {
  operationResult.value = null;
  infoMessage.value = "";
  clearPendingDetailChanges();
  detailViewId.value = viewId;
  detailResponse.value = null;
  newObjectOwner = { ownerViewId, ownerId: parentObjId, property };
  const initialized = await initNew(viewId, parentObjId);
  if (!initialized) {
    return;
  }
  detailResponse.value = initialized;
  selectedObjectId.value = detailResultObjectId(initialized.data) || nextObjectId();
  isCreatingObject.value = true;
  syncDetailDrafts();
  await loadFieldEnums();
}

async function saveSelectedObject() {
  if (!selectedObjectId.value) {
    errorMessage.value = "请先选择记录。";
    return;
  }
  navigateAfterSave.value = false;
  saveDialogVisible.value = true;
  const saved = isCreatingObject.value ? await saveNewObj() : await saveObj(pendingItemProperties.value);
  if (!saved) {
    saveDialogVisible.value = false;
    return;
  }
  isCreatingObject.value = false;
  navigateAfterSave.value = true;
  saveDialogVisible.value = false;
}

function finishSaveNavigation() {
  if (!navigateAfterSave.value) return;
  navigateAfterSave.value = false;
  window.history.back();
}

function addDetailItem(group: QueryDataDetailItemGroup, requestedItemId = "") {
  if (blockChildAddForNewObject()) return;
  if (!selectedObjectId.value) {
    errorMessage.value = "请先保存主记录。";
    return;
  }
  const selectedViewId = groupSelectedViewId(group);
  if (selectedViewId) {
    window.location.assign(legacyChildNewHref(
      selectedViewId,
      selectedObjectId.value,
      Number(detailViewId.value),
      groupKey(group)
    ));
    return;
  }
  const drafts = emptyGroupDraft(group);
  const firstField = groupColumns(group)[0];
  const firstFieldKey = firstField ? fieldKey(firstField) : "";
  const itemId = requestedItemId || nextObjectId();
  if (firstFieldKey && !drafts[firstFieldKey]) {
    drafts[firstFieldKey] = itemId;
  }
  const item = buildAddedDetailItem(group, itemId, drafts);
  addPendingDetailItem(group, item);
  childDrafts.value = { ...childDrafts.value, [itemKey(group, item)]: drafts };
}

async function loadExistingDetailView(group: QueryDataDetailItemGroup) {
  if (blockChildAddForNewObject()) return false;
  const viewId = groupListViewId(group);
  if (!viewId) {
    errorMessage.value = "未配置可选择视图。";
    return false;
  }
  const viewRequest = buildLegacyListViewRequest({
    token: token.value,
    viewId
  });
  const view = await runAction("child-select-view", () => postApi<ListViewInfo>("/api/v1/view/getlistview", viewRequest));
  if (!view) {
    return false;
  }
  setCandidateView(group, viewColumns(view.data));
  return true;
}

async function queryExistingDetailItems(group: QueryDataDetailItemGroup, resetPage = true) {
  if (blockChildAddForNewObject()) return;
  const viewId = groupListViewId(group);
  if (!viewId) {
    errorMessage.value = "未配置可选择视图。";
    return;
  }
  if (!candidateColumns(group).length) {
    await loadExistingDetailView(group);
    if (!candidateColumns(group).length) return;
  }
  if (resetPage) setCandidateState(group, { pageIndex: 1 });
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
  setCandidateResults(group, candidateColumns(group), listRows(data.data), {
    totalItem: listTotalItems(data.data),
    totalPage: listTotalPages(data.data)
  });
}

function addExistingDetailItem(group: QueryDataDetailItemGroup, row: ListDataItem) {
  if (blockChildAddForNewObject()) return;
  if (!selectedObjectId.value) {
    errorMessage.value = "请先保存主记录。";
    return;
  }
  const columns = candidateColumns(group);
  const property = buildSelectedExistingItemProperty(group, row, columns);
  const addedItem = property.addedItems?.[0];
  if (!addedItem?.itemId) return;
  const drafts = buildDraftsFromRow(groupColumns(group), row, columns);
  const item = buildAddedDetailItem(group, addedItem.itemId, drafts);
  addPendingDetailItem(group, item, property);
  childDrafts.value = { ...childDrafts.value, [itemKey(group, item)]: drafts };
}

function blockChildAddForNewObject() {
  if (!isCreatingObject.value) return false;
  infoMessage.value = "请先保存当前内容，再新建子项";
  return true;
}

function updateDetailItem(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
  if (!selectedObjectId.value || isCreatingObject.value || !itemDataId(item)) {
    errorMessage.value = "请先选择已保存的子项。";
    return;
  }
  const drafts = childDrafts.value[itemKey(group, item)] || buildGroupItemDrafts(group, item);
  const isAdded = isPendingAddedDetailItem(group, item);
  stageItemProperty(
    isAdded
      ? buildAddedItemProperty(group, itemDataId(item), drafts)
      : buildUpdatedItemProperty(group, item, drafts)
  );
  if (isAdded) replacePendingAddedDetailItem(group, buildAddedDetailItem(group, itemDataId(item), drafts));
}

function deleteDetailItem(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
  const dataId = itemDataId(item);
  if (!selectedObjectId.value || isCreatingObject.value || !dataId) {
    errorMessage.value = "请先选择已保存的子项。";
    return;
  }
  if (!removePendingAddedDetailItem(group, item)) stageItemProperty(buildDeletedItemProperty(group, item));
}

async function loadFieldEnums() {
  await loadFieldEnumsFor([
    ...detailRows.value,
    ...detailItemGroups.value.flatMap((group) => groupColumns(group))
  ]);
}

async function loadCandidatePage(group: QueryDataDetailItemGroup, pageIndex: number) {
  setCandidateState(group, { pageIndex: Math.max(1, pageIndex) });
  await queryExistingDetailItems(group, false);
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
    :error-code="loginErrorCode"
    :error-message="errorMessage"
    :pending="Boolean(pendingAction)"
    @dismiss-error="dismissLoginError"
    @refresh="refreshLoginCheckCode"
    @submit="submitLegacyLogin"
  />
  <div v-else class="app-shell">
    <header class="shell-header">
      <h2 class="brand">
        <a href="/" @click.prevent="openPrimarySection">
          {{ shellAppName }}
          <small v-if="shellAppVersion">{{ shellAppVersion }}</small>
        </a>
      </h2>

      <div class="desktop-navigation">
        <nav class="nav-list nav-list-horizontal" aria-label="Main">
          <button type="button" @click="openPrimarySection">首页</button>
        </nav>
        <LegacyMenuNav
          :disabled="Boolean(pendingAction)"
          :expanded-auth-code="subMenuParentAuthCode"
          horizontal
          :items="topMenuItems"
          label="FoolFrame menu"
          :sub-items="subMenuItems"
          @select="openShellMenu"
        />
        <nav class="nav-list nav-list-horizontal" aria-label="Safe logout">
          <button type="button" :disabled="Boolean(pendingAction)" @click="logout">安全退出</button>
        </nav>
      </div>

      <ShellActions
        v-if="token"
        class="shell-header-actions"
        :active-message="activeShellMessage"
        :user-avatar="shellUserAvatar"
        :user-name="shellUserName"
        @dismiss-message="activeShellMessage = null"
        @open-message="openShellMessage"
      />
    </header>

    <main class="workspace">
      <header class="topbar">
        <Button class="mobile-menu-button" icon="pi pi-bars" severity="secondary" text aria-label="打开导航" @click="mobileMenuOpen = true" />
      </header>

      <Drawer v-model:visible="mobileMenuOpen" position="left" class="mobile-navigation" header="导航">
        <h2 class="brand drawer-brand">
          {{ shellAppName }}
          <small v-if="shellAppVersion">{{ shellAppVersion }}</small>
        </h2>
        <nav class="nav-list" aria-label="Mobile main">
          <button type="button" @click="openMobilePrimarySection">首页</button>
        </nav>
        <LegacyMenuNav
          :disabled="Boolean(pendingAction)"
          :expanded-auth-code="subMenuParentAuthCode"
          :items="topMenuItems"
          label="Mobile FoolFrame menu"
          :sub-items="subMenuItems"
          @select="openMobileShellMenu"
        />
        <nav class="nav-list" aria-label="Mobile safe logout">
          <button type="button" :disabled="Boolean(pendingAction)" @click="logout">安全退出</button>
        </nav>
      </Drawer>

      <section class="view-workflow" :class="{ 'metadata-only': isMetadataOnlyView || isStandaloneDetail || isUnsupportedView }" aria-label="View workflow">
        <p v-if="showUnconfiguredHome" class="home-empty-state">{{ unconfiguredHomeMessage }}</p>
        <ViewListPanel
          v-if="!showUnconfiguredHome && !isMetadataOnlyView && !isStandaloneDetail"
          v-model:keyword="viewKeyword"
          :page-size="pageSize"
          :data="dataResponse?.data"
          :disabled="Boolean(pendingAction)"
          :error-message="errorMessage"
          :navigation-revision="viewNavigationRevision"
          :page-index="pageIndex"
          :panel-data="sudokuPanelData"
          :view="viewResponse?.data"
          @new-object="openNewObject"
          @page="loadResultPage"
          @refresh-panel="refreshSudokuPanel"
          @search="searchCurrentView"
          @select="openListObject"
          @toggle-report="showViewReport = !showViewReport"
        />

        <ViewDetailPanel
          v-if="!isUnsupportedView && (isMetadataOnlyView || isStandaloneDetail)"
          :candidate-columns="candidateColumns"
          :candidate-rows="candidateRows"
          :candidate-state="candidateState"
          :candidate-view-loading="candidateViewLoading"
          :child-draft-value="childDraftValue"
          :detail-drafts="detailDrafts"
          :detail-item-groups="detailItemGroups"
          :detail-rows="detailRows"
          :detail-view-operations="detailViewOperations"
          :enum-field-options="fieldEnumOptions"
          :error-message="errorMessage"
          :field-editor-context="fieldEditorContext"
          :is-creating-object="isCreatingObject"
          :is-pending-added-item="isPendingAddedDetailItem"
          :info-message="infoMessage"
          :load-existing-detail-view="loadExistingDetailView"
          :operation-result="operationResult"
          :pending="Boolean(pendingAction)"
          :schema-only="isMetadataOnlyView"
          :save-dialog-visible="saveDialogVisible"
          :saving="savingDetail"
          :selected-object-id="selectedObjectId"
          :title="detailTitle"
          :view-can-edit="viewCanEdit"
          @add-detail-item="addDetailItem"
          @add-existing-detail-item="addExistingDetailItem"
          @delete-detail-item="deleteDetailItem"
          @dismiss-error="errorMessage = ''"
          @dismiss-info="infoMessage = ''"
          @dismiss-operation-result="operationResult = null"
          @load-candidate-page="loadCandidatePage"
          @query-existing-detail-items="queryExistingDetailItems"
          @run-view-operation="runViewOperation"
          @save-selected-object="saveSelectedObject"
          @save-dialog-hidden="finishSaveNavigation"
          @set-child-draft-value="setChildDraftValue"
          @update-candidate-keyword="updateCandidateKeyword"
          @update-detail-draft="(key, value) => detailDrafts[key] = value"
          @update-detail-item="updateDetailItem"
        />
        <ViewReportPanel
          v-if="showViewReport && isListView && !isMetadataOnlyView && !isStandaloneDetail && currentViewId"
          :key="currentViewId"
          :pending="Boolean(pendingAction)"
          :run-action="runAction"
          :token="token"
          :view-id="currentViewId"
          @close="showViewReport = false"
        />
      </section>

    </main>
    <footer v-if="shellAppPowerBy" class="shell-footer">&copy; {{ shellAppPowerBy }}</footer>
  </div>
</template>
