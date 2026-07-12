<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import Button from "primevue/button";
import Checkbox from "primevue/checkbox";
import Column from "primevue/column";
import DataTable from "primevue/datatable";
import InputNumber from "primevue/inputnumber";
import InputText from "primevue/inputtext";
import Message from "primevue/message";
import Paginator, { type PageState } from "primevue/paginator";
import Select from "primevue/select";
import {
  type CommonResponse,
  type ReportCol,
  type ReportFilterExp,
  type ReportGridResult,
  type ReportModelColumn,
  type ReportModelResult,
  postApi
} from "./api";
import { buildLegacyListViewRequest, buildMakeReportRequest } from "./payload";
import {
  buildReportConditionFilter,
  canGroupReportConditions,
  groupReportConditions,
  type ReportConditionDraft,
  ungroupReportConditions
} from "./reportConditions";
import type { WorkflowActionRunner } from "./useViewDataWorkflow";
import {
  buildReportColsFromModel,
  reportGridCells,
  reportGridPage,
  reportGridTotalPages,
  reportGridTotalRecords,
  reportModelColumnId,
  reportModelColumnName,
  reportModelColumnType,
  reportModelColumns,
  reportModelCompareTypes,
  reportModelOptionId,
  reportModelOptionName,
  reportModelQueryTypes,
  reportModelStates,
  reportModelStateText,
  reportModelStateValue,
  reportRowsFromCells
} from "./viewWorkflow";

const props = defineProps<{
  pending: boolean;
  runAction: WorkflowActionRunner;
  token: string;
  viewId: number;
}>();
const emit = defineEmits<{ close: [] }>();

const currentPage = ref(1);
const pageSize = ref(10);
const reportName = ref("View Report");
const modelResponse = ref<CommonResponse<ReportModelResult> | null>(null);
const reportResponse = ref<CommonResponse<ReportGridResult> | null>(null);
const selectedColumnIds = ref<string[]>([]);
const outputTypeByColumn = ref<Record<string, string>>({});
const orderTypeByColumn = ref<Record<string, string>>({});
const conditions = ref<ReportConditionDraft[]>([]);
const selectedConditionIds = ref<number[]>([]);
const statusMessage = ref("");
let nextConditionId = 1;
const joinOptions = [
  { label: "AND", value: "and" },
  { label: "OR", value: "or" }
];
const orderOptions = [
  { label: "None", value: "2" },
  { label: "Ascending", value: "0" },
  { label: "Descending", value: "1" }
];

const modelColumns = computed(() => reportModelColumns(modelResponse.value?.data));
const selectedColumnModels = computed(() =>
  selectedColumnIds.value
    .map((key) => modelColumns.value.find((column) => columnKey(column) === key))
    .filter((column): column is ReportModelColumn => Boolean(column))
);
const selectedReportCols = computed<ReportCol[]>(() => {
  const defaults = buildReportColsFromModel(selectedColumnModels.value);
  return selectedColumnModels.value.map((column, index) => {
    const key = columnKey(column);
    const selectedTypeId = outputTypeByColumn.value[key] || defaults[index]?.selectedTypeId;
    const selectedType = reportModelQueryTypes(column).find((option) => reportModelOptionId(option) === selectedTypeId);
    const name = reportModelColumnName(column);
    const outputName = selectedType ? reportModelOptionName(selectedType) : "";
    return {
      ...defaults[index],
      colName: outputName ? `${name}[${outputName}]` : name,
      colId: reportModelColumnId(column),
      selectedTypeId,
      index,
      orderType: orderTypeByColumn.value[key] || "2"
    };
  });
});
const reportRows = computed(() => reportRowsFromCells(reportGridCells(reportResponse.value?.data)));
const resultPage = computed(() => reportGridPage(reportResponse.value?.data, currentPage.value));
const resultPages = computed(() => reportGridTotalPages(reportResponse.value?.data));
const resultRecords = computed(() => reportGridTotalRecords(reportResponse.value?.data));
const conditionsComplete = computed(() => conditions.value.every((condition) => condition.columnId && condition.compareId));
const canGroupConditions = computed(() => canGroupReportConditions(conditions.value, selectedConditionIds.value));
const canRun = computed(() => selectedReportCols.value.length > 0 && conditionsComplete.value && !props.pending);
const filterExp = computed<ReportFilterExp | undefined>(() => buildReportConditionFilter(conditions.value, simpleFilter));

