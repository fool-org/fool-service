<script setup lang="ts">
import { computed } from "vue";
import Button from "primevue/button";
import Column from "primevue/column";
import DataTable from "primevue/datatable";
import type { ListDataItem, OperationInfo, TableColumnInfo } from "./api";
import {
  columnKey,
  columnTitle,
  operationKey,
  operationLabel,
  operationTargetViewId,
  rowFormatClass,
  rowObjectId,
  rowValue
} from "./viewWorkflow";

const fillerRow = Symbol("legacy-filler-row");

interface RenderedListDataItem extends ListDataItem {
  [fillerRow]?: true;
}

const props = withDefaults(defineProps<{
  columns: TableColumnInfo[];
  defaultActionLabel?: string;
  disabled: boolean;
  minimumRows?: number;
  rowOperations: OperationInfo[];
  rows: ListDataItem[];
  selectedObjectId?: string;
  showDefaultAction?: boolean;
}>(), {
  defaultActionLabel: "Open",
  minimumRows: 0,
  selectedObjectId: "",
  showDefaultAction: false
});

const emit = defineEmits<{
  select: [row: ListDataItem, viewId?: number];
}>();

const renderedRows = computed<RenderedListDataItem[]>(() => [
  ...props.rows,
  ...Array.from(
    { length: Math.max(0, props.minimumRows - props.rows.length) },
    () => ({ [fillerRow]: true } as RenderedListDataItem)
  )
]);

function isFiller(row: RenderedListDataItem) {
  return row[fillerRow] === true;
}

function tableRowClass(row: RenderedListDataItem) {
  if (isFiller(row)) return "legacy-filler-row";
  return [
    rowObjectId(row, props.columns) === props.selectedObjectId ? "selected" : "",
    rowFormatClass(row)
  ].filter(Boolean).join(" ");
}
</script>

<template>
  <DataTable
    v-if="columns.length"
    class="metadata-data-table"
    :value="renderedRows"
    :row-class="tableRowClass"
    scrollable
    striped-rows
    size="small"
  >
    <Column v-for="column in columns" :key="columnKey(column)" :header="columnTitle(column)">
      <template #body="{ data: row }">
        <span v-if="isFiller(row)" aria-hidden="true">&nbsp;</span>
        <template v-else>{{ rowValue(row, column) }}</template>
      </template>
    </Column>
    <Column v-if="rowOperations.length || showDefaultAction" header="操作">
      <template #body="{ data: row }">
        <div v-if="!isFiller(row)" class="table-actions">
          <Button
            v-for="operation in rowOperations"
            :key="operationKey(operation)"
            type="button"
            :disabled="disabled || operationTargetViewId(operation) <= 0"
            :label="operationLabel(operation)"
            size="small"
            severity="secondary"
            text
            @click="emit('select', row, operationTargetViewId(operation))"
          />
          <Button
            v-if="showDefaultAction"
            type="button"
            :disabled="disabled"
            :label="defaultActionLabel"
            size="small"
            text
            @click="emit('select', row)"
          />
        </div>
      </template>
    </Column>
    <template #empty><div class="empty-state compact">暂无数据。</div></template>
  </DataTable>
  <div v-else class="empty-state">请先加载视图。</div>
</template>
