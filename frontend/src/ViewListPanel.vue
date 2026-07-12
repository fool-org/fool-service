<script setup lang="ts">
import { computed, ref, watch } from "vue";
import Button from "primevue/button";
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
  viewDisplayTitle,
  viewId,
  viewTemplateKind,
  viewTemplateName,
  viewOperations
} from "./viewWorkflow";

const props = defineProps<{
  data?: ListViewResult;
  disabled: boolean;
  errorMessage?: string;
  navigationRevision: number;
  pageIndex: number;
  pageSize: number;
  panelData: Record<number, { view: ListViewInfo; data: ListViewResult | null; detail?: QueryDataDetailResult | null }>;
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
const activePane = ref("table");
const currentViewId = computed(() => viewId(props.view));
const title = computed(() => viewDisplayTitle(props.view, "加载视图"));
const columns = computed(() => listRenderColumns(props.view));
const rows = computed(() => listRows(props.data));
const operations = computed(() => viewOperations(props.view));
const createItems = computed(() => createOperations(operations.value));
const rowItems = computed(() => rowOperations(operations.value));
const templateKind = computed(() => viewTemplateKind(props.view));
const listView = computed(() => templateKind.value === "list");
const chartView = computed(() => templateKind.value === "chart");
const sudokuView = computed(() => templateKind.value === "sudoku");
const supportedTemplate = computed(() => templateKind.value !== "unsupported");
const templateName = computed(() => viewTemplateName(props.view));
const chartData = computed(() => legacyChartData(rows.value));
const resultPageIndex = computed(() => listPageIndex(props.data, props.pageIndex));
const resultTotalItems = computed(() => listTotalItems(props.data));
const resultTotalPages = computed(() => listTotalPages(props.data));
const resultFreshTime = computed(() => listFreshTime(props.data));

watch([currentViewId, templateKind, () => props.navigationRevision], () => {
  activePane.value = "table";
});

function changePage(event: PageState) {
  emit("page", event.page + 1);
}
</script>

<template>
  <article class="panel view-list-panel">
    <div class="panel-heading">
      <h2>{{ title }}</h2>
    </div>
    <div v-if="supportedTemplate && !sudokuView" class="workflow-toolbar">
      <InputText v-model="keyword" class="list-query-input" type="search" placeholder="输入条件" aria-label="查询条件" @keyup.enter="emit('search')" />
      <Button type="button" label="查找" :disabled="disabled" @click="emit('search')" />
      <template v-if="listView">
        <Button type="button" label="统计" severity="secondary" outlined :disabled="disabled || !currentViewId" @click="emit('toggleReport')" />
        <Button
          v-for="operation in createItems"
          :key="operationKey(operation)"
          type="button"
          :disabled="disabled"
          :label="operationLabel(operation)"
          severity="secondary"
          outlined
          @click="emit('newObject', operationTargetViewId(operation) || currentViewId)"
        />
      </template>
    </div>

    <Message v-if="errorMessage" severity="error" :closable="false">{{ errorMessage }}</Message>
    <Message v-if="!supportedTemplate" severity="warn" :closable="false">
      旧版模板 {{ templateName }} 尚未迁移。
    </Message>

    <Tabs v-if="chartView" v-model:value="activePane" class="view-template-tabs">
      <TabList>
        <Tab value="table">数据</Tab>
        <Tab value="chart">图表</Tab>
      </TabList>
    </Tabs>

    <SudokuPanels v-if="sudokuView" :disabled="disabled" :panel-data="panelData" :panels="viewColumns(view)" @refresh-panel="emit('refreshPanel', $event)" />

    <div v-if="supportedTemplate" v-show="(!chartView && !sudokuView) || activePane === 'table'" class="table-wrap view-table">
      <ListDataTable
        :columns="columns"
        :disabled="disabled"
        :row-operations="rowItems"
        :rows="rows"
        @select="(row, viewId) => emit('select', row, viewId)"
      />
    </div>
    <LegacyChartPanel v-if="chartView && activePane === 'chart' && chartData.series.length" :data="chartData" />
    <div v-else-if="chartView && activePane === 'chart'" class="empty-state compact">暂无图表数据。</div>
    <div v-if="supportedTemplate && (rows.length || resultTotalItems || resultFreshTime)" class="list-pagination">
      <Paginator
        :first="Math.max(0, (resultPageIndex - 1) * pageSize)"
        :rows="pageSize"
        :total-records="resultTotalItems"
        :disabled="disabled"
        template="FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink"
        current-page-report-template="第 {currentPage} / {totalPages} 页 · 共 {totalRecords} 条"
        @page="changePage"
      />
      <span v-if="resultFreshTime" class="fresh-time"><i class="pi pi-clock"></i> 更新时间 {{ resultFreshTime }}</span>
    </div>
  </article>
</template>
