import type { ListDataValue } from "./api";
import { fieldModelId } from "./viewWorkflow";

export interface SelectOption {
  label: string;
  value: string;
}

export const services = [
  { label: "Docker Backend", value: "8080", state: "ready" },
  { label: "MySQL", value: "car_wash", state: "ready" },
  { label: "Redis", value: "6379", state: "ready" }
];

export function nextObjectId() {
  return String(Date.now());
}

export function enumFieldOptions(optionsByModel: Record<string, SelectOption[]>, field: ListDataValue) {
  return optionsByModel[String(fieldModelId(field))] || [];
}
