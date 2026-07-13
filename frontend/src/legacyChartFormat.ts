import type { LegacyChartSeries } from "./viewWorkflow";

export function legacyChartNumberText(value: number | string | undefined) {
  if (value === undefined || Number.isNaN(Number(value))) return "-";
  const parts = String(value).split(".");
  return parts[0].replace(/(\d{1,3})(?=(?:\d{3})+(?!\d))/g, "$1,")
    + (parts.length > 1 ? `.${parts[1]}` : "");
}

export function legacyChartValueText(series: LegacyChartSeries, index: number) {
  const value = series.type === "bar"
    ? series.formattedValues?.[index] ?? series.values[index]
    : series.values[index];
  return value === undefined ? "" : String(value);
}

export function legacyChartTooltipText(series: LegacyChartSeries, index: number) {
  return legacyChartNumberText(series.formattedValues?.[index] ?? series.values[index]);
}
