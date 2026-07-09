<script setup lang="ts">
import type { ListDataItem, ListDataValue, OperationInfo, QueryDataDetailDataItem, QueryDataDetailItemGroup, TableColumnInfo } from "./api";
import ListDataTable from "./ListDataTable.vue";
import MetadataFieldEditor from "./MetadataFieldEditor.vue";
import type { SelectOption } from "./viewShell";
import {
  fieldDisplayValue,
  fieldKey,
  fieldTitle,
  groupColumns,
  groupItems,
  groupKey,
  groupSelectFromExists,
  groupTitle,
  itemDataId,
  itemKey,
  itemValue,
  operationKey,
  operationLabel,
  operationParamKey,
  operationParamLabel,
  operationParams
} from "./viewWorkflow";

defineProps<{
  candidateColumns: (group: QueryDataDetailItemGroup) => TableColumnInfo[];
  candidateRows: (group: QueryDataDetailItemGroup) => ListDataItem[];
  candidateState: (group: QueryDataDetailItemGroup) => { keyword: string; pageIndex: number; pageSize: number; totalPage: number };
  childDraftValue: (group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem, field: ListDataValue) => string;
  detailDrafts: Record<string, string>;
  detailItemGroups: QueryDataDetailItemGroup[];
  detailRows: ListDataValue[];
  detailViewOperations: OperationInfo[];
  enumFieldOptions: (field: ListDataValue) => SelectOption[];
  fieldEditorContext: Record<string, unknown>;
  isCreatingObject: boolean;
  newChildDraftValue: (group: QueryDataDetailItemGroup, field: ListDataValue) => string;
  pending: boolean;
  selectedObjectId: string;
  viewCanEdit: boolean;
}>();

const emit = defineEmits<{
  addDetailItem: [group: QueryDataDetailItemGroup];
  addExistingDetailItem: [group: QueryDataDetailItemGroup, row: ListDataItem];
  deleteDetailItem: [group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem];
  loadCandidatePage: [group: QueryDataDetailItemGroup, pageIndex: number];
  loadExistingDetailItems: [group: QueryDataDetailItemGroup];
  runViewOperation: [operation: OperationInfo];
  saveSelectedObject: [];
  setChildDraftValue: [group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem, field: ListDataValue, value: string];
  setNewChildDraftValue: [group: QueryDataDetailItemGroup, field: ListDataValue, value: string];
  updateCandidateKeyword: [group: QueryDataDetailItemGroup, event: Event];
  updateCandidatePage: [group: QueryDataDetailItemGroup, event: Event];
  updateCandidatePageSize: [group: QueryDataDetailItemGroup, event: Event];
  updateDetailDraft: [key: string, value: string];
  updateDetailItem: [group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem];
}>();
</script>

