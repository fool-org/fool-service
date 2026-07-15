<script setup lang="ts">
import { ref, watch } from "vue";
import Button from "primevue/button";
import Dialog from "primevue/dialog";
import InputText from "primevue/inputtext";
import Tab from "primevue/tab";
import TabList from "primevue/tablist";
import TabPanel from "primevue/tabpanel";
import TabPanels from "primevue/tabpanels";
import Tabs from "primevue/tabs";
import type { ListDataItem, ListDataValue, OperationInfo, QueryDataDetailDataItem, QueryDataDetailItemGroup, TableColumnInfo } from "./api";
import { fieldInputType } from "./fieldInput";
import LegacyErrorDialog from "./LegacyErrorDialog.vue";
import LegacyPagination from "./LegacyPagination.vue";
import ListDataTable from "./ListDataTable.vue";
import MetadataFieldEditor from "./MetadataFieldEditor.vue";
import { candidateRecordInfo, type ChildCandidateState } from "./useChildCandidates";
import { nextObjectId, type SelectOption } from "./viewShell";
import {
  buildGroupItemDrafts,
  fieldDisplayValue,
  fieldKey,
  fieldTitle,
  groupColumns,
  groupDetailViewId,
  groupItems,
  groupKey,
  groupListViewId,
  groupSelectFromExists,
  groupSelectedViewId,
  groupTitle,
  groupViewName,
  itemDataId,
  itemKey,
  itemValue,
  isEnumField,
  operationKey,
  operationLabel
} from "./viewWorkflow";

const props = defineProps<{
  candidateColumns: (group: QueryDataDetailItemGroup) => TableColumnInfo[];
  candidateRows: (group: QueryDataDetailItemGroup) => ListDataItem[];
  candidateState: (group: QueryDataDetailItemGroup) => ChildCandidateState;
  candidateViewLoading: boolean;
  childDraftValue: (group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem, field: ListDataValue) => string;
  detailDrafts: Record<string, string>;
  detailItemGroups: QueryDataDetailItemGroup[];
  detailRows: ListDataValue[];
  detailViewOperations: OperationInfo[];
  enumFieldOptions: (field: ListDataValue) => SelectOption[];
  errorMessage: string;
  fieldEditorContext: Record<string, unknown>;
  infoMessage: string;
  isCreatingObject: boolean;
  isPendingAddedItem: (group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) => boolean;
  loadExistingDetailView: (group: QueryDataDetailItemGroup) => Promise<boolean>;
  operationResult: { message: string; success: boolean } | null;
  saveDialogVisible: boolean;
  saving: boolean;
  schemaOnly: boolean;
  selectedObjectId: string;
  title: string;
  viewCanEdit: boolean;
}>();

const isEditing = ref(false);
const activeGroupKey = ref("");
const editingItemKey = ref("");
const pickerGroupKey = ref("");
const stagedItemKeys = ref<Set<string>>(new Set());

watch(
  () => [props.selectedObjectId, props.isCreatingObject] as const,
  () => {
    const firstGroup = props.detailItemGroups[0];
    isEditing.value = props.isCreatingObject;
    activeGroupKey.value = props.schemaOnly ? "" : firstGroup ? groupKey(firstGroup) : "";
    editingItemKey.value = "";
    pickerGroupKey.value = "";
    stagedItemKeys.value = new Set();
  },
  { immediate: true }
);

watch(
  () => props.detailRows,
  () => {
    if (!props.isCreatingObject) isEditing.value = false;
  }
);

watch(isEditing, (editing) => {
  if (!editing) editingItemKey.value = "";
});

watch(
  () => props.detailItemGroups.map(groupKey),
  (keys) => {
    if (props.schemaOnly) activeGroupKey.value = "";
    else if (!keys.includes(activeGroupKey.value)) activeGroupKey.value = keys[0] || "";
    if (!keys.includes(pickerGroupKey.value)) pickerGroupKey.value = "";
  },
  { immediate: true }
);

