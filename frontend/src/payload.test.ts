import { describe, expect, it } from "vitest";
import apiSource from "./api.ts?raw";
import nginxConfig from "../nginx.conf?raw";
import viteConfig from "../vite.config.ts?raw";
import appSource from "./App.vue?raw";
import listDataTableSource from "./ListDataTable.vue?raw";
import metadataFieldEditorSource from "./MetadataFieldEditor.vue?raw";
import payloadSource from "./payload.ts?raw";
import resultsPanelSource from "./ResultsPanel.vue?raw";
import sudokuPanelsSource from "./SudokuPanels.vue?raw";
import viewDetailPanelSource from "./ViewDetailPanel.vue?raw";
import viewShellSource from "./viewShell.ts?raw";
import viewDataWorkflowSource from "./useViewDataWorkflow.ts?raw";
import viewWorkflowSource from "./viewWorkflow.ts?raw";
import {
  buildGetEnumRequest,
  buildInputQueryRequest,
  buildLegacyListViewRequest,
  buildLegacyQueryDataRequest,
  buildLegacyReadItemViewRequest,
  buildMakeReportRequest,
  buildInitNewRequest,
  buildQueryDataDetailRequest,
  buildRunOperationRequest,
  buildSaveObjRequest,
  buildSaveNewObjRequest,
  buildTokenRequest
} from "./payload";

