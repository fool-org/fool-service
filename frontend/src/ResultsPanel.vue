<script setup lang="ts">
import type { ListDataItem, OperationInfo, TableColumnInfo } from "./api";
import ListDataTable from "./ListDataTable.vue";

defineProps<{
  columns: TableColumnInfo[];
  disabled: boolean;
  errorMessage: string;
  pendingAction: string;
  responseDump: string;
  rows: ListDataItem[];
}>();

const noRowOperations: OperationInfo[] = [];
</script>

<template>
  <section class="panel results-panel" aria-label="Results">
    <div class="panel-heading">
      <h2>Response & Result Set</h2>
      <span v-if="pendingAction">Running {{ pendingAction }}...</span>
      <span v-else>Ready</span>
    </div>

    <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

    <div class="result-layout">
      <div class="table-wrap">
        <ListDataTable
          v-if="columns.length > 0"
          :columns="columns"
          :disabled="disabled"
          :row-operations="noRowOperations"
          :rows="rows"
          selected-object-id=""
          :show-default-action="false"
        />
        <div v-else class="empty-state">No query rows loaded.</div>
      </div>

      <pre class="json-output">{{ responseDump }}</pre>
    </div>
  </section>
</template>