const emit = defineEmits<{
  addDetailItem: [group: QueryDataDetailItemGroup, itemId?: string];
  addExistingDetailItem: [group: QueryDataDetailItemGroup, row: ListDataItem];
  deleteDetailItem: [group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem];
  dismissError: [];
  dismissInfo: [];
  dismissOperationResult: [];
  loadCandidatePage: [group: QueryDataDetailItemGroup, pageIndex: number];
  queryExistingDetailItems: [group: QueryDataDetailItemGroup];
  runViewOperation: [operation: OperationInfo, editing: boolean];
  saveDialogHidden: [];
  saveSelectedObject: [];
  setChildDraftValue: [group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem, field: ListDataValue, value: string];
  updateCandidateKeyword: [group: QueryDataDetailItemGroup, event: Event];
  updateDetailDraft: [key: string, value: string];
  updateDetailItem: [group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem];
}>();

async function openExistingPicker(group: QueryDataDetailItemGroup) {
  if (await props.loadExistingDetailView(group)) pickerGroupKey.value = groupKey(group);
}

function addItem(group: QueryDataDetailItemGroup) {
  if (props.isCreatingObject) {
    emit("addDetailItem", group);
    return;
  }
  if (groupSelectFromExists(group)) {
    openExistingPicker(group);
    return;
  }
  const itemId = groupSelectedViewId(group) ? "" : nextObjectId();
  if (itemId && editingItemKey.value) stageEditingItem();
  emit("addDetailItem", group, itemId);
  if (itemId && isEditing.value) editingItemKey.value = `${groupKey(group)}:${itemId}`;
}

function selectExistingItem(group: QueryDataDetailItemGroup, row: ListDataItem) {
  emit("addExistingDetailItem", group, row);
  pickerGroupKey.value = "";
}

function currentEditingItem() {
  for (const group of props.detailItemGroups) {
    const item = groupItems(group).find((candidate) => itemKey(group, candidate) === editingItemKey.value);
    if (item) return { group, item };
  }
  return null;
}

function stageEditingItem() {
  const current = currentEditingItem();
  if (!current) return;
  emit("updateDetailItem", current.group, current.item);
  stagedItemKeys.value = new Set([...stagedItemKeys.value, editingItemKey.value]);
}

function toggleDetailItem(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
  if (!isEditing.value) return;
  const key = itemKey(group, item);
  if (editingItemKey.value) {
    const previousKey = editingItemKey.value;
    stageEditingItem();
    editingItemKey.value = "";
    if (previousKey === key) return;
  }
  editingItemKey.value = key;
}

function deleteItem(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
  const key = itemKey(group, item);
  if (editingItemKey.value === key) editingItemKey.value = "";
  stagedItemKeys.value = new Set([...stagedItemKeys.value].filter((itemKeyValue) => itemKeyValue !== key));
  emit("deleteDetailItem", group, item);
}

function displayedItemValue(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem, field: ListDataValue) {
  if (!stagedItemKeys.value.has(itemKey(group, item))) return itemValue(item, field);
  const value = props.childDraftValue(group, item, field);
  const originalValue = buildGroupItemDrafts(group, item)[fieldKey(field)] ?? "";
  if (value === originalValue) return itemValue(item, field);
  if (isEnumField(field)) return props.enumFieldOptions(field).find((option) => option.value === value)?.label || value;
  if (fieldInputType(field) === "checkbox") return value === "true" || value === "1" ? "是" : "否";
  return value;
}

function detailItemHref(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
  return `/view${groupDetailViewId(group)}/${itemDataId(item)}`;
}

function childActionColumnCount(group: QueryDataDetailItemGroup) {
  return groupDetailViewId(group) ? 3 : 2;
}
</script>