describe("App defaults", () => {
  it("opens with a metadata-driven view workflow before API tools", () => {
    expect(appSource).toContain('const activeSection = ref("views")');
    expect(appSource).toContain("onMounted");
    expect(appSource).toContain("void loadViewWorkflow()");
    expect(appSource).toContain("View workflow");
    expect(appSource).toContain("Load View");
    expect(appSource).toContain("await loadLegacyListView()");
    expect(appSource).toContain("await queryCurrentViewData()");
    expect(appSource).toContain("useViewDataWorkflow");
    expect(appSource).toContain('v-model.number="legacyListViewId"');
    expect(appSource).toContain("New Row");
    expect(viewDetailPanelSource).toContain("Create Row");
    expect(viewDetailPanelSource).toContain("Save Row");
    expect(appSource).toContain("async function selectObject");
    expect(appSource).toContain("async function startNewObject");
    expect(appSource).toContain("async function addDetailItem");
    expect(appSource).toContain("async function updateDetailItem");
    expect(appSource).toContain("async function deleteDetailItem");
    expect(appSource).toContain("resultColumns");
    expect(appSource).toContain("detailDrafts");
    expect(appSource).toContain("childDrafts");
    expect(listDataTableSource).toContain("rowValue(row, column)");
    expect(appSource).toContain("detailTitle");
    expect(appSource).toContain(':title="detailTitle"');
    expect(viewDetailPanelSource).toContain("<h2>{{ title }}</h2>");
    expect(viewDetailPanelSource).not.toContain("<h2>Detail</h2>");
  });

  it("routes every view row table through the shared metadata renderer", () => {
    expect(viewDetailPanelSource).toContain(':row-operations="[]"');
    expect(viewDetailPanelSource).toContain('default-action-label="Select"');
    expect(resultsPanelSource).toContain(':show-default-action="false"');
    expect(appSource).not.toContain("rowValue(row, column)");
    expect(listDataTableSource).toContain("defaultActionLabel");
    expect(listDataTableSource).toContain("showDefaultAction");
  });

  it("does not render data rows before View columns exist", () => {
    expect(listDataTableSource).toContain('v-if="columns.length"');
    expect(listDataTableSource).not.toContain("columns.length || rows.length");
  });

  it("does not keep generic record-map table helpers", () => {
    expect(viewWorkflowSource).not.toContain("recordColumns");
    expect(viewWorkflowSource).not.toContain("recordRowKey");
  });

  it("renders detail View operations from the loaded detail payload", () => {
    expect(viewDetailPanelSource).toContain("View Operations");
    expect(appSource).toContain("dataOperations(detailResponse.value?.data)");
    expect(appSource).toContain("detailViewOperations");
    expect(appSource).toContain('@run-view-operation="runViewOperation"');
    expect(viewDetailPanelSource).toContain("operationParams(operation)");
    expect(viewDetailPanelSource).toContain("operationParamKey(param, index)");
    expect(viewDetailPanelSource).toContain("operationParamLabel(param)");
    expect(viewDetailPanelSource).not.toContain("operation.params");
    expect(viewDetailPanelSource).not.toContain("param.paramName");
  });

  it("renders row operations through their target detail View id", () => {
    expect(appSource).toContain("listRowOperations");
    expect(appSource).toContain(':row-operations="listRowOperations"');
    expect(listDataTableSource).toContain("rowOperations");
    expect(listDataTableSource).toContain("emit('select', row, operationTargetViewId(operation))");
    expect(listDataTableSource).toContain("operationTargetViewId(operation) <= 0");
  });

  it("initializes child add drafts as soon as read-item View metadata loads", () => {
    const readViewSource = appSource.slice(
      appSource.indexOf("async function loadReadItemView"),
      appSource.indexOf("async function queryCurrentViewData")
    );

    expect(viewDataWorkflowSource).toContain("readItemViewResponse.value = response");
    expect(readViewSource).toContain("loadReadItemViewBase(viewId)");
    expect(readViewSource).toContain("syncDetailDrafts()");
  });

  it("renders list paging from legacy querydata totals", () => {
    expect(appSource).toContain("resultTotalItems");
    expect(appSource).toContain("resultFreshTime");
    expect(appSource).toContain("loadResultPage(resultPageIndex + 1)");
    expect(appSource).toContain("Page {{ resultPageIndex }} / {{ resultTotalPages || 1 }}");
    expect(appSource).toContain("Updated {{ resultFreshTime }}");
  });

  it("renders the legacy viewWithChart template as data and chart panes", () => {
    expect(appSource).toContain("viewUsesChartTemplate(viewResponse.value?.data)");
    expect(appSource).toContain("legacyChartData(resultRows.value)");
    expect(appSource).toContain("activeViewPane");
    expect(appSource).toContain('class="view-template-tabs"');
    expect(appSource).toContain('class="legacy-chart-pane"');
  });

  it("renders the legacy Sudoku template from ViewFile panels", () => {
    expect(appSource).toContain("viewUsesSudokuTemplate(viewResponse.value?.data)");
    expect(appSource).toContain("SudokuPanels");
    expect(appSource).toContain("sudokuPanelKind(panel)");
    expect(appSource).toContain("loadSudokuPanels");
    expect(appSource).toContain("loadViewDataById(panelViewId, \"sudoku-panel\", 5)");
    expect(sudokuPanelsSource).toContain('class="sudoku-grid"');
  });

  it("loads Sudoku child panels without requiring root querydata", () => {
    const start = appSource.indexOf("async function queryCurrentViewData");
    const source = appSource.slice(start, appSource.indexOf("async function loadSudokuPanels", start));
    expect(source).toContain("if (isSudokuView.value)");
    expect(source.indexOf("await loadSudokuPanels()")).toBeLessThan(source.indexOf("queryCurrentViewDataBase()"));
  });

  it("renders Sudoku linechart panels from child row chart items", () => {
    expect(sudokuPanelsSource).toContain("sudokuPanelKind(panel) === 'linechart'");
    expect(sudokuPanelsSource).toContain("sudokuPanelChart(panel).series");
    expect(sudokuPanelsSource).toContain("sudokuPanelChartMax(panel)");
  });

  it("renders Sudoku map panels from child row map items", () => {
    expect(sudokuPanelsSource).toContain("sudokuPanelKind(panel) === 'map'");
    expect(sudokuPanelsSource).toContain("sudokuPanelMarkers(panel)");
  });

  it("renders Sudoku item panels from legacy detail SimpleData", () => {
    const sudokuLoadSource = appSource.slice(
      appSource.indexOf("async function loadSudokuPanel"),
      appSource.indexOf("function sudokuPanelResult")
    );

    expect(sudokuPanelsSource).toContain("sudokuPanelKind(panel) === 'item'");
    expect(sudokuPanelsSource).toContain("sudokuPanelItemFields(panel)");
    expect(sudokuLoadSource).toContain('sudokuPanelKind(panel) !== "item"');
    expect(sudokuLoadSource).toContain("/api/v1/data/querydatadetail");
    expect(sudokuLoadSource).toContain("buildQueryDataDetailRequest");
    expect(sudokuPanelsSource).toContain("legacyItemDetailFields");
    expect(sudokuPanelsSource).not.toContain("legacyItemFields");
  });

  it("loads and renders Sudoku group child list panels", () => {
    expect(appSource).toContain("sudokuPanelKind(panel) === \"group\"");
    expect(appSource).toContain("for (const childPanel of sudokuGroupPanels(panel))");
    expect(appSource).toContain("sudokuPanelListViewType(childPanel) !== 0");
    expect(appSource).toContain("sudokuPanelData.value = { ...loaded }");
    expect(sudokuPanelsSource).toContain("sudokuPanelKind(panel) === 'group'");
    expect(sudokuPanelsSource).toContain("简单项");
  });

  it("resets the main View search to the first page", () => {
    expect(appSource).toContain("async function loadViewWorkflow(resetPage = false)");
    expect(appSource).toContain("pageIndex.value = 1");
    expect(appSource).toContain('@click="loadViewWorkflow(true)"');
  });

  it("keeps the Vue workspace on view-id driven legacy view and data APIs", () => {
    expect(viewDataWorkflowSource).toContain("/api/v1/view/getlistview");
    expect(viewDataWorkflowSource).toContain("/api/v1/data/querydata");
    expect(viewDataWorkflowSource).toContain("viewDetailViewId(view, loadedViewId)");
    expect(appSource).toContain("async function selectObject(row: ListDataItem, viewId = Number(detailViewId.value))");
    expect(appSource).toContain("async function startNewObject(viewId = Number(detailViewId.value)");
    expect(appSource).toContain("await queryDetail(Number(detailViewId.value))");
    expect(appSource).toContain("saveViewId.value = String(detailViewId.value)");
    expect(viewDataWorkflowSource).toContain("listRenderColumns(viewResponse.value?.data)");
    expect(appSource).not.toContain("Object.keys(first)");
    expect(appSource).not.toContain("viewName: viewName.value");
    expect(appSource).not.toContain("/api/v1/view/get-view");
    expect(appSource).not.toContain("/api/v1/data/query-list");
    expect(appSource).not.toContain("buildQueryRequest");
  });

  it("passes the main View filter as legacy querydata QueryFilter", () => {
    const querySource = viewDataWorkflowSource.slice(
      viewDataWorkflowSource.indexOf("async function queryLoadedViewData"),
      viewDataWorkflowSource.indexOf("async function queryLegacyData")
    );

    expect(appSource).toContain('v-model="legacyQueryFilter"');
    expect(querySource).toContain("queryFilter: options.queryFilter.value");
    expect(querySource).not.toContain("keyword:");
  });

  it("does not let querydata define table columns when View columns are absent", () => {
    const columnsSource = viewWorkflowSource.slice(
      viewWorkflowSource.indexOf("export function listRenderColumns"),
      viewWorkflowSource.indexOf("export function fieldModelId")
    );
    const childSource = appSource.slice(
      appSource.indexOf("async function loadExistingDetailItems"),
      appSource.indexOf("async function addExistingDetailItem")
    );

    expect(columnsSource).toContain("if (!view)");
    expect(columnsSource).toContain("return viewColumns(view)");
    expect(columnsSource).not.toContain("columnsFromListResult");
    expect(columnsSource).not.toContain("columnsFromRowItems");
    expect(childSource).toContain("setCandidateResults(group, viewColumns(view.data), listRows(data.data)");
    expect(childSource).not.toContain("columnsFromListResult");
    expect(childSource).not.toContain("declaredColumns.length ? declaredColumns : resultColumns");
  });

  it("keeps enum option lookup on the shared field metadata helper", () => {
    expect(viewShellSource).toContain("fieldModelId(field)");
    expect(viewShellSource).not.toContain("field.prpModelId");
    expect(viewShellSource).not.toContain("field.PrpModelId");
  });

  it("renders select-existing child controls through the shared group helper", () => {
    expect(viewDetailPanelSource).toContain("groupSelectFromExists(group)");
    expect(viewDetailPanelSource).not.toContain("group.selectFromExists");
  });

  it("renders child group labels and rows through shared group helpers", () => {
    expect(viewDetailPanelSource).toContain("groupTitle(group)");
    expect(viewDetailPanelSource).toContain("groupItems(group).length");
    expect(viewDetailPanelSource).toContain("v-for=\"item in groupItems(group)\"");
    expect(viewDetailPanelSource).toContain("itemDataId(item)");
    expect(viewDetailPanelSource).not.toContain("group.itemName");
    expect(viewDetailPanelSource).not.toContain("group.items");
    expect(viewDetailPanelSource).not.toContain("item.dataId");
  });

  it("refreshes the main View workflow from legacy AutoFreshTime", () => {
    expect(appSource).toContain("listAutoFreshTime");
    expect(appSource).toContain("scheduleAutoRefresh(response.data)");
    expect(appSource).toContain("window.setInterval");
    expect(appSource).toContain("pageIndex.value = 1");
    expect(appSource).toContain('activeSection.value === "views"');
    expect(appSource).toContain("onUnmounted(stopAutoRefresh)");
  });

  it("keeps metadata lookup tied to the rendered view id", () => {
    expect(metadataFieldEditorSource).toContain("viewId: props.viewId");
    expect(metadataFieldEditorSource).not.toContain("viewName");
  });

  it("passes parent context into child lookup editors", () => {
    expect(metadataFieldEditorSource).toContain("ownerId: props.ownerId");
    expect(viewDetailPanelSource).toContain(':owner-id="selectedObjectId"');
    expect(viewDetailPanelSource).toContain(':is-added="true"');
    expect(viewDetailPanelSource).toContain(':object-id="itemDataId(item)"');
  });

  it("renders native metadata inputs for field-specific widgets", () => {
    expect(metadataFieldEditorSource).toContain("fieldInputChecked");
    expect(metadataFieldEditorSource).toContain("fieldInputType");
    expect(metadataFieldEditorSource).toContain("fieldInputValue");
    expect(metadataFieldEditorSource).toContain("isMultilineField");
    expect(metadataFieldEditorSource).toContain(':checked="fieldInputChecked(field, value)"');
    expect(metadataFieldEditorSource).toContain(':type="fieldInputType(field)"');
    expect(metadataFieldEditorSource).toContain(':value="fieldInputValue(field, value)"');
    expect(metadataFieldEditorSource).toContain('<textarea v-else-if="isMultilineField(field)"');
    expect(metadataFieldEditorSource).toContain("HTMLTextAreaElement");
  });

  it("does not keep ViewName as a frontend lookup or workflow shortcut", () => {
    const inputQueryRequestSource = apiSource.slice(
      apiSource.indexOf("export interface InputQueryRequest"),
      apiSource.indexOf("export interface InputQueryItem")
    );

    expect(appSource).not.toContain("const viewName = ref");
    expect(appSource).not.toContain("viewName.value");
    expect(payloadSource).not.toContain("viewName?: string");
    expect(payloadSource).not.toContain("request.viewName");
    expect(inputQueryRequestSource).not.toContain("viewName");
  });

  it("loads the View definition before the API-tool data query", () => {
    const querySource = viewDataWorkflowSource.slice(
      viewDataWorkflowSource.indexOf("async function queryLegacyData"),
      viewDataWorkflowSource.indexOf("async function queryCurrentViewData")
    );

    expect(querySource.indexOf("await loadLegacyListView()")).toBeGreaterThanOrEqual(0);
    expect(querySource.indexOf("await loadLegacyListView()")).toBeLessThan(querySource.indexOf("queryLoadedViewData("));
    expect(querySource).toContain("queryLoadedViewData(");
    expect(querySource).not.toContain("viewId: Number(options.queryViewId.value)");
  });

  it("loads the rendered View before the current data query", () => {
    const querySource = viewDataWorkflowSource.slice(
      viewDataWorkflowSource.indexOf("async function queryCurrentViewData"),
      viewDataWorkflowSource.indexOf("function readItemViewFor")
    );

    expect(querySource).toContain("viewId(viewResponse.value.data) !== requestedViewId");
    expect(querySource.indexOf("await loadLegacyListView()")).toBeGreaterThanOrEqual(0);
    expect(querySource.indexOf("await loadLegacyListView()")).toBeLessThan(querySource.indexOf("queryLoadedViewData("));
    expect(querySource).toContain("const loadedViewId = Number(currentViewId.value)");
  });

  it("loads the read-item View before detail data and aborts if it cannot render", () => {
    const detailSource = appSource.slice(
      appSource.indexOf("async function queryDetail"),
      appSource.indexOf("async function initNew")
    );

    expect(detailSource).toContain("const readView = await loadReadItemView(viewId)");
    expect(detailSource.indexOf("const readView = await loadReadItemView(viewId)")).toBeLessThan(
      detailSource.indexOf("/api/v1/data/querydatadetail")
    );
    expect(detailSource).toContain("if (!readView?.data) return null");
  });

  it("renders detail tool tables from read-item View metadata, not raw DTO rows", () => {
    expect(appSource).toContain("renderedDetailFields(currentReadItemView.value");
    expect(appSource).toContain("renderedDetailFields(currentInitNewReadItemView.value");
    expect(appSource).not.toContain('v-if="detailDataRows.length"');
    expect(appSource).not.toContain('v-for="item in detailDataRows"');
  });

  it("keeps child update fallback drafts on rendered group columns", () => {
    const updateSource = appSource.slice(
      appSource.indexOf("async function updateDetailItem"),
      appSource.indexOf("async function deleteDetailItem")
    );

    expect(updateSource).toContain("buildGroupItemDrafts(group, item)");
    expect(updateSource).not.toContain("buildFieldDrafts(detailItemValues(item))");
  });

  it("keeps read-item View state keyed by the rendered View id", () => {
    expect(viewDataWorkflowSource).toContain("const readItemViews = ref<Record<number, ReadItemViewInfo>>({})");
    expect(viewDataWorkflowSource).toContain("readViewForId(readItemViews.value, Number(viewId))");
    expect(viewDataWorkflowSource).toContain("readItemViews.value = rememberReadView(readItemViews.value, viewId, response.data)");
    expect(appSource).toContain("readItemViewFor(Number(detailViewId.value))");
    expect(appSource).toContain("readItemViewFor(Number(initNewViewId.value))");

    const createSource = appSource.slice(
      appSource.indexOf("async function startNewObject"),
      appSource.indexOf("async function saveSelectedObject")
    );
    expect(createSource).toContain("detailViewId.value = viewId");
    expect(createSource).toContain("detailResponse.value = null");
  });

  it("does not bootstrap View/data rendering from the seeded business View", () => {
    expect(appSource).not.toContain("ref(100)");
    expect(viewDataWorkflowSource).toContain("const currentViewId = computed(() => viewId(viewResponse.value?.data))");
    expect(viewDataWorkflowSource).not.toContain("viewId(viewResponse.value?.data, options.listViewId.value)");
  });

  it("loads the default first-screen View from the legacy app shell", () => {
    const workflowSource = appSource.slice(
      appSource.indexOf("async function loadViewWorkflow"),
      appSource.indexOf("onMounted(()")
    );
    const shellSource = appSource.slice(
      appSource.indexOf("async function ensureLegacyShell"),
      appSource.indexOf("async function loadViewWorkflow")
    );

    expect(appSource).toContain('const password = ref("admin")');
    expect(appSource).toContain("async function ensureLegacySession");
    expect(shellSource.indexOf("await ensureLegacySession()")).toBeGreaterThanOrEqual(0);
    expect(shellSource.indexOf("await ensureLegacySession()")).toBeLessThan(shellSource.indexOf("await loadMainInfo()"));
    expect(appSource).toContain("legacyAppDefaultViewId");
    expect(appSource).toContain("applyDefaultAppView(response.data)");
    expect(shellSource.indexOf("await loadMainInfo()")).toBeGreaterThanOrEqual(0);
    expect(workflowSource).toContain("if (!(await ensureLegacyShell())) return");
    expect(workflowSource.indexOf("await ensureLegacyShell()")).toBeLessThan(workflowSource.indexOf("await loadLegacyListView()"));
  });

  it("starts old Web /view:id paths through the same View-first workflow", () => {
    expect(appSource).toContain("legacyViewPathId(window.location.pathname)");
    expect(appSource).toContain("if (routeViewId) applyRequestedViewId(routeViewId)");
    expect(appSource).toContain("if (defaultViewId && !legacyListViewId.value) applyRequestedViewId(defaultViewId)");
  });

  it("starts old Web detail and new paths through existing View-first detail flows", () => {
    expect(appSource).toContain("legacyDetailPath(window.location.pathname)");
    expect(appSource).toContain("void loadLegacyDetailPath(detailRoute)");
    expect(appSource).toContain("await queryDetail(route.viewId)");
    expect(appSource).toContain("legacyItemViewPathId(window.location.pathname)");
    expect(appSource).toContain("void loadLegacyItemView(itemViewId)");
    expect(appSource).toContain("legacyNewPath(window.location.pathname)");
    expect(appSource).toContain("void loadLegacyNewPath(newRoute)");
    expect(appSource).toContain("await startNewObject(route.viewId, route.parentObjId, route.ownerViewId, route.property)");
    expect(appSource).toContain('async function startNewObject(viewId = Number(detailViewId.value), parentObjId = "", ownerViewId = "", property = "")');
    expect(appSource).toContain("saveNewOwnerViewId.value = ownerViewId");
    expect(appSource).toContain("saveNewOwnerId.value = parentObjId");
    expect(appSource).toContain("saveNewProperty.value = property");
    expect(appSource).not.toContain('saveNewOwnerViewId.value = ""');
  });

  it("renders /itemview:id from View metadata without querying an empty object", () => {
    const itemViewSource = appSource.slice(
      appSource.indexOf("async function loadLegacyItemView"),
      appSource.indexOf("async function loadLegacyNewPath")
    );

    expect(itemViewSource).toContain("await loadReadItemView(viewId)");
    expect(itemViewSource).not.toContain("queryDetail");
    expect(itemViewSource).not.toContain("querydatadetail");
    expect(appSource).toContain(':schema-only="isMetadataOnlyView"');
    expect(viewDetailPanelSource).toContain("View definition loaded.");
  });

  it("retries the first-screen legacy shell after a stale stored token", () => {
    const shellSource = appSource.slice(
      appSource.indexOf("async function ensureLegacyShell"),
      appSource.indexOf("async function loadViewWorkflow")
    );

    expect(shellSource).toContain("if (await loadMainInfo()) return true");
    expect(shellSource).toContain("token.value = \"\"");
    expect(shellSource).toContain('localStorage.removeItem("fool-service-token")');
    expect(shellSource).toContain("return (await ensureLegacySession()) && Boolean(await loadMainInfo())");
  });

  it("renders legacy shell menu entries and opens their View ids", () => {
    const menuSource = appSource.slice(
      appSource.indexOf("async function openShellMenu"),
      appSource.indexOf("async function loadLegacyListView")
    );

    expect(appSource).toContain("legacyMainMenuItems(mainInfoResponse.value?.data)");
    expect(appSource).toContain("shellMenuItems");
    expect(appSource).toContain('@click="openShellMenu(item)"');
    expect(menuSource).toContain("legacyAuthViewId(item)");
    expect(menuSource).toContain("legacyListViewId.value = itemViewId");
    expect(menuSource).not.toContain("legacyQueryViewId.value = itemViewId");
    expect(menuSource).toContain("await loadViewWorkflow(true)");
    expect(menuSource).toContain("subMenuParentAuthCode.value = authNo");
    expect(menuSource).toContain("await loadSubMenu()");
  });

  it("does not prefill business-specific data DTO fields by default", () => {
    expect(appSource).toContain('const enumModelId = ref("")');
    expect(appSource).toContain('const legacyQueryFilter = ref("")');
    expect(appSource).toContain('const detailObjId = ref("")');
    expect(appSource).toContain('const saveObjId = ref("")');
    expect(appSource).toContain('const saveNewObjId = ref("")');
    expect(appSource).toContain('const operationObjectId = ref("")');
    expect(appSource).toContain("const operationId = ref(0)");
    expect(appSource).toContain('const savePropertyiesJson = ref("[]")');
    expect(appSource).toContain('const saveNewPropertyiesJson = ref("[]")');
    expect(appSource).not.toContain('order_state="0"');
    expect(appSource).not.toContain("BTC-USDT");
    expect(appSource).not.toContain('const enumModelId = ref("102")');
    expect(appSource).not.toContain("const operationId = ref(7001)");
    expect(appSource).not.toContain('const detailObjId = ref("1001")');
    expect(appSource).not.toContain('const saveObjId = ref("1001")');
    expect(appSource).not.toContain('const saveNewObjId = ref("9001")');
    expect(appSource).not.toContain('const operationObjectId = ref("1001")');
  });

  it("does not expose the backend seed DTO smoke route in the Vue workspace", () => {
    expect(appSource).not.toContain("Backend Smoke");
    expect(appSource).not.toContain('fetch("/test")');
    expect(appSource).not.toContain("backendSmokeResponse");
  });

  it("exposes the legacy report grid route in the Vue console", () => {
    expect(appSource).toContain("Report Grid");
    expect(appSource).toContain("/api/v1/report/makereport");
    expect(appSource).toContain("/api/v1/report/getrpt");
    expect(appSource).toContain("Get Report");
    expect(appSource).toContain("reportResponse");
  });

  it("exposes the legacy report column candidate route in the Vue console", () => {
    expect(appSource).toContain("Report Columns");
    expect(appSource).toContain("/api/v1/report/getmkqview");
    expect(appSource).toContain("reportModelResponse");
    expect(appSource).toContain("buildReportColsFromModel");
    expect(appSource).toContain("reportColsJson.value = JSON.stringify");
  });

  it("exposes the legacy save report definition route in the Vue console", () => {
    expect(appSource).toContain("Save Report Definition");
    expect(appSource).toContain("/api/v1/report/saverpt");
    expect(appSource).toContain("saveReportResponse");
  });

  it("exposes the legacy message polling route in the Vue console", () => {
    expect(appSource).toContain("Messages");
    expect(appSource).toContain("/api/v1/message/getmsg");
    expect(appSource).toContain("messageResponse");
  });

  it("exposes the legacy notify count route in the Vue console", () => {
    expect(appSource).toContain("Notify Counts");
    expect(appSource).toContain("/api/v1/message/getnotify");
    expect(appSource).toContain("notifyResponse");
  });

  it("exposes the legacy user info route in the Vue console", () => {
    expect(appSource).toContain("Legacy User Info");
    expect(appSource).toContain("/api/v1/auth/getuserinfo");
    expect(appSource).toContain("legacyUserInfoResponse");
  });

  it("exposes the legacy checkcode routes in the Vue console", () => {
    expect(appSource).toContain("Check Code");
    expect(appSource).toContain("/api/v1/auth/getcheckcode");
    expect(appSource).toContain("/api/v1/auth/checkcode");
    expect(appSource).toContain("checkCodeResponse");
  });

  it("exposes the legacy loginv2 route in the Vue console", () => {
    expect(appSource).toContain("Legacy Login V2");
    expect(appSource).toContain("/api/v1/auth/loginv2");
    expect(appSource).toContain("legacyLoginResponse");
  });

  it("exposes the legacy initapp route in the Vue console", () => {
    expect(appSource).toContain("Init App");
    expect(appSource).toContain("/api/v1/auth/initapp");
    expect(appSource).toContain("initAppResponse");
  });

  it("exposes the legacy submenu route in the Vue console", () => {
    expect(appSource).toContain("Sub Menu");
    expect(appSource).toContain("/api/v1/auth/getsubmenu");
    expect(appSource).toContain("subMenuResponse");
  });

  it("exposes the legacy main-info route in the Vue console", () => {
    expect(appSource).toContain("Main Info");
    expect(appSource).toContain("/api/v1/auth/getmain");
    expect(appSource).toContain("mainInfoResponse");
  });

  it("exposes the legacy app-info route in the Vue console", () => {
    expect(appSource).toContain("App Info");
    expect(appSource).toContain("/api/v1/auth/getapp");
    expect(appSource).toContain("appInfoResponse");
  });

  it("exposes the legacy initnew route in the Vue console", () => {
    expect(appSource).toContain("Init New Object");
    expect(appSource).toContain("/api/v1/data/initnew");
    expect(appSource).toContain("initNewResponse");
  });

  it("exposes the legacy savenewobj route in the Vue console", () => {
    expect(appSource).toContain("Save New Object");
    expect(appSource).toContain("/api/v1/data/savenewobj");
    expect(appSource).toContain("saveNewObjResponse");
  });

  it("exposes the legacy runoperation route in the Vue console", () => {
    expect(appSource).toContain("Run Operation");
    expect(appSource).toContain("/api/v1/data/runoperation");
    expect(appSource).toContain("runOperationResponse");
  });

  it("keeps the frontend proxy surface on migrated API routes", () => {
    expect(viteConfig).not.toContain('"/test"');
    expect(nginxConfig).not.toContain("location /test");
    expect(viteConfig).toContain('"/api"');
    expect(nginxConfig).toContain("location /api/");
  });

});

