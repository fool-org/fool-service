<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";
import Button from "primevue/button";
import InputText from "primevue/inputtext";
import Message from "primevue/message";
import Tab from "primevue/tab";
import TabList from "primevue/tablist";
import Tabs from "primevue/tabs";
import type { ListDataItem, ListViewInfo, ListViewResult, QueryDataDetailResult, TableColumnInfo } from "./api";
import LegacyChartPanel from "./LegacyChartPanel.vue";
import LegacyPagination from "./LegacyPagination.vue";
import ListDataTable from "./ListDataTable.vue";
import SudokuPanels from "./SudokuPanels.vue";
import {
  createOperations,
  legacyChartData,
  listPageIndex,
  listRenderColumns,
  listRows,
  listTotalItems,
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
const chartTablePane = ref<HTMLElement | null>(null);
const chartPaneHeight = ref(0);
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

async function lockChartPaneHeight() {
  if (!chartView.value || !props.data || chartPaneHeight.value) return;
  await nextTick();
  if (!chartView.value || chartPaneHeight.value) return;
  const height = chartTablePane.value?.getBoundingClientRect().height ?? 0;
  if (height > 0) chartPaneHeight.value = Math.round(height);
}

watch([currentViewId, templateKind, () => props.navigationRevision], () => {
  activePane.value = "table";
  chartPaneHeight.value = 0;
});

watch(() => props.data, () => void lockChartPaneHeight(), { immediate: true });
</script>

<template>
  <article class="panel view-list-panel">
    <div v-if="!sudokuView" class="panel-heading">
      <h2>{{ title }}</h2>
    </div>
    <div v-if="supportedTemplate && !sudokuView" class="workflow-toolbar" :class="{ 'chart-workflow-toolbar': chartView }">
      <InputText v-model="keyword" class="list-query-input" type="search" placeholder="输入条件" aria-label="查询条件" @keyup.enter="emit('search')" />
      <Button type="button" label="查找" severity="secondary" outlined @click="emit('search')" />
      <template v-if="listView">
        <Button type="button" label="统计" severity="secondary" outlined :disabled="!currentViewId" @click="emit('toggleReport')" />
        <Button
          v-for="operation in createItems"
          :key="operationKey(operation)"
          type="button"
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

    <Tabs v-if="chartView" v-model:value="activePane" class="view-template-tabs legacy-tabs">
      <TabList>
        <Tab value="table">数据</Tab>
        <Tab value="chart">图表</Tab>
      </TabList>
    </Tabs>

    <SudokuPanels v-if="sudokuView" :disabled="disabled" :panel-data="panelData" :panels="viewColumns(view)" @refresh-panel="emit('refreshPanel', $event)" />

    <div ref="chartTablePane" v-if="supportedTemplate && !sudokuView" v-show="!chartView || activePane === 'table'" class="table-wrap view-table">
      <ListDataTable
        :columns="columns"
        :disabled="disabled"
        :minimum-rows="pageSize"
        :row-operations="rowItems"
        :rows="rows"
        @select="(row, viewId) => emit('select', row, viewId)"
      />
    </div>
    <LegacyChartPanel v-if="chartView && activePane === 'chart'" :data="chartData" :rendered-height="chartPaneHeight" />
    <LegacyPagination
      v-if="supportedTemplate && data"
      class="list-pagination"
      :disabled="disabled"
      :page-index="resultPageIndex"
      :page-size="pageSize"
      :total-items="resultTotalItems"
      @page="emit('page', $event)"
    />
  </article>
</template>
