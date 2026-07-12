<script setup lang="ts">
import { ref, watch } from "vue";
import Button from "primevue/button";
import Dialog from "primevue/dialog";
import InputText from "primevue/inputtext";
import Message from "primevue/message";
import Tab from "primevue/tab";
import TabList from "primevue/tablist";
import TabPanel from "primevue/tabpanel";
import TabPanels from "primevue/tabpanels";
import Tabs from "primevue/tabs";
import Tag from "primevue/tag";
import type { ListDataItem, ListDataValue, OperationInfo, QueryDataDetailDataItem, QueryDataDetailItemGroup, TableColumnInfo } from "./api";
import ListDataTable from "./ListDataTable.vue";
import MetadataFieldEditor from "./MetadataFieldEditor.vue";
import type { SelectOption } from "./viewShell";
import {
  fieldDisplayValue,
  fieldKey,
  fieldTitle,
  groupColumns,
  groupDetailViewId,
  groupItems,
  groupKey,
  groupSelectFromExists,
  groupTitle,
  itemDataId,
  itemKey,
  itemValue,
  operationKey,
  operationLabel
} from "./viewWorkflow";

const props = defineProps<{
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
  operationResult: { message: string; success: boolean } | null;
  pending: boolean;
  schemaOnly: boolean;
  selectedObjectId: string;
  title: string;
  viewCanEdit: boolean;
}>();

const isEditing = ref(false);
const activeGroupKey = ref("");
const pickerGroupKey = ref("");

watch(
  () => [props.selectedObjectId, props.isCreatingObject] as const,
  () => {
    isEditing.value = props.isCreatingObject;
  },
  { immediate: true }
);

watch(
  () => props.detailRows,
  () => {
    if (!props.isCreatingObject) isEditing.value = false;
  }
);

watch(
  () => props.detailItemGroups.map(groupKey),
  (keys) => {
    if (!keys.includes(activeGroupKey.value)) activeGroupKey.value = keys[0] || "";
    if (!keys.includes(pickerGroupKey.value)) pickerGroupKey.value = "";
  },
  { immediate: true }
);

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

function openExistingPicker(group: QueryDataDetailItemGroup) {
  pickerGroupKey.value = groupKey(group);
  emit("loadExistingDetailItems", group);
}

function selectExistingItem(group: QueryDataDetailItemGroup, row: ListDataItem) {
  emit("addExistingDetailItem", group, row);
  pickerGroupKey.value = "";
}
</script>