function columnKey(column: ReportModelColumn) {
  return reportModelColumnId(column) || reportModelColumnName(column);
}

function columnFor(condition: ReportConditionDraft) {
  return modelColumns.value.find((column) => columnKey(column) === condition.columnId);
}

function selectedPosition(column: ReportModelColumn) {
  const index = selectedColumnIds.value.indexOf(columnKey(column));
  return index < 0 ? "" : String(index + 1);
}

function moveColumn(column: ReportModelColumn, offset: number) {
  const index = selectedColumnIds.value.indexOf(columnKey(column));
  const target = index + offset;
  if (index < 0 || target < 0 || target >= selectedColumnIds.value.length) return;
  const next = [...selectedColumnIds.value];
  [next[index], next[target]] = [next[target], next[index]];
  selectedColumnIds.value = next;
}

function canMoveColumn(column: ReportModelColumn, offset: number) {
  const index = selectedColumnIds.value.indexOf(columnKey(column));
  const target = index + offset;
  return index >= 0 && target >= 0 && target < selectedColumnIds.value.length;
}

function compareTypes(condition: ReportConditionDraft) {
  return reportModelCompareTypes(columnFor(condition) || {});
}

function states(condition: ReportConditionDraft) {
  return reportModelStates(columnFor(condition) || {});
}

function modelColumnOptions() {
  return modelColumns.value.map((column) => ({ label: reportModelColumnName(column), value: columnKey(column) }));
}

function queryTypeOptions(column: ReportModelColumn) {
  const options = reportModelQueryTypes(column).map((option) => ({
    label: reportModelOptionName(option),
    value: reportModelOptionId(option)
  }));
  return options.length ? options : [{ label: "Raw value", value: "" }];
}

function compareTypeOptions(condition: ReportConditionDraft) {
  return compareTypes(condition).map((option) => ({
    label: reportModelOptionName(option),
    value: reportModelOptionId(option)
  }));
}

function stateOptions(condition: ReportConditionDraft) {
  return states(condition).map((state) => ({
    label: reportModelStateText(state),
    value: reportModelStateValue(state)
  }));
}

function changeResultPage(event: PageState) {
  void runReport(event.page + 1);
}

function addCondition() {
  const column = modelColumns.value[0];
  const compare = column && reportModelCompareTypes(column)[0];
  const state = column && reportModelStates(column)[0];
  conditions.value.push({
    id: nextConditionId++,
    columnId: column ? columnKey(column) : "",
    compareId: compare ? reportModelOptionId(compare) : "",
    groupPath: [],
    join: "and",
    value: state ? reportModelStateValue(state) : ""
  });
}

function groupSelectedConditions() {
  conditions.value = groupReportConditions(conditions.value, selectedConditionIds.value);
  selectedConditionIds.value = [];
}

function ungroupCondition(condition: ReportConditionDraft) {
  conditions.value = ungroupReportConditions(conditions.value, condition.groupPath);
}

function startsConditionGroup(condition: ReportConditionDraft, index: number) {
  if (!condition.groupPath.length || index === 0) return Boolean(condition.groupPath.length);
  const previousPath = conditions.value[index - 1].groupPath;
  return condition.groupPath.some((value, pathIndex) => previousPath[pathIndex] !== value);
}

function removeCondition(index: number) {
  const [removed] = conditions.value.splice(index, 1);
  selectedConditionIds.value = selectedConditionIds.value.filter((id) => id !== removed?.id);
}

function updateConditionColumn(condition: ReportConditionDraft) {
  const compare = compareTypes(condition)[0];
  const state = states(condition)[0];
  condition.compareId = compare ? reportModelOptionId(compare) : "";
  condition.value = state ? reportModelStateValue(state) : "";
}

function simpleFilter(condition: ReportConditionDraft): ReportFilterExp | null {
  const column = columnFor(condition);
  const compare = compareTypes(condition).find((option) => reportModelOptionId(option) === condition.compareId);
  if (!column || !compare) return null;
  const state = states(condition).find((item) => reportModelStateValue(item) === condition.value);
  return {
    col: { id: reportModelColumnId(column), name: reportModelColumnName(column) },
    compareOp: { id: reportModelOptionId(compare), name: reportModelOptionName(compare) },
    valueExp: condition.value,
    valueFmt: state ? reportModelStateText(state) : condition.value
  };
}

