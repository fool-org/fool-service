import type { ReportFilterExp } from "./api";

export interface ReportConditionDraft {
  id: number;
  columnId: string;
  compareId: string;
  groupPath: number[];
  join: "and" | "or";
  value: string;
}

interface FilterNode {
  join: "and" | "or";
  expression?: ReportFilterExp;
  children?: FilterNode[];
  groupId?: number;
}

export function canGroupReportConditions(conditions: ReportConditionDraft[], selectedIds: number[]) {
  return selectedGroupContext(conditions, selectedIds) !== null;
}

export function reportConditionGroupError(conditions: ReportConditionDraft[], selectedIds: number[]) {
  const selectedCount = conditions.filter((condition) => selectedIds.includes(condition.id)).length;
  if (!selectedCount) return "请选择要合并的条件";
  if (selectedCount === 1) return "不能合并单个";
  return selectedGroupContext(conditions, selectedIds) ? "" : "不连续不能合并";
}

export function groupReportConditions(conditions: ReportConditionDraft[], selectedIds: number[]) {
  const context = selectedGroupContext(conditions, selectedIds);
  if (!context) return conditions;
  const groupId = Math.max(0, ...conditions.flatMap((condition) => condition.groupPath)) + 1;
  const selected = new Set(selectedIds);
  return conditions.map((condition) => {
    if (!selected.has(condition.id)) return condition;
    return {
      ...condition,
      groupPath: [
        ...condition.groupPath.slice(0, context.parentPath.length),
        groupId,
        ...condition.groupPath.slice(context.parentPath.length)
      ]
    };
  });
}

export function ungroupReportConditions(conditions: ReportConditionDraft[], groupPath: number[]) {
  if (!groupPath.length) return conditions;
  const removeIndex = groupPath.length - 1;
  return conditions.map((condition) => {
    if (!startsWithPath(condition.groupPath, groupPath)) return condition;
    return {
      ...condition,
      groupPath: condition.groupPath.filter((_, index) => index !== removeIndex)
    };
  });
}

export function buildReportConditionFilter(
  conditions: ReportConditionDraft[],
  expressionFor: (condition: ReportConditionDraft) => ReportFilterExp | null
) {
  const root: FilterNode = { join: "and", children: [] };
  for (const condition of conditions) {
    const expression = expressionFor(condition);
    if (!expression) continue;
    let parent = root;
    for (const groupId of condition.groupPath) {
      let group = parent.children?.find((node) => node.groupId === groupId);
      if (!group) {
        group = { groupId, join: condition.join, children: [] };
        parent.children?.push(group);
      }
      parent = group;
    }
    parent.children?.push({ join: condition.join, expression });
  }
  return serializeNode(root);
}

function selectedGroupContext(conditions: ReportConditionDraft[], selectedIds: number[]) {
  const selected = new Set(selectedIds);
  const indexes = conditions.flatMap((condition, index) => selected.has(condition.id) ? [index] : []);
  if (indexes.length < 2 || indexes[indexes.length - 1] - indexes[0] + 1 !== indexes.length) return null;
  const selectedConditions = indexes.map((index) => conditions[index]);
  const parentPath = commonPath(selectedConditions.map((condition) => condition.groupPath));
  for (const condition of selectedConditions) {
    const childGroup = condition.groupPath.slice(0, parentPath.length + 1);
    if (childGroup.length === parentPath.length) continue;
    if (conditions.some((candidate) => startsWithPath(candidate.groupPath, childGroup) && !selected.has(candidate.id))) {
      return null;
    }
  }
  return { parentPath };
}

function commonPath(paths: number[][]) {
  const first = paths[0] || [];
  let length = 0;
  while (length < first.length && paths.every((path) => path[length] === first[length])) length++;
  return first.slice(0, length);
}

function startsWithPath(path: number[], prefix: number[]) {
  return prefix.every((value, index) => path[index] === value);
}

function serializeNode(node: FilterNode): ReportFilterExp | undefined {
  if (node.expression) return node.expression;
  const children = (node.children || [])
    .map((child) => ({ child, expression: serializeNode(child) }))
    .filter((entry): entry is { child: FilterNode; expression: ReportFilterExp } => Boolean(entry.expression));
  if (!children.length) return undefined;
  if (children.length === 1) return children[0].expression;
  return {
    firstExp: children[0].expression,
    sequences: children.slice(1).map(({ child, expression }) => ({
      boolOp: { dbName: child.join, showName: child.join.toUpperCase() },
      addedExp: expression
    }))
  };
}
