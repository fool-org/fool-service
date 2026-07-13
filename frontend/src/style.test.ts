import { describe, expect, it } from "vitest";
import styleSource from "./style.css?inline";

describe("shared chart styles", () => {
  it("matches the legacy chart surface and ECharts axis defaults", () => {
    expect(styleSource).toContain(`.legacy-chart-pane {
  position: relative;
  display: block;
  min-height: 280px;
  min-width: 0;
}`);
    expect(styleSource).toContain(`.chart-grid-line line {
  stroke: #cccccc;
  stroke-width: 1;
}`);
    expect(styleSource).toContain(`.chart-axis {
  stroke: #333333;
  stroke-width: 1;
}`);
    expect(styleSource).toContain(`.chart-axis-label,
.chart-axis-name {
  fill: #333333;
  font-size: 12px;
}`);
  });
});