describe("buildTokenRequest", () => {
  it("matches the common token-only request DTO shape", () => {
    const request = buildTokenRequest(" token-1 ");

    expect(request).toEqual({
      token: "token-1"
    });
  });
});

describe("buildInputQueryRequest", () => {
  it("matches the legacy inputquery DTO shape", () => {
    const request = buildInputQueryRequest({
      token: "token-1",
      viewId: 100,
      viewItemId: "name",
      text: "  Ada  ",
      objID: "1001",
      ownerId: "5001",
      isAdded: true
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      viewItemId: "name",
      text: "Ada",
      objID: "1001",
      ownerId: "5001",
      isAdded: true
    });
  });

  it("includes the current view id for view-driven lookup", () => {
    const request = buildInputQueryRequest({
      token: "token-1",
      viewId: 100,
      viewItemId: "customer",
      text: "Ad"
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      viewItemId: "customer",
      text: "Ad",
      isAdded: false
    });
  });
});

describe("buildSaveObjRequest", () => {
  it("matches the legacy saveobj DTO shape", () => {
    const request = buildSaveObjRequest({
      token: "token-1",
      id: " 1001 ",
      viewID: " 100 ",
      propertyiesJson: "[{\"key\":\"name\",\"value\":\"Sample\"},{\"key\":\"state\",\"value\":\"0\"}]",
      itempropertiesJson:
        "[{\"key\":\"children\",\"items\":[{\"itemId\":\"2001\",\"isExist\":true,\"propertyies\":[{\"key\":\"childName\",\"value\":\"Updated child\"}]}]}]"
    });

    expect(request).toEqual({
      token: "token-1",
      saveObj: {
        id: "1001",
        viewID: "100",
        propertyies: [
          { key: "name", value: "Sample" },
          { key: "state", value: "0" }
        ],
        itemproperties: [
          {
            key: "children",
            items: [
              {
                itemId: "2001",
                isExist: true,
                propertyies: [{ key: "childName", value: "Updated child" }]
              }
            ]
          }
        ]
      }
    });
  });

});

