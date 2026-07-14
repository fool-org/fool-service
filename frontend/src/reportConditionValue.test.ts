import { describe, expect, it } from "vitest";
import { fieldInputType } from "./fieldInput";
import {
  reportConditionEditorField,
  reportConditionFormattedValue,
  reportConditionInitialValue
} from "./reportConditionValue";

describe("report condition value metadata", () => {
  it("adapts report View metadata for the shared field editor", () => {
    const field = reportConditionEditorField({
      ID: "customer_id",
      Name: "Customer",
      PrpType: 16,
      ModelId: 200
    });

    expect(field).toMatchObject({
      PrpId: "Customer",
      PrpShowName: "Customer",
      PrpType: "16",
      PrpModelId: 200
    });
    expect(fieldInputType(reportConditionEditorField({ PrpType: 12 }))).toBe("date");
    expect(fieldInputType(reportConditionEditorField({ PrpType: 13 }))).toBe("time");
    expect(fieldInputType(reportConditionEditorField({ PrpType: 14 }))).toBe("datetime-local");
  });

  it("uses the first enum state and its display text", () => {
    const column = {
      PrpType: 15,
      ModelId: 102,
      States: [
        { DBName: "0", ShowName: "Open" },
        { DBName: "1", ShowName: "Closed" }
      ]
    };

    expect(reportConditionInitialValue(column)).toBe("0");
    expect(reportConditionFormattedValue(column, "1")).toBe("Closed");
  });

  it("matches the old unchecked Boolean value and Chinese display text", () => {
    const column = { PrpType: 8 };

    expect(reportConditionInitialValue(column)).toBe("false");
    expect(reportConditionFormattedValue(column, "false")).toBe("否");
    expect(reportConditionFormattedValue(column, "true")).toBe("是");
  });

  it("retains a selected business-object label separately from its id", () => {
    const column = { Name: "Customer", PrpType: 16, ModelId: 200 };

    expect(reportConditionInitialValue(column)).toBe("");
    expect(reportConditionFormattedValue(column, "3001", "Alice")).toBe("Alice");
    expect(reportConditionFormattedValue(column, "3001")).toBe("3001");
  });
});
