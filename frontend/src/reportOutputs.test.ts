import { describe, expect, it } from "vitest";
import { addReportOutput, moveReportOutput, removeReportOutput, setReportOutputOrder } from "./reportOutputs";

const amount = {
  id: "amount",
  name: "金额",
  queryTypes: [
    { id: "raw", name: "原值" },
    { id: "sum", name: "合计" }
  ]
};

describe("report outputs", () => {
  it("allows one metadata column with different output types", () => {
    const raw = addReportOutput([], amount, "raw");
    const both = addReportOutput(raw, amount, "sum");

    expect(both).toEqual([
      { colName: "金额[原值]", colId: "amount", selectedTypeId: "raw", orderType: "2", index: 0 },
      { colName: "金额[合计]", colId: "amount", selectedTypeId: "sum", orderType: "2", index: 1 }
    ]);
    expect(addReportOutput(both, amount, "sum")).toBe(both);
  });

  it("uses the retained output-method text with the rebuilt candidate list", () => {
    expect(addReportOutput([], amount, "count", "计数")).toEqual([
      { colName: "金额[计数]", colId: "amount", selectedTypeId: "count", orderType: "2", index: 0 }
    ]);
  });

  it("moves and reindexes selected outputs", () => {
    const outputs = addReportOutput(addReportOutput([], amount, "raw"), amount, "sum");
    expect(moveReportOutput(outputs, 1, -1).map((output) => [output.selectedTypeId, output.index])).toEqual([
      ["sum", 0],
      ["raw", 1]
    ]);
    expect(moveReportOutput(outputs, 0, -1)).toBe(outputs);
  });

  it("updates sorting and removes selected outputs", () => {
    const outputs = addReportOutput(addReportOutput([], amount, "raw"), amount, "sum");
    const sorted = setReportOutputOrder(outputs, 0, "1");

    expect(sorted[0].orderType).toBe("1");
    expect(removeReportOutput(sorted, 0)).toEqual([
      { colName: "金额[合计]", colId: "amount", selectedTypeId: "sum", orderType: "2", index: 0 }
    ]);
  });
});