describe("buildSaveNewObjRequest", () => {
  it("matches the legacy savenewobj DTO shape", () => {
    const request = buildSaveNewObjRequest({
      token: "token-1",
      id: " 2009 ",
      viewID: " 200 ",
      propertyiesJson: "[{\"key\":\"itemName\",\"value\":\"New child\"}]",
      ownerViewId: " 100 ",
      ownerId: " 1001 ",
      property: " items "
    });

    expect(request).toEqual({
      token: "token-1",
      saveObj: {
        id: "2009",
        viewID: "200",
        propertyies: [{ key: "itemName", value: "New child" }],
        itemproperties: []
      },
      ownerViewId: "100",
      ownerId: "1001",
      property: "items"
    });
  });
});

describe("buildRunOperationRequest", () => {
  it("matches the legacy runoperation DTO shape", () => {
    const request = buildRunOperationRequest({
      token: "token-1",
      objectId: " 1001 ",
      viewId: 100,
      operationId: 7001
    });

    expect(request).toEqual({
      token: "token-1",
      objectId: "1001",
      viewId: 100,
      operationId: 7001
    });
  });
});

describe("buildQueryDataDetailRequest", () => {
  it("matches the legacy querydatadetail DTO shape", () => {
    const request = buildQueryDataDetailRequest({
      token: "token-1",
      viewId: 100,
      objId: " 1001 ",
      idExp: " record_id "
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      objId: "1001",
      idExp: "record_id"
    });
  });
});

