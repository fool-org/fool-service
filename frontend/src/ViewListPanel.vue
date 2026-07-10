<script setup lang="ts">
import { computed, ref } from "vue";
import type { ListDataItem, ListViewInfo, ListViewResult, QueryDataDetailResult } from "./api";
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
  viewDetailViewId,
  viewDisplayName,
  viewDisplayTitle,
  viewId,
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
const chartData = computed(() => legacyChartData(rows.value));
const resultPageIndex = computed(() => listPageIndex(props.data, props.pageIndex));
const resultTotalItems = computed(() => listTotalItems(props.data));
const resultTotalPages = computed(() => listTotalPages(props.data));
const resultFreshTime = computed(() => listFreshTime(props.data));
const defaultNewViewId = computed(() => viewDetailViewId(props.view, currentViewId.value));
</script>

<template>
  <article class="panel view-list-panel">
    <div class="panel-heading">
      <h2>{{ title }}</h2>
      <span>{{ name }}</span>
    </div>
    <div class="workflow-toolbar">
      <label>
        Search
        <input v-model="keyword" type="search" @keyup.enter="emit('search')" />
      </label>
      <label>
        Page size
        <input v-model.number="pageSize" min="1" type="number" />
      </label>
      <button class="primary" type="button" :disabled="disabled" @click="emit('search')">Search</button>
      <button
        v-for="operation in createItems"
        :key="operationKey(operation)"
        type="button"
        :disabled="disabled"
        @click="emit('newObject', operationTargetViewId(operation) || currentViewId)"
      >
        {{ operationLabel(operation) }}
      </button>
      <button v-if="!createItems.length" type="button" :disabled="disabled" @click="emit('newObject', defaultNewViewId)">
        New Row
      </button>
      <button type="button" :disabled="disabled || !currentViewId" @click="emit('toggleReport')">Report</button>
    </div>

    <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

    <div v-if="chartView" class="view-template-tabs" role="tablist">
      <button type="button" :class="{ active: activePane === 'table' }" @click="activePane = 'table'">数据</button>
      <button type="button" :class="{ active: activePane === 'chart' }" @click="activePane = 'chart'">图表</button>
    </div>

    <SudokuPanels v-if="sudokuView" :disabled="disabled" :panel-data="panelData" :panels="viewColumns(view)" />

    <div v-show="(!chartView && !sudokuView) || activePane === 'table'" class="table-wrap view-table">
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
    <div v-if="rows.length || resultTotalItems || resultFreshTime" class="button-row">
      <button type="button" :disabled="disabled || resultPageIndex <= 1" @click="emit('page', resultPageIndex - 1)">Previous</button>
      <span>Page {{ resultPageIndex }} / {{ resultTotalPages || 1 }} · {{ resultTotalItems }} rows</span>
      <button
        type="button"
        :disabled="disabled || resultTotalPages === 0 || resultPageIndex >= resultTotalPages"
        @click="emit('page', resultPageIndex + 1)"
      >
        Next
      </button>
      <span v-if="resultFreshTime">Updated {{ resultFreshTime }}</span>
    </div>
  </article>
</template>
