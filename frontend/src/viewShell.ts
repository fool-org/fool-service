import type { ListDataValue } from "./api";
import { fieldModelId } from "./viewWorkflow";

export interface SelectOption {
  label: string;
  value: string;
}

export function nextObjectId() {
  return String(Date.now());
}

export function enumFieldOptions(optionsByModel: Record<string, SelectOption[]>, field: ListDataValue) {
  return optionsByModel[String(fieldModelId(field))] || [];
}
