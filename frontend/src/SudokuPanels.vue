<script setup lang="ts">
import Button from "primevue/button";
import Panel from "primevue/panel";
import Tag from "primevue/tag";
import type { ListViewInfo, ListViewResult, QueryDataDetailResult, TableColumnInfo } from "./api";
import LegacyChartPanel from "./LegacyChartPanel.vue";
import LegacyMapPanel from "./LegacyMapPanel.vue";
import ListDataTable from "./ListDataTable.vue";
import {
  fieldTitle,
  legacyChartData,
  legacyItemDetailFields,
  legacyMapMarkers,
  listFreshTime,
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
const emit = defineEmits<{ refreshPanel: [panel: TableColumnInfo] }>();

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

function sudokuPanelFreshTime(panel: TableColumnInfo) {
  return listFreshTime(sudokuPanelResult(panel)?.data || undefined);
}

function sudokuPanelChart(panel: TableColumnInfo) {
  return legacyChartData(sudokuPanelRows(panel));
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
    <Panel v-for="panel in panels" :key="`${sudokuPanelViewId(panel)}-${fieldTitle(panel)}-${sudokuPanelKind(panel)}`" class="sudoku-panel">
      <template #header>
        <div class="sudoku-panel-heading">
          <strong>{{ fieldTitle(panel) }}</strong>
          <div class="sudoku-panel-actions">
            <small v-if="sudokuPanelFreshTime(panel)">{{ sudokuPanelFreshTime(panel) }}</small>
            <Tag :value="sudokuPanelKind(panel)" severity="secondary" rounded />
            <Button type="button" icon="pi pi-refresh" severity="secondary" text size="small" :aria-label="`Refresh ${fieldTitle(panel)}`" :disabled="disabled" @click="emit('refreshPanel', panel)" />
          </div>
        </div>
      </template>
      <div v-if="sudokuPanelKind(panel) === 'list' && sudokuPanelRows(panel).length" class="table-wrap sudoku-panel-body">
        <ListDataTable
          :columns="sudokuPanelColumns(panel)"
          :disabled="Boolean(disabled)"
          :row-operations="[]"
          :rows="sudokuPanelRows(panel)"
          :show-default-action="false"
        />
      </div>
      <LegacyChartPanel
        v-else-if="sudokuPanelKind(panel) === 'linechart' && sudokuPanelChart(panel).series.length"
        class="sudoku-panel-body"
        :data="sudokuPanelChart(panel)"
      />
      <LegacyMapPanel
        v-else-if="sudokuPanelKind(panel) === 'map' && sudokuPanelMarkers(panel).length"
        class="sudoku-panel-body"
        :markers="sudokuPanelMarkers(panel)"
      />
      <div v-else-if="sudokuPanelKind(panel) === 'item' && sudokuPanelItemFields(panel).length" class="table-wrap sudoku-panel-body">
        <table class="legacy-item-table">
          <tbody>
            <tr v-for="(item, index) in sudokuPanelItemFields(panel)" :key="`${item.label}-${index}`">
              <th>{{ item.label }}</th>
              <td>{{ item.text }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else-if="sudokuPanelKind(panel) === 'group' && sudokuGroupPanels(panel).length" class="sudoku-panel-body">
        <Panel v-for="childPanel in sudokuGroupPanels(panel)" :key="`${sudokuPanelViewId(childPanel)}-${fieldTitle(childPanel)}`" class="sudoku-panel">
          <template #header>
            <div class="sudoku-panel-heading">
              <strong>{{ fieldTitle(childPanel) }}</strong>
              <div class="sudoku-panel-actions">
                <small v-if="sudokuPanelFreshTime(childPanel)">{{ sudokuPanelFreshTime(childPanel) }}</small>
                <Tag :value="sudokuPanelListViewType(childPanel) === 0 ? 'list' : 'item'" severity="secondary" rounded />
                <Button v-if="sudokuPanelListViewType(childPanel) === 0" type="button" icon="pi pi-refresh" severity="secondary" text size="small" :aria-label="`Refresh ${fieldTitle(childPanel)}`" :disabled="disabled" @click="emit('refreshPanel', childPanel)" />
              </div>
            </div>
          </template>
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
        </Panel>
      </div>
      <div v-else class="empty-state compact">
        {{ sudokuPanelResult(panel) ? `${sudokuPanelRows(panel).length} rows loaded` : "No panel data loaded." }}
      </div>
    </Panel>
  </div>
</template>
