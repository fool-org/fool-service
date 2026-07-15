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
    expect(chartSource).toContain('@mouseleave="scheduleAxisTooltipHide"');
    expect(chartSource).toContain("viewX < plot.left || viewX > width.value - plotRight.value");
    expect(chartSource).toContain("viewY < plot.top || viewY > height - plot.bottom");
    expect(chartSource.indexOf('class="chart-axis-hit"')).toBeLessThan(chartSource.indexOf('class="chart-series"'));
    expect(styleSource).toContain(".chart-bar:hover");
    expect(styleSource).toContain(".chart-scatter:hover");
  });

  it("replays the last chart-local pointer position after resize", () => {
    expect(chartSource).toContain("const lastTooltipPosition = ref<{ left: number; top: number } | null>(null)");
    expect(chartSource.match(/refreshAxisTooltip\(\);/g)).toHaveLength(2);
    expect(chartSource).toContain("showAxisTooltip({ clientX: rect.left + position.left, clientY: rect.top + position.top })");
    expect(chartSource).toContain("lastTooltipPosition.value = { left: event.clientX - rect.left, top: event.clientY - rect.top }");
    expect(chartSource).toContain("lastTooltipPosition.value = null");
  });

  it("keeps the ECharts tooltip hide delay cancellable", () => {
    expect(chartSource).toContain("let hideTooltipTimer: ReturnType<typeof setTimeout> | undefined");
    expect(chartSource).toContain("if (hideTooltipTimer !== undefined) return");
    expect(chartSource).toContain("}, 100)");
    expect(chartSource).toContain('@mouseleave="scheduleAxisTooltipHide"');
    expect(chartSource).toContain("clearTimeout(hideTooltipTimer)");
  });

  it("matches the ECharts tooltip box and movement defaults", () => {
    const tooltipStyle = styleSource.match(/\.chart-axis-tooltip \{([\s\S]*?)\}/)?.[1] ?? "";
    expect(tooltipStyle).toContain("z-index: 9999999");
    expect(tooltipStyle).toContain("display: block");
    expect(tooltipStyle).toContain("line-height: 21px");
    expect(tooltipStyle).toContain("white-space: nowrap");
    expect(tooltipStyle).toContain("transition: left 0.4s cubic-bezier(0.23, 1, 0.32, 1), top 0.4s cubic-bezier(0.23, 1, 0.32, 1)");
    expect(tooltipStyle).not.toContain("gap:");
    expect(styleSource).toContain("translate(20px, 20px)");
    expect(styleSource).toContain("translate(calc(-100% - 20px), calc(-100% - 20px))");
  });
});

describe("shared report styles", () => {
  it("wraps the complete result command group without splitting its buttons", () => {
    expect(styleSource).toContain(`.report-result-heading {
  flex-wrap: wrap;
  gap: 8px;
}`);
  });
});

describe("legacy detail field layout", () => {
  it("pairs labels and controls on desktop and stacks them on mobile", () => {
    expect(styleSource).toContain(`.detail-field-grid > div,
.detail-field-edit > label {
  display: grid;
  grid-template-columns: minmax(110px, 0.5fr) minmax(0, 1fr);`);
    expect(styleSource).toContain(`.detail-field-grid > div,
  .detail-field-edit > label {
    grid-template-columns: 1fr;
    gap: 7px;`);
  });

  it("keeps read values at the old paragraph weight", () => {
    expect(styleSource).toContain(`.detail-field-value {
  overflow-wrap: anywhere;
  font-weight: 400;`);
  });
});
