import type { ListDataValue } from "./api";

export interface SelectOption {
  label: string;
  value: string;
}

export const services = [
  { label: "Docker Backend", value: "8080", state: "ready" },
  { label: "MySQL", value: "car_wash", state: "ready" },
  { label: "Redis", value: "6379", state: "ready" }
];

export const navItems = [
  { id: "views", label: "Views" },
  { id: "tools", label: "API Tools" },
  { id: "migration", label: "Migration" }
];

export function nextObjectId() {
  return String(Date.now());
}

export function enumFieldOptions(optionsByModel: Record<string, SelectOption[]>, field: ListDataValue) {
  return optionsByModel[String(field.prpModelId ?? field.PrpModelId ?? "")] || [];
}
