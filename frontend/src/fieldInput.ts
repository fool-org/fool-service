import type { ListDataValue } from "./api";

export function fieldType(field: ListDataValue) {
  return field.prpType ?? field.PrpType;
}

export function fieldEditType(field: ListDataValue) {
  return field.editType ?? field.EditType;
}

type NumericInputConstraint = { decimal: boolean; maxLength: number };

const constrainedNumberFieldTypes = new Map<string, NumericInputConstraint>([
  ["int", { decimal: false, maxLength: 4 }],
  ["uint", { decimal: false, maxLength: 4 }],
  ["1", { decimal: false, maxLength: 4 }],
  ["2", { decimal: false, maxLength: 4 }],
  ["long", { decimal: false, maxLength: 8 }],
  ["ulong", { decimal: false, maxLength: 8 }],
  ["3", { decimal: false, maxLength: 8 }],
  ["4", { decimal: false, maxLength: 8 }],
  ["float", { decimal: true, maxLength: 4 }],
  ["5", { decimal: true, maxLength: 4 }],
  ["double", { decimal: true, maxLength: 8 }],
  ["6", { decimal: true, maxLength: 8 }]
]);

function normalizedEditType(field: ListDataValue) {
  return String(fieldEditType(field) ?? "").toLowerCase();
}

function normalizedPropertyType(field: ListDataValue) {
  return String(fieldType(field) ?? "").toLowerCase();
}

export function fieldInputType(field: ListDataValue) {
  const type = normalizedPropertyType(field);
  if (type === "boolean" || type === "8") return "checkbox";
  if (type === "date" || type === "12") return "date";
  if (type === "time" || type === "13") return "time";
  if (type === "datetime" || type === "14") return "datetime-local";
  if (type) return "text";

  const editType = normalizedEditType(field);
  if (editType === "checkbox" || editType === "2") return "checkbox";
  if (editType === "datepicker" || editType === "6") return "date";
  if (editType === "timepicker" || editType === "7") return "time";
  if (editType === "datetimepicker" || editType === "8") return "datetime-local";
  return "text";
}

export function fieldInputMaxLength(field: ListDataValue) {
  return constrainedNumberFieldTypes.get(normalizedPropertyType(field))?.maxLength;
}

export function sanitizeFieldInput(field: ListDataValue, value: unknown) {
  const text = String(value ?? "");
  const constraint = constrainedNumberFieldTypes.get(normalizedPropertyType(field));
  if (!constraint) return text;
  const sanitized = text.replace(constraint.decimal ? /[^0-9.]/g : /[^0-9]/g, "");
  return sanitized.slice(0, constraint.maxLength);
}

export function fieldInputChecked(field: ListDataValue, value: string) {
  if (fieldInputType(field) !== "checkbox") return false;
  const text = String(value ?? "").trim().toLowerCase();
  return text === "true" || text === "1";
}

export function fieldInputValue(field: ListDataValue, value: string) {
  const text = String(value ?? "");
  if (fieldInputType(field) !== "datetime-local") return text;
  const match = text.trim().match(/^(\d{4}-\d{2}-\d{2})[ T](\d{2}:\d{2}(?::\d{2})?)(?:\.\d+)?$/);
  return match ? `${match[1]}T${match[2]}` : text;
}
