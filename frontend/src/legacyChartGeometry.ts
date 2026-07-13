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
