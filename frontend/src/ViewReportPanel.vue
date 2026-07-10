<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
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

interface ConditionDraft {
  columnId: string;
  compareId: string;
  join: "and" | "or";
  value: string;
}

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
const conditions = ref<ConditionDraft[]>([]);
const statusMessage = ref("");

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
const canRun = computed(() => selectedReportCols.value.length > 0 && conditionsComplete.value && !props.pending);
const filterExp = computed<ReportFilterExp | undefined>(() => {
  const expressions = conditions.value
    .map(simpleFilter)
    .filter((expression): expression is ReportFilterExp => Boolean(expression));
  if (!expressions.length) return undefined;
  if (expressions.length === 1) return expressions[0];
  return {
    firstExp: expressions[0],
    sequences: expressions.slice(1).map((expression, index) => ({
      boolOp: {
        dbName: conditions.value[index + 1].join,
        showName: conditions.value[index + 1].join.toUpperCase()
      },
      addedExp: expression
    }))
  };
});

function columnKey(column: ReportModelColumn) {
  return reportModelColumnId(column) || reportModelColumnName(column);
}

function columnFor(condition: ConditionDraft) {
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

function compareTypes(condition: ConditionDraft) {
  return reportModelCompareTypes(columnFor(condition) || {});
}

function states(condition: ConditionDraft) {
  return reportModelStates(columnFor(condition) || {});
}

function addCondition() {
  const column = modelColumns.value[0];
  const compare = column && reportModelCompareTypes(column)[0];
  const state = column && reportModelStates(column)[0];
  conditions.value.push({
    columnId: column ? columnKey(column) : "",
    compareId: compare ? reportModelOptionId(compare) : "",
    join: "and",
    value: state ? reportModelStateValue(state) : ""
  });
}

function updateConditionColumn(condition: ConditionDraft) {
  const compare = compareTypes(condition)[0];
  const state = states(condition)[0];
  condition.compareId = compare ? reportModelOptionId(compare) : "";
  condition.value = state ? reportModelStateValue(state) : "";
}

function simpleFilter(condition: ConditionDraft): ReportFilterExp | null {
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
        <input v-model.number="pageSize" min="1" type="number" />
      </label>
      <label>
        Report name
        <input v-model="reportName" />
      </label>
      <button class="primary" type="button" :disabled="!canRun" @click="runReport()">Run Report</button>
      <button type="button" :disabled="!canRun || !reportName.trim()" @click="saveReport">Save Definition</button>
      <button type="button" :disabled="pending" @click="emit('close')">Close</button>
    </div>
    <p v-if="statusMessage" class="report-status">{{ statusMessage }}</p>

    <section class="report-section">
      <div class="section-heading">
        <h3>Output columns</h3>
        <button type="button" :disabled="pending" @click="loadReportColumns">Reload</button>
      </div>
      <div class="table-wrap report-columns">
        <table v-if="modelColumns.length">
          <thead>
            <tr>
              <th>Output</th>
              <th>Column</th>
              <th>Type</th>
              <th>Output type</th>
              <th>Order</th>
              <th>Position</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="column in modelColumns" :key="columnKey(column)">
              <td>
                <input
                  v-model="selectedColumnIds"
                  :aria-label="`Output ${reportModelColumnName(column)}`"
                  type="checkbox"
                  :value="columnKey(column)"
                />
              </td>
              <td>{{ reportModelColumnName(column) }}</td>
              <td>{{ reportModelColumnType(column) }}</td>
              <td>
                <select
                  v-model="outputTypeByColumn[columnKey(column)]"
                  :disabled="pending || !selectedColumnIds.includes(columnKey(column))"
                >
                  <option v-if="!reportModelQueryTypes(column).length" value="">Raw value</option>
                  <option
                    v-for="option in reportModelQueryTypes(column)"
                    :key="reportModelOptionId(option)"
                    :value="reportModelOptionId(option)"
                  >
                    {{ reportModelOptionName(option) }}
                  </option>
                </select>
              </td>
              <td>
                <select
                  v-model="orderTypeByColumn[columnKey(column)]"
                  :disabled="pending || !selectedColumnIds.includes(columnKey(column))"
                >
                  <option value="2">None</option>
                  <option value="0">Ascending</option>
                  <option value="1">Descending</option>
                </select>
              </td>
              <td>
                <div class="report-position">
                  <span>{{ selectedPosition(column) }}</span>
                  <button
                    type="button"
                    title="Move output column up"
                    :disabled="pending || !canMoveColumn(column, -1)"
                    @click="moveColumn(column, -1)"
                  >&uarr;</button>
                  <button
                    type="button"
                    title="Move output column down"
                    :disabled="pending || !canMoveColumn(column, 1)"
                    @click="moveColumn(column, 1)"
                  >&darr;</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state compact">No report columns.</div>
      </div>
    </section>

    <section class="report-section">
      <div class="section-heading">
        <h3>Conditions</h3>
        <button type="button" :disabled="pending || !modelColumns.length" @click="addCondition">Add condition</button>
      </div>
      <div v-if="conditions.length" class="report-conditions">
        <div v-for="(condition, index) in conditions" :key="index" class="report-condition-row">
          <select v-if="index" v-model="condition.join" aria-label="Condition join" :disabled="pending">
            <option value="and">AND</option>
            <option value="or">OR</option>
          </select>
          <span v-else class="condition-first">Where</span>
          <select v-model="condition.columnId" aria-label="Condition column" :disabled="pending" @change="updateConditionColumn(condition)">
            <option v-for="column in modelColumns" :key="columnKey(column)" :value="columnKey(column)">
              {{ reportModelColumnName(column) }}
            </option>
          </select>
          <select v-model="condition.compareId" aria-label="Condition comparison" :disabled="pending">
            <option v-for="option in compareTypes(condition)" :key="reportModelOptionId(option)" :value="reportModelOptionId(option)">
              {{ reportModelOptionName(option) }}
            </option>
          </select>
          <select v-if="states(condition).length" v-model="condition.value" aria-label="Condition value" :disabled="pending">
            <option v-for="state in states(condition)" :key="reportModelStateValue(state)" :value="reportModelStateValue(state)">
              {{ reportModelStateText(state) }}
            </option>
          </select>
          <input v-else v-model="condition.value" aria-label="Condition value" :disabled="pending" />
          <button
            class="icon-button"
            type="button"
            title="Remove condition"
            :disabled="pending"
            @click="conditions.splice(index, 1)"
          >&times;</button>
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
        <table v-if="reportRows.length">
          <thead>
            <tr>
              <th v-for="(cell, index) in reportRows[0]" :key="`report-head-${index}`">{{ cell }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, rowIndex) in reportRows.slice(1)" :key="`report-row-${rowIndex}`">
              <td v-for="(cell, colIndex) in row" :key="`report-cell-${rowIndex}-${colIndex}`">{{ cell }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state compact">Run the report to see results.</div>
      </div>
      <div v-if="reportResponse" class="button-row report-pagination">
        <button type="button" :disabled="pending || resultPage <= 1" @click="runReport(resultPage - 1)">Previous</button>
        <button
          type="button"
          :disabled="pending || !resultPages || resultPage >= resultPages"
          @click="runReport(resultPage + 1)"
        >Next</button>
      </div>
    </section>
  </article>
</template>
