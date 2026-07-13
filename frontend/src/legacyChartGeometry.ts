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
