import type { LegacyChartSeries } from "./viewWorkflow";

export function legacyChartDomain(values: number[]) {
  const finiteValues = values.filter(Number.isFinite);
  if (!finiteValues.length) return { min: 0, max: 1 };

  let min = Math.min(...finiteValues);
  let max = Math.max(...finiteValues);
  if (min === 0 && max === 0) return { min: 0, max: 1 };

  max += (max - min) * 0.5;
  if (min > 0 && max > 0) min = 0;
  if (min < 0 && max < 0) max = 0;
  return { min, max };
}

export function legacyChartScale(values: number[]) {
  const domain = legacyChartDomain(values);
  const interval = niceNumber((domain.max - domain.min) / 5);
  const min = round(Math.floor(domain.min / interval) * interval);
  const max = round(Math.ceil(domain.max / interval) * interval);
  const ticks: number[] = [];
  for (let tick = max; tick >= min; tick = round(tick - interval)) ticks.push(tick);
  return { min, max, ticks };
}

export function legacyChartStackGeometry(series: LegacyChartSeries[]) {
  const values: number[][] = [];
  const bases: number[][] = [];
  const stackedOn: number[] = [];
  const barGroups: number[] = [];
  const barGroupKeys: string[] = [];
  const barTops = new Map<string, { negative: number[]; positive: number[] }>();

  for (const [seriesIndex, item] of series.entries()) {
    barGroups[seriesIndex] = -1;
    const stack = item.stack || "";
    let previousStackIndex = -1;
    for (let index = seriesIndex - 1; stack && index >= 0; index -= 1) {
      if (series[index].stack === stack) {
        previousStackIndex = index;
        break;
      }
    }
    stackedOn.push(previousStackIndex);

    const stackedValues = item.values.map((rawValue, dataIndex) => {
      let value = rawValue;
      if (!stack) return value;
      for (let previousIndex = seriesIndex - 1; previousIndex >= 0; previousIndex -= 1) {
        if (series[previousIndex].stack !== stack) continue;
        const previousValue = series[previousIndex].values[dataIndex];
        if ((value >= 0 && previousValue > 0) || (value <= 0 && previousValue < 0)) value += previousValue;
      }
      return value;
    });
    values.push(stackedValues);

    if (item.type === "line") {
      const previous = series[previousStackIndex];
      bases.push(item.values.map((value, index) => previous && sign(previous.values[index]) === sign(value)
        ? values[previousStackIndex][index] ?? 0
        : 0));
    } else if (item.type === "bar") {
      const groupKey = stack || `__series_${seriesIndex}`;
      let groupIndex = barGroupKeys.indexOf(groupKey);
      if (groupIndex < 0) groupIndex = barGroupKeys.push(groupKey) - 1;
      barGroups[seriesIndex] = groupIndex;
      const tops = barTops.get(groupKey) ?? { negative: [], positive: [] };
      barTops.set(groupKey, tops);
      bases.push(item.values.map((value, index) => {
        const stackTops = value >= 0 ? tops.positive : tops.negative;
        const base = stackTops[index] ?? 0;
        stackTops[index] = stackedValues[index];
        return base;
      }));
    } else {
      bases.push(item.values.map(() => 0));
    }
  }

  return { values, bases, stackedOn, barGroups, domainValues: values.flat() };
}

function niceNumber(value: number) {
  const exponent = Math.floor(Math.log(value) / Math.LN10);
  const power = Math.pow(10, exponent);
  const fraction = value / power;
  const niceFraction = fraction < 1.5 ? 1 : fraction < 2.5 ? 2 : fraction < 4 ? 3 : fraction < 7 ? 5 : 10;
  return niceFraction * power;
}

function round(value: number) {
  return Number(value.toFixed(10));
}

function sign(value: number) {
  return value >= 0 ? 1 : -1;
}
