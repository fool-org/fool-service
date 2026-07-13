<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";
import Button from "primevue/button";
import Panel from "primevue/panel";
import Tab from "primevue/tab";
import TabList from "primevue/tablist";
import TabPanel from "primevue/tabpanel";
import TabPanels from "primevue/tabpanels";
import Tabs from "primevue/tabs";
import type { ListViewInfo, ListViewResult, QueryDataDetailResult, TableColumnInfo } from "./api";
import LegacyChartPanel from "./LegacyChartPanel.vue";
import LegacyItemPanel from "./LegacyItemPanel.vue";
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
const gridElement = ref<HTMLElement | null>(null);
const panelHeight = ref("");

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

function sudokuPanelWidth(panel: TableColumnInfo) {
  const width = Math.trunc(Number(panel.width ?? panel.Width ?? 12));
  return width >= 1 && width <= 12 ? width : 12;
}

function sudokuChildKey(panel: TableColumnInfo, index: number) {
  return `${sudokuPanelViewId(panel)}-${index}`;
}

function sudokuPanelRefreshable(panel: TableColumnInfo) {
  return ["list", "linechart", "map"].includes(sudokuPanelKind(panel));
}

const allPanelsReady = computed(() => props.panels.length > 0 && props.panels.every((panel) => {
  const result = sudokuPanelResult(panel);
  if (!result) return false;
  if (sudokuPanelKind(panel) !== "group") return true;
  return viewColumns(result.view)
    .filter((childPanel) => sudokuPanelListViewType(childPanel) === 0)
    .every((childPanel) => Boolean(sudokuPanelResult(childPanel)));
}));

watch(allPanelsReady, async (ready) => {
  panelHeight.value = "";
  if (!ready) return;
  await nextTick();
  const panels = gridElement.value?.querySelectorAll<HTMLElement>(".sudoku-panel") ?? [];
  const heights = [...panels].map((panel) => panel.getBoundingClientRect().height);
  if (heights.length) panelHeight.value = `${Math.max(...heights)}px`;
}, { immediate: true });
</script>

<template>
  <div ref="gridElement" class="sudoku-grid" :style="{ gridAutoRows: panelHeight || 'auto' }">
    <Panel
      v-for="panel in panels"
      :key="`${sudokuPanelViewId(panel)}-${fieldTitle(panel)}-${sudokuPanelKind(panel)}`"
      class="sudoku-panel"
      :style="{ '--sudoku-panel-span': sudokuPanelWidth(panel) }"
    >
      <template #header>
        <div class="sudoku-panel-heading">
          <strong>{{ fieldTitle(panel) }}</strong>
        </div>
      </template>
      <div v-if="sudokuPanelKind(panel) === 'list'" class="table-wrap sudoku-panel-body">
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
        compact
        :data="sudokuPanelChart(panel)"
      />
      <LegacyMapPanel
        v-else-if="sudokuPanelKind(panel) === 'map' && sudokuPanelMarkers(panel).length"
        class="sudoku-panel-body"
        :markers="sudokuPanelMarkers(panel)"
      />
      <LegacyItemPanel
        v-else-if="sudokuPanelKind(panel) === 'item' && sudokuPanelItemFields(panel).length"
        :fields="sudokuPanelItemFields(panel)"
      />
      <div v-else-if="sudokuPanelKind(panel) === 'group' && sudokuGroupPanels(panel).length" class="sudoku-panel-body">
        <Tabs :value="sudokuChildKey(sudokuGroupPanels(panel)[0], 0)">
          <TabList>
            <Tab v-for="(childPanel, index) in sudokuGroupPanels(panel)" :key="sudokuChildKey(childPanel, index)" :value="sudokuChildKey(childPanel, index)">
              {{ fieldTitle(childPanel) }}
            </Tab>
          </TabList>
          <TabPanels>
            <TabPanel v-for="(childPanel, index) in sudokuGroupPanels(panel)" :key="sudokuChildKey(childPanel, index)" :value="sudokuChildKey(childPanel, index)">
              <div v-if="sudokuPanelListViewType(childPanel) === 0" class="table-wrap sudoku-panel-body">
                <ListDataTable
                  :columns="sudokuPanelColumns(childPanel)"
                  :disabled="Boolean(disabled)"
                  :row-operations="[]"
                  :rows="sudokuPanelRows(childPanel)"
                  :show-default-action="false"
                />
              </div>
              <div v-else-if="sudokuPanelListViewType(childPanel) === 1" class="empty-state compact">这是简单项</div>
              <div v-else class="empty-state compact">暂无数据。</div>
              <div v-if="sudokuPanelListViewType(childPanel) === 0" class="sudoku-panel-footer">
                <span v-if="sudokuPanelFreshTime(childPanel)">更新时间 {{ sudokuPanelFreshTime(childPanel) }}</span>
                <Button type="button" label="刷新" severity="secondary" text size="small" :disabled="disabled" @click="emit('refreshPanel', childPanel)" />
              </div>
            </TabPanel>
          </TabPanels>
        </Tabs>
      </div>
      <div v-else class="empty-state compact">暂无数据。</div>
      <div v-if="sudokuPanelRefreshable(panel)" class="sudoku-panel-footer">
        <span v-if="sudokuPanelFreshTime(panel)">更新时间 {{ sudokuPanelFreshTime(panel) }}</span>
        <Button type="button" label="刷新" severity="secondary" text size="small" :disabled="disabled" @click="emit('refreshPanel', panel)" />
      </div>
    </Panel>
  </div>
</template>
