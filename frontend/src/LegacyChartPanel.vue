<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from "vue";
import { legacyChartScale } from "./legacyChartGeometry";
import type { LegacyChartData, LegacyChartSeries } from "./viewWorkflow";

const props = defineProps<{ compact?: boolean; data: LegacyChartData; title?: string }>();
const chartElement = ref<SVGSVGElement | null>(null);
const height = props.compact ? 160 : 300;
const width = ref(720);
const renderedWidth = ref(720);
const hiddenSeriesNames = ref<string[]>([]);
const activeTooltipIndex = ref<number | null>(null);
const tooltipPosition = ref({ left: 0, top: 0 });
const tooltipAlignRight = ref(false);
const tooltipAlignBottom = ref(false);
const plot = { left: 52, top: props.compact && props.title ? 38 : 18, bottom: 46 };
const plotRight = computed(() => width.value * 0.2);
const colors = ["#c23531", "#2f4554", "#61a0a8", "#d48265", "#91c7ae", "#749f83"];
const formatter = new Intl.NumberFormat(undefined, { maximumFractionDigits: 2 });
let resizeObserver: ResizeObserver | undefined;

onMounted(() => {
  if (!chartElement.value) return;
  resizeObserver = new ResizeObserver(([entry]) => {
    const rect = entry?.contentRect;
    if (!rect?.width) return;
    renderedWidth.value = rect.width;
    if (props.compact && rect.height) width.value = Math.max(320, Math.round(rect.width * height / rect.height));
  });
  resizeObserver.observe(chartElement.value);
});

onUnmounted(() => resizeObserver?.disconnect());

const labelCount = computed(() => Math.max(
  1,
  props.data.labels.length,
  ...props.data.series.map((series) => series.values.length)
));
const visibleSeries = computed(() => props.data.series.filter(isSeriesVisible));
const scale = computed(() => legacyChartScale(visibleSeries.value.flatMap((series) => series.values)));
const ticks = computed(() => scale.value.ticks);
const barSeries = computed(() => visibleSeries.value.filter((series) => series.type === "bar"));

function x(index: number) {
  const plotWidth = width.value - plot.left - plotRight.value;
  if (labelCount.value === 1) return plot.left + plotWidth / 2;
  return plot.left + plotWidth * index / (labelCount.value - 1);
}

function y(value: number) {
  const plotHeight = height - plot.top - plot.bottom;
  return plot.top + (scale.value.max - value) / (scale.value.max - scale.value.min) * plotHeight;
}

function seriesPoints(series: LegacyChartSeries) {
  return series.values.map((value, index) => ({ x: x(index), y: y(value) }));
}

function linePath(series: LegacyChartSeries) {
  const points = seriesPoints(series);
  if (!points.length) return "";
  return points.slice(1).reduce((path, point, index) => {
    const previous = points[index];
    const controlX = (previous.x + point.x) / 2;
    return `${path} C ${controlX},${previous.y} ${controlX},${point.y} ${point.x},${point.y}`;
  }, `M ${points[0].x},${points[0].y}`);
}

function lineAreaPath(series: LegacyChartSeries) {
  const points = seriesPoints(series);
  if (!points.length) return "";
  const baseline = y(0);
  return `${linePath(series)} L ${points[points.length - 1].x},${baseline} L ${points[0].x},${baseline} Z`;
}

function barWidth() {
  const maxWidth = props.compact ? 28 : 15 * width.value / renderedWidth.value;
  return Math.min(maxWidth, (width.value - plot.left - plotRight.value) / labelCount.value / Math.max(1, barSeries.value.length) * 0.62);
}

function barX(series: LegacyChartSeries, index: number) {
  const order = barSeries.value.indexOf(series);
  return x(index) - barWidth() * barSeries.value.length / 2 + order * barWidth();
}

function barY(value: number) {
  return Math.min(y(value), y(0));
}

function barHeight(value: number) {
  return Math.max(1, Math.abs(y(value) - y(0)));
}

function valueLabelX(series: LegacyChartSeries, index: number) {
  return series.type === "bar" ? barX(series, index) + barWidth() / 2 : x(index);
}

function valueLabelY(value: number) {
  return Math.max(plot.top + 10, y(value) - 7);
}

function showLabel(index: number) {
  if (props.compact) {
    const maxLabels = Math.max(2, Math.floor((width.value - plot.left - plotRight.value) / 100));
    if (labelCount.value <= maxLabels) return true;
    const lastIndex = labelCount.value - 1;
    return Array.from({ length: maxLabels }, (_, position) =>
      Math.round(lastIndex * position / (maxLabels - 1))
    ).includes(index);
  }
  const step = Math.max(1, Math.ceil(labelCount.value / 8));
  return index % step === 0 || index === labelCount.value - 1;
}

function label(index: number) {
  return props.data.labels[index] || "";
}

function seriesName(series: LegacyChartSeries, index: number) {
  return series.name || `系列 ${index + 1}`;
}

function isSeriesVisible(series: LegacyChartSeries, index: number) {
  return !hiddenSeriesNames.value.includes(seriesName(series, index));
}

function toggleSeries(series: LegacyChartSeries, index: number) {
  const name = seriesName(series, index);
  hiddenSeriesNames.value = hiddenSeriesNames.value.includes(name)
    ? hiddenSeriesNames.value.filter((item) => item !== name)
    : [...hiddenSeriesNames.value, name];
  activeTooltipIndex.value = null;
}