<template>
  <article class="panel view-detail-panel">
    <div class="panel-heading">
      <h2>{{ isCreatingObject ? `${title} -新建` : selectedObjectId ? `${title} -${selectedObjectId}` : title }}</h2>
    </div>

    <div v-if="selectedObjectId && (viewCanEdit || isCreatingObject)" class="detail-toolbar legacy-button-group">
      <Button
        v-if="!isCreatingObject"
        type="button"
        label="编辑"
        icon="pi pi-pencil"
        severity="secondary"
        outlined
        :disabled="isEditing"
        @click="isEditing = true"
      />
      <Button
        type="button"
        label="保存"
        icon="pi pi-check"
        severity="secondary"
        outlined
        :loading="saving"
        :disabled="saving || !isEditing"
        @click="emit('saveSelectedObject')"
      />
      <template v-if="!isCreatingObject">
        <Button
          v-for="operation in detailViewOperations"
          :key="operationKey(operation)"
          type="button"
          :label="operationLabel(operation)"
          icon="pi pi-check"
          severity="secondary"
          outlined
          @click="emit('runViewOperation', operation, isEditing)"
        />
      </template>
    </div>

    <Dialog
      :visible="candidateViewLoading"
      modal
      header="加载中"
      :closable="false"
      :close-on-escape="false"
      :dismissable-mask="false"
      :draggable="false"
    >
      <p>正在加载，请稍后....</p>
    </Dialog>

    <Dialog
      :visible="saveDialogVisible"
      modal
      header="保存中"
      :closable="false"
      :close-on-escape="false"
      :dismissable-mask="false"
      :draggable="false"
      @after-hide="emit('saveDialogHidden')"
    >
      <p>正在保存，请稍后....</p>
    </Dialog>

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
    <div v-if="!isEditing && (selectedObjectId || schemaOnly)" class="detail-field-grid">
      <div v-for="item in detailRows" :key="fieldKey(item)">
        <span>{{ fieldTitle(item) }}</span>
        <strong>{{ fieldDisplayValue(item) || "\u00a0" }}</strong>
      </div>
    </div>

    <Dialog
      v-if="infoMessage"
      :visible="true"
      modal
      header="操作提示"
      :closable="false"
      :draggable="false"
      @update:visible="(visible) => { if (!visible) emit('dismissInfo') }"
    >
      <p>操作成功</p>
      <p>{{ infoMessage }}</p>
      <template #footer>
        <Button type="button" label="确定" severity="secondary" outlined @click="emit('dismissInfo')" />
      </template>
    </Dialog>

    <LegacyErrorDialog :message="errorMessage" @dismiss="emit('dismissError')" />

    <Dialog
      v-if="operationResult"
      :visible="true"
      modal
      header="执行结果"
      :closable="false"
      :draggable="false"
      @update:visible="(visible) => { if (!visible) emit('dismissOperationResult') }"
    >
      <p>{{ operationResult.success ? "操作成功" : "操作失败" }}</p>
      <p>{{ operationResult.message }}</p>
      <template #footer>
        <Button type="button" label="确定" severity="secondary" outlined @click="emit('dismissOperationResult')" />
      </template>
    </Dialog>

    <div v-if="detailItemGroups.length && (selectedObjectId || schemaOnly)" class="view-items-panel">
      <Tabs v-model:value="activeGroupKey" class="detail-collection-tabs legacy-tabs">
        <TabList scrollable>
          <Tab v-for="group in detailItemGroups" :key="groupKey(group)" :value="groupKey(group)">
            {{ groupTitle(group) }}
          </Tab>
        </TabList>
        <TabPanels>
          <TabPanel v-for="group in detailItemGroups" :key="groupKey(group)" :value="groupKey(group)">
            <div v-if="!schemaOnly" class="detail-collection-toolbar legacy-button-group">
              <Button type="button" label="增加" icon="pi pi-plus" severity="secondary" outlined @click="addItem(group)" />
            </div>
            <Dialog
              v-if="!schemaOnly"
              :visible="pickerGroupKey === groupKey(group)"
              modal
              class="detail-picker-dialog"
              :header="`选择 ${groupTitle(group)}`"
              :draggable="false"
              dismissable-mask
              @update:visible="(visible) => { if (!visible) pickerGroupKey = '' }"
            >
              <template #closeicon><span class="legacy-dialog-close-icon" aria-hidden="true">&times;</span></template>
              <div class="detail-picker-content">
                <div class="candidate-query-toolbar">
                  <InputText
                    :model-value="candidateState(group).keyword"
                    class="candidate-query-input"
                    type="text"
                    placeholder="输入条件"
                    aria-label="查询条件"
                    @input="emit('updateCandidateKeyword', group, $event)"
                    @keyup.enter="emit('queryExistingDetailItems', group)"
                  />
                  <Button type="button" label="查找" severity="secondary" outlined @click="emit('queryExistingDetailItems', group)" />
                </div>
                <div class="table-wrap detail-picker-results">
                  <ListDataTable
                    :columns="candidateColumns(group)"
                    :condensed="false"
                    default-action-label="选择"
                    :minimum-rows="candidateState(group).queried ? candidateState(group).pageSize : 0"
                    :row-operations="[]"
                    :rows="candidateRows(group)"
                    selected-object-id=""
                    :show-action-header="false"
                    :show-default-action="true"
                    @select="(row) => selectExistingItem(group, row)"
                  />
                </div>
                <LegacyPagination
                  class="candidate-results-footer"
                  :page-index="candidateState(group).pageIndex"
                  :page-size="candidateState(group).pageSize"
                  :record-label="candidateRecordInfo(candidateState(group))"
                  :show-pager="candidateRows(group).length > 0 || candidateState(group).totalPage > 0"
                  :total-items="candidateState(group).totalItem"
                  @page="emit('loadCandidatePage', group, $event)"
                />
              </div>
              <template #footer>
                <Button type="button" label="取消" severity="secondary" outlined @click="pickerGroupKey = ''" />
                <Button type="button" label="确定" />
              </template>
            </Dialog>
            <div class="table-wrap detail-items-table">
              <table class="legacy-item-table detail-items-grid">
                <thead>
                  <tr>
                    <th v-for="field in groupColumns(group)" :key="fieldKey(field)">{{ fieldTitle(field) }}</th>
                    <th :colspan="schemaOnly ? 1 : childActionColumnCount(group)">操作</th>
                  </tr>
                </thead>
                <tbody v-if="!schemaOnly">
                  <tr v-for="item in groupItems(group)" :key="itemKey(group, item)">
                    <td v-for="field in groupColumns(group)" :key="fieldKey(field)">
                      <div v-if="isEditing && editingItemKey === itemKey(group, item) && !groupDetailViewId(group)" class="detail-item-editor">
                        <MetadataFieldEditor
                          :model-value="childDraftValue(group, item, field)"
                          :field="field"
                          :options="enumFieldOptions(field)"
                          :readonly-value="itemValue(item, field)"
                          v-bind="fieldEditorContext"
                          :is-added="isPendingAddedItem(group, item)"
                          :object-id="itemDataId(item)"
                          :owner-id="selectedObjectId"
                          :view-id="groupListViewId(group)"
                          :view-name="groupViewName(group)"
                          @update:model-value="(value) => emit('setChildDraftValue', group, item, field, value)"
                        />
                      </div>
                      <span v-else>{{ displayedItemValue(group, item, field) }}</span>
                    </td>
                    <td>
                      <Button
                        v-if="!groupSelectFromExists(group) && !groupDetailViewId(group)"
                        type="button"
                        :label="editingItemKey === itemKey(group, item) ? '保存' : '编辑'"
                        :icon="editingItemKey === itemKey(group, item) ? 'pi pi-save' : 'pi pi-pencil'"
                        size="small"
                        severity="secondary"
                        text
                        @click="toggleDetailItem(group, item)"
                      />
                      <a v-if="!groupSelectFromExists(group) && groupDetailViewId(group)" class="detail-item-link" :href="detailItemHref(group, item)">
                        <i class="pi pi-arrow-right" aria-hidden="true"></i>
                        编辑
                      </a>
                    </td>
                    <td>
                      <Button type="button" label="删除" icon="pi pi-trash" size="small" severity="danger" text @click="deleteItem(group, item)" />
                    </td>
                    <td v-if="groupDetailViewId(group)">
                      <a class="detail-item-link" :href="detailItemHref(group, item)">
                        <i class="pi pi-eye" aria-hidden="true"></i>
                        详细
                      </a>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </TabPanel>
        </TabPanels>
      </Tabs>
    </div>
  </article>
</template>
