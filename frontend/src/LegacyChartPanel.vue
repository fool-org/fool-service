<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from "vue";
import type { LegacyChartData, LegacyChartSeries } from "./viewWorkflow";

const props = defineProps<{ compact?: boolean; data: LegacyChartData }>();
const chartElement = ref<SVGSVGElement | null>(null);
const height = props.compact ? 160 : 300;
const width = ref(720);
const plot = { left: 52, right: 18, top: 18, bottom: 46 };
const colors = ["#c23531", "#2f4554", "#61a0a8", "#d48265", "#91c7ae", "#749f83"];
const formatter = new Intl.NumberFormat(undefined, { maximumFractionDigits: 2 });
let resizeObserver: ResizeObserver | undefined;

onMounted(() => {
  if (!props.compact || !chartElement.value) return;
  resizeObserver = new ResizeObserver(([entry]) => {
    const rect = entry?.contentRect;
    if (rect?.width && rect.height) width.value = Math.max(320, Math.round(rect.width * height / rect.height));
  });
  resizeObserver.observe(chartElement.value);
});

onUnmounted(() => resizeObserver?.disconnect());

const labelCount = computed(() => Math.max(
  1,
  props.data.labels.length,
  ...props.data.series.map((series) => series.values.length)
));
const domain = computed(() => {
  const values = props.data.series.flatMap((series) => series.values);
  const min = Math.min(0, ...values);
  const max = Math.max(0, ...values);
  return { min, max: max === min ? min + 1 : max };
});
const ticks = computed(() => Array.from({ length: 5 }, (_, index) =>
  domain.value.max - (domain.value.max - domain.value.min) * index / 4
));
const barSeries = computed(() => props.data.series.filter((series) => series.type === "bar"));

function x(index: number) {
  const plotWidth = width.value - plot.left - plot.right;
  return plot.left + plotWidth * (index + 0.5) / labelCount.value;
}

function y(value: number) {
  const plotHeight = height - plot.top - plot.bottom;
  return plot.top + (domain.value.max - value) / (domain.value.max - domain.value.min) * plotHeight;
}

function linePoints(series: LegacyChartSeries) {
  return series.values.map((value, index) => `${x(index)},${y(value)}`).join(" ");
}

function barWidth() {
  return Math.min(28, (width.value - plot.left - plot.right) / labelCount.value / Math.max(1, barSeries.value.length) * 0.62);
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

function showLabel(index: number) {
  if (props.compact) {
    const maxLabels = Math.max(2, Math.floor((width.value - plot.left - plot.right) / 100));
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
</script>

<template>
  <div class="legacy-chart-pane" :class="{ 'compact-chart': compact }">
    <svg ref="chartElement" class="legacy-chart" :viewBox="`0 0 ${width} ${height}`" role="img" aria-label="视图数据图表">
      <g v-for="tick in ticks" :key="tick" class="chart-grid-line">
        <line :x1="plot.left" :x2="width - plot.right" :y1="y(tick)" :y2="y(tick)" />
        <text :x="plot.left - 8" :y="y(tick) + 4" text-anchor="end">{{ formatter.format(tick) }}</text>
      </g>
      <line class="chart-axis" :x1="plot.left" :x2="plot.left" :y1="plot.top" :y2="height - plot.bottom" />
      <line class="chart-axis" :x1="plot.left" :x2="width - plot.right" :y1="y(0)" :y2="y(0)" />
      <g v-for="(_, index) in labelCount" :key="index">
        <text v-if="index < data.labels.length && showLabel(index)" class="chart-axis-label" :x="x(index)" :y="height - 18" text-anchor="middle">
          {{ label(index) }}
        </text>
      </g>
      <g v-for="(series, seriesIndex) in data.series" :key="`${seriesName(series, seriesIndex)}-${series.type}`">
        <polyline
          v-if="series.type === 'line'"
          fill="none"
          :points="linePoints(series)"
          :stroke="colors[seriesIndex % colors.length]"
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="3"
        />
        <template v-for="(value, index) in series.values" :key="`${seriesIndex}-${index}`">
          <rect
            v-if="series.type === 'bar'"
            :x="barX(series, index)"
            :y="barY(value)"
            :width="barWidth()"
            :height="barHeight(value)"
            :fill="colors[seriesIndex % colors.length]"
            rx="2"
          ><title>{{ seriesName(series, seriesIndex) }} · {{ label(index) }}: {{ formatter.format(value) }}</title></rect>
          <circle
            v-else
            :cx="x(index)"
            :cy="y(value)"
            :r="series.type === 'scatter' ? 6 : 4"
            :fill="colors[seriesIndex % colors.length]"
          ><title>{{ seriesName(series, seriesIndex) }} · {{ label(index) }}: {{ formatter.format(value) }}</title></circle>
        </template>
      </g>
    </svg>
    <ul class="chart-legend">
      <li v-for="(series, index) in data.series" :key="`${seriesName(series, index)}-${index}`">
        <span :style="{ backgroundColor: colors[index % colors.length] }"></span>
        <strong>{{ seriesName(series, index) }}</strong>
      </li>
    </ul>
  </div>
</template>
