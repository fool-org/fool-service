import { describe, expect, it } from "vitest";
import { legacyChartDomain, legacyChartScale } from "./legacyChartGeometry";

describe("legacy chart geometry", () => {
  it("applies the ECharts value-axis upper boundary gap before crossing zero", () => {
    expect(legacyChartDomain([0, 62500])).toEqual({ min: 0, max: 93750 });
    expect(legacyChartDomain([10, 12])).toEqual({ min: 0, max: 13 });
    expect(legacyChartDomain([-12, -10])).toEqual({ min: -12, max: 0 });
    expect(legacyChartDomain([-10, 10])).toEqual({ min: -10, max: 20 });
  });

  it("retains the legacy empty-axis fallback", () => {
    expect(legacyChartDomain([])).toEqual({ min: 0, max: 1 });
    expect(legacyChartDomain([0, Number.NaN])).toEqual({ min: 0, max: 1 });
  });

  it("nicifies the extent and ticks with the ECharts 3.1.7 interval scale", () => {
    expect(legacyChartScale([0, 62500])).toEqual({
      min: 0,
      max: 100000,
      ticks: [100000, 80000, 60000, 40000, 20000, 0]
    });
    expect(legacyChartScale([0, 0.25])).toEqual({
      min: 0,
      max: 0.4,
      ticks: [0.4, 0.3, 0.2, 0.1, 0]
    });
    expect(legacyChartScale([])).toEqual({
      min: 0,
      max: 1,
      ticks: [1, 0.8, 0.6, 0.4, 0.2, 0]
    });
  });
});
