import { describe, expect, it } from "vitest";
import {
  buildAddedItemProperty,
  buildDeletedItemProperty,
  buildFieldDrafts,
  buildSavePropertyies,
  buildUpdatedItemProperty,
  columnKey,
  itemKey,
  rowObjectId,
  rowValue
} from "./viewWorkflow";

describe("view workflow helpers", () => {
  it("renders rows from view columns and row data", () => {
    const columns = [
      { property: "orderId", title: "Order ID" },
      { property: "state", title: "State" }
    ];
    const row = {
      id: "1001",
      values: { orderId: 1001, state: "0" },
      items: [
        { prpId: "orderId", fmtValue: "1001" },
        { prpId: "state", fmtValue: "Open" }
      ]
    };

    expect(columnKey(columns[0])).toBe("orderId");
    expect(rowObjectId(row, columns)).toBe("1001");
    expect(rowValue(row, columns[1])).toBe("Open");
  });

  it("builds generic save propertyies from detail fields", () => {
    const fields = [
      { prpId: "symbol", objId: "BTC-USDT", fmtValue: "BTC-USDT" },
      { prpId: "state", objId: "0", fmtValue: "Open" }
    ];
    const drafts = buildFieldDrafts(fields);
    drafts.symbol = "ETH-USDT";

    expect(buildSavePropertyies(fields, drafts)).toEqual([
      { key: "symbol", value: "ETH-USDT" },
      { key: "state", value: "0" }
    ]);
  });

  it("keeps legacy child collection add/update/delete payload names", () => {
    const group = {
      prpId: "items",
      properties: [
        { prpId: "itemId", fmtValue: "" },
        { prpId: "itemName", fmtValue: "" }
      ]
    };
    const item = {
      dataId: "2001",
      values: [
        { prpId: "itemId", objId: "2001", fmtValue: "2001" },
        { prpId: "itemName", objId: "Old item", fmtValue: "Old item" }
      ]
    };

    expect(itemKey(group, item)).toBe("items:2001");
    expect(buildAddedItemProperty(group, "2002", { itemId: "2002", itemName: "New item" })).toEqual({
      key: "items",
      addedItems: [
        {
          itemId: "2002",
          isExist: true,
          propertyies: [
            { key: "itemId", value: "2002" },
            { key: "itemName", value: "New item" }
          ]
        }
      ]
    });
    expect(buildUpdatedItemProperty(group, item, { itemId: "2001", itemName: "Updated item" })).toEqual({
      key: "items",
      items: [
        {
          itemId: "2001",
          isExist: true,
          propertyies: [
            { key: "itemId", value: "2001" },
            { key: "itemName", value: "Updated item" }
          ]
        }
      ]
    });
    expect(buildDeletedItemProperty(group, item)).toMatchObject({
      key: "items",
      delteItems: [{ itemId: "2001", isExist: true }]
    });
  });
});