function showAxisTooltip(event: MouseEvent) {
  const chart = chartElement.value;
  if (!chart || !visibleSeries.value.length) {
    activeTooltipIndex.value = null;
    return;
  }
  const rect = chart.getBoundingClientRect();
  const paneRect = chart.parentElement?.getBoundingClientRect();
  if (!rect.width || !rect.height || !paneRect) return;
  const viewX = (event.clientX - rect.left) * width.value / rect.width;
  const plotWidth = width.value - plot.left - plotRight.value;
  activeTooltipIndex.value = labelCount.value === 1
    ? 0
    : Math.round((viewX - plot.left) / plotWidth * (labelCount.value - 1));
  tooltipPosition.value = {
    left: event.clientX - paneRect.left,
    top: event.clientY - paneRect.top
  };
  tooltipAlignRight.value = event.clientX > rect.left + rect.width / 2;
  tooltipAlignBottom.value = event.clientY > rect.top + rect.height / 2;
}

function hideAxisTooltip() {
  activeTooltipIndex.value = null;
}

function hasTooltipValue(series: LegacyChartSeries) {
  return activeTooltipIndex.value !== null && series.values[activeTooltipIndex.value] !== undefined;
}

function tooltipValue(series: LegacyChartSeries) {
  return formatter.format(series.values[activeTooltipIndex.value ?? 0]);
}
</script>

<template>
  <div class="legacy-chart-pane" :class="{ 'compact-chart': compact }">
    <svg ref="chartElement" class="legacy-chart" :viewBox="`0 0 ${width} ${height}`" role="img" aria-label="视图数据图表">
      <text v-if="compact && title" class="chart-title" x="8" y="22">{{ title }}</text>
      <g v-for="tick in ticks" :key="tick" class="chart-grid-line">
        <line :x1="plot.left" :x2="width - plotRight" :y1="y(tick)" :y2="y(tick)" />
        <text :x="plot.left - 8" :y="y(tick) + 4" text-anchor="end">{{ formatter.format(tick) }}</text>
      </g>
      <line class="chart-axis" :x1="plot.left" :x2="plot.left" :y1="plot.top" :y2="height - plot.bottom" />
      <line class="chart-axis" :x1="plot.left" :x2="width - plotRight" :y1="y(0)" :y2="y(0)" />
      <text v-if="data.axisName" class="chart-axis-name" :x="width - plotRight" :y="height - 2" text-anchor="end">{{ data.axisName }}</text>
      <g v-for="(_, index) in labelCount" :key="index">
        <text v-if="index < data.labels.length && showLabel(index)" class="chart-axis-label" :x="x(index)" :y="height - 18" text-anchor="middle">
          {{ label(index) }}
        </text>
      </g>
      <g
        v-for="(series, seriesIndex) in data.series"
        :key="`${seriesName(series, seriesIndex)}-${series.type}`"
        :style="{ display: isSeriesVisible(series, seriesIndex) ? undefined : 'none' }"
      >
        <path
          v-if="series.type === 'line'"
          class="chart-line-area"
          :d="lineAreaPath(series)"
          :fill="colors[seriesIndex % colors.length]"
        />
        <path
          v-if="series.type === 'line'"
          class="chart-line"
          :d="linePath(series)"
          :stroke="colors[seriesIndex % colors.length]"
        />
        <template v-for="(value, index) in series.values" :key="`${seriesIndex}-${index}`">
          <rect
            v-if="series.type === 'bar'"
            class="chart-bar"
            :x="barX(series, index)"
            :y="barY(value)"
            :width="barWidth()"
            :height="barHeight(value)"
            :fill="colors[seriesIndex % colors.length]"
            rx="2"
          />
          <circle
            v-else-if="series.type === 'scatter'"
            :cx="x(index)"
            :cy="y(value)"
            r="6"
            :fill="colors[seriesIndex % colors.length]"
          />
          <text class="chart-value-label" :x="valueLabelX(series, index)" :y="valueLabelY(value)">{{ formatter.format(value) }}</text>
        </template>
      </g>
      <line
        v-if="activeTooltipIndex !== null"
        class="chart-axis-pointer"
        :x1="x(activeTooltipIndex)"
        :x2="x(activeTooltipIndex)"
        :y1="plot.top"
        :y2="height - plot.bottom"
      />
      <rect
        class="chart-axis-hit"
        :x="plot.left"
        :y="plot.top"
        :width="width - plot.left - plotRight"
        :height="height - plot.top - plot.bottom"
        @mousemove="showAxisTooltip"
        @mouseleave="hideAxisTooltip"
      />
    </svg>
    <div
      v-if="activeTooltipIndex !== null"
      class="chart-axis-tooltip"
      :class="{ 'align-right': tooltipAlignRight, 'align-bottom': tooltipAlignBottom }"
      :style="{ left: `${tooltipPosition.left}px`, top: `${tooltipPosition.top}px` }"
      role="tooltip"
    >
      <strong v-if="label(activeTooltipIndex)">{{ label(activeTooltipIndex) }}</strong>
      <template v-for="(series, index) in data.series" :key="`${seriesName(series, index)}-${index}`">
        <span v-if="isSeriesVisible(series, index) && hasTooltipValue(series)">
          <i :style="{ backgroundColor: colors[index % colors.length] }"></i>
          {{ seriesName(series, index) }} : {{ tooltipValue(series) }}
        </span>
      </template>
    </div>
    <ul class="chart-legend">
      <li v-for="(series, index) in data.series" :key="`${seriesName(series, index)}-${index}`">
        <button
          type="button"
          :aria-pressed="isSeriesVisible(series, index)"
          :class="{ 'series-hidden': !isSeriesVisible(series, index) }"
          @click="toggleSeries(series, index)"
        >
          <span :style="{ backgroundColor: isSeriesVisible(series, index) ? colors[index % colors.length] : '#cccccc' }"></span>
          <strong>{{ seriesName(series, index) }}</strong>
        </button>
      </li>
    </ul>
  </div>
</template>
