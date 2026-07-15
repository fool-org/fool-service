<script setup lang="ts">
import { computed } from "vue";
import Button from "primevue/button";
import Column from "primevue/column";
import DataTable from "primevue/datatable";
import type { ListDataItem, OperationInfo, TableColumnInfo } from "./api";
import {
  columnKey,
  columnMinimumWidth,
  columnTitle,
  columnWidth,
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
  condensed?: boolean;
  defaultActionLabel?: string;
  disabled?: boolean;
  minimumRows?: number;
  rowOperations: OperationInfo[];
  rows: ListDataItem[];
  selectedObjectId?: string;
  showActionHeader?: boolean;
  showDefaultAction?: boolean;
  striped?: boolean;
}>(), {
  condensed: true,
  defaultActionLabel: "Open",
  disabled: false,
  minimumRows: 0,
  selectedObjectId: "",
  showActionHeader: true,
  showDefaultAction: false,
  striped: true
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
const showsActions = computed(() => props.rowOperations.length > 0 || props.showDefaultAction);
const tableMinimumWidth = computed(() => (
  props.columns.reduce((width, column) => width + columnMinimumWidth(column), 0)
  + (showsActions.value ? 72 : 0)
));

function isFiller(row: RenderedListDataItem) {
  return row[fillerRow] === true;
}

function tableColumnStyle(column: TableColumnInfo) {
  const width = columnWidth(column);
  return {
    minWidth: `${columnMinimumWidth(column)}px`,
    ...(width ? { width: `${width}px` } : {})
  };
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
    class="metadata-data-table"
    :class="{ 'metadata-empty-table': !columns.length || !renderedRows.length }"
    :value="columns.length ? renderedRows : []"
    :row-class="tableRowClass"
    :table-style="{ minWidth: `${tableMinimumWidth}px` }"
    scrollable
    :striped-rows="striped"
    :size="condensed ? 'small' : undefined"
  >
    <Column v-for="column in columns" :key="columnKey(column)" :header="columnTitle(column)" :style="tableColumnStyle(column)">
      <template #body="{ data: row }">
        <span v-if="isFiller(row)" aria-hidden="true">&nbsp;</span>
        <template v-else>{{ rowValue(row, column) }}</template>
      </template>
    </Column>
    <Column
      v-if="rowOperations.length || showDefaultAction"
      header="操作"
      :style="{ minWidth: '72px' }"
      :header-style="showActionHeader ? undefined : { display: 'none' }"
    >
      <template #body="{ data: row }">
        <div v-if="!isFiller(row)" class="table-actions">
          <template v-for="operation in rowOperations" :key="operationKey(operation)">
            <Button
              v-if="operationTargetViewId(operation) > 0"
              type="button"
              :disabled="disabled"
              :label="operationLabel(operation)"
              size="small"
              severity="secondary"
              text
              @click="emit('select', row, operationTargetViewId(operation))"
            />
            <span v-else class="legacy-inert-operation">{{ operationLabel(operation) }}</span>
          </template>
          <Button
            v-if="showDefaultAction"
            type="button"
            :disabled="disabled"
            :label="defaultActionLabel"
            size="small"
            severity="secondary"
            outlined
            @click="emit('select', row)"
          />
        </div>
      </template>
    </Column>
  </DataTable>
</template>
