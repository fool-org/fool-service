<script setup lang="ts">
import type { ListDataItem, OperationInfo, TableColumnInfo } from "./api";
import {
  columnKey,
  columnTitle,
  operationKey,
  operationLabel,
  operationTargetViewId,
  rowFormatClass,
  rowObjectId,
  rowRenderKey,
  rowValue
} from "./viewWorkflow";

withDefaults(defineProps<{
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
</script>

<template>
  <table v-if="columns.length">
    <thead>
      <tr>
        <th v-for="column in columns" :key="columnKey(column)">
          {{ columnTitle(column) }}
        </th>
        <th v-if="rowOperations.length || showDefaultAction"></th>
      </tr>
    </thead>
    <tbody>
      <tr
        v-for="(row, rowIndex) in rows"
        :key="rowRenderKey(row, rowIndex, columns)"
        :class="[{ selected: rowObjectId(row, columns) === selectedObjectId }, rowFormatClass(row)]"
      >
        <td v-for="column in columns" :key="columnKey(column)">
          {{ rowValue(row, column) }}
        </td>
        <td v-if="rowOperations.length || showDefaultAction">
          <button
            v-for="operation in rowOperations"
            :key="operationKey(operation)"
            type="button"
            :disabled="disabled || operationTargetViewId(operation) <= 0"
            @click="emit('select', row, operationTargetViewId(operation))"
          >
            {{ operationLabel(operation) }}
          </button>
          <button v-if="showDefaultAction" type="button" :disabled="disabled" @click="emit('select', row)">
            {{ defaultActionLabel }}
          </button>
        </td>
      </tr>
    </tbody>
  </table>
  <div v-else class="empty-state">Load a view to start.</div>
</template>
