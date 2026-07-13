import { describe, expect, it } from "vitest";
import { legacyChartDomain, legacyChartScale, legacyChartStackGeometry } from "./legacyChartGeometry";

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

  it("stacks realtime series with ECharts mixed-type coordinates", () => {
    const geometry = legacyChartStackGeometry([
      { name: "Line", type: "line", values: [2, -2, 0] },
      { name: "Bar A", type: "bar", values: [3, -3, 4] },
      { name: "Scatter", type: "scatter", values: [5, -5, -1] },
      { name: "Bar B", type: "bar", values: [1, -1, 2] }
    ], true);

    expect(geometry.values).toEqual([
      [2, -2, 0],
      [5, -5, 4],
      [10, -10, -1],
      [11, -11, 6]
    ]);
    expect(geometry.bases).toEqual([
      [0, 0, 0],
      [0, 0, 0],
      [0, 0, 0],
      [5, -5, 4]
    ]);
    expect(geometry.domainValues).toContain(11);
    expect(geometry.domainValues).toContain(-11);
  });

  it("uses the immediately stacked series as a same-sign line area base", () => {
    expect(legacyChartStackGeometry([
      { name: "First", type: "line", values: [2, -2, 2] },
      { name: "Second", type: "line", values: [3, -3, -1] }
    ], true)).toMatchObject({
      values: [[2, -2, 2], [5, -5, -1]],
      bases: [[0, 0, 0], [2, -2, 0]]
    });
  });

  it("keeps top-level series unstacked", () => {
    expect(legacyChartStackGeometry([
      { name: "Line", type: "line", values: [2] },
      { name: "Bar", type: "bar", values: [3] }
    ], false)).toEqual({
      values: [[2], [3]],
      bases: [[0], [0]],
      domainValues: [2, 3]
    });
  });
});
