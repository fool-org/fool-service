<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import Button from "primevue/button";
import Checkbox from "primevue/checkbox";
import Dialog from "primevue/dialog";
import InputText from "primevue/inputtext";
import Message from "primevue/message";
import Select from "primevue/select";
import Tab from "primevue/tab";
import TabList from "primevue/tablist";
import TabPanel from "primevue/tabpanel";
import TabPanels from "primevue/tabpanels";
import Tabs from "primevue/tabs";
import ReportOutputSelector from "./ReportOutputSelector.vue";
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
  groupReportConditions,
  reportConditionGroupError,
  reportConditionSelectionIds,
  type ReportConditionDraft,
  ungroupReportConditions
} from "./reportConditions";
import type { WorkflowActionRunner } from "./useViewDataWorkflow";
import {
  reportGridCells,
  reportGridPage,
  reportGridTotalPages,
  reportModelColumnId,
  reportModelColumnName,
  reportModelColumns,
  reportModelCompareTypes,
  reportModelOptionId,
  reportModelOptionName,
  reportModelStates,
  reportModelStateText,
  reportModelStateValue,
  reportRowsFromCells
} from "./viewWorkflow";

const props = defineProps<{
  runAction: WorkflowActionRunner;
  token: string;
  viewId: number;
}>();
const emit = defineEmits<{ close: [] }>();

const currentPage = ref(1);
const pageSize = 10;
const reportName = ref("");
const modelResponse = ref<CommonResponse<ReportModelResult> | null>(null);
const reportResponse = ref<CommonResponse<ReportGridResult> | null>(null);
const reportCols = ref<ReportCol[]>([]);
const conditions = ref<ReportConditionDraft[]>([]);
const selectedConditionIds = ref<number[]>([]);
const statusMessage = ref("");
const activeTab = ref("output");
const showingResults = ref(false);
const reportSetupLoading = ref(true);
const reportRunning = ref(false);
let nextConditionId = 1;
const joinOptions = [
  { label: "与", value: "and" },
  { label: "或", value: "or" }
];

const modelColumns = computed(() => reportModelColumns(modelResponse.value?.data));
const reportRows = computed(() => reportRowsFromCells(reportGridCells(reportResponse.value?.data)));
const resultPage = computed(() => reportGridPage(reportResponse.value?.data, currentPage.value));
const resultPages = computed(() => reportGridTotalPages(reportResponse.value?.data));
const filterExp = computed<ReportFilterExp | undefined>(() => buildReportConditionFilter(conditions.value, simpleFilter));

function columnKey(column: ReportModelColumn) {
  return reportModelColumnId(column) || reportModelColumnName(column);
}

