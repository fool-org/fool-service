<script setup lang="ts">
import type { ListViewInfo, ListViewResult, QueryDataDetailResult, TableColumnInfo } from "./api";
import LegacyMapPanel from "./LegacyMapPanel.vue";
import ListDataTable from "./ListDataTable.vue";
import {
  fieldTitle,
  legacyChartData,
  legacyItemDetailFields,
  legacyMapMarkers,
  listRows,
  sudokuPanelKind,
  sudokuPanelListViewType,
  sudokuPanelViewId,
  viewColumns
} from "./viewWorkflow";

const props = defineProps<{
  disabled?: boolean;
  panelData: Record<number, { view: ListViewInfo; data: ListViewResult | null; detail?: QueryDataDetailResult | null }>;
  panels: TableColumnInfo[];
}>();

function sudokuPanelResult(panel: TableColumnInfo) {
  return props.panelData[sudokuPanelViewId(panel)];
}

function sudokuPanelColumns(panel: TableColumnInfo) {
  return viewColumns(sudokuPanelResult(panel)?.view);
}

function sudokuGroupPanels(panel: TableColumnInfo) {
  return viewColumns(sudokuPanelResult(panel)?.view);
}

function sudokuPanelRows(panel: TableColumnInfo) {
  return listRows(sudokuPanelResult(panel)?.data || undefined);
}

function sudokuPanelChart(panel: TableColumnInfo) {
  return legacyChartData(sudokuPanelRows(panel));
}

function sudokuPanelChartMax(panel: TableColumnInfo) {
  return Math.max(1, ...sudokuPanelChart(panel).series.flatMap((series) => series.values));
}

function sudokuPanelMarkers(panel: TableColumnInfo) {
  return legacyMapMarkers(sudokuPanelRows(panel));
}

function sudokuPanelItemFields(panel: TableColumnInfo) {
  return legacyItemDetailFields(sudokuPanelResult(panel)?.detail || undefined);
}
</script>

<template>
  <div class="sudoku-grid">
    <section v-for="panel in panels" :key="`${sudokuPanelViewId(panel)}-${fieldTitle(panel)}-${sudokuPanelKind(panel)}`" class="sudoku-panel">
      <header>
        <strong>{{ fieldTitle(panel) }}</strong>
        <span>{{ sudokuPanelKind(panel) }}</span>
      </header>
      <div v-if="sudokuPanelKind(panel) === 'list' && sudokuPanelRows(panel).length" class="table-wrap sudoku-panel-body">
        <ListDataTable
          :columns="sudokuPanelColumns(panel)"
          :disabled="Boolean(disabled)"
          :row-operations="[]"
          :rows="sudokuPanelRows(panel)"
          :show-default-action="false"
        />
      </div>
      <div v-else-if="sudokuPanelKind(panel) === 'linechart' && sudokuPanelChart(panel).series.length" class="legacy-chart-pane sudoku-panel-body">
        <section v-for="series in sudokuPanelChart(panel).series" :key="`${sudokuPanelViewId(panel)}-${series.name}`" class="chart-series">
          <header>
            <strong>{{ series.name }}</strong>
            <span>{{ series.type }}</span>
          </header>
          <div v-for="(value, index) in series.values" :key="`${series.name}-${index}`" class="chart-row">
            <span>{{ sudokuPanelChart(panel).labels[index] || index + 1 }}</span>
            <meter :value="value" min="0" :max="sudokuPanelChartMax(panel)"></meter>
            <strong>{{ value }}</strong>
          </div>
        </section>
      </div>
      <LegacyMapPanel
        v-else-if="sudokuPanelKind(panel) === 'map' && sudokuPanelMarkers(panel).length"
        class="sudoku-panel-body"
        :markers="sudokuPanelMarkers(panel)"
      />
      <div v-else-if="sudokuPanelKind(panel) === 'item' && sudokuPanelItemFields(panel).length" class="table-wrap sudoku-panel-body">
        <table>
          <tbody>
            <tr v-for="(item, index) in sudokuPanelItemFields(panel)" :key="`${item.label}-${index}`">
              <th>{{ item.label }}</th>
              <td>{{ item.text }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else-if="sudokuPanelKind(panel) === 'group' && sudokuGroupPanels(panel).length" class="sudoku-panel-body">
        <section v-for="childPanel in sudokuGroupPanels(panel)" :key="`${sudokuPanelViewId(childPanel)}-${fieldTitle(childPanel)}`" class="sudoku-panel">
          <header>
            <strong>{{ fieldTitle(childPanel) }}</strong>
            <span>{{ sudokuPanelListViewType(childPanel) === 0 ? "list" : "item" }}</span>
          </header>
          <div v-if="sudokuPanelListViewType(childPanel) === 0 && sudokuPanelRows(childPanel).length" class="table-wrap sudoku-panel-body">
            <ListDataTable
              :columns="sudokuPanelColumns(childPanel)"
              :disabled="Boolean(disabled)"
              :row-operations="[]"
              :rows="sudokuPanelRows(childPanel)"
              :show-default-action="false"
            />
          </div>
          <div v-else-if="sudokuPanelListViewType(childPanel) === 1" class="empty-state compact">简单项</div>
          <div v-else class="empty-state compact">
            {{ sudokuPanelResult(childPanel) ? `${sudokuPanelRows(childPanel).length} rows loaded` : "No group data loaded." }}
          </div>
        </section>
      </div>
      <div v-else class="empty-state compact">
        {{ sudokuPanelResult(panel) ? `${sudokuPanelRows(panel).length} rows loaded` : "No panel data loaded." }}
      </div>
    </section>
  </div>
</template>
