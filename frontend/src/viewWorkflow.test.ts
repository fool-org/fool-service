import { describe, expect, it } from "vitest";
import {
  buildAddedItemProperty,
  buildDeletedItemProperty,
  buildDraftsFromRow,
  buildFieldDrafts,
  buildSavePropertyies,
  buildSelectedExistingItemProperty,
  buildUpdatedItemProperty,
  columnKey,
  fieldModelId,
  isEnumField,
  isLookupField,
  isReadonlyField,
  itemKey,
  rowFormatClass,
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
    expect(rowFormatClass({ ...row, rowFmt: "warning-row " })).toBe("warning-row");
  });

  it("builds generic save propertyies from detail fields", () => {
    const fields = [
      { prpId: "orderId", objId: "1001", fmtValue: "1001", readOnly: true },
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

  it("identifies enum fields by metadata", () => {
    expect(isEnumField({ prpId: "state", prpType: "Enum", prpModelId: 102 })).toBe(true);
    expect(fieldModelId({ prpId: "state", prpType: "Enum", prpModelId: 102 })).toBe(102);
    expect(isEnumField({ prpId: "name", prpType: "String", prpModelId: 0 })).toBe(false);
  });

  it("identifies lookup fields by metadata", () => {
    expect(isLookupField({ prpId: "customer", prpType: "BusinessObject", prpModelId: 200 })).toBe(true);
    expect(isLookupField({ prpId: "customer", prpType: "16", prpModelId: 200 })).toBe(true);
    expect(isLookupField({ prpId: "name", prpType: "String", prpModelId: 0 })).toBe(false);
  });

  it("identifies readonly fields by metadata", () => {
    expect(isReadonlyField({ prpId: "id", readOnly: true })).toBe(true);
    expect(isReadonlyField({ prpId: "id", editType: "ReadOnly" })).toBe(true);
    expect(isReadonlyField({ prpId: "symbol", readOnly: false, editType: "TextBox" })).toBe(false);
  });

  it("keeps legacy child collection add/update/delete payload names", () => {
    const group = {
      prpId: "items",
      properties: [
        { prpId: "itemId", fmtValue: "", editType: "ReadOnly" },
        { prpId: "itemName", fmtValue: "" }
      ]
    };
    const item = {
      dataId: "2001",
      values: [
        { prpId: "itemId", objId: "2001", fmtValue: "2001", editType: "ReadOnly" },
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
          propertyies: [{ key: "itemName", value: "New item" }]
        }
      ]
    });
    expect(buildUpdatedItemProperty(group, item, { itemId: "2001", itemName: "Updated item" })).toEqual({
      key: "items",
      items: [
        {
          itemId: "2001",
          isExist: true,
          propertyies: [{ key: "itemName", value: "Updated item" }]
        }
      ]
    });
    expect(buildDeletedItemProperty(group, item)).toMatchObject({
      key: "items",
      delteItems: [{ itemId: "2001", isExist: true }]
    });
  });

  it("maps a selected existing row into AddedItems by child fields", () => {
    const group = {
      prpId: "items",
      properties: [
        { prpId: "itemId", fmtValue: "", editType: "ReadOnly" },
        { prpId: "itemName", fmtValue: "", editType: "ReadOnly" }
      ]
    };
    const columns = [
      { property: "id", title: "ID" },
      { property: "name", title: "Name" }
    ];
    const row = {
      id: "3001",
      values: { id: "3001", name: "Existing item" },
      items: [
        { prpId: "id", objId: "3001", fmtValue: "3001" },
        { prpId: "name", objId: "Existing item", fmtValue: "Existing item" }
      ]
    };

    expect(buildDraftsFromRow(group.properties, row, columns)).toEqual({
      itemId: "3001",
      itemName: "Existing item"
    });
    expect(buildSelectedExistingItemProperty(group, row, columns)).toEqual({
      key: "items",
      addedItems: [
        {
          itemId: "3001",
          isExist: true,
          propertyies: [
            { key: "itemId", value: "3001" },
            { key: "itemName", value: "Existing item" }
          ]
        }
      ]
    });
  });
});
