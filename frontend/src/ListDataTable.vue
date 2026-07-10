<script setup lang="ts">
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

const props = withDefaults(defineProps<{
  columns: TableColumnInfo[];
  defaultActionLabel?: string;
  disabled: boolean;
  rowOperations: OperationInfo[];
  rows: ListDataItem[];
  selectedObjectId?: string;
  showDefaultAction?: boolean;
}>(), {
  defaultActionLabel: "Open",
  selectedObjectId: "",
  showDefaultAction: true
});

const emit = defineEmits<{
  select: [row: ListDataItem, viewId?: number];
}>();

function tableRowClass(row: ListDataItem) {
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
    :value="rows"
    :row-class="tableRowClass"
    scrollable
    striped-rows
    size="small"
  >
    <Column v-for="column in columns" :key="columnKey(column)" :header="columnTitle(column)">
      <template #body="{ data: row }">
        {{ rowValue(row, column) }}
      </template>
    </Column>
    <Column v-if="rowOperations.length || showDefaultAction" header="Actions" frozen align-frozen="right">
      <template #body="{ data: row }">
        <div class="table-actions">
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
            icon="pi pi-arrow-right"
            icon-pos="right"
            size="small"
            text
            @click="emit('select', row)"
          />
        </div>
      </template>
    </Column>
    <template #empty><div class="empty-state compact">No rows.</div></template>
  </DataTable>
  <div v-else class="empty-state">Load a view to start.</div>
</template>