<template>
  <article class="panel view-detail-panel">
    <div class="panel-heading">
      <h2>{{ title }}</h2>
      <Tag :value="selectedObjectId || 'No row selected'" :severity="selectedObjectId ? 'secondary' : 'warn'" rounded />
    </div>

    <div v-if="selectedObjectId && (viewCanEdit || isCreatingObject)" class="detail-toolbar">
      <Button
        v-if="!isEditing"
        type="button"
        label="Edit"
        icon="pi pi-pencil"
        :disabled="pending"
        @click="isEditing = true"
      />
      <Button
        v-else
        type="button"
        :label="isCreatingObject ? 'Create Row' : 'Save Row'"
        :icon="isCreatingObject ? 'pi pi-plus' : 'pi pi-save'"
        :loading="pending"
        :disabled="pending"
        @click="emit('saveSelectedObject')"
      />
      <template v-if="!isCreatingObject">
        <Button
          v-for="operation in detailViewOperations"
          :key="operationKey(operation)"
          type="button"
          :disabled="pending || isEditing"
          :label="operationLabel(operation)"
          icon="pi pi-bolt"
          severity="secondary"
          outlined
          @click="emit('runViewOperation', operation)"
        />
      </template>
    </div>

    <div v-if="isEditing" class="detail-field-grid detail-field-edit">
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
    </div>
    <div v-else-if="!selectedObjectId" class="empty-state compact">
      {{ schemaOnly ? "View definition loaded." : "Select a row from the list." }}
    </div>

    <div v-if="!isEditing && (selectedObjectId || schemaOnly)" class="detail-field-grid">
      <div v-for="item in detailRows" :key="fieldKey(item)">
        <span>{{ fieldTitle(item) }}</span>
        <strong>{{ fieldDisplayValue(item) }}</strong>
      </div>
    </div>

    <div v-if="schemaOnly && detailItemGroups.length" class="view-items-panel">
      <div class="detail-field-grid">
        <div v-for="group in detailItemGroups" :key="groupKey(group)">
          <span>{{ groupTitle(group) }}</span>
          <strong>{{ groupColumns(group).map(fieldTitle).join(", ") }}</strong>
        </div>
      </div>
    </div>

    <Message v-if="operationResult" :severity="operationResult.success ? 'success' : 'error'" :closable="false">
      {{ operationResult.message }}
    </Message>

    <div v-if="selectedObjectId && !isCreatingObject" class="view-items-panel">
      <Tabs v-if="detailItemGroups.length" v-model:value="activeGroupKey" class="detail-collection-tabs">
        <TabList scrollable>
          <Tab v-for="group in detailItemGroups" :key="groupKey(group)" :value="groupKey(group)">
            {{ groupTitle(group) }}
            <span>{{ groupItems(group).length }}</span>
          </Tab>
        </TabList>
        <TabPanels>
          <TabPanel v-for="group in detailItemGroups" :key="groupKey(group)" :value="groupKey(group)">
            <div v-if="isEditing && !groupSelectFromExists(group)" class="item-add-row">
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
              <Button type="button" label="Add" icon="pi pi-plus" severity="secondary" outlined :disabled="pending" @click="emit('addDetailItem', group)" />
            </div>
            <div v-if="isEditing && groupSelectFromExists(group)" class="detail-collection-toolbar">
              <Button type="button" label="Add existing" icon="pi pi-plus" severity="secondary" outlined :disabled="pending" @click="openExistingPicker(group)" />
            </div>
            <Dialog
              :visible="pickerGroupKey === groupKey(group)"
              modal
              class="detail-picker-dialog"
              :header="`Select ${groupTitle(group)}`"
              :closable="!pending"
              :draggable="false"
              :dismissable-mask="!pending"
              @update:visible="(visible) => { if (!visible) pickerGroupKey = '' }"
            >
              <div class="detail-picker-content">
                <div class="inline-fields">
                  <label>
                    Search
                    <InputText :model-value="candidateState(group).keyword" fluid @input="emit('updateCandidateKeyword', group, $event)" />
                  </label>
                  <label>
                    Page
                    <InputText min="1" type="number" :model-value="String(candidateState(group).pageIndex)" fluid @input="emit('updateCandidatePage', group, $event)" />
                  </label>
                  <label>
                    Page size
                    <InputText min="1" type="number" :model-value="String(candidateState(group).pageSize)" fluid @input="emit('updateCandidatePageSize', group, $event)" />
                  </label>
                  <Button type="button" label="Search" icon="pi pi-search" severity="secondary" outlined :disabled="pending" @click="emit('loadExistingDetailItems', group)" />
                </div>
                <div v-if="candidateRows(group).length || candidateState(group).totalPage" class="button-row">
                  <Button type="button" label="Previous" icon="pi pi-chevron-left" severity="secondary" text :disabled="pending || candidateState(group).pageIndex <= 1" @click="emit('loadCandidatePage', group, candidateState(group).pageIndex - 1)" />
                  <span>Page {{ candidateState(group).pageIndex }} / {{ candidateState(group).totalPage || 1 }}</span>
                  <Button type="button" label="Next" icon="pi pi-chevron-right" icon-pos="right" severity="secondary" text :disabled="pending || candidateState(group).totalPage === 0 || candidateState(group).pageIndex >= candidateState(group).totalPage" @click="emit('loadCandidatePage', group, candidateState(group).pageIndex + 1)" />
                </div>
                <div class="table-wrap detail-picker-results">
                  <ListDataTable
                    v-if="candidateRows(group).length"
                    :columns="candidateColumns(group)"
                    default-action-label="选择"
                    :disabled="pending"
                    :row-operations="[]"
                    :rows="candidateRows(group)"
                    selected-object-id=""
                    :show-default-action="true"
                    @select="(row) => selectExistingItem(group, row)"
                  />
                  <div v-else class="empty-state compact">No candidate rows.</div>
                </div>
              </div>
              <template #footer>
                <Button type="button" label="Close" icon="pi pi-times" severity="secondary" text :disabled="pending" @click="pickerGroupKey = ''" />
              </template>
            </Dialog>
            <div class="table-wrap detail-items-table">
              <table class="legacy-item-table detail-items-grid">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th v-for="field in groupColumns(group)" :key="fieldKey(field)">{{ fieldTitle(field) }}</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in groupItems(group)" :key="itemKey(group, item)">
                    <td>{{ itemDataId(item) }}</td>
                    <td v-for="field in groupColumns(group)" :key="fieldKey(field)">
                      <div v-if="isEditing && !groupDetailViewId(group)" class="detail-item-editor">
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
                      </div>
                      <span v-else>{{ itemValue(item, field) }}</span>
                    </td>
                    <td>
                      <div class="detail-item-actions">
                        <Button v-if="isEditing && !groupDetailViewId(group)" type="button" label="Save" icon="pi pi-save" size="small" :disabled="pending" @click="emit('updateDetailItem', group, item)" />
                        <a v-if="groupDetailViewId(group)" class="detail-item-link" :href="`/view${groupDetailViewId(group)}/${itemDataId(item)}`">
                          <i class="pi pi-arrow-right" aria-hidden="true"></i>
                          Edit
                        </a>
                        <Button v-if="isEditing" type="button" label="Delete" icon="pi pi-trash" size="small" severity="danger" outlined :disabled="pending" @click="emit('deleteDetailItem', group, item)" />
                      </div>
                    </td>
                  </tr>
                  <tr v-if="!groupItems(group).length">
                    <td :colspan="groupColumns(group).length + 2">No child rows.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </TabPanel>
        </TabPanels>
      </Tabs>
      <div v-else class="empty-state compact">No child rows loaded.</div>
    </div>
  </article>
</template>
