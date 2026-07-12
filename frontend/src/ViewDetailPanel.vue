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
import ListDataTable from "./ListDataTable.vue";
import MetadataFieldEditor from "./MetadataFieldEditor.vue";
import { nextObjectId, type SelectOption } from "./viewShell";
import {
  buildGroupItemDrafts,
  fieldDisplayValue,
  fieldInputType,
  fieldKey,
  fieldTitle,
  groupColumns,
  groupDetailViewId,
  groupItems,
  groupKey,
  groupSelectFromExists,
  groupSelectedViewId,
  groupTitle,
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
  candidateState: (group: QueryDataDetailItemGroup) => { keyword: string; pageIndex: number; pageSize: number; totalPage: number };
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
  operationResult: { message: string; success: boolean } | null;
  pending: boolean;
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
    isEditing.value = props.isCreatingObject;
    editingItemKey.value = "";
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
    if (!keys.includes(activeGroupKey.value)) activeGroupKey.value = keys[0] || "";
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
  loadExistingDetailItems: [group: QueryDataDetailItemGroup];
  runViewOperation: [operation: OperationInfo, editing: boolean];
  saveSelectedObject: [];
  setChildDraftValue: [group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem, field: ListDataValue, value: string];
  updateCandidateKeyword: [group: QueryDataDetailItemGroup, event: Event];
  updateDetailDraft: [key: string, value: string];
  updateDetailItem: [group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem];
}>();

