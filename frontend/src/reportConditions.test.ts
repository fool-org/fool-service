import { describe, expect, it } from "vitest";
import type { ReportFilterExp } from "./api";
import {
  buildReportConditionFilter,
  canGroupReportConditions,
  groupReportConditions,
  type ReportConditionDraft,
  ungroupReportConditions
} from "./reportConditions";

const conditions: ReportConditionDraft[] = [
  { id: 1, columnId: "a", compareId: "1", groupPath: [], join: "and", value: "A" },
  { id: 2, columnId: "b", compareId: "1", groupPath: [], join: "and", value: "B" },
  { id: 3, columnId: "c", compareId: "1", groupPath: [], join: "or", value: "C" }
];

function expression(condition: ReportConditionDraft): ReportFilterExp {
  return { valueExp: condition.value };
}

describe("report condition groups", () => {
  it("groups consecutive siblings into a nested filter", () => {
    const grouped = groupReportConditions(conditions, [2, 3]);
    expect(grouped.map((condition) => condition.groupPath)).toEqual([[], [1], [1]]);
    expect(buildReportConditionFilter(grouped, expression)).toEqual({
      firstExp: { valueExp: "A" },
      sequences: [{
        boolOp: { dbName: "and", showName: "AND" },
        addedExp: {
          firstExp: { valueExp: "B" },
          sequences: [{ boolOp: { dbName: "or", showName: "OR" }, addedExp: { valueExp: "C" } }]
        }
      }]
    });
  });

  it("wraps a complete existing group with an adjacent condition", () => {
    const inner = groupReportConditions(conditions, [1, 2]);
    const outer = groupReportConditions(inner, [1, 2, 3]);
    expect(outer.map((condition) => condition.groupPath)).toEqual([[2, 1], [2, 1], [2]]);
    expect(buildReportConditionFilter(outer, expression)?.firstExp?.firstExp?.valueExp).toBe("A");
  });

  it("rejects gaps and partial existing groups", () => {
    expect(canGroupReportConditions(conditions, [1, 3])).toBe(false);
    const grouped = groupReportConditions(conditions, [1, 2]);
    expect(canGroupReportConditions(grouped, [1, 3])).toBe(false);
  });

  it("removes one nested group level", () => {
    const grouped = groupReportConditions(conditions, [1, 2]);
    expect(ungroupReportConditions(grouped, [1])).toEqual(conditions);
  });
});