async function loadReportColumns() {
  statusMessage.value = "";
  const response = await props.runAction("report-columns", () =>
    postApi<ReportModelResult>("/api/v1/report/getmkqview", buildLegacyListViewRequest({
      token: props.token,
      viewId: props.viewId
    }))
  );
  if (!response) {
    statusMessage.value = "Unable to load report columns.";
    return;
  }
  modelResponse.value = response;
  const columns = reportModelColumns(response.data);
  const defaults = buildReportColsFromModel(columns);
  selectedColumnIds.value = columns.map(columnKey);
  outputTypeByColumn.value = Object.fromEntries(columns.map((column, index) => [
    columnKey(column),
    defaults[index]?.selectedTypeId || ""
  ]));
  orderTypeByColumn.value = Object.fromEntries(columns.map((column) => [columnKey(column), "2"]));
}

function buildRequest(name?: string) {
  return buildMakeReportRequest({
    token: props.token,
    viewId: props.viewId,
    currentPage: currentPage.value,
    pageSize: pageSize.value,
    reportCols: selectedReportCols.value,
    filterExp: filterExp.value,
    reportName: name
  });
}

async function runReport(page = currentPage.value) {
  currentPage.value = Math.max(1, page);
  statusMessage.value = "";
  const response = await props.runAction("mkrpt", () =>
    postApi<ReportGridResult>("/api/v1/report/mkrpt", buildRequest())
  );
  if (response) {
    reportResponse.value = response;
  } else {
    statusMessage.value = "Unable to run the report.";
  }
}

async function saveReport() {
  statusMessage.value = "";
  const response = await props.runAction("saverpt", () =>
    postApi<void>("/api/v1/report/saverpt", buildRequest(reportName.value))
  );
  statusMessage.value = response ? "Definition submitted." : "Unable to submit the definition.";
}

onMounted(() => void loadReportColumns());
</script>