function openExistingPicker(group: QueryDataDetailItemGroup) {
  pickerGroupKey.value = groupKey(group);
  emit("loadExistingDetailItems", group);
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
</script>

<template>
  <article class="panel view-detail-panel">
    <div class="panel-heading">
      <h2>{{ isCreatingObject ? `${title} -新建` : selectedObjectId ? `${title} -${selectedObjectId}` : title }}</h2>
    </div>

    <div v-if="selectedObjectId && (viewCanEdit || isCreatingObject)" class="detail-toolbar">
      <Button
        v-if="!isCreatingObject"
        type="button"
        label="编辑"
        icon="pi pi-pencil"
        :disabled="pending || isEditing"
        @click="isEditing = true"
      />
      <Button
        type="button"
        label="保存"
        icon="pi pi-check"
        :loading="pending"
        :disabled="pending || !isEditing"
        @click="emit('saveSelectedObject')"
      />
      <template v-if="!isCreatingObject">
        <Button
          v-for="operation in detailViewOperations"
          :key="operationKey(operation)"
          type="button"
          :disabled="pending"
          :label="operationLabel(operation)"
          icon="pi pi-check"
          severity="secondary"
          outlined
          @click="emit('runViewOperation', operation, isEditing)"
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
      {{ schemaOnly ? "已加载视图定义。" : "请从列表选择记录。" }}
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

    <Dialog
      v-if="infoMessage"
      :visible="true"
      modal
      header="操作提示"
      :draggable="false"
      @update:visible="(visible) => { if (!visible) emit('dismissInfo') }"
    >
      <p>操作成功</p>
      <p>{{ infoMessage }}</p>
      <template #footer>
        <Button type="button" label="确定" severity="secondary" @click="emit('dismissInfo')" />
      </template>
    </Dialog>

    <Dialog
      v-if="errorMessage"
      :visible="true"
      modal
      header="发生错误"
      :draggable="false"
      @update:visible="(visible) => { if (!visible) emit('dismissError') }"
    >
      <p>{{ errorMessage }}</p>
      <template #footer>
        <Button type="button" label="关闭" severity="secondary" @click="emit('dismissError')" />
      </template>
    </Dialog>

    <Dialog
      v-if="operationResult"
      :visible="true"
      modal
      header="执行结果"
      :draggable="false"
      @update:visible="(visible) => { if (!visible) emit('dismissOperationResult') }"
    >
      <p>{{ operationResult.success ? "操作成功" : "操作失败" }}</p>
      <p>{{ operationResult.message }}</p>
      <template #footer>
        <Button type="button" label="关闭" severity="secondary" @click="emit('dismissOperationResult')" />
      </template>
    </Dialog>

    <div v-if="selectedObjectId" class="view-items-panel">
      <Tabs v-if="detailItemGroups.length" v-model:value="activeGroupKey" class="detail-collection-tabs">
        <TabList scrollable>
          <Tab v-for="group in detailItemGroups" :key="groupKey(group)" :value="groupKey(group)">
            {{ groupTitle(group) }}
          </Tab>
        </TabList>
        <TabPanels>
          <TabPanel v-for="group in detailItemGroups" :key="groupKey(group)" :value="groupKey(group)">
            <div class="detail-collection-toolbar">
              <Button type="button" label="增加" icon="pi pi-plus" severity="secondary" outlined :disabled="pending" @click="addItem(group)" />
            </div>
            <Dialog
              :visible="pickerGroupKey === groupKey(group)"
              modal
              class="detail-picker-dialog"
              :header="`选择 ${groupTitle(group)}`"
              :closable="!pending"
              :draggable="false"
              :dismissable-mask="!pending"
              @update:visible="(visible) => { if (!visible) pickerGroupKey = '' }"
            >
              <div class="detail-picker-content">
                <div class="inline-fields">
                  <label>
                    查询条件
                    <InputText :model-value="candidateState(group).keyword" fluid @input="emit('updateCandidateKeyword', group, $event)" />
                  </label>
                  <Button type="button" label="查找" severity="secondary" outlined :disabled="pending" @click="emit('loadExistingDetailItems', group)" />
                </div>
                <div v-if="candidateRows(group).length || candidateState(group).totalPage" class="button-row">
                  <Button type="button" label="上一页" severity="secondary" text :disabled="pending || candidateState(group).pageIndex <= 1" @click="emit('loadCandidatePage', group, candidateState(group).pageIndex - 1)" />
                  <span>第 {{ candidateState(group).pageIndex }} / {{ candidateState(group).totalPage || 1 }} 页</span>
                  <Button type="button" label="下一页" severity="secondary" text :disabled="pending || candidateState(group).totalPage === 0 || candidateState(group).pageIndex >= candidateState(group).totalPage" @click="emit('loadCandidatePage', group, candidateState(group).pageIndex + 1)" />
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
                  <div v-else class="empty-state compact">暂无候选记录。</div>
                </div>
              </div>
              <template #footer>
                <Button type="button" label="关闭" severity="secondary" text :disabled="pending" @click="pickerGroupKey = ''" />
              </template>
            </Dialog>
            <div class="table-wrap detail-items-table">
              <table class="legacy-item-table detail-items-grid">
                <thead>
                  <tr>
                    <th v-for="field in groupColumns(group)" :key="fieldKey(field)">{{ fieldTitle(field) }}</th>
                    <th colspan="2">操作</th>
                  </tr>
                </thead>
                <tbody>
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
                        :disabled="pending"
                        @click="toggleDetailItem(group, item)"
                      />
                      <a v-if="groupDetailViewId(group)" class="detail-item-link" :href="`/view${groupDetailViewId(group)}/${itemDataId(item)}`">
                        <i class="pi pi-arrow-right" aria-hidden="true"></i>
                        编辑
                      </a>
                    </td>
                    <td>
                      <Button type="button" label="删除" icon="pi pi-trash" size="small" severity="danger" outlined :disabled="pending" @click="deleteItem(group, item)" />
                    </td>
                  </tr>
                  <tr v-if="!groupItems(group).length">
                    <td :colspan="groupColumns(group).length + 2">暂无子项。</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </TabPanel>
        </TabPanels>
      </Tabs>
      <div v-else class="empty-state compact">暂无子项。</div>
    </div>
  </article>
</template>
