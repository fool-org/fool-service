import { describe, expect, it } from "vitest";
import { legacyChartDomain } from "./legacyChartGeometry";

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
});
