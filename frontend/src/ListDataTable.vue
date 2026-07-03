<script setup lang="ts">
import type { ListDataItem, OperationInfo, TableColumnInfo } from "./api";
import { columnKey, columnTitle, rowFormatClass, rowObjectId, rowValue } from "./viewWorkflow";

defineProps<{
  columns: TableColumnInfo[];
  disabled: boolean;
  rowOperations: OperationInfo[];
  rows: ListDataItem[];
  selectedObjectId: string;
}>();

const emit = defineEmits<{
  select: [row: ListDataItem, viewId?: number];
}>();

function operationKey(operation: OperationInfo) {
  return operation.id || operation.name || operation.viewId || "operation";
}

function operationLabel(operation: OperationInfo) {
  return operation.text || operation.name || `Open ${operation.viewId}`;
}
</script>

<template>
  <table v-if="rows.length">
    <thead>
      <tr>
        <th v-for="column in columns" :key="columnKey(column)">
          {{ columnTitle(column) }}
        </th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr
        v-for="row in rows"
        :key="rowObjectId(row, columns)"
        :class="[{ selected: rowObjectId(row, columns) === selectedObjectId }, rowFormatClass(row)]"
      >
        <td v-for="column in columns" :key="columnKey(column)">
          {{ rowValue(row, column) }}
        </td>
        <td>
          <button
            v-for="operation in rowOperations"
            :key="operationKey(operation)"
            type="button"
            :disabled="disabled"
            @click="emit('select', row, operation.viewId)"
          >
            {{ operationLabel(operation) }}
          </button>
          <button type="button" :disabled="disabled" @click="emit('select', row)">Open</button>
        </td>
      </tr>
    </tbody>
  </table>
  <div v-else class="empty-state">Load a view to start.</div>
</template>
