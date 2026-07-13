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

export function legacyChartStackGeometry(series: LegacyChartSeries[], stacked: boolean) {
  if (!stacked) {
    const values = series.map((item) => [...item.values]);
    return { values, bases: values.map((items) => items.map(() => 0)), domainValues: values.flat() };
  }

  const values: number[][] = [];
  const bases: number[][] = [];
  const positiveBarTops: number[] = [];
  const negativeBarTops: number[] = [];

  for (const [seriesIndex, item] of series.entries()) {
    const stackedValues = item.values.map((rawValue, dataIndex) => {
      let value = rawValue;
      for (let previousIndex = seriesIndex - 1; previousIndex >= 0; previousIndex -= 1) {
        const previousValue = series[previousIndex].values[dataIndex];
        if ((value >= 0 && previousValue > 0) || (value <= 0 && previousValue < 0)) value += previousValue;
      }
      return value;
    });
    values.push(stackedValues);

    if (item.type === "line") {
      const previous = series[seriesIndex - 1];
      bases.push(item.values.map((value, index) => previous && sign(previous.values[index]) === sign(value)
        ? values[seriesIndex - 1][index] ?? 0
        : 0));
    } else if (item.type === "bar") {
      bases.push(item.values.map((value, index) => {
        const barTops = value >= 0 ? positiveBarTops : negativeBarTops;
        const base = barTops[index] ?? 0;
        barTops[index] = stackedValues[index];
        return base;
      }));
    } else {
      bases.push(item.values.map(() => 0));
    }
  }

  return { values, bases, domainValues: values.flat() };
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
