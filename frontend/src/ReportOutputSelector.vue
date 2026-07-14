<script setup lang="ts">
import { computed, ref, watch } from "vue";
import Button from "primevue/button";
import type { ReportCol, ReportModelColumn } from "./api";
import { addReportOutput, moveReportOutput, removeReportOutput, setReportOutputOrder } from "./reportOutputs";
import {
  reportModelColumnId,
  reportModelColumnName,
  reportModelOptionId,
  reportModelOptionName,
  reportModelQueryTypes
} from "./viewWorkflow";

const props = defineProps<{ columns: ReportModelColumn[] }>();
const outputs = defineModel<ReportCol[]>({ required: true });
const candidateKey = ref("");
const queryTypeOptions = ref<{ label: string; value: string }[]>([]);
const selectedTypeId = ref("");
const selectedOutputIndex = ref<number | null>(null);

const candidateOptions = computed(() => props.columns.map((column) => ({
  label: reportModelColumnName(column),
  value: columnKey(column)
})));
const selectedCandidate = computed(() => props.columns.find((column) => columnKey(column) === candidateKey.value));
const selectedOptions = computed(() => outputs.value.map((output, index) => ({
  label: `${output.colName || output.colId || "字段"}${orderLabel(output.orderType)}`,
  value: index
})));

watch(() => props.columns, (columns) => {
  candidateKey.value = columns[0] ? columnKey(columns[0]) : "";
}, { immediate: true });

function columnKey(column: ReportModelColumn) {
  return reportModelColumnId(column) || reportModelColumnName(column);
}

function selectFirstQueryType() {
  selectedTypeId.value = queryTypeOptions.value[0]?.value || "";
}

function chooseCandidate() {
  queryTypeOptions.value = selectedCandidate.value
    ? reportModelQueryTypes(selectedCandidate.value).map((option) => ({
      label: reportModelOptionName(option),
      value: reportModelOptionId(option)
    }))
    : [];
  selectFirstQueryType();
  if (queryTypeOptions.value.length === 1) addOutput();
}

function addOutput() {
  if (!selectedCandidate.value) return;
  if (!queryTypeOptions.value.length) return;
  const hadOutputs = outputs.value.length > 0;
  const selectedTypeName = queryTypeOptions.value.find((option) => option.value === selectedTypeId.value)?.label;
  const next = addReportOutput(outputs.value, selectedCandidate.value, selectedTypeId.value, selectedTypeName);
  if (next === outputs.value) return;
  outputs.value = next;
  if (!hadOutputs) selectedOutputIndex.value = 0;
}

function moveOutput(offset: number) {
  if (selectedOutputIndex.value === null) return;
  const target = selectedOutputIndex.value + offset;
  outputs.value = moveReportOutput(outputs.value, selectedOutputIndex.value, offset);
  if (target >= 0 && target < outputs.value.length) selectedOutputIndex.value = target;
}

function removeOutput() {
  if (selectedOutputIndex.value === null) return;
  const index = selectedOutputIndex.value;
  outputs.value = removeReportOutput(outputs.value, index);
  selectedOutputIndex.value = outputs.value.length ? Math.min(index, outputs.value.length - 1) : null;
}

function orderOutput(orderType: string) {
  if (selectedOutputIndex.value === null) return;
  outputs.value = setReportOutputOrder(outputs.value, selectedOutputIndex.value, orderType);
}

function orderLabel(orderType?: string) {
  if (orderType === "0") return "[升序]";
  if (orderType === "1") return "[降序]";
  return "";
}
</script>

<template>
  <div class="report-output-selector">
    <section>
      <h3>候选列</h3>
      <select v-model="candidateKey" size="10" aria-label="候选列" @change="chooseCandidate">
        <option v-for="option in candidateOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
      </select>
    </section>

    <section class="report-output-method">
      <h3>输出方式</h3>
      <select v-model="selectedTypeId" size="10" aria-label="输出方式">
        <option v-for="option in queryTypeOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
      </select>
      <Button type="button" icon="pi pi-arrow-right" title="加入已选列" aria-label="加入已选列" size="small" severity="secondary" outlined @click="addOutput" />
    </section>

    <section>
      <h3>已选列</h3>
      <select v-model="selectedOutputIndex" size="10" aria-label="已选列">
        <option v-for="option in selectedOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
      </select>
      <div class="report-output-actions">
        <div class="legacy-button-group-xs" role="group" aria-label="调整已选列">
          <Button type="button" icon="pi pi-arrow-up" title="上调" aria-label="上调" severity="secondary" text @click="moveOutput(-1)" />
          <Button type="button" icon="pi pi-arrow-down" title="下调" aria-label="下调" severity="secondary" text @click="moveOutput(1)" />
          <Button type="button" icon="pi pi-trash" title="删除" aria-label="删除" severity="danger" text @click="removeOutput" />
        </div>
        <div class="legacy-button-group-xs" role="group" aria-label="设置排序">
          <Button type="button" icon="pi pi-sort-alpha-down" title="升序" aria-label="升序" severity="secondary" text @click="orderOutput('0')" />
          <Button type="button" icon="pi pi-sort-alpha-up" title="降序" aria-label="降序" severity="secondary" text @click="orderOutput('1')" />
          <Button type="button" icon="pi pi-times" title="取消排序" aria-label="取消排序" severity="secondary" text @click="orderOutput('2')" />
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.report-output-selector {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(160px, 0.8fr) minmax(0, 1.2fr);
  gap: 14px;
}

.report-output-selector section {
  min-width: 0;
}

.report-output-selector h3 {
  margin: 0 0 8px;
  font-size: 0.9rem;
}

.report-output-selector select {
  width: 100%;
  min-height: 220px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  background: #ffffff;
  padding: 4px;
}

.report-output-method {
  display: grid;
  align-content: start;
  gap: 8px;
}

.report-output-method .p-button {
  justify-self: end;
  width: 34px;
  height: 34px;
  padding: 0;
}

.report-output-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 6px;
}

@media (max-width: 700px) {
  .report-output-selector {
    grid-template-columns: 1fr;
  }

  .report-output-selector select {
    min-height: 180px;
  }
}
</style>
