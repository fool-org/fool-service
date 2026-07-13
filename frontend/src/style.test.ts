import { describe, expect, it } from "vitest";
import chartSource from "./LegacyChartPanel.vue?raw";
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

  it("matches ECharts legend item geometry", () => {
    expect(chartSource).toContain('class="chart-legend-symbol"');
    expect(chartSource).toContain(':class="`series-${series.type}`"');
    expect(chartSource).toContain('ref="legendElement" class="chart-legend"');
    expect(chartSource).toContain("Math.max(0.2, (renderedLegendWidth.value + 5) / renderedWidth.value)");
    expect(chartSource).toContain("resizeObserver.observe(legendElement.value)");
    expect(chartSource).not.toContain("<strong>{{ seriesName(series, index) }}</strong>");
    expect(styleSource).toContain(`.chart-legend li {
  font-size: 12px;
}`);
    expect(styleSource).toContain(`grid-template-columns: 25px auto;
  gap: 5px;`);
    expect(styleSource).toContain(`.chart-legend-symbol {
  position: relative;
  display: block;
  width: 25px;
  height: 14px;
  border-radius: 3.5px;
  background: currentColor;
}`);
    expect(styleSource).toContain(".chart-legend-symbol.series-line::after");
    expect(styleSource).toContain(".chart-legend-symbol.series-scatter");
  });

  it("links legend hover to series emphasis", () => {
    expect(chartSource).toContain("const hoveredSeriesName = ref<string | null>(null)");
    expect(chartSource).toContain(":class=\"{ 'series-highlighted': hoveredSeriesName === seriesName(series, seriesIndex) }\"");
    expect(chartSource).toContain('@mouseenter="hoveredSeriesName = seriesName(series, index)"');
    expect(chartSource).toContain('@mouseleave="hoveredSeriesName = null"');
    expect(chartSource).toContain('class="chart-scatter"');
    expect(styleSource).toContain(`.chart-bar:hover,
.chart-scatter:hover,
.chart-series.series-highlighted .chart-bar,
.chart-series.series-highlighted .chart-scatter {
  filter: brightness(1.1);
}`);
    expect(styleSource).toContain(`.chart-scatter:hover,
.chart-series.series-highlighted .chart-scatter {
  transform: scale(1.3);
}`);
    expect(styleSource).toContain(`.chart-legend button:hover {
  filter: brightness(1.1);
}`);
  });

  it("matches ECharts scatter symbol defaults", () => {
    expect(chartSource).toContain("const scatterRadius = computed(() => 5 * width.value / renderedWidth.value)");
    expect(chartSource).toContain(':r="scatterRadius"');
    expect(chartSource).toContain('opacity="0.8"');
    expect(chartSource).not.toContain('r="6"');
  });

  it("matches the ECharts line-area opacity", () => {
    expect(styleSource).toContain(`.chart-line-area {
  opacity: 0.7;
}`);
  });

  it("omits labels for symbol-less line series", () => {
    expect(chartSource).toContain(`v-if="series.type !== 'line'"`);
  });

  it("centers bar and scatter labels inside their symbols", () => {
    expect(chartSource).toContain("barY(series, index) + barHeight(series, index) / 2");
    expect(chartSource).toContain("dominant-baseline=\"central\"");
    expect(chartSource).toContain(":opacity=\"series.type === 'scatter' ? 0.8 : undefined\"");
    expect(styleSource).toContain(`.chart-value-label {
  fill: #ffffff;
  font-size: 12px;`);
  });

  it("keeps chart items above the axis tooltip hit surface", () => {
    expect(chartSource).toContain('@mousemove="showAxisTooltip"');
    expect(chartSource).toContain('@mouseleave="hideAxisTooltip"');
    expect(chartSource).toContain("viewX < plot.left || viewX > width.value - plotRight.value");
    expect(chartSource).toContain("viewY < plot.top || viewY > height - plot.bottom");
    expect(chartSource.indexOf('class="chart-axis-hit"')).toBeLessThan(chartSource.indexOf('class="chart-series"'));
    expect(styleSource).toContain(".chart-bar:hover");
    expect(styleSource).toContain(".chart-scatter:hover");
  });
});
