import type { ListDataValue, ReportModelColumn } from "./api";
import { fieldInputChecked, fieldInputType } from "./fieldInput";
import {
  reportModelColumnName,
  reportModelColumnType,
  reportModelStateText,
  reportModelStateValue,
  reportModelStates
} from "./viewWorkflow";

export function reportConditionEditorField(column: ReportModelColumn): ListDataValue {
  const name = reportModelColumnName(column);
  const type = reportModelColumnType(column);
  const modelId = column.modelId ?? column.ModelId ?? 0;
  return {
    prpId: name,
    PrpId: name,
    prpShowName: name,
    PrpShowName: name,
    prpType: type,
    PrpType: type,
    prpModelId: modelId,
    PrpModelId: modelId
  };
}

export function reportConditionInitialValue(column: ReportModelColumn) {
  const state = reportModelStates(column)[0];
  if (state) return reportModelStateValue(state);
  return fieldInputType(reportConditionEditorField(column)) === "checkbox" ? "false" : "";
}

export function reportConditionFormattedValue(
  column: ReportModelColumn,
  value: string,
  selectedText = ""
) {
  const state = reportModelStates(column).find((item) => reportModelStateValue(item) === value);
  if (state) return reportModelStateText(state);
  const field = reportConditionEditorField(column);
  if (fieldInputType(field) === "checkbox") return fieldInputChecked(field, value) ? "是" : "否";
  return selectedText || value;
}
