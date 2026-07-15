import { describe, expect, it } from "vitest";
import apiSource from "./api.ts?raw";
import nginxConfig from "../nginx.conf?raw";
import viteConfig from "../vite.config.ts?raw";
import appSource from "./App.vue?raw";
import legacyChartPanelSource from "./LegacyChartPanel.vue?raw";
import legacyErrorDialogSource from "./LegacyErrorDialog.vue?raw";
import legacyItemPanelSource from "./LegacyItemPanel.vue?raw";
import legacyMapPanelSource from "./LegacyMapPanel.vue?raw";
import legacyMenuNavSource from "./LegacyMenuNav.vue?raw";
import legacyPaginationSource from "./LegacyPagination.vue?raw";
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

    expect(appSource).toContain("onMounted(() => {");
    expect(appSource).toContain("void initializeApp();");
    expect(appSource).toContain("await loadInitialRoute()");
    expect(appSource).toContain("await loadViewWorkflow()");
    expect(appSource).toContain("View workflow");
    expect(mainViewSource).toContain("输入条件");
    expect(mainViewSource).toContain('class="list-query-input"');
    expect(mainViewSource).toContain('aria-label="查询条件"');
    expect(mainViewSource).not.toContain("<label>");
    expect(mainViewSource).toContain('label="查找"');
    expect(mainViewSource).toContain('label="查找" severity="secondary" outlined');
    expect(mainViewSource).toContain('label="查找" severity="secondary" outlined @click="emit(\'search\')"');
    expect(mainViewSource).not.toContain('label="查找" severity="secondary" outlined :disabled="disabled"');
    expect(mainViewSource).toContain('label="统计"');
    expect(mainViewSource).toContain('label="统计" severity="secondary" outlined :disabled="!currentViewId"');
    expect(mainViewSource).not.toContain('disabled || !currentViewId');
    expect(mainViewSource).toContain('<template v-if="listView">');
    expect(mainViewSource).not.toMatch(/v-for="operation in createItems"[\s\S]*?:disabled="disabled"[\s\S]*?@click="emit\('newObject'/);
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
    expect(appSource).toContain("function addDetailItem");
    expect(appSource).toContain("function updateDetailItem");
    expect(appSource).toContain("function deleteDetailItem");
    expect(appSource).toContain("pendingItemProperties");
    expect(appSource).toContain("usePendingChildChanges");
    expect(appSource).toContain("saveObj(pendingItemProperties.value)");
    expect(appSource).toContain("renderPendingDetailGroups");
    const deleteHandlerSource = appSource.slice(
      appSource.indexOf("function deleteDetailItem"),
      appSource.indexOf("async function loadFieldEnums")
    );
    expect(deleteHandlerSource).not.toContain("window.confirm");
    expect(deleteHandlerSource).not.toContain("saveObj(");
    expect(deleteHandlerSource).not.toContain("queryDetail()");
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
    expect(listDataTableSource).toContain("showActionHeader");
    expect(listDataTableSource).toContain("showActionHeader: true");
    expect(listDataTableSource).toContain("showDefaultAction");
    expect(listDataTableSource).toContain("showDefaultAction: false");
    expect(listDataTableSource).toContain("<DataTable");
    expect(listDataTableSource).toContain("<Column v-for");
    expect(listDataTableSource).toContain('v-if="rowOperations.length || showDefaultAction"');
    expect(listDataTableSource).toContain("showActionHeader ? undefined : { display: 'none' }");
    expect(listDataTableSource).not.toContain('align-frozen="right"');
    expect(listDataTableSource).not.toContain('icon="pi pi-arrow-right"');
    expect(listDataTableSource).toMatch(/v-if="showDefaultAction"[\s\S]*?:label="defaultActionLabel"[\s\S]*?severity="secondary"\s+outlined/);
  });

  it("does not render data rows before View columns exist", () => {
    expect(listDataTableSource).toContain(':value="columns.length ? renderedRows : []"');
    expect(listDataTableSource).not.toContain("columns.length || rows.length");
  });

  it("does not keep generic record-map table helpers", () => {
    expect(viewWorkflowSource).not.toContain("recordColumns");
    expect(viewWorkflowSource).not.toContain("recordRowKey");
  });

  it("renders detail View operations from the loaded detail payload", () => {
    const operationRequestSource = appSource.slice(
      appSource.indexOf("async function runOperation"),
      appSource.indexOf("async function runViewOperation")
    );
    const operationHandlerSource = appSource.slice(
      appSource.indexOf("async function runViewOperation"),
      appSource.indexOf("function applyDefaultAppView")
    );
    const toolbarSource = viewDetailPanelSource.slice(
      viewDetailPanelSource.indexOf('class="detail-toolbar legacy-button-group"'),
      viewDetailPanelSource.indexOf('class="detail-field-grid detail-field-edit"')
    );
    const operationDialogSource = viewDetailPanelSource.slice(
      viewDetailPanelSource.indexOf('v-if="operationResult"'),
      viewDetailPanelSource.indexOf('class="view-items-panel"')
    );
    const infoDialogSource = viewDetailPanelSource.slice(
      viewDetailPanelSource.indexOf('v-if="infoMessage"'),
      viewDetailPanelSource.indexOf('v-if="errorMessage"')
    );

    expect(appSource).toContain("dataOperations(detailResponse.value?.data)");
    expect(appSource).toContain("detailViewOperations");
    expect(appSource).toContain('@run-view-operation="runViewOperation"');
    expect(appSource).toContain('errorMessage.value = "请先保存当前信息"');
    expect(operationHandlerSource).toMatch(/if \(editing\) \{[\s\S]*?请先保存当前信息[\s\S]*?return;[\s\S]*?runOperation\(id\)/);
    expect(appSource).toContain(':error-message="errorMessage"');
    expect(appSource).toContain("@dismiss-error=\"errorMessage = ''\"");
    expect(viewDetailPanelSource).toContain('class="detail-toolbar legacy-button-group"');
    expect(toolbarSource).toContain('label="编辑"');
    expect(toolbarSource).toContain('label="保存"');
    expect(toolbarSource.match(/severity="secondary"\s+outlined/g)).toHaveLength(3);
    expect(viewDetailPanelSource).toContain('v-for="operation in detailViewOperations"');
    expect(viewDetailPanelSource).toContain("emit('runViewOperation', operation, isEditing)");
    expect(legacyErrorDialogSource).toContain('header="发生错误"');
    expect(viewDetailPanelSource).toContain("emit('dismissError')");
    expect(toolbarSource).not.toContain(':disabled="pending"');
    expect(viewDetailPanelSource).not.toContain("operationParams(operation)");
    expect(appSource).toContain("legacyRunOperationMessage(response.data)");
    expect(operationRequestSource).toContain("{ silentTransport: true }");
    expect(appSource).toContain("operationResult.value = { message, success }");
    expect(operationHandlerSource).not.toContain("queryCurrentViewData()");
    expect(operationHandlerSource).not.toContain("queryDetail()");
    expect(appSource).toContain('@dismiss-operation-result="operationResult = null"');
    expect(viewDetailPanelSource).toContain('header="执行结果"');
    expect(viewDetailPanelSource).toContain('operationResult.success ? "操作成功" : "操作失败"');
    expect(viewDetailPanelSource).toContain("emit('dismissOperationResult')");
    expect(viewDetailPanelSource).not.toContain("<Message v-if=\"operationResult\"");
    expect(operationDialogSource).toContain('label="确定"');
    expect(operationDialogSource).not.toContain('label="关闭"');
    expect(infoDialogSource).toContain(':closable="false"');
    expect(legacyErrorDialogSource).toContain(':closable="false"');
    expect(operationDialogSource).toContain(':closable="false"');
    expect(toolbarSource.match(/icon="pi pi-check"/g)).toHaveLength(2);
    expect(toolbarSource).not.toContain('icon="pi pi-save"');
    expect(toolbarSource).not.toContain('icon="pi pi-bolt"');
  });

  it("renders the old detail heading with object or create context", () => {
    expect(appSource).toContain('viewDisplayTitle(currentReadItemView.value, "详情")');
    expect(viewDetailPanelSource).toContain("`${title} -新建`");
    expect(viewDetailPanelSource).toContain("`${title} -${selectedObjectId}`");
    expect(viewDetailPanelSource).toContain('fieldDisplayValue(item) || "\\u00a0"');
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
    expect(viewDetailPanelSource).toContain(':disabled="isEditing"');
    expect(viewDetailPanelSource).toContain('v-if="!isCreatingObject"');
    expect(viewDetailPanelSource).toContain(':disabled="saving || !isEditing"');
    expect(viewDetailPanelSource).not.toContain("pending: boolean");
    expect(appSource).not.toContain(':pending="Boolean(pendingAction)"\n          :schema-only');
    expect(viewDetailPanelSource).not.toContain('v-if="!isEditing"\n        type="button"\n        label="编辑"');
    expect(viewDetailPanelSource).not.toContain('v-else\n        type="button"\n        label="保存"');
    expect(viewDetailPanelSource).toContain("isEditing.value = props.isCreatingObject");
  });

  it("uses child DetailViewId for deep editing instead of inline editors", () => {
    expect(viewDetailPanelSource).toContain("groupDetailViewId(group)");
    expect(viewDetailPanelSource).toContain("function detailItemHref");
    expect(viewDetailPanelSource).toContain('v-if="!groupSelectFromExists(group) && groupDetailViewId(group)"');
    expect(viewDetailPanelSource).toMatch(/:label="editingItemKey[\s\S]*?severity="secondary"\s+text/);
    expect(viewDetailPanelSource).toContain('label="删除" icon="pi pi-trash" size="small" severity="danger" text');
    expect(viewDetailPanelSource).not.toContain('label="删除" icon="pi pi-trash" size="small" severity="danger" outlined');
    expect(viewDetailPanelSource).toContain(':href="detailItemHref(group, item)"');
    expect(viewDetailPanelSource).toContain("详细");
    expect(viewDetailPanelSource).toContain('editingItemKey === itemKey(group, item) && !groupDetailViewId(group)');
  });

  it("renders legacy detail collections as metadata-driven tabs and tables", () => {
    expect(viewDetailPanelSource).toContain('v-model:value="activeGroupKey"');
    expect(viewDetailPanelSource).toContain('activeGroupKey.value = ""');
    expect(viewDetailPanelSource).toContain('pickerGroupKey.value = ""');
    expect(viewDetailPanelSource).toContain('<Tab v-for="group in detailItemGroups"');
    expect(viewDetailPanelSource).toContain('<TabPanel v-for="group in detailItemGroups"');
    expect(viewDetailPanelSource).toContain('class="legacy-item-table detail-items-grid"');
    expect(viewDetailPanelSource).toContain('<th v-for="field in groupColumns(group)"');
    expect(viewDetailPanelSource).toContain('<th :colspan="schemaOnly ? 1 : childActionColumnCount(group)">操作</th>');
    expect(viewDetailPanelSource).toContain('<tbody v-if="!schemaOnly">');
    expect(viewDetailPanelSource).not.toContain('class="detail-item-actions"');
    expect(viewDetailPanelSource).toContain('<tr v-for="item in groupItems(group)"');
    expect(viewDetailPanelSource).not.toContain("<span>{{ groupItems(group).length }}</span>");
    expect(viewDetailPanelSource).toContain('v-if="detailItemGroups.length && (selectedObjectId || schemaOnly)" class="view-items-panel"');
    expect(viewDetailPanelSource).not.toContain('v-if="!groupItems(group).length"');
    expect(viewDetailPanelSource).not.toContain("暂无子项。");
  });

  it("opens select-from-existing collection candidates in the legacy modal flow", () => {
    expect(viewDetailPanelSource).toContain('<div v-if="!schemaOnly" class="detail-collection-toolbar legacy-button-group">');
    expect(viewDetailPanelSource).toContain('label="增加"');
    expect(viewDetailPanelSource).toContain("function addItem(group");
    expect(viewDetailPanelSource).toContain("nextObjectId()");
    expect(viewDetailPanelSource).toContain("if (itemId && editingItemKey.value) stageEditingItem()");
    expect(viewDetailPanelSource).toContain('if (itemId && isEditing.value) editingItemKey.value = `${groupKey(group)}:${itemId}`');
    expect(viewDetailPanelSource).toContain("if (!isEditing.value) return");
    expect(viewDetailPanelSource).toContain('v-if="!groupSelectFromExists(group) && !groupDetailViewId(group)"');
    expect(viewDetailPanelSource).not.toContain('<Button v-if="isEditing" type="button" label="删除"');
    expect(viewDetailPanelSource).not.toContain("item-add-row");
    expect(viewDetailPanelSource).not.toContain("newChildDraftValue");
    expect(viewDetailPanelSource).toContain("if (props.isCreatingObject)");
    expect(viewDetailPanelSource).toContain('v-if="detailItemGroups.length && (selectedObjectId || schemaOnly)" class="view-items-panel"');
    expect(viewDetailPanelSource).not.toContain('selectedObjectId && !isCreatingObject');
    expect(viewDetailPanelSource).toContain('header="操作提示"');
    expect(viewDetailPanelSource).toContain("<p>操作成功</p>");
    expect(viewDetailPanelSource).toContain("{{ infoMessage }}");
    expect(appSource).toContain('infoMessage.value = "请先保存当前内容，再新建子项"');
    expect(viewDetailPanelSource).toContain("function openExistingPicker");
    expect(viewDetailPanelSource).toContain('class="detail-picker-dialog"');
    expect(viewDetailPanelSource).toContain("await props.loadExistingDetailView(group)");
    expect(viewDetailPanelSource).toContain('header="加载中"');
    expect(viewDetailPanelSource).toContain("正在加载，请稍后....");
    expect(appSource).toContain('const candidateViewLoading = computed(() => pendingAction.value === "child-select-view")');
    expect(appSource).toContain(":load-existing-detail-view=\"loadExistingDetailView\"");
    expect(viewDetailPanelSource).toContain("emit('queryExistingDetailItems', group)");
    expect(viewDetailPanelSource).toContain('@keyup.enter="emit(\'queryExistingDetailItems\', group)"');
    expect(appSource).toContain("async function queryExistingDetailItems(group: QueryDataDetailItemGroup, resetPage = true)");
    expect(appSource).toContain("if (resetPage) setCandidateState(group, { pageIndex: 1 })");
    expect(appSource).toContain("await queryExistingDetailItems(group, false)");
    expect(viewDetailPanelSource).toContain("candidateRecordInfo(candidateState(group))");
    expect(viewDetailPanelSource).not.toContain('v-if="candidateState(group).queried"');
    expect(viewDetailPanelSource).toContain(':minimum-rows="candidateState(group).queried ? candidateState(group).pageSize : 0"');
    expect(viewDetailPanelSource).toContain(':show-action-header="false"');
    expect(viewDetailPanelSource).not.toContain("暂无候选记录。");
    expect(viewDetailPanelSource.indexOf('class="detail-picker-results"')).toBeLessThan(
      viewDetailPanelSource.indexOf("<LegacyPagination")
    );
    expect(viewDetailPanelSource).toContain("function selectExistingItem");
    expect(viewDetailPanelSource).toContain('@select="(row) => selectExistingItem(group, row)"');
    expect(appSource).toContain("const viewId = groupListViewId(group)");
    expect(appSource).toContain("legacyChildNewHref(");
    const addExistingSource = appSource.slice(
      appSource.indexOf("function addExistingDetailItem"),
      appSource.indexOf("function updateDetailItem")
    );
    expect(addExistingSource).toContain("addPendingDetailItem");
    expect(addExistingSource).not.toContain("saveObj(");
    expect(addExistingSource).not.toContain("queryDetail()");
  });

  it("renders row operations through their target detail View id", () => {
    expect(viewListPanelSource).toContain("rowOperations(operations.value)");
    expect(viewListPanelSource).toContain(':row-operations="rowItems"');
    expect(listDataTableSource).toContain("rowOperations");
    expect(listDataTableSource).toContain("emit('select', row, operationTargetViewId(operation))");
    expect(listDataTableSource).toContain('v-if="operationTargetViewId(operation) > 0"');
    expect(listDataTableSource).toContain('v-else class="legacy-inert-operation"');
    expect(listDataTableSource).not.toContain("disabled || operationTargetViewId(operation) <= 0");
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
    expect(viewListPanelSource).toContain("<LegacyPagination");
    expect(viewListPanelSource).toContain('class="view-template-tabs legacy-tabs"');
    expect(viewListPanelSource).toContain(':total-items="resultTotalItems"');
    expect(viewListPanelSource).toContain(':minimum-rows="pageSize"');
    expect(listDataTableSource).toContain('Symbol("legacy-filler-row")');
    expect(listDataTableSource).toContain("props.minimumRows - props.rows.length");
    expect(listDataTableSource).toContain("columnWidth(column)");
    expect(listDataTableSource).toContain(':style="tableColumnStyle(column)"');
    expect(listDataTableSource).toContain('return "legacy-filler-row"');
    expect(listDataTableSource).toContain('v-if="!isFiller(row)" class="table-actions"');
    expect(listDataTableSource).not.toContain('v-if="columns.length"');
    expect(listDataTableSource).not.toContain("暂无数据。");
    expect(listDataTableSource).not.toContain("请先加载视图。");
    expect(listDataTableSource).toContain("'metadata-empty-table': !columns.length || !renderedRows.length");
    expect(listDataTableSource).toContain("striped: true");
    expect(listDataTableSource).toContain(':striped-rows="striped"');
    expect(sudokuPanelsSource.match(/:striped="false"/g)).toHaveLength(2);
    expect(listDataTableSource).toContain("condensed: true");
    expect(listDataTableSource).toContain(`:size="condensed ? 'small' : undefined"`);
    expect(sudokuPanelsSource.match(/:condensed="false"/g)).toHaveLength(2);
    expect(viewDetailPanelSource).toContain(':condensed="false"');
    expect(viewDetailPanelSource).toContain('class="detail-collection-tabs legacy-tabs"');
    expect(viewReportPanelSource).toContain('class="report-tabs legacy-tabs"');
    expect(sudokuPanelsSource).toContain('class="legacy-tabs"');
    expect(legacyPaginationSource).toContain("event.page + 1");
    expect(legacyPaginationSource).toContain("<Paginator");
    expect(legacyPaginationSource).toContain('class="record-info"');
    expect(legacyPaginationSource).toContain(':page-link-size="7"');
    expect(legacyPaginationSource).toContain('template="PrevPageLink PageLinks NextPageLink"');
    expect(legacyPaginationSource).toContain('<template #previcon><span aria-hidden="true">&laquo;</span></template>');
    expect(legacyPaginationSource).toContain('<template #nexticon><span aria-hidden="true">&raquo;</span></template>');
    expect(legacyPaginationSource).not.toContain("FirstPageLink");
    expect(legacyPaginationSource).not.toContain("LastPageLink");
    expect(viewListPanelSource).not.toContain("更新时间 {{ resultFreshTime }}");
    expect(listDataTableSource).toContain('header="操作"');
  });

  it("renders the legacy viewWithChart template as data and chart panes", () => {
    expect(appSource).toContain("viewUsesChartTemplate(viewResponse.value?.data)");
    expect(viewListPanelSource).toContain("legacyChartData(rows.value)");
    expect(viewListPanelSource).toContain("activePane");
    expect(appSource).toContain("viewNavigationRevision.value += 1");
    expect(appSource).toContain(':navigation-revision="viewNavigationRevision"');
    expect(viewListPanelSource).toContain("watch([currentViewId, templateKind, () => props.navigationRevision]");
    expect(viewListPanelSource).toContain('activePane.value = "table";');
    expect(viewListPanelSource).toContain("const chartPaneHeight = ref(0)");
    expect(viewListPanelSource).toContain("await nextTick()");
    expect(viewListPanelSource).toContain("chartTablePane.value?.getBoundingClientRect().height");
    expect(viewListPanelSource).toContain(':rendered-height="chartPaneHeight"');
    expect(viewListPanelSource).toContain("<Tabs");
    expect(viewListPanelSource).toContain("<Tab value=\"chart\"");
    expect(viewListPanelSource).toContain('<Tab value="table">数据</Tab>');
    expect(viewListPanelSource).toContain('<Tab value="chart">图表</Tab>');
    expect(viewListPanelSource).toContain('v-if="supportedTemplate && !sudokuView" class="workflow-toolbar"');
    expect(viewListPanelSource).toContain(":class=\"{ 'chart-workflow-toolbar': chartView }\"");
    expect(viewListPanelSource).toContain('<template v-if="listView">');
    expect(viewListPanelSource).not.toContain('class="pi pi-chart-line"');
    expect(viewListPanelSource).toContain("<LegacyChartPanel");
    expect(viewListPanelSource).toContain('v-if="chartView && activePane === \'chart\'" :data="chartData"');
    expect(viewListPanelSource).not.toContain("暂无图表数据。");
    expect(legacyChartPanelSource).toContain('return props.data.labels[index] || ""');
    expect(legacyChartPanelSource).toContain('v-if="index < data.labels.length && showLabel(index)"');
    expect(legacyChartPanelSource).toContain('class="legacy-chart"');
    expect(legacyChartPanelSource).toContain("renderedHeight?: number");
    expect(legacyChartPanelSource).toContain(":style=\"paneStyle\"");
    expect(legacyChartPanelSource).toContain("props.compact || fixedHeight.value");
    expect(legacyChartPanelSource).toContain("const renderedPlotWidth = plotWidth * renderedWidth.value / width.value");
    expect(legacyChartPanelSource).toContain("function linePath");
    expect(legacyChartPanelSource).toContain('pointsPath(basePoints.reverse(), "L", smoothBase)');
    expect(legacyChartPanelSource).toContain('class="chart-line-area"');
    expect(legacyChartPanelSource).toContain('class="chart-line"');
    expect(legacyChartPanelSource).not.toContain('class="chart-line-hit"');
    expect(legacyChartPanelSource).toContain('class="chart-value-label"');
    expect(legacyChartPanelSource).toContain('v-if="data.axisName" class="chart-axis-name"');
    expect(legacyChartPanelSource).toContain(':y="height - 2"');
    expect(legacyChartPanelSource).toContain("if (labelCount.value === 1) return plot.left + plotWidth / 2");
    expect(legacyChartPanelSource).toContain("plotWidth * index / (labelCount.value - 1)");
    expect(legacyChartPanelSource).not.toContain("plotWidth * (index + 0.5) / labelCount.value");
    expect(legacyChartPanelSource).not.toContain("<polyline");
    expect(legacyChartPanelSource).toContain('class="chart-bar"');
    expect(legacyChartPanelSource).not.toContain('rx="2"');
    expect(legacyChartPanelSource).not.toContain("Math.max(1, Math.abs(y(renderedValue");
    expect(legacyChartPanelSource).toContain("renderedWidth.value = rect.width");
    expect(legacyChartPanelSource).toContain("props.compact ? 28 : 15 * width.value / renderedWidth.value");
    expect(legacyChartPanelSource).toContain(':x2="width - plotRight"');
    expect(legacyChartPanelSource).not.toContain("right: 18");
    expect(legacyChartPanelSource).toContain("const hiddenSeriesNames = ref<string[]>([])");
    expect(legacyChartPanelSource).toContain("const visibleSeries = computed(() => props.data.series.filter(isSeriesVisible))");
    expect(legacyChartPanelSource).toContain("geometry.value.domainValues");
    expect(legacyChartPanelSource).toContain("legacyChartScale");
    expect(legacyChartPanelSource).toContain("legacyChartStackGeometry");
    expect(legacyChartPanelSource).toContain("legacyChartStackGeometry(visibleSeries.value)");
    expect(legacyChartPanelSource).toContain("geometry.value.barGroups");
    expect(legacyChartPanelSource).toContain("legendSeries");
    expect(legacyChartPanelSource).toContain(':aria-pressed="isSeriesVisible(series, index)"');
    expect(legacyChartPanelSource).toContain('@click="toggleSeries(series, index)"');
    expect(legacyChartPanelSource).toContain(":style=\"{ display: isSeriesVisible(series, seriesIndex) ? undefined : 'none' }\"");
    expect(legacyChartPanelSource).toContain("const activeTooltipIndex = ref<number | null>(null)");
    expect(legacyChartPanelSource).toContain('class="chart-axis-hit"');
    expect(legacyChartPanelSource).toContain('@mousemove="showAxisTooltip"');
    expect(legacyChartPanelSource).toContain('class="chart-axis-pointer"');
    expect(legacyChartPanelSource).toContain('role="tooltip"');
    expect(legacyChartPanelSource).toContain("isSeriesVisible(series, index) && hasTooltipValue(series)");
    expect(legacyChartPanelSource).not.toContain("<title>");
    expect(legacyChartPanelSource).toContain("<circle");
    expect(legacyChartPanelSource).not.toContain("<meter");
    expect(legacyChartPanelSource).toContain('"#c23531", "#2f4554", "#61a0a8"');
    expect(legacyChartPanelSource).toContain("`系列 ${index + 1}`");
    expect(legacyChartPanelSource).toContain('aria-label="视图数据图表"');
    expect(legacyChartPanelSource).toContain("'compact-chart': compact");
    expect(legacyChartPanelSource).toContain("rect.width * height / rect.height");
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
    expect(viewListPanelSource).toContain('v-if="supportedTemplate && !sudokuView" class="workflow-toolbar"');
  });

  it("renders the legacy Sudoku template from ViewFile panels", () => {
    expect(appSource).toContain("viewUsesSudokuTemplate(viewResponse.value?.data)");
    expect(viewListPanelSource).toContain("SudokuPanels");
    expect(viewListPanelSource).toContain('v-if="!sudokuView" class="panel-heading"');
    expect(viewListPanelSource).toContain('v-if="supportedTemplate && !sudokuView" class="workflow-toolbar"');
    expect(viewListPanelSource).toContain('v-if="supportedTemplate && !sudokuView" v-show="tableVisible"');
    expect(appSource).toContain("useSudokuPanels");
    expect(appSource).toContain("loadSudokuPanels");
    expect(sudokuWorkflowSource).toContain('loadViewDataById(panelViewId, "sudoku-panel", 5, silentTransport)');
    expect(sudokuWorkflowSource).toContain("scheduleRefresh(panel, next)");
    expect(appSource).toContain("stopSudokuPanelRefresh()");
    expect(viewListPanelSource).toContain("@refresh-panel=\"emit('refreshPanel', $event)\"");
    expect(sudokuPanelsSource).toContain("listFreshTime");
    expect(sudokuPanelsSource).toContain('sudokuPanelKind(panel) !== "list"');
    expect(sudokuPanelsSource).toContain("emit('refreshPanel', panel)");
    expect(sudokuPanelsSource).toContain("sudokuPanelManualRefreshable(panel)");
    expect(sudokuPanelsSource).toContain('["list", "linechart", "map", "item"]');
    expect(sudokuPanelsSource).toContain('class="sudoku-panel-passive-refresh"');
    expect(sudokuPanelsSource).toContain("<span>更新时间 {{ sudokuPanelFreshTime(panel) }}</span>");
    expect(sudokuWorkflowSource).toContain('if (kind === "map")');
    expect(sudokuPanelsSource).toContain('class="sudoku-grid"');
    expect(sudokuPanelsSource).toContain(':class="`sudoku-panel-${sudokuPanelKind(panel)}`"');
    expect(sudokuPanelsSource).toContain("<span>{{ fieldTitle(panel) }}</span>");
    expect(sudokuPanelsSource).not.toContain("<strong>{{ fieldTitle(panel) }}</strong>");
    expect(sudokuPanelsSource.match(/class="sudoku-panel-detail-link"/g)).toHaveLength(2);
    expect(sudokuPanelsSource).toContain("allPanelsReady");
    expect(sudokuPanelsSource).toContain("gridAutoRows: panelHeight || 'auto'");
    expect(sudokuPanelsSource).toContain("Math.max(...heights)");
    expect(sudokuPanelsSource).toContain("sudokuPanelWidth(panel)");
    expect(sudokuPanelsSource.match(/:minimum-rows="5"/g)).toHaveLength(2);
    expect(sudokuPanelsSource).not.toContain("rows loaded");
  });

  it("loads Sudoku child panels without requiring root querydata", () => {
    const start = appSource.indexOf("async function queryCurrentViewData");
    const source = appSource.slice(start, appSource.indexOf("async function loadResultPage", start));
    expect(source).toContain("if (isSudokuView.value)");
    expect(source.indexOf("await loadSudokuPanels()")).toBeLessThan(source.indexOf("queryCurrentViewDataBase()"));
  });

  it("renders Sudoku linechart panels from child row chart items", () => {
    expect(sudokuPanelsSource).toContain('v-else-if="sudokuPanelKind(panel) === \'linechart\'"');
    expect(sudokuPanelsSource).not.toContain("linechart' && sudokuPanelChart(panel).series.length");
    expect(sudokuPanelsSource).toContain("<LegacyChartPanel");
    expect(sudokuPanelsSource).toContain("compact");
    expect(sudokuPanelsSource).toContain(':title="fieldTitle(panel)"');
    expect(legacyChartPanelSource).toContain('v-if="compact && title" class="chart-title"');
    expect(sudokuPanelsSource).toContain("result?.chart ?? legacyChartData");
    expect(sudokuWorkflowSource).toContain('kind === "item" || kind === "linechart"');
    expect(sudokuWorkflowSource).toContain('postApi<QueryDataDetailResult>("/api/v1/data/querydatadetail"');
    expect(sudokuWorkflowSource).toContain("appendLegacyChartSample");
    expect(sudokuWorkflowSource).toContain('kind === "linechart" ? result.detail : result.data');
    expect(sudokuPanelsSource).not.toContain("<meter");
  });

  it("renders Sudoku map panels from child row map items", () => {
    expect(sudokuPanelsSource).toContain('v-else-if="sudokuPanelKind(panel) === \'map\'"');
    expect(sudokuPanelsSource).not.toContain("map' && sudokuPanelMarkers(panel).length");
    expect(sudokuPanelsSource).toContain("sudokuPanelMarkers(panel)");
    expect(sudokuPanelsSource).toContain("<LegacyMapPanel");
    expect(legacyMapPanelSource).toContain('import("leaflet")');
    expect(legacyMapPanelSource).toContain("leaflet.map(mapElement.value, { scrollWheelZoom: true })");
    expect(legacyMapPanelSource).toContain("watch(() => props.markers, renderMarkers, { deep: true })");
    expect(legacyMapPanelSource).toContain("markerLayer.clearLayers()");
    expect(legacyMapPanelSource).toContain("map.setView([39.94917, 116.32], 18)");
    expect(legacyMapPanelSource).toContain("leafletApi.circleMarker");
    expect(legacyMapPanelSource).toContain("if (marker.title || marker.info.length) {");
    expect(legacyMapPanelSource).toContain("{ minWidth: 240, maxWidth: 240 }");
    expect(legacyMapPanelSource).toContain('content.className = "legacy-map-popup"');
    expect(legacyMapPanelSource).toContain("index += 2");
    expect(legacyMapPanelSource).toContain("map.fitBounds");
    expect(legacyMapPanelSource).toContain("title.textContent");
    expect(legacyMapPanelSource).toContain("marker.info[0]");
    expect(legacyMapPanelSource).not.toContain("map-location-list");
    expect(legacyMapPanelSource).not.toContain("没有有效的地图位置。");
    expect(legacyMapPanelSource).not.toContain('"Location"');
    expect(legacyMapPanelSource).not.toContain('|| "位置"');
  });

  it("renders Sudoku item panels from legacy detail SimpleData", () => {
    const sudokuLoadSource = sudokuWorkflowSource;

    expect(sudokuPanelsSource).toContain('v-else-if="sudokuPanelKind(panel) === \'item\'"');
    expect(sudokuPanelsSource).not.toContain("item' && sudokuPanelItemFields(panel).length");
    expect(sudokuPanelsSource).toContain("sudokuPanelItemFields(panel)");
    expect(sudokuPanelsSource).toContain("<LegacyItemPanel");
    expect(legacyItemPanelSource).toContain("Math.max(6, Math.ceil(props.fields.length / 2))");
    expect(legacyItemPanelSource).toContain('colspan="2"');
    expect(sudokuLoadSource).toContain('kind === "item" || kind === "linechart"');
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
    expect(sudokuPanelsSource).toContain('v-else-if="sudokuPanelKind(panel) === \'group\'"');
    expect(sudokuPanelsSource).not.toContain("group' && sudokuGroupPanels(panel).length");
    expect(sudokuPanelsSource).toContain('<Tabs v-if="sudokuGroupPanels(panel).length"');
    expect(sudokuPanelsSource).toContain("<TabList>");
    expect(sudokuPanelsSource).toContain("这是简单项");
    expect(sudokuPanelsSource).toContain('class="sudoku-simple-item"');
    expect(sudokuPanelsSource).not.toContain('class="empty-state compact">这是简单项');
    expect(sudokuPanelsSource).not.toContain("暂无数据。");
  });

  it("resets plain View search while preserving the chart View page", () => {
    const searchSource = appSource.slice(
      appSource.indexOf("async function searchCurrentView"),
      appSource.indexOf("async function loadLegacyDetailPath")
    );
    expect(searchSource).toContain("if (!isChartView.value) pageIndex.value = 1");
    expect(searchSource).not.toContain("stopAutoRefresh()");
    expect(searchSource.indexOf("if (!isChartView.value) pageIndex.value = 1")).toBeLessThan(
      searchSource.indexOf("queryCurrentViewData()")
    );
    expect(viewListPanelSource).toContain("emit('search')");
    expect(appSource).toContain('@search="searchCurrentView"');
  });

  it("keeps the Vue workspace on view-id driven legacy view and data APIs", () => {
    expect(viewDataWorkflowSource).toContain("/api/v1/view/getlistview");
    expect(viewDataWorkflowSource).toContain("/api/v1/data/querydata");
    expect(viewDataWorkflowSource).toContain("{ silentTransport: true }");
    expect(appSource).toContain("options.silentTransport === true && isTransportError(error)");
    expect(viewDataWorkflowSource).toContain("viewDetailViewId(view, loadedViewId)");
    expect(appSource).toContain("function openListObject(row: ListDataItem, targetViewId = 0)");
    expect(appSource).toContain("legacyDetailHref(targetViewId");
    expect(appSource).toContain("function openNewObject(targetViewId: number)");
    expect(appSource).toContain("legacyNewHref(targetViewId)");
    expect(appSource).toContain("async function startNewObject(viewId = Number(detailViewId.value)");
    expect(appSource).toContain("async function queryDetail(viewId = Number(detailViewId.value)");
    expect(appSource).toContain("await queryDetail(route.viewId, objectId)");
    expect(appSource).toContain("detailResultViewName(detailResponse.value?.data) || String(detailViewId.value)");
    expect(appSource.match(/viewID: detailSaveViewKey\.value/g)).toHaveLength(2);
    expect(appSource).not.toContain("viewID: String(detailViewId.value)");
    expect(viewDataWorkflowSource).toContain("listRenderColumns(viewResponse.value?.data)");
    expect(appSource).not.toContain("Object.keys(first)");
    expect(appSource).not.toContain("viewName: viewName.value");
    expect(appSource).not.toContain("/api/v1/view/get-view");
    expect(appSource).not.toContain("/api/v1/data/query-list");
    expect(appSource).not.toContain("buildQueryRequest");
  });

  it("keeps legacy candidate query transport failures silent", () => {
    const candidateQuerySource = appSource.slice(
      appSource.indexOf("async function queryExistingDetailItems"),
      appSource.indexOf("function addExistingDetailItem")
    );
    expect(candidateQuerySource).toContain('"child-select-data"');
    expect(candidateQuerySource).toContain("{ silentTransport: true }");
  });

  it("keeps the legacy candidate View loader pending on transport failures", () => {
    const actionRunnerSource = appSource.slice(
      appSource.indexOf("async function runAction"),
      appSource.indexOf("async function initApp")
    );
    const candidateViewSource = appSource.slice(
      appSource.indexOf("async function loadExistingDetailView"),
      appSource.indexOf("async function queryExistingDetailItems")
    );

    expect(actionRunnerSource).toContain("let preservePending = false");
    expect(actionRunnerSource).toContain("options.preservePendingOnTransport === true");
    expect(actionRunnerSource).toContain('if (!preservePending) pendingAction.value = ""');
    expect(candidateViewSource).toContain("{ silentTransport: true, preservePendingOnTransport: true }");
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
      appSource.indexOf("async function loadExistingDetailView"),
      appSource.indexOf("function addExistingDetailItem")
    );

    expect(columnsSource).toContain("if (!view)");
    expect(childSource.indexOf("/api/v1/view/getlistview")).toBeLessThan(childSource.indexOf("/api/v1/data/querydata"));
    expect(columnsSource).toContain("return viewColumns(view)");
    expect(columnsSource).not.toContain("columnsFromListResult");
    expect(columnsSource).not.toContain("columnsFromRowItems");
    expect(childSource).toContain("setCandidateView(group, viewColumns(view.data))");
    expect(childSource).toContain("setCandidateResults(group, candidateColumns(group), listRows(data.data)");
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
      viewDetailPanelSource.indexOf('class="table-wrap detail-items-table"')
    );

    expect(viewDetailPanelSource).toContain("groupSelectFromExists(group)");
    expect(pickerSource).toContain('class="candidate-query-toolbar"');
    expect(viewDetailPanelSource).toContain('<template #closeicon><span class="legacy-dialog-close-icon" aria-hidden="true">&times;</span></template>');
    expect(pickerSource).toContain('class="candidate-query-input"');
    expect(pickerSource).toContain('placeholder="输入条件"');
    expect(pickerSource).toContain('aria-label="查询条件"');
    expect(pickerSource).not.toContain("<label>");
    expect(viewDetailPanelSource).toContain("<LegacyPagination");
    expect(pickerSource).toContain(':minimum-rows="candidateState(group).queried ? candidateState(group).pageSize : 0"');
    expect(viewDetailPanelSource).toContain(':total-items="candidateState(group).totalItem"');
    expect(viewDetailPanelSource).toContain('@page="emit(\'loadCandidatePage\', group, $event)"');
    expect(pickerSource).toContain('label="取消" severity="secondary" outlined');
    expect(pickerSource).not.toContain('severity="secondary" text');
    expect(pickerSource).not.toContain('label="关闭"');
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
    expect(viewDetailPanelSource).not.toContain("groupItems(group).length");
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
    expect(appSource).toContain("autoRefreshInterval === seconds");
    expect(appSource).toContain("if (viewTableVisible.value)");
    expect(appSource).toContain("pageIndex.value = 1");
    expect(appSource).not.toContain("if (!pendingAction.value)");
    expect(appSource).toContain("onUnmounted(() => {");
    expect(appSource).toContain("stopAutoRefresh()");
    expect(appSource).toContain("stopShellPolling()");
  });

  it("keeps metadata lookup tied to the rendered View identity", () => {
    expect(metadataFieldEditorSource).toContain("viewId: props.viewId");
    expect(metadataFieldEditorSource).toContain("viewName: props.viewName");
    expect(metadataFieldEditorSource).toContain("<AutoComplete");
    expect(metadataFieldEditorSource).toContain('@complete="searchLookup($event.query)"');
    expect(metadataFieldEditorSource).toContain('@option-select="selectLookup($event.value)"');
    expect(metadataFieldEditorSource).toContain("ref<string | LookupChoice>(props.readonlyValue || props.modelValue)");
    expect(metadataFieldEditorSource).toContain("watch(() => props.field");
    expect(metadataFieldEditorSource).toContain("lookupTerm.value = props.readonlyValue || props.modelValue");
    expect(metadataFieldEditorSource).toContain('@update:model-value="updateLookupTerm"');
    expect(metadataFieldEditorSource).toContain('if (term === "" || term === null) {');
    expect(metadataFieldEditorSource).toContain('emit("update:modelValue", "")');
    expect(metadataFieldEditorSource).not.toContain(':placeholder="readonlyValue || modelValue"');
    expect(metadataFieldEditorSource).toContain("未找到匹配的选项");
    expect(metadataFieldEditorSource).toContain("&ndash; {{ option.id }}");
    expect(metadataFieldEditorSource).toContain("查找更多");
    expect(metadataFieldEditorSource).toContain('import { isTransportError, postApi } from "./api"');
    expect(metadataFieldEditorSource).toContain("if (!isTransportError(error))");
    expect(metadataFieldEditorSource).not.toContain("lookupDisabled");
    expect(appSource).not.toContain("lookupDisabled");
    expect(metadataFieldEditorSource).not.toContain("<InputGroup>");
    expect(metadataFieldEditorSource).not.toContain("<Listbox");
  });

  it("passes parent context into child lookup editors", () => {
    expect(metadataFieldEditorSource).toContain("ownerId: props.ownerId");
    expect(appSource).toContain("detailResultParentId(detailResponse.value?.data) || newObjectOwner.ownerId");
    expect(appSource).toContain("ownerId: detailOwnerId.value");
    expect(viewDetailPanelSource).toContain(':owner-id="selectedObjectId"');
    expect(viewDetailPanelSource).toContain(':is-added="isPendingAddedItem(group, item)"');
    expect(viewDetailPanelSource).toContain(':object-id="itemDataId(item)"');
    expect(viewDetailPanelSource).toContain(':view-id="groupListViewId(group)"');
    expect(viewDetailPanelSource).toContain(':view-name="groupViewName(group)"');
  });

  it("keeps child item ids in interaction state instead of a hard-coded table column", () => {
    expect(viewDetailPanelSource).not.toContain("<th>ID</th>");
    expect(viewDetailPanelSource).not.toContain("<td>{{ itemDataId(item) }}</td>");
    expect(viewDetailPanelSource).toContain(":object-id=\"itemDataId(item)\"");
    expect(viewDetailPanelSource).toContain("groupDetailViewId(group)}/${itemDataId(item)}");
  });

  it("renders PrimeVue metadata controls without changing legacy string values", () => {
    expect(metadataFieldEditorSource).toContain('const readonlyText = computed(() => props.readonlyValue || props.modelValue || "\\u00a0")');
    expect(metadataFieldEditorSource).toContain('<span v-if="isReadonlyField(field)" class="metadata-readonly-value">');
    expect(metadataFieldEditorSource).not.toContain('<InputText v-if="isReadonlyField(field)"');
    expect(metadataFieldEditorSource).toContain("fieldInputChecked");
    expect(metadataFieldEditorSource).toContain("fieldInputMaxLength");
    expect(metadataFieldEditorSource).toContain("fieldInputType");
    expect(metadataFieldEditorSource).toContain("fieldInputValue");
    expect(metadataFieldEditorSource).toContain("sanitizeFieldInput");
    expect(metadataFieldEditorSource).toContain("isMultilineField");
    expect(metadataFieldEditorSource).toContain("fieldInputChecked(props.field, value.value)");
    expect(metadataFieldEditorSource).toContain(':type="fieldInputType(field)"');
    expect(metadataFieldEditorSource).toContain(':maxlength="fieldInputMaxLength(field)"');
    expect(metadataFieldEditorSource).toContain(':model-value="fieldInputValue(field, value)"');
    expect(metadataFieldEditorSource).toContain('@update:model-value="value = sanitizeFieldInput(field, $event)"');
    expect(metadataFieldEditorSource).toContain("<Textarea v-else-if");
    expect(metadataFieldEditorSource).toContain("<Checkbox v-model=\"checked\"");
    expect(metadataFieldEditorSource).toContain("next ? \"true\" : \"false\"");
    expect(metadataFieldEditorSource).not.toContain('checked ? "是" : "否"');
    expect(metadataFieldEditorSource).not.toContain('<small v-if="modelValue">');
    expect(metadataFieldEditorSource).toContain('response.message || "查找失败。"');
  });

  it("uses only rendered metadata as the frontend ViewName source", () => {
    const inputQueryRequestSource = apiSource.slice(
      apiSource.indexOf("export interface InputQueryRequest"),
      apiSource.indexOf("export interface InputQueryItem")
    );

    expect(appSource).not.toContain("const viewName = ref");
    expect(appSource).not.toContain("viewName.value");
    expect(appSource).toContain("viewName: detailResultViewName(detailResponse.value?.data)");
    expect(payloadSource).toContain("viewName?: string");
    expect(payloadSource).toContain("request.viewName = input.viewName.trim()");
    expect(inputQueryRequestSource).toContain("viewName?: string");
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
    expect(detailSource).toContain("{ silentTransport: true }");
  });

  it("renders detail fields from read-item View metadata, not raw DTO rows", () => {
    expect(appSource).toContain("renderedDetailFields(currentReadItemView.value");
    expect(appSource).not.toContain('v-if="detailDataRows.length"');
    expect(appSource).not.toContain('v-for="item in detailDataRows"');
  });

  it("keeps child update fallback drafts on rendered group columns", () => {
    const updateSource = appSource.slice(
      appSource.indexOf("function updateDetailItem"),
      appSource.indexOf("function deleteDetailItem")
    );

    expect(updateSource).toContain("buildGroupItemDrafts(group, item)");
    expect(updateSource).toContain("stageItemProperty");
    expect(updateSource).not.toContain("saveObj(");
    expect(updateSource).not.toContain("queryDetail()");
    expect(updateSource).not.toContain("buildFieldDrafts(detailItemValues(item))");
    expect(viewDetailPanelSource).toContain("editingItemKey");
    expect(viewDetailPanelSource).toContain("toggleDetailItem(group, item)");
    expect(viewDetailPanelSource).toContain("stageEditingItem()");
    expect(viewDetailPanelSource).toContain("editingItemKey === itemKey(group, item) ? '保存' : '编辑'");
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
    expect(createSource).toContain("detailResultObjectId(initialized.data) || nextObjectId()");
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
    expect(appSource).toContain("if (routeViewId) {");
    expect(appSource).toContain("applyRequestedViewId(routeViewId)");
    expect(appSource).toContain("await loadViewWorkflow();");
    expect(appSource).toContain("if (defaultViewId && !legacyListViewId.value) applyRequestedViewId(defaultViewId)");
  });

  it("returns Home and the desktop brand to the configured default View", () => {
    const homeSource = appSource.slice(
      appSource.indexOf("async function openPrimarySection"),
      appSource.indexOf("async function openMobilePrimarySection")
    );
    expect(homeSource).toContain("async function openPrimarySection()");
    expect(homeSource).toContain("await loadPrimarySection(true)");
    expect(homeSource).toContain("async function loadPrimarySection(updatePath: boolean)");
    expect(homeSource).toContain("await ensureLegacyShell()");
    expect(homeSource).toContain('if (updatePath) pushLegacyPath("/")');
    expect(homeSource).toContain("legacyAppDefaultViewId(mainInfoResponse.value?.data)");
    expect(homeSource).toContain("applyRequestedViewId(defaultViewId)");
    expect(homeSource).toContain("await loadViewWorkflow(true)");
    expect(homeSource).toContain("showUnconfiguredHome.value = true");
    expect(homeSource).toContain('window.location.pathname === "/main"');
    expect(appSource).toContain("await loadPrimarySection(false);");
    expect(appSource).toContain("async function openMobilePrimarySection()");
    expect(appSource).toContain("await openPrimarySection();");
    expect(appSource).toContain('<a href="/" @click.prevent="openPrimarySection">');
    expect(appSource).toContain('replaceLegacyPath("/main")');
    expect(appSource).toContain("默认首页 还没有配置");
    expect(appSource).toContain("欢迎使用SOWAY无码系统，这是默认的首页，没有配置，请参考相关说明进行设定");
    expect(appSource).toContain("{{ unconfiguredHomeMessage }}");
    expect(appSource.match(/showUnconfiguredHome\.value = false;/g)?.length).toBeGreaterThanOrEqual(4);
  });

  it("starts old Web detail and new paths through existing View-first detail flows", () => {
    expect(appSource).toContain("legacyDetailPath(window.location.pathname)");
    expect(appSource).toContain("await loadLegacyDetailPath(detailRoute)");
    expect(appSource).toContain("isStandaloneDetail.value = true");
    expect(appSource).toContain('v-if="!showUnconfiguredHome && !isMetadataOnlyView && !isStandaloneDetail"');
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
    expect(viewDetailPanelSource).not.toContain("已加载视图定义。");
    expect(viewDetailPanelSource).not.toContain("请从列表选择记录。");
    expect(viewDetailPanelSource).toContain('v-if="detailItemGroups.length && (selectedObjectId || schemaOnly)"');
    expect(viewDetailPanelSource).toContain('v-if="!schemaOnly" class="detail-collection-toolbar legacy-button-group"');
    expect(viewDetailPanelSource).toContain(':colspan="schemaOnly ? 1 : childActionColumnCount(group)"');
    expect(viewDetailPanelSource).toContain('<tbody v-if="!schemaOnly">');
    expect(viewDetailPanelSource).not.toContain('v-if="schemaOnly && detailItemGroups.length" class="view-items-panel"');
  });

  it("keeps getmain transport failures on the shell while returning stale tokens to login", () => {
    const mainInfoSource = appSource.slice(
      appSource.indexOf("async function loadMainInfo"),
      appSource.indexOf("async function loadCheckCode")
    );
    const shellSource = appSource.slice(
      appSource.indexOf("async function ensureLegacyShell"),
      appSource.indexOf("async function loadViewWorkflow")
    );

    expect(mainInfoSource).toContain("{ silentTransport: true }");
    expect(shellSource).toContain("if (await loadMainInfo()) return true");
    expect(shellSource).toContain("if (!errorMessage.value) return false");
    expect(shellSource.indexOf("if (!errorMessage.value) return false")).toBeLessThan(
      shellSource.indexOf("clearLegacySession()")
    );
    expect(shellSource).toContain("clearLegacySession()");
    expect(shellSource).toContain("await prepareLegacyLogin()");
    expect(shellSource).toContain("return false");
    expect(shellSource).not.toContain("loginV2");
  });

  it("keeps legacy top menus visible while opening child View ids", () => {
    const subMenuRequestSource = appSource.slice(
      appSource.indexOf("async function loadSubMenu"),
      appSource.indexOf("async function openShellMenu")
    );
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
    expect(legacyMenuNavSource).toContain(':aria-expanded="legacyAuthViewId(item) ? undefined : legacyAuthNo(item) === expandedAuthCode"');
    expect(legacyMenuNavSource).toContain('<button v-if="legacyAuthViewId(child)"');
    expect(legacyMenuNavSource).toContain('<span v-else class="nav-static-item">');
    expect(legacyMenuNavSource).not.toContain("disabled");
    expect(appSource).not.toContain('<LegacyMenuNav\n          :disabled="Boolean(pendingAction)"');
    expect(legacyMenuNavSource).not.toContain('disabled || !legacyAuthViewId(child)');
    expect(menuSource).toContain("legacyAuthViewId(item)");
    expect(menuSource).toContain("closeShellNavigation()");
    expect(menuSource).toContain("applyRequestedViewId(itemViewId)");
    expect(menuSource).not.toContain("legacyQueryViewId.value = itemViewId");
    expect(menuSource).toContain("await loadViewWorkflow(true)");
    expect(menuSource).toContain("subMenuParentAuthCode.value = authNo");
    expect(menuSource).toContain("if (authNo === subMenuParentAuthCode.value)");
    expect(menuSource).toContain("subMenuResponse.value = null");
    expect(menuSource).toContain("await loadSubMenu()");
    expect(subMenuRequestSource).toContain("{ silentTransport: true }");
    expect(menuSource).not.toContain("if (legacyAuthViewId(item)) mobileMenuOpen.value = false");
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
    expect(appSource).toContain('v-if="isListView && !isMetadataOnlyView && !isStandaloneDetail && currentViewId"');
    expect(appSource).toContain(':visible="showViewReport"');
    expect(appSource).toContain("showViewReport.value = false;");
    expect(appSource).toContain(':view-id="currentViewId"');
    expect(viewReportPanelSource).toContain("<Dialog");
    expect(viewReportPanelSource).toContain("modal");
    expect(viewReportPanelSource).toContain('<template #closeicon><span class="legacy-dialog-close-icon" aria-hidden="true">&times;</span></template>');
    expect(viewReportPanelSource).toContain('<Tab value="output"');
    expect(viewReportPanelSource).toContain('<Tab value="conditions"');
    expect(viewReportPanelSource).toContain('<Tab value="save"');
    expect(viewReportPanelSource).not.toContain('class="pi pi-table"');
    expect(viewReportPanelSource).not.toContain('class="pi pi-filter"');
    expect(viewReportPanelSource).not.toContain('<Tab value="save"><i');
    expect(viewReportPanelSource).toContain("生成报表");
    expect(viewReportPanelSource).toContain("报表结果");
    expect(viewReportPanelSource).toContain("报表结果 共{{ resultPages }}页 当前第{{ resultPage }}页");
    expect(viewReportPanelSource).toContain('<table class="report-result-table">');
    expect(viewReportPanelSource).not.toContain("暂无报表数据。");
    expect(viewReportPanelSource).toContain('class="report-result-actions legacy-button-group-xs"');
    expect(viewReportPanelSource).toContain('label="前一页"');
    expect(viewReportPanelSource).toContain('label="下一页"');
    expect(viewReportPanelSource).not.toContain('label="前一页" size="small"');
    expect(viewReportPanelSource).toContain("function changeReportPage(offset: number)");
    expect(viewReportPanelSource).toContain("page > Math.max(1, resultPages.value)");
    expect(viewReportPanelSource).toContain('label="前一页" severity="secondary" outlined @click="changeReportPage(-1)"');
    expect(viewReportPanelSource).toContain('label="下一页" severity="secondary" outlined @click="changeReportPage(1)"');
    const currentPageExport = viewReportPanelSource.match(/<Button[^>]*label="导出当前页"[^>]*\/>/)?.[0] ?? "";
    const allPagesExport = viewReportPanelSource.match(/<Button[^>]*label="导出全部"[^>]*\/>/)?.[0] ?? "";
    expect(currentPageExport).toContain('label="导出当前页"');
    expect(allPagesExport).toContain('label="导出全部"');
    expect(currentPageExport).not.toContain("@click");
    expect(allPagesExport).not.toContain("@click");
    expect(viewReportPanelSource).toContain('class="report-result-table"');
    expect(viewReportPanelSource).toContain("backToReportSetup");
    expect(viewReportPanelSource).toContain("currentPage.value = 1");
    expect(viewReportPanelSource).not.toContain("<Paginator");
    expect(viewReportPanelSource).not.toContain("<DataTable");
    expect(viewReportPanelSource).not.toContain("视图 {{ viewId }}");
    expect(viewReportPanelSource).toContain("/api/v1/report/mkrpt");
    expect(viewReportPanelSource).toContain("reportResponse");
    expect(viewReportPanelSource).toContain("showingResults.value = true");
    expect(viewReportPanelSource).toContain("const reportSetupLoading = ref(true)");
    expect(viewReportPanelSource).toContain("const reportRunning = ref(false)");
    expect(viewReportPanelSource).toContain('v-if="visible && !reportSetupLoading && !reportRunning"');
    const runReportSource = viewReportPanelSource.slice(
      viewReportPanelSource.indexOf("async function runReport"),
      viewReportPanelSource.indexOf("function changeReportPage")
    );
    expect(runReportSource).toMatch(/reportRunning\.value = revealResults;[\s\S]*runSuccessOnlyAction\("mkrpt"/);
    expect(viewReportPanelSource).toContain('@click="backToReportSetup"');
    expect(appSource).not.toContain("Report Grid");
  });

  it("builds report output and conditions from View metadata", () => {
    expect(viewReportPanelSource).toContain("/api/v1/report/getmkqview");
    expect(viewReportPanelSource).toContain("ReportOutputSelector");
    expect(viewReportPanelSource).toContain('v-model="reportCols"');
    expect(viewReportPanelSource).toContain(':columns="modelColumns"');
    expect(viewReportPanelSource).not.toContain("暂无报表字段。");
    expect(reportOutputSelectorSource).toContain('defineModel<{ label: string; value: string }[]>("queryTypeOptions"');
    expect(reportOutputSelectorSource).toContain("queryTypeOptions.value = candidate");
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
    expect(reportOutputSelectorSource).toContain('aria-label="加入已选列" size="small" severity="secondary" outlined');
    expect(reportOutputSelectorSource).not.toContain("disabled || !selectedCandidate");
    expect(reportOutputSelectorSource).toContain("if (!candidate) return;");
    expect(reportOutputSelectorSource).toContain("const hadOutputs = outputs.value.length > 0;");
    expect(reportOutputSelectorSource).toContain("if (next === outputs.value) return;");
    expect(reportOutputSelectorSource).toContain("if (!hadOutputs) selectedOutputIndex.value = 0;");
    expect(reportOutputSelectorSource).not.toContain("const index = next.findIndex");
    expect(reportOutputSelectorSource).toContain("min-height: 180px");
    expect(reportOutputSelectorSource).toContain("moveOutput");
    expect(reportOutputSelectorSource.match(/class="legacy-button-group-xs"/g)).toHaveLength(2);
    expect(reportOutputSelectorSource).toContain('role="group" aria-label="调整已选列"');
    expect(reportOutputSelectorSource).toContain('role="group" aria-label="设置排序"');
    expect(reportOutputSelectorSource).not.toContain(':disabled="disabled || selectedOutputIndex');
    expect(reportOutputSelectorSource).toContain('return "[升序]"');
    expect(reportOutputSelectorSource).toContain('return "[降序]"');
    expect(reportOutputsSource).toContain("addReportOutput");
    expect(viewReportPanelSource).toContain("filterExp");
    expect(viewReportPanelSource).toContain("合并分组");
    expect(viewReportPanelSource).toContain('icon="pi pi-list"');
    expect(viewReportPanelSource).not.toContain("pi-object-group");
    expect(viewReportPanelSource).not.toContain("未设置条件，将包含全部记录。");
    expect(viewReportPanelSource).toContain("groupReportConditions");
    expect(viewReportPanelSource).toContain("reportConditionGroupError");
    expect(viewReportPanelSource).toContain("reportConditionSelectionIds");
    expect(viewReportPanelSource).toContain('aria-label="合并分组" @click="groupSelectedConditions"');
    expect(viewReportPanelSource).toContain('v-if="!condition.groupPath.length || startsConditionGroup(condition, index)"');
    expect(viewReportPanelSource).toContain(':model-value="conditionSelectionChecked(condition)"');
    expect(viewReportPanelSource).not.toContain('<Checkbox v-model="selectedConditionIds"');
    expect(viewReportPanelSource).toContain("condition-group-marker");
    expect(viewReportPanelSource).toContain("拆分分组");
    expect(viewReportPanelSource).not.toContain("`G${id}`");
    expect(viewReportPanelSource).toContain('{ label: "与", value: "and" }');
    expect(viewReportPanelSource).toContain('columnId: ""');
    expect(viewReportPanelSource).toContain('compareId: ""');
    expect(viewReportPanelSource).toContain('<MetadataFieldEditor v-if="condition.columnId && condition.compareId"');
    expect(viewReportPanelSource).not.toContain("const column = modelColumns.value[0]");
    expect(viewReportPanelSource).not.toContain('class="condition-first"');
    expect(viewReportPanelSource).toContain('class="report-condition-header"');
    expect(viewReportPanelSource).toContain("<strong>与/或</strong>");
    expect(viewReportPanelSource).toContain("<strong>字段</strong>");
    expect(viewReportPanelSource).toContain("<strong>运算</strong>");
    expect(viewReportPanelSource).toContain("<strong>值</strong>");
    expect(viewReportPanelSource).toContain('class="report-condition-footer"');
    expect(viewReportPanelSource).not.toContain("pending || !modelColumns.length");
    expect(viewReportPanelSource.match(/aria-label="增加条件" @click="addCondition"/g)).toHaveLength(2);
    expect(viewReportPanelSource).not.toContain(' label="合并分组"');
    expect(viewReportPanelSource).not.toContain(' label="增加条件"');
    expect(reportConditionsSource).toContain("buildReportConditionFilter");
    expect(reportConditionsSource).toContain("serializeNode");
    expect(viewReportPanelSource).not.toContain("Report Columns JSON");
    expect(viewReportPanelSource).not.toContain("QueryFilter");
  });

  it("keeps the legacy inert save report command in the View report panel", () => {
    const footerSource = viewReportPanelSource.slice(viewReportPanelSource.indexOf("<template #footer>"));

    expect(viewReportPanelSource).toContain("保存报表定义");
    expect(viewReportPanelSource).toContain('const reportName = ref("")');
    expect(viewReportPanelSource).toContain("输入报表信息以保存该报表");
    expect(viewReportPanelSource).not.toContain('ref("视图报表")');
    expect(viewReportPanelSource).not.toContain("<h3>保存报表</h3>");
    expect(viewReportPanelSource).not.toContain("/api/v1/report/saverpt");
    expect(appSource).not.toContain("Save Report Definition");
    expect(footerSource).not.toContain('icon="pi pi-arrow-left"');
    expect(footerSource).not.toContain('icon="pi pi-times"');
    expect(footerSource).not.toContain('icon="pi pi-play"');
    expect(footerSource).not.toContain('icon="pi pi-save"');
    expect(footerSource).toContain('label="取消" severity="secondary" outlined');
    expect(footerSource).toContain('label="确定" @click="runReport()"');
    expect(footerSource).toContain('label="保存报表定义" severity="info"');
    expect(footerSource).not.toContain('@click="saveReport"');
    expect(viewReportPanelSource).not.toContain("canRun");
    expect(viewReportPanelSource).not.toContain("conditionsComplete");
    expect(footerSource).not.toContain('severity="secondary" text');
    expect(footerSource).toContain('label="保存报表定义" severity="info"');
  });

  it("moves legacy message polling into the signed-in shell", () => {
    const enterShellSource = appSource.slice(
      appSource.indexOf("async function enterAuthenticatedShell"),
      appSource.indexOf("function handleHistoryNavigation")
    );
    const pollShellSource = appSource.slice(
      appSource.indexOf("async function pollShellMessages"),
      appSource.indexOf("function startShellPolling")
    );
    expect(appSource).toContain("/api/v1/message/getmsg");
    expect(appSource).toContain("15_000");
    expect(pollShellSource).toContain("if (!token.value) return;");
    expect(pollShellSource).not.toContain("shellPollInFlight");
    expect(appSource).toContain("const fetchedMessages = legacyMessages(messages.data)");
    expect(appSource).toContain("activeShellMessage.value = fetchedMessages[0]");
    expect(enterShellSource).toContain("startShellPolling();");
    expect(enterShellSource).not.toContain("pollShellMessages");
    expect(appSource).toContain('v-if="token"');
    expect(shellActionsSource).toContain("系统消息");
    expect(shellActionsSource).toContain('v-if="activeMessage"');
    expect(shellActionsSource).toContain('header="系统消息"');
    expect(shellActionsSource).toContain('label="查看详细"');
    expect(shellActionsSource).not.toContain(":disabled=");
    expect(shellActionsSource).toContain("if (!legacyMessageResultView(message)) return;");
    expect(shellActionsSource).toContain('label="确定"');
    expect(shellActionsSource).toContain('label="确定" severity="secondary" outlined');
    expect(shellActionsSource).toContain(':closable="false"');
    expect(shellActionsSource).toContain('emit("dismissMessage")');
    expect(shellActionsSource).toContain('emit("openMessage", message)');
    expect(appSource).toContain("pushLegacyPath(legacyDetailHref(targetViewId, targetObjectId))");
    expect(appSource).toContain("pushLegacyPath(legacyViewHref(targetViewId))");
    expect(appSource).toContain("closeShellNavigation();");
    expect(appSource).not.toContain("messageResponse");
    expect(shellActionsSource).not.toContain("<Popover");
    expect(shellActionsSource).not.toContain("message-popover");
    expect(shellActionsSource).not.toContain("暂无消息");
    expect(appSource).not.toContain("<h2>Messages</h2>");
  });

  it("keeps the unimplemented legacy notify contract out of the shell UI", () => {
    expect(apiSource).toContain("GetNotifyResult");
    expect(viewWorkflowSource).toContain("legacyNotifies");
    expect(appSource).not.toContain("/api/v1/message/getnotify");
    expect(appSource).not.toContain("notifyResponse");
    expect(legacyMenuNavSource).not.toContain("notifyCount");
    expect(legacyMenuNavSource).not.toContain('class="nav-count"');
  });

  it("keeps legacy menu state route-neutral", () => {
    expect(legacyMenuNavSource).not.toContain("currentViewId");
    expect(legacyMenuNavSource).not.toContain("active:");
    expect(appSource).not.toContain(':current-view-id="currentViewId"');
    expect(appSource).toContain("pushLegacyPath(legacyViewHref(itemViewId))");
    expect(appSource).toContain('window.addEventListener("popstate", handleHistoryNavigation)');
    expect(appSource).toContain('window.removeEventListener("popstate", handleHistoryNavigation)');
    expect(appSource).toContain("void loadInitialRoute();");
    expect(appSource).toContain("function closeShellNavigation()");
    expect(appSource).toContain("mobileMenuOpen.value = false");
  });

  it("moves legacy user info and logout into the signed-in shell", () => {
    const logoutSource = appSource.slice(
      appSource.indexOf("async function logout"),
      appSource.indexOf("async function loadReadItemView")
    );
    const clearSessionSource = appSource.slice(
      appSource.indexOf("function clearLegacySession"),
      appSource.indexOf("async function logout")
    );
    expect(appSource).not.toContain("/api/v1/auth/getuserinfo");
    expect(appSource).not.toContain("legacyUserInfoResponse");
    expect(appSource).toContain("legacyUserName(mainInfoResponse.value?.data)");
    expect(appSource).toContain("legacyUserAvatar(mainInfoResponse.value?.data)");
    expect(appSource).toContain("legacyUserName");
    expect(appSource).toContain("legacyUserAvatar");
    expect(shellActionsSource).toContain("userName");
    expect(shellActionsSource).toContain('class="shell-user-name"');
    expect(shellActionsSource).toContain('v-if="userAvatar"');
    expect(shellActionsSource).toContain('class="shell-avatar"');
    expect(shellActionsSource).toMatch(/\.shell-user > i,\s*\.shell-avatar\s*\{[^}]*width: 50px;[^}]*height: 50px;/s);
    expect(shellActionsSource).toMatch(/\.shell-user-name\s*\{[^}]*color: #337ab7;[^}]*padding: 10px 15px;/s);
    expect(appSource).toContain("安全退出");
    expect(appSource).toContain('aria-label="Mobile safe logout"');
    expect(appSource).toContain('<button type="button" @click="logout">安全退出</button>');
    expect(appSource).not.toContain('<button type="button" :disabled="Boolean(pendingAction)" @click="logout">安全退出</button>');
    expect(appSource.indexOf("安全退出")).toBeGreaterThan(appSource.indexOf("<LegacyMenuNav"));
    expect(shellActionsSource).not.toContain("安全退出");
    expect(appSource).not.toContain("Legacy User Info");
    expect(appSource).toContain("clearLegacySession()");
    expect(appSource).toContain("await prepareLegacyLogin()");
    expect(logoutSource).toContain("{ silentTransport: true }");
    expect(logoutSource).toContain('replaceLegacyPath("/")');
    expect(clearSessionSource).not.toContain("replaceLegacyPath");
  });

  it("moves the legacy checkcode route into the signed-out login panel", () => {
    const checkCodeSource = appSource.slice(
      appSource.indexOf("async function loadCheckCode"),
      appSource.indexOf("async function prepareLegacyLogin")
    );
    expect(appSource).toContain("/api/v1/auth/getcheckcode");
    expect(appSource).toContain("checkCodeResponse");
    expect(checkCodeSource).toContain("if (!response) {");
    expect(checkCodeSource).toContain('errorMessage.value = "";');
    expect(loginPanelSource).toContain('placeholder="验证码"');
    expect(loginPanelSource).toContain("captchaImage");
    expect(loginPanelSource).toContain('label="刷新"');
    expect(appSource).not.toContain("<h2>Check Code</h2>");
  });

  it("moves the legacy loginv2 route into the signed-out login panel", () => {
    const loginSource = appSource.slice(
      appSource.indexOf("async function loginV2"),
      appSource.indexOf("async function submitLegacyLogin")
    );
    const submitLoginSource = appSource.slice(
      appSource.indexOf("async function submitLegacyLogin"),
      appSource.indexOf("async function dismissLoginError")
    );
    const dismissLoginErrorSource = appSource.slice(
      appSource.indexOf("async function dismissLoginError"),
      appSource.indexOf("async function loadMainInfo")
    );
    expect(appSource).toContain("/api/v1/auth/loginv2");
    expect(appSource).toContain("localStorage.setItem(\"fool-service-token\", token.value)");
    expect(appSource).toContain("async function submitLegacyLogin");
    expect(loginPanelSource).toContain('placeholder="用户名"');
    expect(loginPanelSource).toContain('placeholder="密码"');
    expect(loginPanelSource).toContain("legacyInitAppDbId(props.appInfo)");
    expect(loginPanelSource).not.toContain("<Select");
    expect(loginPanelSource).not.toContain("databaseOptions");
    expect(appSource).toContain('const legacyDbId = ref("")');
    expect(appSource).not.toContain('const legacyDbId = ref("car_wash")');
    expect(loginPanelSource).not.toContain(" required");
    expect(loginPanelSource).not.toContain("userId.value.trim()");
    expect(loginPanelSource).not.toContain("checkCodeValue.value.trim()");
    expect(loginPanelSource).not.toContain('maxlength="8"');
    expect(loginPanelSource).toContain('<form class="login-form" aria-label="登录" @submit.prevent="submit">');
    expect(loginPanelSource).toContain('<Button type="submit" label="登录" :disabled="!captchaKey" />');
    expect(loginPanelSource).not.toContain('<Button type="button" label="登录"');
    expect(loginPanelSource).not.toContain("登录中...");
    expect(loginPanelSource).not.toContain(":loading=");
    expect(loginPanelSource).not.toContain(':disabled="pending"');
    expect(loginPanelSource).not.toContain("pending || !captchaKey");
    expect(appSource).not.toContain(':pending="Boolean(pendingAction)"\n    @dismiss-error');
    expect(loginPanelSource).toContain('label="重置"');
    expect(loginPanelSource).toContain('@click="resetDialogVisible = true"');
    expect(loginPanelSource).not.toContain("function reset()");
    expect(loginPanelSource).toContain('v-if="errorMessage || resetDialogVisible"');
    expect(loginPanelSource).toContain("if (resetDialogVisible.value) {");
    expect(loginPanelSource).toContain('resetDialogVisible.value = false;\n    emit("refresh");');
    expect(loginPanelSource).toContain('emit("dismissError");');
    expect(loginPanelSource).toContain('!appUrl.value.includes("://") ? `http://${appUrl.value}` : appUrl.value');
    expect(loginPanelSource).toContain(':href="appHref"');
    expect(loginPanelSource).not.toContain(':href="appUrl"');
    expect(loginPanelSource).toContain('header="发生错误"');
    expect(loginPanelSource).toContain("错误代码:{{ errorCode }}");
    expect(loginPanelSource).toContain("错误信息:${errorMessage}");
    expect(loginPanelSource).toContain('label="关闭" severity="secondary" outlined');
    expect(loginPanelSource).toContain('@click="dismissLoginDialog"');
    expect(appSource).toContain(':error-code="loginErrorCode"');
    expect(appSource).toContain('@dismiss-error="dismissLoginError"');
    expect(loginSource).toContain('if (!response) {');
    expect(loginSource).toContain('errorMessage.value = "";');
    expect(submitLoginSource).not.toContain("refreshLoginCheckCode");
    expect(dismissLoginErrorSource).toContain("await refreshLoginCheckCode()");
    expect(loginPanelSource).not.toContain("Welcome back");
    expect(loginPanelSource).not.toContain("<Card");
    expect(appSource).not.toContain("Legacy Login V2");
  });

  it("loads legacy initapp metadata for the signed-out login panel", () => {
    const initAppSource = appSource.slice(
      appSource.indexOf("async function initApp"),
      appSource.indexOf("async function loginV2")
    );

    expect(appSource).toContain("/api/v1/auth/initapp");
    expect(appSource).toContain("initAppResponse");
    expect(appSource).toContain("async function prepareLegacyLogin");
    expect(initAppSource).toContain("{ silentTransport: true }");
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
    expect(appSource).toContain("legacyAppPowerBy(mainInfoResponse.value?.data)");
    expect(appSource).toContain("{{ shellAppName }}");
    expect(appSource).toContain('<footer v-if="shellAppPowerBy" class="shell-footer">&copy; {{ shellAppPowerBy }}</footer>');
    expect(appSource).not.toContain("Docker Backend");
    expect(viewShellSource).not.toContain("services");
  });

  it("renders the old application name in the browser title", () => {
    expect(appSource).toContain("watchEffect(() =>");
    expect(appSource).toContain("document.title = token.value");
    expect(appSource).toContain('legacyAppName(initAppResponse.value?.data, "Fool Service")');
    expect(appSource).toContain("shellAppName.value");
  });

  it("keeps the old text-only application brand in the shell", () => {
    expect(appSource.match(/<h2 class="brand(?: drawer-brand)?">/g)).toHaveLength(2);
    expect(appSource).toContain("{{ shellAppName }}");
    expect(appSource).toContain("{{ shellAppVersion }}");
    expect(appSource).not.toContain("<strong>{{ shellAppName }}</strong>");
    expect(appSource).not.toContain("shellAppMark");
    expect(appSource).not.toContain("brand-mark");
  });

  it("runs legacy initnew from the rendered View workflow", () => {
    const initNewSource = appSource.slice(
      appSource.indexOf("async function initNew"),
      appSource.indexOf("async function saveObj")
    );

    expect(initNewSource).toContain("/api/v1/data/initnew");
    expect(initNewSource).toContain("async function initNew(viewId: number");
    expect(initNewSource).toContain("{ silentTransport: true }");
  });

  it("runs legacy savenewobj from the rendered View workflow", () => {
    const saveRequestSource = appSource.slice(
      appSource.indexOf("async function saveObj"),
      appSource.indexOf("async function runOperation")
    );
    const saveHandlerSource = appSource.slice(
      appSource.indexOf("async function saveSelectedObject"),
      appSource.indexOf("function finishSaveNavigation")
    );

    expect(appSource).toContain("/api/v1/data/savenewobj");
    expect(appSource).toContain("async function saveSelectedObject");
    expect(saveRequestSource.match(/\{ silentTransport: true \}/g)).toHaveLength(2);
    expect(appSource).toContain("saveDialogVisible.value = true");
    expect(saveHandlerSource).toContain("if (errorMessage.value) saveDialogVisible.value = false");
    expect(saveHandlerSource).not.toContain("if (!saved) {\n    saveDialogVisible.value = false;");
    expect(appSource).toContain("navigateAfterSave.value = true");
    expect(appSource).toContain("function finishSaveNavigation");
    expect(appSource).toContain("window.history.back()");
    expect(viewDetailPanelSource).toContain('header="保存中"');
    expect(viewDetailPanelSource).toContain("正在保存，请稍后....");
    expect(viewDetailPanelSource).toContain("@after-hide=\"emit('saveDialogHidden')\"");
    expect(viewDetailPanelSource).toContain(':loading="saving"');
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
      viewName: " OrderDetail ",
      viewItemId: "name",
      text: "  Ada  ",
      objID: "1001",
      ownerId: "5001",
      isAdded: true
    });

    expect(request).toEqual({
      token: "token-1",
      viewName: "OrderDetail",
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