<template>
  <article class="panel view-report-panel">
    <div class="panel-heading">
      <h2>Report</h2>
      <span>View {{ viewId }}</span>
    </div>

    <div class="report-toolbar">
      <label>
        Page size
        <InputNumber v-model="pageSize" :min="1" :max="200" :use-grouping="false" fluid />
      </label>
      <label>
        Report name
        <InputText v-model="reportName" fluid />
      </label>
      <Button type="button" label="Run Report" icon="pi pi-play" :disabled="!canRun" @click="runReport()" />
      <Button type="button" label="Save Definition" icon="pi pi-save" severity="secondary" outlined :disabled="!canRun || !reportName.trim()" @click="saveReport" />
      <Button type="button" label="Close" icon="pi pi-times" severity="secondary" text :disabled="pending" @click="emit('close')" />
    </div>
    <Message v-if="statusMessage" severity="info" :closable="false">{{ statusMessage }}</Message>

    <section class="report-section">
      <div class="section-heading">
        <h3>Output columns</h3>
        <Button type="button" label="Reload" icon="pi pi-refresh" size="small" severity="secondary" text :disabled="pending" @click="loadReportColumns" />
      </div>
      <div class="table-wrap report-columns">
        <DataTable v-if="modelColumns.length" :value="modelColumns" scrollable striped-rows size="small">
          <Column header="Output">
            <template #body="{ data: column }">
              <Checkbox v-model="selectedColumnIds" :input-id="`output-${columnKey(column)}`" :aria-label="`Output ${reportModelColumnName(column)}`" :value="columnKey(column)" />
            </template>
          </Column>
          <Column header="Column"><template #body="{ data: column }">{{ reportModelColumnName(column) }}</template></Column>
          <Column header="Type"><template #body="{ data: column }">{{ reportModelColumnType(column) }}</template></Column>
          <Column header="Output type">
            <template #body="{ data: column }">
              <Select v-model="outputTypeByColumn[columnKey(column)]" :options="queryTypeOptions(column)" option-label="label" option-value="value" size="small" :disabled="pending || !selectedColumnIds.includes(columnKey(column))" />
            </template>
          </Column>
          <Column header="Order">
            <template #body="{ data: column }">
              <Select v-model="orderTypeByColumn[columnKey(column)]" :options="orderOptions" option-label="label" option-value="value" size="small" :disabled="pending || !selectedColumnIds.includes(columnKey(column))" />
            </template>
          </Column>
          <Column header="Position">
            <template #body="{ data: column }">
              <div class="report-position">
                <span>{{ selectedPosition(column) }}</span>
                <Button type="button" icon="pi pi-arrow-up" size="small" severity="secondary" text title="Move output column up" :disabled="pending || !canMoveColumn(column, -1)" @click="moveColumn(column, -1)" />
                <Button type="button" icon="pi pi-arrow-down" size="small" severity="secondary" text title="Move output column down" :disabled="pending || !canMoveColumn(column, 1)" @click="moveColumn(column, 1)" />
              </div>
            </template>
          </Column>
        </DataTable>
        <div v-else class="empty-state compact">No report columns.</div>
      </div>
    </section>

    <section class="report-section">
      <div class="section-heading">
        <h3>Conditions</h3>
        <div class="report-condition-actions">
          <Button type="button" label="Group selected" icon="pi pi-object-group" size="small" severity="secondary" outlined :disabled="pending || !canGroupConditions" @click="groupSelectedConditions" />
          <Button type="button" label="Add condition" icon="pi pi-plus" size="small" severity="secondary" outlined :disabled="pending || !modelColumns.length" @click="addCondition" />
        </div>
      </div>
      <div v-if="conditions.length" class="report-conditions">
        <div v-for="(condition, index) in conditions" :key="condition.id" class="report-condition-row" :style="{ marginLeft: `${condition.groupPath.length * 14}px` }">
          <Checkbox v-model="selectedConditionIds" :input-id="`condition-${condition.id}`" :value="condition.id" :aria-label="`Select condition ${index + 1}`" />
          <div class="condition-group">
            <span v-if="condition.groupPath.length">{{ condition.groupPath.map((id) => `G${id}`).join(" / ") }}</span>
            <Button v-if="startsConditionGroup(condition, index)" type="button" icon="pi pi-reply" severity="secondary" text size="small" title="Ungroup" aria-label="Ungroup" :disabled="pending" @click="ungroupCondition(condition)" />
          </div>
          <Select v-if="index" v-model="condition.join" :options="joinOptions" option-label="label" option-value="value" aria-label="Condition join" :disabled="pending" />
          <span v-else class="condition-first">Where</span>
          <Select v-model="condition.columnId" :options="modelColumnOptions()" option-label="label" option-value="value" aria-label="Condition column" :disabled="pending" @change="updateConditionColumn(condition)" />
          <Select v-model="condition.compareId" :options="compareTypeOptions(condition)" option-label="label" option-value="value" aria-label="Condition comparison" :disabled="pending" />
          <Select v-if="states(condition).length" v-model="condition.value" :options="stateOptions(condition)" option-label="label" option-value="value" aria-label="Condition value" :disabled="pending" />
          <InputText v-else v-model="condition.value" aria-label="Condition value" :disabled="pending" fluid />
          <Button
            type="button"
            icon="pi pi-trash"
            severity="danger"
            text
            title="Remove condition"
            aria-label="Remove condition"
            :disabled="pending"
            @click="removeCondition(index)"
          />
        </div>
      </div>
      <div v-else class="empty-state compact">All rows are included.</div>
    </section>

    <section class="report-section">
      <div class="section-heading">
        <h3>Results</h3>
        <span v-if="reportResponse">{{ resultRecords }} rows · Page {{ resultPage }} / {{ resultPages || 1 }}</span>
      </div>
      <div class="table-wrap report-results">
        <DataTable v-if="reportRows.length" :value="reportRows.slice(1)" scrollable striped-rows size="small">
          <Column v-for="(cell, index) in reportRows[0]" :key="`report-head-${index}`" :header="cell">
            <template #body="{ data: row }">{{ row[index] }}</template>
          </Column>
        </DataTable>
        <div v-else class="empty-state compact">Run the report to see results.</div>
      </div>
      <Paginator
        v-if="reportResponse"
        :first="Math.max(0, (resultPage - 1) * pageSize)"
        :rows="pageSize"
        :total-records="resultRecords"
        :disabled="pending"
        template="FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink"
        current-page-report-template="Page {currentPage} of {totalPages} · {totalRecords} rows"
        @page="changeResultPage"
      />
    </section>
  </article>
</template>
