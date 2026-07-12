<script setup lang="ts">
import { computed, ref } from "vue";
import Button from "primevue/button";
import InputNumber from "primevue/inputnumber";
import InputText from "primevue/inputtext";
import Message from "primevue/message";
import Paginator, { type PageState } from "primevue/paginator";
import Tab from "primevue/tab";
import TabList from "primevue/tablist";
import Tabs from "primevue/tabs";
import type { ListDataItem, ListViewInfo, ListViewResult, QueryDataDetailResult, TableColumnInfo } from "./api";
import LegacyChartPanel from "./LegacyChartPanel.vue";
import ListDataTable from "./ListDataTable.vue";
import SudokuPanels from "./SudokuPanels.vue";
import {
  createOperations,
  legacyChartData,
  listFreshTime,
  listPageIndex,
  listRenderColumns,
  listRows,
  listTotalItems,
  listTotalPages,
  operationKey,
  operationLabel,
  operationTargetViewId,
  rowOperations,
  viewColumns,
  viewDisplayName,
  viewDisplayTitle,
  viewId,
  viewTemplateKind,
  viewTemplateName,
  viewOperations,
  viewUsesChartTemplate,
  viewUsesSudokuTemplate
} from "./viewWorkflow";

const props = defineProps<{
  data?: ListViewResult;
  disabled: boolean;
  errorMessage?: string;
  pageIndex: number;
  panelData: Record<number, { view: ListViewInfo; data: ListViewResult | null; detail?: QueryDataDetailResult | null }>;
  selectedObjectId: string;
  view?: ListViewInfo;
}>();

const emit = defineEmits<{
  newObject: [viewId: number];
  page: [pageIndex: number];
  refreshPanel: [panel: TableColumnInfo];
  search: [];
  select: [row: ListDataItem, viewId?: number];
  toggleReport: [];
}>();

const keyword = defineModel<string>("keyword", { required: true });
const pageSize = defineModel<number>("pageSize", { required: true });
const activePane = ref("table");
const currentViewId = computed(() => viewId(props.view));
const title = computed(() => viewDisplayTitle(props.view, "Load a View"));
const name = computed(() => viewDisplayName(props.view));
const columns = computed(() => listRenderColumns(props.view));
const rows = computed(() => listRows(props.data));
const operations = computed(() => viewOperations(props.view));
const createItems = computed(() => createOperations(operations.value));
const rowItems = computed(() => rowOperations(operations.value));
const chartView = computed(() => viewUsesChartTemplate(props.view));
const sudokuView = computed(() => viewUsesSudokuTemplate(props.view));
const supportedTemplate = computed(() => viewTemplateKind(props.view) !== "unsupported");
const templateName = computed(() => viewTemplateName(props.view));
const chartData = computed(() => legacyChartData(rows.value));
const resultPageIndex = computed(() => listPageIndex(props.data, props.pageIndex));
const resultTotalItems = computed(() => listTotalItems(props.data));
const resultTotalPages = computed(() => listTotalPages(props.data));
const resultFreshTime = computed(() => listFreshTime(props.data));
function changePage(event: PageState) {
  emit("page", event.page + 1);
}
</script>

<template>
  <article class="panel view-list-panel">
    <div class="panel-heading">
      <h2>{{ title }}</h2>
      <span>{{ name }}</span>
    </div>
    <div v-if="supportedTemplate" class="workflow-toolbar">
      <label>
        Search
        <InputText v-model="keyword" type="search" placeholder="Search this view" fluid @keyup.enter="emit('search')" />
      </label>
      <label>
        Page size
        <InputNumber v-model="pageSize" :min="1" :max="200" :use-grouping="false" fluid />
      </label>
      <Button type="button" label="Search" icon="pi pi-search" :disabled="disabled" @click="emit('search')" />
      <Button
        v-for="operation in createItems"
        :key="operationKey(operation)"
        type="button"
        :disabled="disabled"
        :label="operationLabel(operation)"
        icon="pi pi-plus"
        severity="secondary"
        outlined
        @click="emit('newObject', operationTargetViewId(operation) || currentViewId)"
      />
      <Button type="button" label="Report" icon="pi pi-chart-bar" severity="secondary" outlined :disabled="disabled || !currentViewId" @click="emit('toggleReport')" />
    </div>

    <Message v-if="errorMessage" severity="error" :closable="false">{{ errorMessage }}</Message>
    <Message v-if="!supportedTemplate" severity="warn" :closable="false">
      Legacy template {{ templateName }} has not been migrated.
    </Message>

    <Tabs v-if="chartView" v-model:value="activePane" class="view-template-tabs">
      <TabList>
        <Tab value="table"><i class="pi pi-table"></i> 数据</Tab>
        <Tab value="chart"><i class="pi pi-chart-line"></i> 图表</Tab>
      </TabList>
    </Tabs>

    <SudokuPanels v-if="sudokuView" :disabled="disabled" :panel-data="panelData" :panels="viewColumns(view)" @refresh-panel="emit('refreshPanel', $event)" />

    <div v-if="supportedTemplate" v-show="(!chartView && !sudokuView) || activePane === 'table'" class="table-wrap view-table">
      <ListDataTable
        :columns="columns"
        :disabled="disabled"
        :row-operations="rowItems"
        :rows="rows"
        :selected-object-id="selectedObjectId"
        @select="(row, viewId) => emit('select', row, viewId)"
      />
    </div>
    <LegacyChartPanel v-if="chartView && activePane === 'chart' && chartData.series.length" :data="chartData" />
    <div v-else-if="chartView && activePane === 'chart'" class="empty-state compact">No chart data.</div>
    <div v-if="supportedTemplate && (rows.length || resultTotalItems || resultFreshTime)" class="list-pagination">
      <Paginator
        :first="Math.max(0, (resultPageIndex - 1) * pageSize)"
        :rows="pageSize"
        :total-records="resultTotalItems"
        :disabled="disabled"
        template="FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink"
        current-page-report-template="Page {currentPage} of {totalPages} · {totalRecords} rows"
        @page="changePage"
      />
      <span v-if="resultFreshTime" class="fresh-time"><i class="pi pi-clock"></i> Updated {{ resultFreshTime }}</span>
    </div>
  </article>
</template>
