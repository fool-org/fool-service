import { describe, expect, it } from "vitest";
import { legacyChartNumberText, legacyChartTooltipText, legacyChartValueText } from "./legacyChartFormat";

describe("legacy chart text formatting", () => {
  it("matches ECharts 3 addCommas without losing decimal precision", () => {
    expect(legacyChartNumberText("62500.0000000000")).toBe("62,500.0000000000");
    expect(legacyChartNumberText(-1234.5)).toBe("-1,234.5");
    expect(legacyChartNumberText("invalid")).toBe("-");
  });

  it("uses View formatted text for bars and numeric text for scatter", () => {
    const bar = { formattedValues: ["1.5000000000"], name: "Amount", type: "bar" as const, values: [1.5] };
    const scatter = { formattedValues: ["62500.0000000000"], name: "Price", type: "scatter" as const, values: [62500] };

    expect(legacyChartValueText(bar, 0)).toBe("1.5000000000");
    expect(legacyChartValueText(scatter, 0)).toBe("62500");
    expect(legacyChartTooltipText(scatter, 0)).toBe("62,500.0000000000");
  });
});