<template>
  <article class="panel view-detail-panel">
    <div class="panel-heading">
      <h2>Detail</h2>
      <span>{{ selectedObjectId || "No row selected" }}</span>
    </div>

    <div v-if="viewCanEdit" class="view-edit-grid">
      <label v-for="field in detailRows" :key="fieldKey(field)">
        {{ fieldTitle(field) }}
        <MetadataFieldEditor
          :model-value="detailDrafts[fieldKey(field)]"
          :field="field"
          :options="enumFieldOptions(field)"
          :readonly-value="fieldDisplayValue(field)"
          v-bind="fieldEditorContext"
          @update:model-value="(value) => emit('updateDetailDraft', fieldKey(field), value)"
        />
      </label>
      <button class="primary" type="button" :disabled="pending" @click="emit('saveSelectedObject')">
        {{ isCreatingObject ? "Create Row" : "Save Row" }}
      </button>
    </div>
    <div v-else class="empty-state compact">Select a row from the list.</div>

    <div class="detail-fields">
      <div v-for="item in detailRows" :key="fieldKey(item)">
        <span>{{ fieldTitle(item) }}</span>
        <strong>{{ fieldDisplayValue(item) }}</strong>
      </div>
    </div>

    <div v-if="selectedObjectId && !isCreatingObject && detailViewOperations.length" class="view-operations">
      <h3>View Operations</h3>
      <div class="button-row">
        <button
          v-for="operation in detailViewOperations"
          :key="operationKey(operation)"
          type="button"
          :disabled="pending"
          @click="emit('runViewOperation', operation)"
        >
          {{ operationLabel(operation) }}
        </button>
      </div>
      <div v-for="operation in detailViewOperations" :key="`params-${operationKey(operation)}`">
        <span
          v-for="(param, index) in operationParams(operation)"
          :key="operationParamKey(param, index)"
          class="operation-param"
        >
          {{ operationParamLabel(param) }}
        </span>
      </div>
    </div>

    <div v-if="selectedObjectId && !isCreatingObject" class="view-items-panel">
      <div v-if="detailItemGroups.length" class="detail-fields">
        <template v-for="group in detailItemGroups" :key="groupKey(group)">
          <div>
            <span>{{ groupTitle(group) }}</span>
            <strong>{{ groupItems(group).length }} rows</strong>
          </div>
          <div class="item-add-row">
            <label v-for="field in groupColumns(group)" :key="fieldKey(field)">
              {{ fieldTitle(field) }}
              <MetadataFieldEditor
                :model-value="newChildDraftValue(group, field)"
                :field="field"
                :options="enumFieldOptions(field)"
                v-bind="fieldEditorContext"
                :is-added="true"
                :object-id="''"
                :owner-id="selectedObjectId"
                @update:model-value="(value) => emit('setNewChildDraftValue', group, field, value)"
              />
            </label>
            <button type="button" :disabled="pending" @click="emit('addDetailItem', group)">Add</button>
          </div>
          <div v-if="groupSelectFromExists(group)" class="table-wrap">
            <div class="inline-fields">
              <label>
                Search
                <input :value="candidateState(group).keyword" @input="emit('updateCandidateKeyword', group, $event)" />
              </label>
              <label>
                Page
                <input min="1" type="number" :value="candidateState(group).pageIndex" @input="emit('updateCandidatePage', group, $event)" />
              </label>
              <label>
                Page size
                <input min="1" type="number" :value="candidateState(group).pageSize" @input="emit('updateCandidatePageSize', group, $event)" />
              </label>
              <button type="button" :disabled="pending" @click="emit('loadExistingDetailItems', group)">
                Load Existing
              </button>
            </div>
            <div v-if="candidateRows(group).length || candidateState(group).totalPage" class="button-row">
              <button type="button" :disabled="pending || candidateState(group).pageIndex <= 1" @click="emit('loadCandidatePage', group, candidateState(group).pageIndex - 1)">
                Previous
              </button>
              <span>
                Page {{ candidateState(group).pageIndex }} / {{ candidateState(group).totalPage || 1 }}
              </span>
              <button type="button" :disabled="pending || candidateState(group).totalPage === 0 || candidateState(group).pageIndex >= candidateState(group).totalPage" @click="emit('loadCandidatePage', group, candidateState(group).pageIndex + 1)">
                Next
              </button>
            </div>
            <ListDataTable
              v-if="candidateRows(group).length"
              :columns="candidateColumns(group)"
              default-action-label="Select"
              :disabled="pending"
              :row-operations="[]"
              :rows="candidateRows(group)"
              selected-object-id=""
              @select="(row) => emit('addExistingDetailItem', group, row)"
            />
          </div>
          <div
            v-for="item in groupItems(group)"
            :key="itemKey(group, item)"
            class="detail-item-row"
          >
            <span>{{ itemDataId(item) }}</span>
            <label v-for="field in groupColumns(group)" :key="fieldKey(field)">
              {{ fieldTitle(field) }}
              <MetadataFieldEditor
                :model-value="childDraftValue(group, item, field)"
                :field="field"
                :options="enumFieldOptions(field)"
                :readonly-value="itemValue(item, field)"
                v-bind="fieldEditorContext"
                :is-added="false"
                :object-id="itemDataId(item)"
                :owner-id="selectedObjectId"
                @update:model-value="(value) => emit('setChildDraftValue', group, item, field, value)"
              />
            </label>
            <button type="button" :disabled="pending" @click="emit('updateDetailItem', group, item)">
              Save
            </button>
            <button type="button" :disabled="pending" @click="emit('deleteDetailItem', group, item)">
              Delete
            </button>
          </div>
        </template>
      </div>
      <div v-else class="empty-state compact">No child rows loaded.</div>
    </div>
  </article>
</template>
