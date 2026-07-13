import { describe, expect, it } from "vitest";
import {
  fieldInputChecked,
  fieldInputMaxLength,
  fieldInputType,
  fieldInputValue,
  sanitizeFieldInput
} from "./fieldInput";

describe("metadata field input", () => {
  it("maps property and edit metadata to input types", () => {
    expect(fieldInputType({ prpId: "tradeDate", prpType: "Date" })).toBe("date");
    expect(fieldInputType({ prpId: "tradeDate", PrpType: 12 })).toBe("date");
    expect(fieldInputType({ prpId: "tradeTime", PrpType: "13" })).toBe("time");
    expect(fieldInputType({ prpId: "createdAt", PrpType: 14 })).toBe("datetime-local");
    expect(fieldInputType({ prpId: "planned", EditType: "DatePicker" })).toBe("date");
    expect(fieldInputType({ prpId: "planned", EditType: 7 })).toBe("time");
    expect(fieldInputType({ prpId: "planned", EditType: 8 })).toBe("datetime-local");
    expect(fieldInputType({ prpId: "active", PrpType: 8 })).toBe("checkbox");
    expect(fieldInputType({ prpId: "active", EditType: "CheckBox" })).toBe("checkbox");
    expect(fieldInputType({ prpId: "planned", prpType: "String", EditType: 6 })).toBe("text");
    expect(fieldInputType({ prpId: "amount", prpType: "Decimal" })).toBe("text");
    expect(fieldInputType({ prpId: "count", PrpType: "1" })).toBe("text");
  });

  it("matches legacy numeric text filtering and length limits", () => {
    expect(fieldInputMaxLength({ PrpType: 1 })).toBe(4);
    expect(fieldInputMaxLength({ PrpType: "UInt" })).toBe(4);
    expect(fieldInputMaxLength({ PrpType: 3 })).toBe(8);
    expect(fieldInputMaxLength({ PrpType: "Double" })).toBe(8);
    expect(fieldInputMaxLength({ PrpType: 7 })).toBeUndefined();

    expect(sanitizeFieldInput({ PrpType: 1 }, "12e-345")).toBe("1234");
    expect(sanitizeFieldInput({ PrpType: "Long" }, "12 34-56789")).toBe("12345678");
    expect(sanitizeFieldInput({ PrpType: 5 }, "1a.2.34")).toBe("1.2.");
    expect(sanitizeFieldInput({ PrpType: "Double" }, "12.34abc567")).toBe("12.34567");
    expect(sanitizeFieldInput({ PrpType: "Decimal" }, "-12.50")).toBe("-12.50");
  });

  it("checks Boolean metadata values", () => {
    expect(fieldInputChecked({ PrpType: "8" }, "true")).toBe(true);
    expect(fieldInputChecked({ prpType: "Boolean" }, "1")).toBe(true);
    expect(fieldInputChecked({ EditType: "CheckBox" }, "false")).toBe(false);
    expect(fieldInputChecked({ prpType: "String" }, "true")).toBe(false);
  });

  it("normalizes only DateTime metadata values", () => {
    expect(fieldInputValue({ PrpType: "14" }, "2026-07-03 09:05:06.0")).toBe("2026-07-03T09:05:06");
    expect(fieldInputValue({ prpType: "DateTime" }, "2026-07-03T09:05:06.123")).toBe("2026-07-03T09:05:06");
    expect(fieldInputValue({ EditType: "DateTimePicker" }, "2026-07-03 09:05:06.0")).toBe(
      "2026-07-03T09:05:06"
    );
    expect(fieldInputValue({ prpType: "String" }, "2026-07-03 09:05:06.0")).toBe("2026-07-03 09:05:06.0");
  });
});