describe("buildInitNewRequest", () => {
  it("matches the legacy initnew DTO shape", () => {
    const request = buildInitNewRequest({
      token: "token-1",
      viewId: 100,
      parentObjId: " 5001 "
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      parentObjId: "5001"
    });
  });
});

describe("buildGetEnumRequest", () => {
  it("matches the legacy getenums DTO shape", () => {
    const request = buildGetEnumRequest({
      token: "token-1",
      modelId: " 100 "
    });

    expect(request).toEqual({
      token: "token-1",
      modelId: "100"
    });
  });
});

describe("buildLegacyListViewRequest", () => {
  it("matches the legacy getlistview DTO shape", () => {
    const request = buildLegacyListViewRequest({
      token: "token-1",
      viewId: 100
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100
    });
  });
});

describe("buildLegacyReadItemViewRequest", () => {
  it("matches the legacy getreaditemview DTO shape", () => {
    const request = buildLegacyReadItemViewRequest({
      token: "token-1",
      viewId: 100
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100
    });
  });
});

describe("buildLegacyQueryDataRequest", () => {
  it("matches the legacy querydata DTO shape", () => {
    const request = buildLegacyQueryDataRequest({
      token: "token-1",
      viewId: 100,
      pageSize: 10,
      pageIndex: 2,
      queryFilter: " record_state=\"0\" ",
      keyword: "  Ada  ",
      orderByItem: 1001,
      orderByType: 1
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      pageSize: 10,
      pageIndex: 2,
      queryFilter: "record_state=\"0\"",
      keyword: "Ada",
      orderByItem: 1001,
      orderByType: 1
    });
  });
});

describe("buildMakeReportRequest", () => {
  it("matches the legacy makereport DTO shape", () => {
    const request = buildMakeReportRequest({
      token: "token-1",
      viewId: 100,
      currentPage: 2,
      pageSize: 10,
      queryFilter: " record_state=\"0\" ",
      reportColsJson: "[{\"colName\":\"State\",\"index\":2},{\"colName\":\"Name\",\"index\":1}]",
      reportName: " View Daily "
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      currentPage: 2,
      pageSize: 10,
      queryFilter: "record_state=\"0\"",
      reportName: "View Daily",
      reportCols: [
        { colName: "State", index: 2 },
        { colName: "Name", index: 1 }
      ]
    });
  });
});