function columnFor(condition: ReportConditionDraft) {
  return modelColumns.value.find((column) => columnKey(column) === condition.columnId);
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

function addCondition() {
  conditions.value.push({
    id: nextConditionId++,
    columnId: "",
    compareId: "",
    groupPath: [],
    join: "and",
    value: ""
  });
}

function groupSelectedConditions() {
  statusMessage.value = reportConditionGroupError(conditions.value, selectedConditionIds.value);
  if (statusMessage.value) return;
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

function conditionSelectionChecked(condition: ReportConditionDraft) {
  return reportConditionSelectionIds(conditions.value, condition)
    .every((id) => selectedConditionIds.value.includes(id));
}

function updateConditionSelection(condition: ReportConditionDraft, selected: boolean) {
  const conditionIds = new Set(reportConditionSelectionIds(conditions.value, condition));
  selectedConditionIds.value = selected
    ? [...new Set([...selectedConditionIds.value, ...conditionIds])]
    : selectedConditionIds.value.filter((id) => !conditionIds.has(id));
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
  reportSetupLoading.value = false;
  if (!response) {
    statusMessage.value = "无法加载报表字段。";
    return;
  }
  modelResponse.value = response;
  reportCols.value = [];
}

function buildRequest(name?: string) {
  return buildMakeReportRequest({
    token: props.token,
    viewId: props.viewId,
    currentPage: currentPage.value,
    pageSize,
    reportCols: reportCols.value.map((column, index) => ({ ...column, index })),
    filterExp: filterExp.value,
    reportName: name
  });
}

async function runReport(page = currentPage.value) {
  currentPage.value = Math.max(1, page);
  statusMessage.value = "";
  const revealResults = !showingResults.value;
  reportRunning.value = revealResults;
  const response = await props.runAction("mkrpt", () =>
    postApi<ReportGridResult>("/api/v1/report/mkrpt", buildRequest())
  );
  reportRunning.value = false;
  if (response) {
    reportResponse.value = response;
    if (revealResults) showingResults.value = true;
  } else {
    statusMessage.value = "无法生成报表。";
  }
}

function changeReportPage(offset: number) {
  const page = resultPage.value + offset;
  if (page < 1 || page > Math.max(1, resultPages.value)) return;
  void runReport(page);
}

async function saveReport() {
  statusMessage.value = "";
  const response = await props.runAction("saverpt", () =>
    postApi<void>("/api/v1/report/saverpt", buildRequest(reportName.value))
  );
  statusMessage.value = response ? "报表定义已提交。" : "无法保存报表定义。";
}

function backToReportSetup() {
  showingResults.value = false;
  currentPage.value = 1;
}

onMounted(() => void loadReportColumns());
</script>

<template>
  <Dialog
    v-if="!reportSetupLoading && !reportRunning"
    :visible="true"
    modal
    class="report-dialog"
    :closable="!showingResults"
    :draggable="false"
    dismissable-mask
    @update:visible="(visible) => { if (!visible) emit('close') }"
  >
    <template #closeicon><span class="legacy-dialog-close-icon" aria-hidden="true">&times;</span></template>
    <template #header>
      <div class="report-dialog-heading">
        <strong>{{ showingResults ? "报表结果" : "生成报表" }}</strong>
      </div>
    </template>

    <Message v-if="statusMessage" severity="info" :closable="false">{{ statusMessage }}</Message>

    <section v-if="showingResults" class="report-section">
      <div class="report-result-heading">
        <p>报表结果 共{{ resultPages }}页 当前第{{ resultPage }}页</p>
        <div class="report-result-actions legacy-button-group-xs">
          <Button type="button" label="前一页" severity="secondary" outlined @click="changeReportPage(-1)" />
          <Button type="button" label="下一页" severity="secondary" outlined @click="changeReportPage(1)" />
          <Button type="button" label="导出当前页" severity="secondary" outlined />
          <Button type="button" label="导出全部" severity="secondary" outlined />
        </div>
      </div>
      <div class="table-wrap report-results">
        <table class="report-result-table">
          <tbody>
            <tr v-for="(row, rowIndex) in reportRows" :key="`report-row-${rowIndex}`">
              <td v-for="(cell, cellIndex) in row" :key="`report-cell-${rowIndex}-${cellIndex}`">
                <span v-if="cell">{{ cell }}</span>
                <span v-else aria-hidden="true">&nbsp;</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <Tabs v-else v-model:value="activeTab" class="report-tabs legacy-tabs">
      <TabList scrollable>
        <Tab value="output">输出</Tab>
        <Tab value="conditions">条件</Tab>
        <Tab value="save">保存报表</Tab>
      </TabList>
      <TabPanels>
        <TabPanel value="output">
          <section class="report-section">
            <ReportOutputSelector v-model="reportCols" :columns="modelColumns" />
          </section>
        </TabPanel>

        <TabPanel value="conditions">
          <section class="report-section">
            <div class="report-condition-editor">
              <div class="report-condition-header">
                <Button type="button" icon="pi pi-plus" class="report-condition-icon" size="small" severity="secondary" text title="增加条件" aria-label="增加条件" @click="addCondition" />
                <span aria-hidden="true"></span>
                <span aria-hidden="true"></span>
                <Button type="button" icon="pi pi-list" class="report-condition-icon" size="small" severity="secondary" text title="合并分组" aria-label="合并分组" @click="groupSelectedConditions" />
                <strong>与/或</strong>
                <strong>字段</strong>
                <strong>运算</strong>
                <strong>值</strong>
              </div>
              <div v-if="conditions.length" class="report-conditions">
                <div v-for="(condition, index) in conditions" :key="condition.id" class="report-condition-row" :style="{ marginLeft: `${condition.groupPath.length * 14}px` }">
                  <span aria-hidden="true"></span>
                  <Button type="button" icon="pi pi-trash" class="report-condition-icon" severity="danger" text title="删除条件" aria-label="删除条件" @click="removeCondition(index)" />
                  <Checkbox
                    v-if="!condition.groupPath.length || startsConditionGroup(condition, index)"
                    :model-value="conditionSelectionChecked(condition)"
                    :input-id="`condition-${condition.id}`"
                    :aria-label="`选择条件 ${index + 1}`"
                    binary
                    @update:model-value="updateConditionSelection(condition, Boolean($event))"
                  />
                  <span v-else aria-hidden="true"></span>
                  <div class="condition-group">
                    <span
                      v-for="(groupId, depthIndex) in condition.groupPath"
                      :key="groupId"
                      class="condition-group-marker"
                      :class="{ 'is-alternate': depthIndex % 2 === 1 }"
                      aria-hidden="true"
                    ></span>
                    <Button v-if="startsConditionGroup(condition, index)" type="button" icon="pi pi-reply" class="report-condition-icon" severity="secondary" text size="small" title="拆分分组" aria-label="拆分分组" @click="ungroupCondition(condition)" />
                  </div>
                  <Select v-if="index" v-model="condition.join" :options="joinOptions" option-label="label" option-value="value" aria-label="条件关系" />
                  <span v-else aria-hidden="true"></span>
                  <Select v-model="condition.columnId" :options="modelColumnOptions()" option-label="label" option-value="value" aria-label="条件字段" @change="updateConditionColumn(condition)" />
                  <Select v-model="condition.compareId" :options="compareTypeOptions(condition)" option-label="label" option-value="value" aria-label="条件运算" />
                  <Select v-if="condition.columnId && condition.compareId && states(condition).length" v-model="condition.value" :options="stateOptions(condition)" option-label="label" option-value="value" aria-label="条件值" />
                  <InputText v-else-if="condition.columnId && condition.compareId" v-model="condition.value" aria-label="条件值" fluid />
                  <span v-else aria-hidden="true"></span>
                </div>
              </div>
              <div class="report-condition-footer">
                <Button type="button" icon="pi pi-plus" class="report-condition-icon" size="small" severity="secondary" text title="增加条件" aria-label="增加条件" @click="addCondition" />
              </div>
            </div>
          </section>
        </TabPanel>

        <TabPanel value="save">
          <section class="report-section report-save-section">
            <h4>输入报表信息以保存该报表</h4>
            <label>
              报表名称
              <InputText v-model="reportName" fluid />
            </label>
          </section>
        </TabPanel>
      </TabPanels>
    </Tabs>

    <template #footer>
      <Button v-if="showingResults" type="button" label="返回" @click="backToReportSetup" />
      <Button v-if="!showingResults" type="button" label="取消" severity="secondary" outlined @click="emit('close')" />
      <Button v-if="!showingResults" type="button" label="确定" @click="runReport()" />
      <Button v-if="!showingResults" type="button" label="保存报表定义" severity="info" @click="saveReport" />
    </template>
  </Dialog>
</template>
