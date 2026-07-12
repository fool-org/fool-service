import type { ReportCol, ReportModelColumn } from "./api";
import {
  reportModelColumnId,
  reportModelColumnName,
  reportModelOptionId,
  reportModelOptionName,
  reportModelQueryTypes
} from "./viewWorkflow";

function reindex(outputs: ReportCol[]) {
  return outputs.map((output, index) => ({ ...output, index }));
}

export function addReportOutput(outputs: ReportCol[], column: ReportModelColumn, selectedTypeId: string) {
  const colId = reportModelColumnId(column);
  if (outputs.some((output) => output.colId === colId && output.selectedTypeId === selectedTypeId)) return outputs;
  const selectedType = reportModelQueryTypes(column).find((option) => reportModelOptionId(option) === selectedTypeId);
  const columnName = reportModelColumnName(column);
  const typeName = selectedType ? reportModelOptionName(selectedType) : "";
  return reindex([...outputs, {
    colName: typeName ? `${columnName}[${typeName}]` : columnName,
    colId,
    selectedTypeId,
    orderType: "2"
  }]);
}

export function moveReportOutput(outputs: ReportCol[], index: number, offset: number) {
  const target = index + offset;
  if (index < 0 || target < 0 || target >= outputs.length) return outputs;
  const next = [...outputs];
  [next[index], next[target]] = [next[target], next[index]];
  return reindex(next);
}

export function removeReportOutput(outputs: ReportCol[], index: number) {
  if (index < 0 || index >= outputs.length) return outputs;
  return reindex(outputs.filter((_, outputIndex) => outputIndex !== index));
}

export function setReportOutputOrder(outputs: ReportCol[], index: number, orderType: string) {
  if (index < 0 || index >= outputs.length) return outputs;
  return outputs.map((output, outputIndex) => outputIndex === index ? { ...output, orderType } : output);
}
