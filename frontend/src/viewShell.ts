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

export const migrationModules = [
  { source: "SCPB01-Soway.Data", target: "fool-common" },
  { source: "SCPB02-Soway.DB", target: "fool-dao" },
  { source: "SCPB05-Soway.Model", target: "fool-model" },
  { source: "SWDQ01-Soway.Query", target: "fool-query" },
  { source: "Soway.Server", target: "fool-view" },
  { source: "SWUA Auth", target: "fool-auth" }
];

export function nextObjectId() {
  return String(Date.now());
}

export function enumFieldOptions(optionsByModel: Record<string, SelectOption[]>, field: ListDataValue) {
  return optionsByModel[String(field.prpModelId ?? field.PrpModelId ?? "")] || [];
}
