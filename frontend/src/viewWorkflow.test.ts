import { describe, expect, it } from "vitest";
import {
  buildAddedItemProperty,
  buildDeletedItemProperty,
  buildDraftsFromRow,
  buildReportColsFromModel,
  detailItemValues,
  emptyGroupDraft,
  buildFieldDrafts,
  buildSavePropertyies,
  buildSelectedExistingItemProperty,
  buildUpdatedItemProperty,
  columnKey,
  columnsFromListResult,
  columnsFromRowItems,
  createOperations,
  fieldModelId,
  groupKey,
  isEnumField,
  isLookupField,
  isReadonlyField,
  itemKey,
  listAutoFreshTime,
  listFreshTime,
  listPageIndex,
  listRows,
  listTotalItems,
  listTotalPages,
  reportRowsFromCells,
  recordColumns,
  recordRowKey,
  rowFormatClass,
  rowObjectId,
  rowOperations,
  rowRenderKey,
  rowValue,
  selectedChildViewId,
  viewDetailViewId
} from "./viewWorkflow";

describe("view workflow helpers", () => {
  it("renders rows from view columns and row data", () => {
    const columns = [
      { property: "record_id", propertyName: "recordId", title: "Record ID" },
      { property: "record_state", propertyName: "state", title: "State" }
    ];
    const row = {
      id: "1001",
      values: { record_id: "wrong-id", record_state: "Wrong" },
      items: [
        { prpId: "recordId", fmtValue: "1001" },
        { prpId: "state", fmtValue: "Open" }
      ]
    };

    expect(columnKey(columns[0])).toBe("record_id");
    expect(rowObjectId(row, columns)).toBe("1001");
    expect(rowValue(row, columns[1])).toBe("Open");
    expect(rowFormatClass({ ...row, rowFmt: "warning-row " })).toBe("warning-row");
  });

  it("builds generic save propertyies from detail fields", () => {
    const fields = [
      { prpId: "recordId", objId: "1001", fmtValue: "1001", readOnly: true },
      { prpId: "name", objId: "Sample", fmtValue: "Sample" },
      { prpId: "state", objId: "0", fmtValue: "Open" }
    ];
    const drafts = buildFieldDrafts(fields);
    drafts.name = "Updated";

    expect(buildSavePropertyies(fields, drafts)).toEqual([
      { key: "name", value: "Updated" },
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
    expect(isReadonlyField({ prpId: "name", readOnly: false, editType: "TextBox" })).toBe(false);
  });

  it("derives fallback columns from row item metadata instead of DTO values", () => {
    expect(columnsFromRowItems({
      values: { dtoOnly: "ignored" },
      items: [
        { prpId: "recordId", prpShowName: "Record ID", prpType: "Long", readOnly: true },
        { prpId: "name", prpShowName: "Name", prpType: "String" }
      ]
    })).toEqual([
      {
        property: "recordId",
        propertyName: "recordId",
        title: "Record ID",
        name: "Record ID",
        isReadOnly: true,
        editType: undefined,
        propertyType: "Long",
        propertyModel: undefined
      },
      {
        property: "name",
        propertyName: "name",
        title: "Name",
        name: "Name",
        isReadOnly: undefined,
        editType: undefined,
        propertyType: "String",
        propertyModel: undefined
      }
    ]);
  });

  it("derives fallback columns from legacy querydata Cols before row items", () => {
    expect(columnsFromListResult({ Cols: ["Record ID", "State"] })).toEqual([
      { id: 0, property: "Record ID", propertyName: "Record ID", title: "Record ID", name: "Record ID" },
      { id: 1, property: "State", propertyName: "State", title: "State", name: "State" }
    ]);
    expect(columnsFromListResult({ cols: ["Name"] })[0]).toMatchObject({
      property: "Name",
      title: "Name"
    });
  });

  it("does not use values DTO fields for view row identity or cells", () => {
    const columns = [{ property: "recordId", title: "Record ID" }];
    const row = {
      values: { recordId: "dto-id" },
      items: [{ prpId: "recordId", objId: "item-id", fmtValue: "Item ID" }]
    };

    expect(rowObjectId(row, columns)).toBe("item-id");
    expect(rowValue(row, columns[0])).toBe("Item ID");
    expect(rowObjectId({ values: { recordId: "dto-id" }, rowIndex: 4 }, columns)).toBe("");
    expect(rowValue({ values: { recordId: "dto-id" } }, columns[0])).toBe("");
    expect(rowRenderKey({ values: { recordId: "dto-id" }, rowIndex: 4 }, 7)).toBe("4");
  });

  it("renders Pascal legacy list rows from View item metadata", () => {
    const columns = [
      { property: "recordId", title: "Record ID" },
      { property: "state", title: "State" }
    ];
    const row = {
      Id: "1001",
      RowIndex: 2,
      RowFmt: "warning-row ",
      values: { recordId: "dto-id", state: "DTO state" },
      Items: [
        { PrpId: "recordId", ObjId: "1001", FmtValue: "1001", PrpShowName: "Record ID", ReadOnly: true },
        { PrpId: "state", ObjId: "0", FmtValue: "Open", PrpShowName: "State", PrpType: "Enum", PrpModelId: 102 }
      ]
    };
    const result = { TotalItem: 8, TotalPage: 2, PageIndex: 1, Data: [row] };

    expect(listRows(result)).toEqual([row]);
    expect(listTotalItems(result)).toBe(8);
    expect(listTotalPages(result)).toBe(2);
    expect(listPageIndex(result, 9)).toBe(1);
    expect(rowObjectId(row, columns)).toBe("1001");
    expect(rowValue(row, columns[1])).toBe("Open");
    expect(rowFormatClass(row)).toBe("warning-row");
    expect(columnsFromRowItems(row)[1]).toMatchObject({
      property: "state",
      title: "State",
      propertyType: "Enum",
      propertyModel: 102
    });
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
    expect(detailItemValues({ DataId: "2002", Values: item.values })).toEqual(item.values);
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
      values: { id: "wrong-id", name: "Wrong item" },
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

  it("derives child group metadata helpers from View data", () => {
    const group = {
      name: "Items",
      listViewId: 101,
      properties: [
        { prpId: "itemId", fmtValue: "" },
        { prpId: "itemName", fmtValue: "" }
      ]
    };

    expect(groupKey(group)).toBe("Items");
    expect(selectedChildViewId(group)).toBe(101);
    expect(emptyGroupDraft(group)).toEqual({
      itemId: "",
      itemName: ""
    });
  });

  it("splits legacy list create and row operations", () => {
    const operations = [
      { id: 1, name: "Create", requireSelect: false, viewId: 200 },
      { id: 2, name: "Open", requireSelect: true, viewId: 201 },
      { id: 3, name: "Delete", requireSelect: true, viewId: 0 },
      { id: 4, name: "No target", requireSelect: false, viewId: 0 }
    ];
    expect(createOperations(operations)).toEqual([{ id: 1, name: "Create", requireSelect: false, viewId: 200 }]);
    expect(rowOperations(operations)).toEqual([
      { id: 2, name: "Open", requireSelect: true, viewId: 201 },
      { id: 3, name: "Delete", requireSelect: true, viewId: 0 }
    ]);
  });

  it("reads legacy list paging from direct totals or pageInfo", () => {
    expect(listTotalItems({ totalItem: 8 })).toBe(8);
    expect(listTotalItems({ pageInfo: { total: 5 } })).toBe(5);
    expect(listTotalPages({ totalPage: 4 })).toBe(4);
    expect(listTotalPages({ pageInfo: { pageCount: 3 } })).toBe(3);
    expect(listPageIndex({ pageInfo: { pageIndex: 2 } }, 1)).toBe(2);
    expect(listAutoFreshTime({ autoFreshTime: 30 })).toBe(30);
    expect(listAutoFreshTime({ AutoFreshTime: 45 })).toBe(45);
    expect(listFreshTime({ freshTime: "2026-07-04T00:16:42" })).toBe("2026-07-04T00:16:42");
    expect(listFreshTime({ FreshTime: "2026-07-04T00:17:00" })).toBe("2026-07-04T00:17:00");
  });

  it("uses the rendered View detail id before falling back to the list id", () => {
    expect(viewDetailViewId({ id: 100, detailViewId: 200 }, 100)).toBe(200);
    expect(viewDetailViewId({ id: 100 }, 100)).toBe(100);
  });

  it("renders report cells as a matrix", () => {
    expect(reportRowsFromCells([
      { row: 0, col: 0, rowSpan: 1, colSpan: 1, fmtValue: "Symbol" },
      { row: 0, col: 1, rowSpan: 1, colSpan: 1, fmtValue: "State" },
      { row: 1, col: 0, rowSpan: 1, colSpan: 1, fmtValue: "Sample" }
    ])).toEqual([
      ["Symbol", "State"],
      ["Sample", ""]
    ]);
  });

  it("builds default report columns from loaded report model metadata", () => {
    expect(buildReportColsFromModel([
      { id: "1002", name: "Symbol", queryTypes: [{ id: "1", name: "原值" }] },
      { id: "1003", name: "State", queryTypes: [{ id: "2", name: "计数" }] }
    ])).toEqual([
      { colName: "Symbol[原值]", colId: "1002", selectedTypeId: "1", index: 0, orderType: "2" },
      { colName: "State[计数]", colId: "1003", selectedTypeId: "2", index: 1, orderType: "2" }
    ]);
  });

  it("derives generic record columns without business keys", () => {
    const rows = [
      { id: 1, amount: 120, state: "Open" },
      { id: 2, operator: "Ada" }
    ];

    expect(recordColumns(rows)).toEqual(["id", "amount", "state", "operator"]);
    expect(recordRowKey(rows[0], ["id", "amount"], 0)).toBe("1");
    expect(recordRowKey({ amount: 120 }, ["id", "amount"], 3)).toBe("120");
    expect(recordRowKey({}, [], 4)).toBe("4");
  });
});
