import { describe, expect, it } from "vitest";
import apiSource from "./api.ts?raw";
import nginxConfig from "../nginx.conf?raw";
import viteConfig from "../vite.config.ts?raw";
import appSource from "./App.vue?raw";
import legacyChartPanelSource from "./LegacyChartPanel.vue?raw";
import legacyMapPanelSource from "./LegacyMapPanel.vue?raw";
import legacyMenuNavSource from "./LegacyMenuNav.vue?raw";
import listDataTableSource from "./ListDataTable.vue?raw";
import loginPanelSource from "./LoginPanel.vue?raw";
import metadataFieldEditorSource from "./MetadataFieldEditor.vue?raw";
import payloadSource from "./payload.ts?raw";
import reportConditionsSource from "./reportConditions.ts?raw";
import reportOutputSelectorSource from "./ReportOutputSelector.vue?raw";
import reportOutputsSource from "./reportOutputs.ts?raw";
import shellActionsSource from "./ShellActions.vue?raw";
import sudokuPanelsSource from "./SudokuPanels.vue?raw";
import sudokuWorkflowSource from "./useSudokuPanels.ts?raw";
import themeSource from "./theme.ts?raw";
import viewDetailPanelSource from "./ViewDetailPanel.vue?raw";
import viewListPanelSource from "./ViewListPanel.vue?raw";
import viewReportPanelSource from "./ViewReportPanel.vue?raw";
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
  it("uses the old Bootstrap primary colors instead of the invented indigo theme", () => {
    expect(themeSource).toContain('500: "#337ab7"');
    expect(themeSource).toContain('700: "#286090"');
    expect(themeSource).toContain('800: "#204d74"');
    expect(themeSource).not.toContain("{indigo.");
    expect(legacyMapPanelSource).toContain('fillColor: "#337ab7"');
    expect(legacyMapPanelSource).not.toContain('fillColor: "#6366f1"');
  });

  it("opens with the metadata-driven View workflow", () => {
    const mainViewSource = viewListPanelSource;

    expect(appSource).toContain("onMounted(() => void initializeApp())");
    expect(appSource).toContain("await loadInitialRoute()");
    expect(appSource).toContain("await loadViewWorkflow()");
    expect(appSource).toContain("View workflow");
    expect(mainViewSource).toContain("输入条件");
    expect(mainViewSource).toContain('class="list-query-input"');
    expect(mainViewSource).toContain('aria-label="查询条件"');
    expect(mainViewSource).not.toContain("<label>");
    expect(mainViewSource).toContain('label="查找"');
    expect(mainViewSource).toContain('label="统计"');
    expect(mainViewSource).not.toContain('icon="pi pi-search"');
    expect(mainViewSource).not.toContain('icon="pi pi-chart-bar"');
    expect(mainViewSource).not.toContain('icon="pi pi-plus"');
    expect(appSource).toContain('v-model:keyword="viewKeyword"');
    expect(mainViewSource).toContain('v-model="keyword"');
    expect(appSource).toContain("const pageSize = ref(10)");
    expect(appSource).toContain(':page-size="pageSize"');
    expect(mainViewSource).not.toContain("Page size");
    expect(mainViewSource).not.toContain("InputNumber");
    expect(mainViewSource).not.toContain("QueryFilter");
    expect(mainViewSource).not.toContain("View ID");
    expect(mainViewSource).not.toContain("Load View");
    expect(appSource).toContain("await loadLegacyListView()");
    expect(appSource).toContain("await queryCurrentViewData()");
    expect(appSource).toContain("useViewDataWorkflow");
    expect(mainViewSource).not.toContain("New Row");
    expect(viewDetailPanelSource).toContain('label="保存"');
    expect(viewDetailPanelSource).not.toContain("Create Row");
    expect(appSource).toContain("function openListObject");
    expect(appSource).toContain("async function startNewObject");
    expect(appSource).toContain("async function addDetailItem");
    expect(appSource).toContain("async function updateDetailItem");
    expect(appSource).toContain("async function deleteDetailItem");
    expect(appSource).toContain("viewColumns(viewResponse.value?.data)");
    expect(appSource).toContain("detailDrafts");
    expect(appSource).toContain("childDrafts");
    expect(listDataTableSource).toContain("rowValue(row, column)");
    expect(appSource).toContain("detailTitle");
    expect(appSource).toContain(':title="detailTitle"');
    expect(viewDetailPanelSource).toContain("<h2>{{ isCreatingObject ?");
    expect(viewDetailPanelSource).not.toContain("<h2>Detail</h2>");
  });

  it("renders each View title once without exposing the internal ViewName", () => {
    expect(viewListPanelSource).toContain("<h2>{{ title }}</h2>");
    expect(viewListPanelSource).not.toContain("{{ name }}");
    expect(appSource).not.toContain("pageViewName");
    expect(appSource).not.toContain("pageViewTitle");
    expect(viewDataWorkflowSource).not.toContain("loadedViewName");
    expect(viewDataWorkflowSource).not.toContain("const viewTitle");
  });

  it("routes every view row table through the shared metadata renderer", () => {
    expect(viewDetailPanelSource).toContain(':row-operations="[]"');
    expect(viewDetailPanelSource).toContain('default-action-label="选择"');
    expect(viewDetailPanelSource).toContain(':show-default-action="true"');
    expect(appSource).not.toContain("rowValue(row, column)");
    expect(listDataTableSource).toContain("defaultActionLabel");
    expect(listDataTableSource).toContain("showDefaultAction");
    expect(listDataTableSource).toContain("showDefaultAction: false");
    expect(listDataTableSource).toContain("<DataTable");
    expect(listDataTableSource).toContain("<Column v-for");
    expect(listDataTableSource).toContain('<Column v-if="rowOperations.length || showDefaultAction" header="操作">');
    expect(listDataTableSource).not.toContain('align-frozen="right"');
    expect(listDataTableSource).not.toContain('icon="pi pi-arrow-right"');
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
    const toolbarSource = viewDetailPanelSource.slice(
      viewDetailPanelSource.indexOf('class="detail-toolbar"'),
      viewDetailPanelSource.indexOf('class="detail-field-grid detail-field-edit"')
    );

    expect(appSource).toContain("dataOperations(detailResponse.value?.data)");
    expect(appSource).toContain("detailViewOperations");
    expect(appSource).toContain('@run-view-operation="runViewOperation"');
    expect(viewDetailPanelSource).toContain('class="detail-toolbar"');
    expect(viewDetailPanelSource).toContain('v-for="operation in detailViewOperations"');
    expect(viewDetailPanelSource).not.toContain("operationParams(operation)");
    expect(appSource).toContain("legacyRunOperationMessage(response.data)");
    expect(appSource).toContain("operationResult.value = { message, success }");
    expect(viewDetailPanelSource).toContain(":severity=\"operationResult.success ? 'success' : 'error'\"");
    expect(toolbarSource.match(/icon="pi pi-check"/g)).toHaveLength(2);
    expect(toolbarSource).not.toContain('icon="pi pi-save"');
    expect(toolbarSource).not.toContain('icon="pi pi-bolt"');
  });

  it("renders the old detail heading with object or create context", () => {
    expect(appSource).toContain('viewDisplayTitle(currentReadItemView.value, "详情")');
    expect(viewDetailPanelSource).toContain("`${title} -新建`");
    expect(viewDetailPanelSource).toContain("`${title} -${selectedObjectId}`");
    expect(viewDetailPanelSource).not.toContain("primevue/tag");
    expect(viewDetailPanelSource).not.toContain("未选择记录");
    expect(appSource).not.toContain('viewDisplayTitle(currentReadItemView.value, "Detail")');
  });

  it("keeps existing details read-only until CanEdit starts an edit session", () => {
    expect(appSource).toContain("dataCanEdit(detailResponse.value?.data)");
    expect(viewDetailPanelSource).toContain("const isEditing = ref(false)");
    expect(viewDetailPanelSource).toContain('label="编辑"');
    expect(viewDetailPanelSource).toContain('v-if="isEditing" class="detail-field-grid detail-field-edit"');
    expect(viewDetailPanelSource).toContain('v-if="!isEditing && (selectedObjectId || schemaOnly)" class="detail-field-grid"');
    expect(viewDetailPanelSource).toContain('v-if="!isEditing && (selectedObjectId || schemaOnly)"');
    expect(viewDetailPanelSource).toContain(':disabled="pending || isEditing"');
    expect(viewDetailPanelSource).toContain('v-if="!isCreatingObject"');
    expect(viewDetailPanelSource).toContain(':disabled="pending || !isEditing"');
    expect(viewDetailPanelSource).not.toContain('v-if="!isEditing"\n        type="button"\n        label="编辑"');
    expect(viewDetailPanelSource).not.toContain('v-else\n        type="button"\n        label="保存"');
    expect(viewDetailPanelSource).toContain("isEditing.value = props.isCreatingObject");
  });

  it("uses child DetailViewId for deep editing instead of inline editors", () => {
    expect(viewDetailPanelSource).toContain("groupDetailViewId(group)");
    expect(viewDetailPanelSource).toContain(':href="`/view${groupDetailViewId(group)}/${itemDataId(item)}`"');
    expect(viewDetailPanelSource).toContain('v-if="isEditing && !groupDetailViewId(group)"');
  });

  it("renders legacy detail collections as metadata-driven tabs and tables", () => {
    expect(viewDetailPanelSource).toContain('v-model:value="activeGroupKey"');
    expect(viewDetailPanelSource).toContain('<Tab v-for="group in detailItemGroups"');
    expect(viewDetailPanelSource).toContain('<TabPanel v-for="group in detailItemGroups"');
    expect(viewDetailPanelSource).toContain('class="legacy-item-table detail-items-grid"');
    expect(viewDetailPanelSource).toContain('<th v-for="field in groupColumns(group)"');
    expect(viewDetailPanelSource).toContain('<th colspan="2">操作</th>');
    expect(viewDetailPanelSource).toContain(':colspan="groupColumns(group).length + 2"');
    expect(viewDetailPanelSource).not.toContain('class="detail-item-actions"');
    expect(viewDetailPanelSource).toContain('<tr v-for="item in groupItems(group)"');
    expect(viewDetailPanelSource).not.toContain("<span>{{ groupItems(group).length }}</span>");
  });

  it("opens select-from-existing collection candidates in the legacy modal flow", () => {
    expect(viewDetailPanelSource).toContain('v-if="isEditing && !groupSelectFromExists(group)"');
    expect(viewDetailPanelSource).toContain('label="增加"');
    expect(viewDetailPanelSource).toContain("function openExistingPicker");
    expect(viewDetailPanelSource).toContain('class="detail-picker-dialog"');
    expect(viewDetailPanelSource).toContain("emit(\"loadExistingDetailItems\", group)");
    expect(viewDetailPanelSource).toContain("function selectExistingItem");
    expect(viewDetailPanelSource).toContain('@select="(row) => selectExistingItem(group, row)"');
  });

  it("renders row operations through their target detail View id", () => {
    expect(viewListPanelSource).toContain("rowOperations(operations.value)");
    expect(viewListPanelSource).toContain(':row-operations="rowItems"');
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
    expect(viewListPanelSource).toContain("resultTotalItems");
    expect(viewListPanelSource).toContain("resultFreshTime");
    expect(viewListPanelSource).toContain("event.page + 1");
    expect(viewListPanelSource).toContain("<Paginator");
    expect(viewListPanelSource).toContain("current-page-report-template");
    expect(viewListPanelSource).toContain("更新时间 {{ resultFreshTime }}");
    expect(listDataTableSource).toContain('header="操作"');
  });

  it("renders the legacy viewWithChart template as data and chart panes", () => {
    expect(appSource).toContain("viewUsesChartTemplate(viewResponse.value?.data)");
    expect(viewListPanelSource).toContain("legacyChartData(rows.value)");
    expect(viewListPanelSource).toContain("activePane");
    expect(viewListPanelSource).toContain("<Tabs");
    expect(viewListPanelSource).toContain("<Tab value=\"chart\"");
    expect(viewListPanelSource).toContain('<Tab value="table">数据</Tab>');
    expect(viewListPanelSource).toContain('<Tab value="chart">图表</Tab>');
    expect(viewListPanelSource).not.toContain('class="pi pi-chart-line"');
    expect(viewListPanelSource).toContain("<LegacyChartPanel");
    expect(legacyChartPanelSource).toContain('class="legacy-chart"');
    expect(legacyChartPanelSource).toContain("<polyline");
    expect(legacyChartPanelSource).toContain("<rect");
    expect(legacyChartPanelSource).toContain("<circle");
    expect(legacyChartPanelSource).not.toContain("<meter");
    expect(legacyChartPanelSource).toContain('"#c23531", "#2f4554", "#61a0a8"');
    expect(legacyChartPanelSource).toContain("`系列 ${index + 1}`");
    expect(legacyChartPanelSource).toContain('aria-label="视图数据图表"');
    expect(legacyChartPanelSource).not.toContain("{{ series.type }}");
  });

  it("dispatches known TempFile templates without treating unknown files as lists", () => {
    expect(appSource).toContain('viewTemplateKind(viewResponse.value?.data) === "unsupported"');
    expect(appSource).not.toContain("Unsupported legacy template:");
    expect(appSource).toContain('errorMessage.value = ""');
    expect(viewListPanelSource).toContain("旧版模板 {{ templateName }} 尚未迁移。");
    expect(viewListPanelSource).toContain('viewDisplayTitle(props.view, "加载视图")');
    expect(viewListPanelSource).not.toContain("Load a View");
    expect(viewListPanelSource).not.toContain("Legacy template");
    expect(viewListPanelSource).toContain('v-if="supportedTemplate" class="workflow-toolbar"');
  });

  it("renders the legacy Sudoku template from ViewFile panels", () => {
    expect(appSource).toContain("viewUsesSudokuTemplate(viewResponse.value?.data)");
    expect(viewListPanelSource).toContain("SudokuPanels");
    expect(appSource).toContain("useSudokuPanels");
    expect(appSource).toContain("loadSudokuPanels");
    expect(sudokuWorkflowSource).toContain('loadViewDataById(panelViewId, "sudoku-panel", 5)');
    expect(sudokuWorkflowSource).toContain("scheduleRefresh(panel, response)");
    expect(appSource).toContain("stopSudokuPanelRefresh()");
    expect(viewListPanelSource).toContain("@refresh-panel=\"emit('refreshPanel', $event)\"");
    expect(sudokuPanelsSource).toContain("listFreshTime");
    expect(sudokuPanelsSource).toContain("emit('refreshPanel', panel)");
    expect(sudokuPanelsSource).toContain('class="sudoku-grid"');
    expect(sudokuPanelsSource).toContain("sudokuPanelWidth(panel)");
    expect(sudokuPanelsSource).not.toContain("rows loaded");
  });

  it("loads Sudoku child panels without requiring root querydata", () => {
    const start = appSource.indexOf("async function queryCurrentViewData");
    const source = appSource.slice(start, appSource.indexOf("async function loadResultPage", start));
    expect(source).toContain("if (isSudokuView.value)");
    expect(source.indexOf("await loadSudokuPanels()")).toBeLessThan(source.indexOf("queryCurrentViewDataBase()"));
  });

  it("renders Sudoku linechart panels from child row chart items", () => {
    expect(sudokuPanelsSource).toContain("sudokuPanelKind(panel) === 'linechart'");
    expect(sudokuPanelsSource).toContain("sudokuPanelChart(panel).series");
    expect(sudokuPanelsSource).toContain("<LegacyChartPanel");
    expect(sudokuPanelsSource).not.toContain("<meter");
  });

  it("renders Sudoku map panels from child row map items", () => {
    expect(sudokuPanelsSource).toContain("sudokuPanelKind(panel) === 'map'");
    expect(sudokuPanelsSource).toContain("sudokuPanelMarkers(panel)");
    expect(sudokuPanelsSource).toContain("<LegacyMapPanel");
    expect(legacyMapPanelSource).toContain('import("leaflet")');
    expect(legacyMapPanelSource).toContain("leaflet.circleMarker");
    expect(legacyMapPanelSource).toContain("map.fitBounds");
    expect(legacyMapPanelSource).toContain("title.textContent");
    expect(legacyMapPanelSource).toContain("marker.info[0]");
    expect(legacyMapPanelSource).toContain("没有有效的地图位置。");
    expect(legacyMapPanelSource).not.toContain('"Location"');
  });

  it("renders Sudoku item panels from legacy detail SimpleData", () => {
    const sudokuLoadSource = sudokuWorkflowSource;

    expect(sudokuPanelsSource).toContain("sudokuPanelKind(panel) === 'item'");
    expect(sudokuPanelsSource).toContain("sudokuPanelItemFields(panel)");
    expect(sudokuLoadSource).toContain('sudokuPanelKind(panel) !== "item"');
    expect(sudokuLoadSource).toContain("/api/v1/data/querydatadetail");
    expect(sudokuLoadSource).toContain("buildQueryDataDetailRequest");
    expect(sudokuPanelsSource).toContain("legacyItemDetailFields");
    expect(sudokuPanelsSource).not.toContain("legacyItemFields");
  });

  it("loads and renders Sudoku group child list panels", () => {
    expect(sudokuWorkflowSource).toContain('sudokuPanelKind(panel) !== "group"');
    expect(sudokuWorkflowSource).toContain("for (const childPanel of viewColumns(response.view))");
    expect(sudokuWorkflowSource).toContain("sudokuPanelListViewType(childPanel) !== 0");
    expect(sudokuWorkflowSource).toContain("mergePanelResult(childViewId, childResponse)");
    expect(sudokuPanelsSource).toContain("sudokuPanelKind(panel) === 'group'");
    expect(sudokuPanelsSource).toContain("<Tabs");
    expect(sudokuPanelsSource).toContain("<TabList>");
    expect(sudokuPanelsSource).toContain("这是简单项");
  });

  it("resets the main View search to the first page", () => {
    const searchSource = appSource.slice(
      appSource.indexOf("async function searchCurrentView"),
      appSource.indexOf("async function loadLegacyDetailPath")
    );
    expect(searchSource).toContain("pageIndex.value = 1");
    expect(searchSource.indexOf("pageIndex.value = 1")).toBeLessThan(searchSource.indexOf("queryCurrentViewData()"));
    expect(viewListPanelSource).toContain("emit('search')");
    expect(appSource).toContain('@search="searchCurrentView"');
  });

  it("keeps the Vue workspace on view-id driven legacy view and data APIs", () => {
    expect(viewDataWorkflowSource).toContain("/api/v1/view/getlistview");
    expect(viewDataWorkflowSource).toContain("/api/v1/data/querydata");
    expect(viewDataWorkflowSource).toContain("viewDetailViewId(view, loadedViewId)");
    expect(appSource).toContain("function openListObject(row: ListDataItem, targetViewId = 0)");
    expect(appSource).toContain("legacyDetailHref(targetViewId");
    expect(appSource).toContain("function openNewObject(targetViewId: number)");
    expect(appSource).toContain("legacyNewHref(targetViewId)");
    expect(appSource).toContain("async function startNewObject(viewId = Number(detailViewId.value)");
    expect(appSource).toContain("await queryDetail()");
    expect(appSource).toContain("viewID: String(detailViewId.value)");
    expect(viewDataWorkflowSource).toContain("listRenderColumns(viewResponse.value?.data)");
    expect(appSource).not.toContain("Object.keys(first)");
    expect(appSource).not.toContain("viewName: viewName.value");
    expect(appSource).not.toContain("/api/v1/view/get-view");
    expect(appSource).not.toContain("/api/v1/data/query-list");
    expect(appSource).not.toContain("buildQueryRequest");
  });

  it("uses keyword search without exposing a raw QueryFilter", () => {
    const currentQuerySource = viewDataWorkflowSource.slice(
      viewDataWorkflowSource.indexOf("async function queryCurrentViewData"),
      viewDataWorkflowSource.indexOf("function readItemViewFor")
    );

    expect(appSource).toContain('const viewKeyword = ref("")');
    expect(currentQuerySource).toContain("{ keyword: options.keyword.value }");
    expect(viewDataWorkflowSource).not.toContain("queryFilter");
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
    const pickerSource = viewDetailPanelSource.slice(
      viewDetailPanelSource.indexOf('class="detail-picker-content"'),
      viewDetailPanelSource.indexOf('class="detail-items-table"')
    );

    expect(viewDetailPanelSource).toContain("groupSelectFromExists(group)");
    expect(viewDetailPanelSource).toContain("查询条件");
    expect(viewDetailPanelSource).toContain('label="上一页"');
    expect(viewDetailPanelSource).toContain('label="下一页"');
    expect(viewDetailPanelSource).not.toContain("每页条数");
    expect(viewDetailPanelSource).not.toContain("updateCandidatePageSize");
    expect(viewDetailPanelSource).not.toContain("updateCandidatePage");
    expect(appSource).not.toContain('@update-candidate-page-size');
    expect(appSource).not.toContain('@update-candidate-page');
    expect(viewDetailPanelSource).not.toContain("group.selectFromExists");
    expect(pickerSource).not.toContain('icon="pi pi-search"');
    expect(pickerSource).not.toContain('icon="pi pi-chevron-left"');
    expect(pickerSource).not.toContain('icon="pi pi-chevron-right"');
    expect(pickerSource).not.toContain('icon="pi pi-times"');
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
    expect(appSource).toContain("if (!pendingAction.value)");
    expect(appSource).toContain("onUnmounted(() => {");
    expect(appSource).toContain("stopAutoRefresh()");
    expect(appSource).toContain("stopShellPolling()");
  });

  it("keeps metadata lookup tied to the rendered view id", () => {
    expect(metadataFieldEditorSource).toContain("viewId: props.viewId");
    expect(metadataFieldEditorSource).not.toContain("viewName");
    expect(metadataFieldEditorSource).toContain("<AutoComplete");
    expect(metadataFieldEditorSource).toContain('@complete="searchLookup($event.query)"');
    expect(metadataFieldEditorSource).toContain('@option-select="selectLookup($event.value)"');
    expect(metadataFieldEditorSource).toContain("未找到匹配的选项");
    expect(metadataFieldEditorSource).not.toContain("<InputGroup>");
    expect(metadataFieldEditorSource).not.toContain("<Listbox");
  });

  it("passes parent context into child lookup editors", () => {
    expect(metadataFieldEditorSource).toContain("ownerId: props.ownerId");
    expect(viewDetailPanelSource).toContain(':owner-id="selectedObjectId"');
    expect(viewDetailPanelSource).toContain(':is-added="true"');
    expect(viewDetailPanelSource).toContain(':object-id="itemDataId(item)"');
  });

  it("keeps child item ids in interaction state instead of a hard-coded table column", () => {
    expect(viewDetailPanelSource).not.toContain("<th>ID</th>");
    expect(viewDetailPanelSource).not.toContain("<td>{{ itemDataId(item) }}</td>");
    expect(viewDetailPanelSource).toContain(":object-id=\"itemDataId(item)\"");
    expect(viewDetailPanelSource).toContain("groupDetailViewId(group)}/${itemDataId(item)}");
  });

  it("renders PrimeVue metadata controls without changing legacy string values", () => {
    expect(metadataFieldEditorSource).toContain("fieldInputChecked");
    expect(metadataFieldEditorSource).toContain("fieldInputType");
    expect(metadataFieldEditorSource).toContain("fieldInputValue");
    expect(metadataFieldEditorSource).toContain("isMultilineField");
    expect(metadataFieldEditorSource).toContain("fieldInputChecked(props.field, value.value)");
    expect(metadataFieldEditorSource).toContain(':type="fieldInputType(field)"');
    expect(metadataFieldEditorSource).toContain(':model-value="fieldInputValue(field, value)"');
    expect(metadataFieldEditorSource).toContain("<Textarea v-else-if");
    expect(metadataFieldEditorSource).toContain("<Checkbox v-model=\"checked\"");
    expect(metadataFieldEditorSource).toContain("next ? \"true\" : \"false\"");
    expect(metadataFieldEditorSource).toContain('checked ? "是" : "否"');
    expect(metadataFieldEditorSource).not.toContain('<small v-if="modelValue">');
    expect(metadataFieldEditorSource).toContain('response.message || "查找失败。"');
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

  it("loads the rendered View before the current data query", () => {
    const querySource = viewDataWorkflowSource.slice(
      viewDataWorkflowSource.indexOf("async function queryCurrentViewData"),
      viewDataWorkflowSource.indexOf("function readItemViewFor")
    );

    expect(querySource).toContain("viewId(viewResponse.value.data) !== requestedViewId");
    expect(querySource.indexOf("await loadLegacyListView()")).toBeGreaterThanOrEqual(0);
    expect(querySource.indexOf("await loadLegacyListView()")).toBeLessThan(querySource.indexOf("queryLoadedViewData("));
    expect(viewDataWorkflowSource).toContain("const loadedViewId = Number(currentViewId.value)");
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

  it("renders detail fields from read-item View metadata, not raw DTO rows", () => {
    expect(appSource).toContain("renderedDetailFields(currentReadItemView.value");
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

  it("loads the default first-screen View only after the legacy app shell is authenticated", () => {
    const workflowSource = appSource.slice(
      appSource.indexOf("async function loadViewWorkflow"),
      appSource.indexOf("onMounted(()")
    );
    const shellSource = appSource.slice(
      appSource.indexOf("async function ensureLegacyShell"),
      appSource.indexOf("async function loadViewWorkflow")
    );

    expect(appSource).toContain('const password = ref("")');
    expect(appSource).toContain('<LoginPanel\n    v-if="!token"');
    expect(appSource).not.toContain('const password = ref("admin")');
    expect(appSource).not.toContain("ensureLegacySession");
    expect(loginPanelSource).toContain('autocomplete="username"');
    expect(loginPanelSource).toContain('autocomplete="current-password"');
    expect(loginPanelSource).toContain('name="check-code-key"');
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
    expect(appSource).toContain("await loadLegacyDetailPath(detailRoute)");
    expect(appSource).toContain("isStandaloneDetail.value = true");
    expect(appSource).toContain('v-if="!isMetadataOnlyView && !isStandaloneDetail"');
    expect(appSource).toContain('@click="openPrimarySection"');
    expect(appSource).toContain("await queryDetail(route.viewId, objectId)");
    expect(appSource).toContain("legacyItemViewPathId(window.location.pathname)");
    expect(appSource).toContain("await loadLegacyItemView(itemViewId)");
    expect(appSource).toContain("legacyNewPath(window.location.pathname)");
    expect(appSource).toContain("await loadLegacyNewPath(newRoute)");
    expect(appSource).toContain("await startNewObject(route.viewId, route.parentObjId, route.ownerViewId, route.property)");
    expect(appSource).toContain('async function startNewObject(viewId = Number(detailViewId.value), parentObjId = "", ownerViewId = "", property = "")');
    expect(appSource).toContain("newObjectOwner = { ownerViewId, ownerId: parentObjId, property }");
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
    expect(viewDetailPanelSource).toContain("已加载视图定义。");
  });

  it("returns stale stored tokens to the signed-out login screen", () => {
    const shellSource = appSource.slice(
      appSource.indexOf("async function ensureLegacyShell"),
      appSource.indexOf("async function loadViewWorkflow")
    );

    expect(shellSource).toContain("if (await loadMainInfo()) return true");
    expect(shellSource).toContain("clearLegacySession()");
    expect(shellSource).toContain("await prepareLegacyLogin()");
    expect(shellSource).toContain("return false");
    expect(shellSource).not.toContain("loginV2");
  });

  it("keeps legacy top menus visible while opening child View ids", () => {
    const menuSource = appSource.slice(
      appSource.indexOf("async function openShellMenu"),
      appSource.indexOf("async function loadLegacyListView")
    );

    expect(appSource).toContain("legacyMainMenuItems(mainInfoResponse.value?.data)");
    expect(appSource).not.toContain("shellMenuItems");
    expect(appSource).toContain(':items="topMenuItems"');
    expect(appSource).toContain(':expanded-auth-code="subMenuParentAuthCode"');
    expect(appSource).toContain('@select="openShellMenu"');
    expect(appSource).toContain('class="shell-header"');
    expect(appSource).toContain('class="desktop-navigation"');
    expect(appSource).toContain("horizontal");
    expect(appSource).not.toContain('class="sidebar"');
    expect(legacyMenuNavSource).toContain('v-for="item in items"');
    expect(legacyMenuNavSource).toContain("nav-list-horizontal");
    expect(legacyMenuNavSource).toContain("legacyAuthImageUrl(item)");
    expect(legacyMenuNavSource).toContain("legacyAuthImageUrl(child)");
    expect(legacyMenuNavSource).toContain('class="nav-menu-image"');
    expect(legacyMenuNavSource).toContain('legacyAuthNo(item) === expandedAuthCode && subItems.length');
    expect(menuSource).toContain("legacyAuthViewId(item)");
    expect(menuSource).toContain('subMenuParentAuthCode.value = ""');
    expect(menuSource).toContain("subMenuResponse.value = null");
    expect(menuSource).toContain("applyRequestedViewId(itemViewId)");
    expect(menuSource).not.toContain("legacyQueryViewId.value = itemViewId");
    expect(menuSource).toContain("await loadViewWorkflow(true)");
    expect(menuSource).toContain("subMenuParentAuthCode.value = authNo");
    expect(menuSource).toContain("await loadSubMenu()");
    expect(menuSource).toContain("if (legacyAuthViewId(item)) mobileMenuOpen.value = false");
  });

  it("does not retain console-era business DTO staging", () => {
    expect(appSource).not.toContain("enumModelId");
    expect(appSource).not.toContain("legacyQueryFilter");
    expect(appSource).not.toContain("detailObjId");
    expect(appSource).not.toContain("saveObjId");
    expect(appSource).not.toContain("saveNewObjId");
    expect(appSource).not.toContain("operationObjectId");
    expect(appSource).not.toContain("savePropertyiesJson");
    expect(appSource).not.toContain("saveNewPropertyiesJson");
    expect(appSource).not.toContain("JSON.stringify(buildSavePropertyies");
    expect(appSource).not.toContain('order_state="0"');
    expect(appSource).not.toContain("BTC-USDT");
    expect(appSource).not.toContain("const operationId = ref");
  });

  it("does not expose the backend seed DTO smoke route in the Vue workspace", () => {
    expect(appSource).not.toContain("Backend Smoke");
    expect(appSource).not.toContain('fetch("/test")');
    expect(appSource).not.toContain("backendSmokeResponse");
  });

  it("opens the legacy report workflow from the rendered View", () => {
    expect(appSource).toContain("ViewReportPanel");
    expect(appSource).toContain("showViewReport = !showViewReport");
    expect(appSource).toContain(':view-id="currentViewId"');
    expect(viewReportPanelSource).toContain("<Dialog");
    expect(viewReportPanelSource).toContain("modal");
    expect(viewReportPanelSource).toContain('<Tab value="output"');
    expect(viewReportPanelSource).toContain('<Tab value="conditions"');
    expect(viewReportPanelSource).toContain('<Tab value="save"');
    expect(viewReportPanelSource).not.toContain('class="pi pi-table"');
    expect(viewReportPanelSource).not.toContain('class="pi pi-filter"');
    expect(viewReportPanelSource).not.toContain('<Tab value="save"><i');
    expect(viewReportPanelSource).toContain("生成报表");
    expect(viewReportPanelSource).toContain("报表结果");
    expect(viewReportPanelSource).toContain("报表结果 共{{ resultPages }}页 当前第{{ resultPage }}页");
    expect(viewReportPanelSource).toContain('label="前一页"');
    expect(viewReportPanelSource).toContain('label="下一页"');
    expect(viewReportPanelSource).toContain('class="report-result-table"');
    expect(viewReportPanelSource).toContain("backToReportSetup");
    expect(viewReportPanelSource).toContain("currentPage.value = 1");
    expect(viewReportPanelSource).not.toContain("<Paginator");
    expect(viewReportPanelSource).not.toContain("<DataTable");
    expect(viewReportPanelSource).not.toContain("视图 {{ viewId }}");
    expect(viewReportPanelSource).toContain("/api/v1/report/mkrpt");
    expect(viewReportPanelSource).toContain("reportResponse");
    expect(viewReportPanelSource).toContain("showingResults.value = true");
    expect(viewReportPanelSource).toContain('@click="backToReportSetup"');
    expect(appSource).not.toContain("Report Grid");
  });

  it("builds report output and conditions from View metadata", () => {
    expect(viewReportPanelSource).toContain("/api/v1/report/getmkqview");
    expect(viewReportPanelSource).toContain("ReportOutputSelector");
    expect(viewReportPanelSource).toContain("const pageSize = 10");
    expect(viewReportPanelSource).not.toContain("InputNumber");
    expect(viewReportPanelSource).not.toContain("每页条数");
    expect(viewReportPanelSource).not.toContain("重新加载");
    expect(viewReportPanelSource).toContain("reportModelCompareTypes");
    expect(viewReportPanelSource).toContain("reportCols");
    expect(viewReportPanelSource).not.toContain("selectedColumnIds");
    expect(reportOutputSelectorSource).toContain("候选列");
    expect(reportOutputSelectorSource).toContain("输出方式");
    expect(reportOutputSelectorSource).toContain("已选列");
    expect(reportOutputSelectorSource).toContain("moveOutput");
    expect(reportOutputsSource).toContain("addReportOutput");
    expect(viewReportPanelSource).toContain("filterExp");
    expect(viewReportPanelSource).toContain("合并分组");
    expect(viewReportPanelSource).toContain("groupReportConditions");
    expect(viewReportPanelSource).toContain("condition-group-marker");
    expect(viewReportPanelSource).toContain("拆分分组");
    expect(viewReportPanelSource).not.toContain("`G${id}`");
    expect(viewReportPanelSource).toContain('{ label: "与", value: "and" }');
    expect(viewReportPanelSource).toContain('columnId: ""');
    expect(viewReportPanelSource).toContain('compareId: ""');
    expect(viewReportPanelSource).toContain('v-else-if="condition.columnId && condition.compareId"');
    expect(viewReportPanelSource).not.toContain("const column = modelColumns.value[0]");
    expect(viewReportPanelSource).not.toContain('class="condition-first"');
    expect(viewReportPanelSource).toContain('class="report-condition-header"');
    expect(viewReportPanelSource).toContain("<strong>与/或</strong>");
    expect(viewReportPanelSource).toContain("<strong>字段</strong>");
    expect(viewReportPanelSource).toContain("<strong>运算</strong>");
    expect(viewReportPanelSource).toContain("<strong>值</strong>");
    expect(viewReportPanelSource).toContain('class="report-condition-footer"');
    expect(viewReportPanelSource).not.toContain(' label="合并分组"');
    expect(viewReportPanelSource).not.toContain(' label="增加条件"');
    expect(reportConditionsSource).toContain("buildReportConditionFilter");
    expect(reportConditionsSource).toContain("serializeNode");
    expect(viewReportPanelSource).not.toContain("Report Columns JSON");
    expect(viewReportPanelSource).not.toContain("QueryFilter");
  });

  it("keeps the legacy save report route in the View report panel", () => {
    const footerSource = viewReportPanelSource.slice(viewReportPanelSource.indexOf("<template #footer>"));

    expect(viewReportPanelSource).toContain("保存报表定义");
    expect(viewReportPanelSource).toContain('const reportName = ref("")');
    expect(viewReportPanelSource).toContain("输入报表信息以保存该报表");
    expect(viewReportPanelSource).not.toContain('ref("视图报表")');
    expect(viewReportPanelSource).not.toContain("<h3>保存报表</h3>");
    expect(viewReportPanelSource).toContain("/api/v1/report/saverpt");
    expect(appSource).not.toContain("Save Report Definition");
    expect(footerSource).not.toContain('icon="pi pi-arrow-left"');
    expect(footerSource).not.toContain('icon="pi pi-times"');
    expect(footerSource).not.toContain('icon="pi pi-play"');
    expect(footerSource).not.toContain('icon="pi pi-save"');
  });

  it("moves legacy message polling into the signed-in shell", () => {
    expect(appSource).toContain("/api/v1/message/getmsg");
    expect(appSource).toContain("15_000");
    expect(appSource).toContain("const fetchedMessages = legacyMessages(messages.value.data)");
    expect(appSource).toContain("activeShellMessage.value = fetchedMessages[0]");
    expect(appSource).toContain('v-if="token"');
    expect(shellActionsSource).toContain("系统消息");
    expect(shellActionsSource).toContain('v-if="activeMessage"');
    expect(shellActionsSource).toContain('header="系统消息"');
    expect(shellActionsSource).toContain('label="查看详细"');
    expect(shellActionsSource).toContain('label="确定"');
    expect(shellActionsSource).toContain('emit("dismissMessage")');
    expect(shellActionsSource).toContain('emit("openMessage", message)');
    expect(appSource).not.toContain("messageResponse");
    expect(shellActionsSource).not.toContain("<Popover");
    expect(shellActionsSource).not.toContain("message-popover");
    expect(shellActionsSource).not.toContain("暂无消息");
    expect(appSource).not.toContain("<h2>Messages</h2>");
  });

  it("moves legacy notify counts into shell menu badges", () => {
    expect(appSource).toContain("/api/v1/message/getnotify");
    expect(appSource).toContain("notifyResponse");
    expect(appSource).toContain("legacyNotifyCountForAuth");
    expect(legacyMenuNavSource).toContain('class="nav-count"');
    expect(appSource).not.toContain("<h2>Notify Counts</h2>");
  });

  it("moves legacy user info and logout into the signed-in shell", () => {
    expect(appSource).toContain("/api/v1/auth/getuserinfo");
    expect(appSource).toContain("legacyUserInfoResponse");
    expect(appSource).toContain("legacyUserName");
    expect(appSource).toContain("legacyUserAvatar");
    expect(shellActionsSource).toContain("userName");
    expect(shellActionsSource).toContain('v-if="userAvatar"');
    expect(shellActionsSource).toContain('class="shell-avatar"');
    expect(appSource).toContain("安全退出");
    expect(appSource).toContain('aria-label="Mobile safe logout"');
    expect(appSource.indexOf("安全退出")).toBeGreaterThan(appSource.indexOf("<LegacyMenuNav"));
    expect(shellActionsSource).not.toContain("安全退出");
    expect(appSource).not.toContain("Legacy User Info");
    expect(appSource).toContain("clearLegacySession()");
    expect(appSource).toContain("await prepareLegacyLogin()");
  });

  it("moves the legacy checkcode route into the signed-out login panel", () => {
    expect(appSource).toContain("/api/v1/auth/getcheckcode");
    expect(appSource).toContain("checkCodeResponse");
    expect(loginPanelSource).toContain('placeholder="验证码"');
    expect(loginPanelSource).toContain("captchaImage");
    expect(loginPanelSource).toContain('label="刷新"');
    expect(appSource).not.toContain("<h2>Check Code</h2>");
  });

  it("moves the legacy loginv2 route into the signed-out login panel", () => {
    expect(appSource).toContain("/api/v1/auth/loginv2");
    expect(appSource).toContain("localStorage.setItem(\"fool-service-token\", token.value)");
    expect(appSource).toContain("async function submitLegacyLogin");
    expect(loginPanelSource).toContain('placeholder="用户名"');
    expect(loginPanelSource).toContain('placeholder="密码"');
    expect(loginPanelSource).toContain("pending ? '登录中...' : '登录'");
    expect(loginPanelSource).toContain('label="重置"');
    expect(loginPanelSource).not.toContain("Welcome back");
    expect(loginPanelSource).not.toContain("<Card");
    expect(appSource).not.toContain("Legacy Login V2");
  });

  it("loads legacy initapp metadata for the signed-out login panel", () => {
    expect(appSource).toContain("/api/v1/auth/initapp");
    expect(appSource).toContain("initAppResponse");
    expect(appSource).toContain("async function prepareLegacyLogin");
    expect(loginPanelSource).toContain("appInfo");
    expect(appSource).not.toContain("Init App");
  });

  it("loads legacy submenus into the signed-in shell", () => {
    expect(appSource).toContain("/api/v1/auth/getsubmenu");
    expect(appSource).toContain("subMenuResponse");
  });

  it("loads legacy main info into the signed-in shell", () => {
    expect(appSource).toContain("/api/v1/auth/getmain");
    expect(appSource).toContain("mainInfoResponse");
    expect(appSource).toContain("legacyAppName(mainInfoResponse.value?.data");
    expect(appSource).toContain("legacyAppVersion(mainInfoResponse.value?.data)");
    expect(appSource).toContain("{{ shellAppName }}");
    expect(appSource).not.toContain("Docker Backend");
    expect(viewShellSource).not.toContain("services");
  });

  it("keeps the old text-only application brand in the shell", () => {
    expect(appSource).toContain('class="brand"');
    expect(appSource).toContain("{{ shellAppName }}");
    expect(appSource).toContain("{{ shellAppVersion }}");
    expect(appSource).not.toContain("shellAppMark");
    expect(appSource).not.toContain("brand-mark");
  });

  it("runs legacy initnew from the rendered View workflow", () => {
    expect(appSource).toContain("/api/v1/data/initnew");
    expect(appSource).toContain("async function initNew(viewId: number");
  });

  it("runs legacy savenewobj from the rendered View workflow", () => {
    expect(appSource).toContain("/api/v1/data/savenewobj");
    expect(appSource).toContain("async function saveSelectedObject");
  });

  it("runs legacy operations from rendered View metadata", () => {
    expect(appSource).toContain("/api/v1/data/runoperation");
    expect(appSource).toContain("const response = await runOperation(id)");
  });

  it("does not expose the migration API console in the production shell", () => {
    expect(appSource).not.toContain("API Tools");
    expect(appSource).not.toContain("QueryFilter");
    expect(appSource).not.toContain("Response & Result Set");
    expect(appSource).not.toContain("MigrationMap");
    expect(appSource).not.toContain("activeSection");
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
      propertyies: [
        { key: "name", value: "Sample" },
        { key: "state", value: "0" }
      ],
      itemproperties: [{
        key: "children",
        items: [{
          itemId: "2001",
          isExist: true,
          propertyies: [{ key: "childName", value: "Updated child" }]
        }]
      }]
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
      propertyies: [{ key: "itemName", value: "New child" }],
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
      reportCols: [
        { colName: "State", index: 2 },
        { colName: "Name", index: 1 }
      ],
      filterExp: {
        col: { id: "state", name: "State" },
        compareOp: { id: "1", name: "Equals" },
        valueExp: "0",
        valueFmt: "Open"
      },
      reportName: " View Daily "
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      currentPage: 2,
      pageSize: 10,
      queryFilter: "record_state=\"0\"",
      filterExp: {
        col: { id: "state", name: "State" },
        compareOp: { id: "1", name: "Equals" },
        valueExp: "0",
        valueFmt: "Open"
      },
      reportName: "View Daily",
      reportCols: [
        { colName: "State", index: 2 },
        { colName: "Name", index: 1 }
      ]
    });
  });
});
