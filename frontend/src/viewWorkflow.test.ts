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
  dataOperations,
  detailFieldsFromReadView,
  detailGroupsFromReadView,
  detailResultItems,
  detailResultSimpleData,
  listRenderColumns,
  fieldEditType,
  fieldKey,
  fieldDisplayValue,
  fieldModelId,
  fieldType,
  fieldTitle,
  groupColumns,
  groupKey,
  isEnumField,
  inputQueryItemId,
  inputQueryItemText,
  isLookupField,
  isReadonlyField,
  itemKey,
  legacyAppDefaultViewId,
  legacyAuthIndex,
  legacyAuthNo,
  legacyAuthText,
  legacyAuthViewId,
  legacyCheckCodeCode,
  legacyCheckCodeImage,
  legacyCheckCodeKey,
  legacyEnumName,
  legacyEnumValue,
  legacyEnumValues,
  legacyInitAppCheckCode,
  legacyInitAppDbId,
  legacyInputQueryItems,
  legacyMainMenuItems,
  legacyMessageContent,
  legacyMessageId,
  legacyMessageResultKey,
  legacyMessageResultView,
  legacyMessages,
  legacyNotifies,
  legacyNotifyAuthNo,
  legacyNotifyCount,
  legacyRunOperationSuccess,
  legacySubMenuItems,
  listAutoFreshTime,
  listFreshTime,
  listPageIndex,
  listRows,
  listTotalItems,
  listTotalPages,
  operationLabel,
  operationTargetViewId,
  reportModelColumnId,
  reportModelColumnName,
  reportModelColumns,
  reportModelCompareTypes,
  reportModelOptionName,
  reportModelQueryTypes,
  reportModelStates,
  reportModelStateText,
  reportGridCells,
  reportRowsFromCells,
  rowFormatClass,
  rowObjectId,
  rowOperations,
  rowRenderKey,
  rowValue,
  selectedChildViewId,
  readViewDetailViews,
  readViewFields,
  readViewId,
  renderedDetailFields,
  renderedDetailGroups,
  viewColumns,
  viewDisplayName,
  viewDisplayTitle,
  viewDisplayType,
  viewDetailViewId,
  viewId,
  viewInputCount,
  viewOperations
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
    const runtimeRow = {
      values: { dtoOnly: "ignored" },
      items: [
        { prpId: "recordId", prpShowName: "Record ID", prpType: "Long", readOnly: true },
        { prpId: "name", prpShowName: "Name", prpType: "String" }
      ]
    };

    expect(columnsFromRowItems(runtimeRow)).toEqual([
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

  it("does not render list data columns before a View definition is loaded", () => {
    const result = {
      Cols: ["DTO Only"],
      Data: [{ Items: [{ PrpId: "dtoOnly", FmtValue: "ignored" }] }]
    };

    expect(listRenderColumns(undefined, result)).toEqual([]);
    expect(listRenderColumns({ ID: 100, Items: [] }, result)).toEqual([
      { id: 0, property: "DTO Only", propertyName: "DTO Only", title: "DTO Only", name: "DTO Only" }
    ]);
  });

  it("does not use values DTO fields for view row identity or cells", () => {
    const columns = [{ property: "recordId", title: "Record ID" }];
    const row = {
      values: { recordId: "dto-id" },
      items: [{ prpId: "recordId", objId: "item-id", fmtValue: "Item ID" }]
    };

    expect(rowObjectId(row, columns)).toBe("item-id");
    expect(rowValue(row, columns[0])).toBe("Item ID");
    const dtoOnlyRow = { values: { recordId: "dto-id" }, rowIndex: 4 };
    expect(rowObjectId(dtoOnlyRow, columns)).toBe("");
    expect(rowValue(dtoOnlyRow, columns[0])).toBe("");
    expect(rowRenderKey(dtoOnlyRow, 7)).toBe("4");
  });

  it("merges read item View metadata with detail data values", () => {
    const view = {
      ViewId: 102,
      Items: [
        {
          Name: "Order ID",
          PrpId: "orderId",
          PrpShowName: "Order ID",
          PrpType: "Long",
          PrpModelId: 0,
          ReadOnly: true,
          EditType: "ReadOnly"
        }
      ]
    };
    const fields = detailFieldsFromReadView(view, [
      { PrpId: "orderId", ObjId: "1001", FmtValue: "1001", PrpShowName: "DTO Order ID" },
      { PrpId: "dtoOnly", ObjId: "ignored", FmtValue: "Ignored" }
    ]);

    expect(readViewId(view)).toBe(102);
    expect(readViewFields(view).length).toBe(1);
    expect(fieldKey(fields[0])).toBe("orderId");
    expect(fieldTitle(fields[0])).toBe("Order ID");
    expect(fieldType(fields[0])).toBe("Long");
    expect(fieldEditType(fields[0])).toBe("ReadOnly");
    expect(fields[0]).toMatchObject({
      objId: "1001",
      fmtValue: "1001",
      prpType: "Long",
      readOnly: true,
      editType: "ReadOnly"
    });
    expect(fields).toHaveLength(1);
  });

  it("uses read item DetailViews as child group columns before data rows", () => {
    const view = {
      DetailViews: [
        {
          Name: "Items",
          PrpId: "items",
          Items: [
            { Name: "Item ID", PrpId: "itemId", PrpShowName: "Item ID", PrpType: "Long", ReadOnly: true },
            { Name: "Item Name", PrpId: "itemName", PrpShowName: "Item Name", PrpType: "String" }
          ]
        }
      ]
    };
    const dataGroup = {
      name: "OrderItem",
      prpId: "items",
      listViewId: 101,
      selectedView: 101,
      selectFromExists: true,
      properties: [{ prpId: "dtoOnly", prpShowName: "DTO Only" }],
      items: [
        {
          dataId: "2001",
          values: [
            { prpId: "itemId", objId: "2001", fmtValue: "2001" },
            { prpId: "itemName", objId: "Updated item", fmtValue: "Updated item" }
          ]
        }
      ]
    };

    const groups = detailGroupsFromReadView(view, [dataGroup]);

    expect(readViewDetailViews(view)).toHaveLength(1);
    expect(groups).toHaveLength(1);
    expect(groupKey(groups[0])).toBe("items");
    expect(selectedChildViewId(groups[0])).toBe(101);
    expect(groups[0].items).toBe(dataGroup.items);
    expect(groupColumns(groups[0]).map(fieldKey)).toEqual(["itemId", "itemName"]);
    expect(fieldTitle(groupColumns(groups[0])[0])).toBe("Item ID");
    expect(detailGroupsFromReadView(undefined, [dataGroup])).toEqual([dataGroup]);
    expect(renderedDetailGroups(undefined, [dataGroup])).toEqual([]);
    expect(renderedDetailGroups(view, [dataGroup])).toEqual(groups);
  });

  it("does not render detail DTO fields before a read-item View definition is loaded", () => {
    const dataFields = [{ PrpId: "dtoOnly", ObjId: "ignored", FmtValue: "Ignored" }];
    const view = { ViewId: 102, Items: [{ PrpId: "name", PrpShowName: "Name" }] };

    expect(renderedDetailFields(undefined, dataFields)).toEqual([]);
    expect(renderedDetailFields(view, [{ PrpId: "name", ObjId: "Ada", FmtValue: "Ada" }])).toMatchObject([
      { prpId: "name", objId: "Ada" }
    ]);
  });

  it("reads querydatadetail rows and child items from legacy or camel result payloads", () => {
    const pascal = {
      Data: {
        SimpleData: [{ PrpId: "orderId", PrpShowName: "Order ID", FmtValue: "1001" }],
        Items: [{ name: "Items", prpId: "items", items: [] }]
      }
    };
    const camel = {
      data: {
        simpleData: [{ prpId: "name", prpShowName: "Name", fmtValue: "Ada" }],
        items: [{ name: "Lines", prpId: "lines", items: [] }]
      }
    };

    expect(detailResultSimpleData(pascal)).toEqual(pascal.Data.SimpleData);
    expect(detailResultItems(pascal)).toEqual(pascal.Data.Items);
    expect(fieldKey(detailResultSimpleData(pascal)[0])).toBe("orderId");
    expect(fieldTitle(detailResultSimpleData(pascal)[0])).toBe("Order ID");
    expect(fieldDisplayValue(detailResultSimpleData(pascal)[0])).toBe("1001");
    expect(detailResultSimpleData(camel)).toEqual(camel.data.simpleData);
    expect(detailResultItems(camel)).toEqual(camel.data.items);
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

  it("reads Pascal getlistview metadata through shared View helpers", () => {
    const create = { ID: 1, Name: "Create", RequireSelect: false, ViewID: 200 };
    const open = { ID: 2, Name: "Open", RequireSelect: true, ViewID: 201 };
    const view = {
      ID: 100,
      Name: "Orders",
      Type: "ListView",
      DetailViewId: 300,
      inputInfo: [{ property: "keyword", text: "Keyword" }],
      Items: [{ ID: 901, Name: "Order ID", PropertyName: "orderId" }],
      Operations: [create, open]
    };

    expect(viewId(view)).toBe(100);
    expect(viewDisplayName(view)).toBe("Orders");
    expect(viewDisplayTitle(view)).toBe("Orders");
    expect(viewDisplayType(view)).toBe("ListView");
    expect(viewInputCount(view)).toBe(1);
    expect(viewDetailViewId(view, 100)).toBe(300);
    expect(columnKey(viewColumns(view)[0])).toBe("orderId");
    expect(createOperations(viewOperations(view))).toEqual([create]);
    expect(rowOperations(viewOperations(view))).toEqual([open]);
    expect(operationTargetViewId(open)).toBe(201);
    expect(operationLabel(open)).toBe("Open");
    expect(dataOperations({ Operations: [open] })).toEqual([open]);
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

  it("reads the default View id from legacy app shell payloads", () => {
    expect(legacyAppDefaultViewId({ app: { defaultViewId: 100 } })).toBe(100);
    expect(legacyAppDefaultViewId({ App: { DefaultViewId: 101 } })).toBe(101);
    expect(legacyAppDefaultViewId({ defaultViewId: 102 })).toBe(102);
    expect(legacyAppDefaultViewId({ App: { DefaultViewId: 0 } })).toBe(0);
  });

  it("reads initapp and check-code fields from Pascal or camel legacy payloads", () => {
    const pascal = {
      CheckCode: { Key: "key-1", Code: "A2BC", ChkCodeImg: "image-bytes" },
      Dbs: [{ DbId: "car_wash", DbName: "Car Wash" }]
    };
    const camel = {
      checkCode: { key: "key-2", code: "D4EF", chkCodeImg: "camel-image" },
      dbs: [{ dbId: "main", dbName: "Main" }]
    };

    expect(legacyInitAppCheckCode(pascal)).toBe(pascal.CheckCode);
    expect(legacyInitAppDbId(pascal)).toBe("car_wash");
    expect(legacyCheckCodeKey(pascal.CheckCode)).toBe("key-1");
    expect(legacyCheckCodeCode(pascal.CheckCode)).toBe("A2BC");
    expect(legacyCheckCodeImage(pascal.CheckCode)).toBe("image-bytes");
    expect(legacyInitAppCheckCode(camel)).toBe(camel.checkCode);
    expect(legacyInitAppDbId(camel)).toBe("main");
    expect(legacyCheckCodeKey(camel.checkCode)).toBe("key-2");
    expect(legacyCheckCodeCode(camel.checkCode)).toBe("D4EF");
    expect(legacyCheckCodeImage(camel.checkCode)).toBe("camel-image");
  });

  it("reads tool-panel lists and fields from Pascal or camel legacy payloads", () => {
    const main = { TopMenu: [{ AuthNo: "0", Text: "Views", ViewId: 0, Index: 1 }] };
    const menu = { Items: [{ AuthNo: "1", Text: "Home", ViewId: 100, Index: 2 }] };
    const messages = { Messages: [{ MessageID: "m1", MessageContent: "Ready", ResultView: 100, ResultKey: "1001" }] };
    const notifies = { Notifies: [{ AuthNo: "1", Count: 3 }] };
    const enums = { EnumValues: [{ Name: "Open", Value: 0 }] };
    const inputQuery = { Items: [{ Id: "1001", Text: "Ada" }] };

    expect(legacyMainMenuItems(main)).toHaveLength(1);
    expect(legacyAuthText(main.TopMenu[0])).toBe("Views");
    expect(legacySubMenuItems(menu)).toHaveLength(1);
    expect(legacyAuthNo(menu.Items[0])).toBe("1");
    expect(legacyAuthText(menu.Items[0])).toBe("Home");
    expect(legacyAuthViewId(menu.Items[0])).toBe(100);
    expect(legacyAuthIndex(menu.Items[0])).toBe(2);
    expect(legacyMessages(messages)).toHaveLength(1);
    expect(legacyMessageId(messages.Messages[0])).toBe("m1");
    expect(legacyMessageContent(messages.Messages[0])).toBe("Ready");
    expect(legacyMessageResultView(messages.Messages[0])).toBe(100);
    expect(legacyMessageResultKey(messages.Messages[0])).toBe("1001");
    expect(legacyNotifies(notifies)).toHaveLength(1);
    expect(legacyNotifyAuthNo(notifies.Notifies[0])).toBe("1");
    expect(legacyNotifyCount(notifies.Notifies[0])).toBe(3);
    expect(legacyEnumValues(enums)).toHaveLength(1);
    expect(legacyEnumName(enums.EnumValues[0])).toBe("Open");
    expect(legacyEnumValue(enums.EnumValues[0])).toBe("0");
    expect(legacyInputQueryItems(inputQuery)).toHaveLength(1);
    expect(inputQueryItemId(inputQuery.Items[0])).toBe("1001");
    expect(inputQueryItemText(inputQuery.Items[0])).toBe("Ada");
  });

  it("reads runoperation success from camel or legacy result fields", () => {
    expect(legacyRunOperationSuccess({ success: true })).toBe(true);
    expect(legacyRunOperationSuccess({ IsSuccess: true })).toBe(true);
    expect(legacyRunOperationSuccess({ success: false, IsSuccess: false })).toBe(false);
  });

  it("uses the rendered View detail id before falling back to the list id", () => {
    expect(viewDetailViewId({ id: 100, detailViewId: 200 }, 100)).toBe(200);
    expect(viewDetailViewId({ ID: 100, DetailViewId: 300 }, 100)).toBe(300);
    expect(viewDetailViewId({ id: 100, detailViewId: 0 }, 100)).toBe(100);
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
    const pascal = {
      Cells: [
        { Row: 0, Col: 0, RowSpan: 1, ColSpan: 1, FmtValue: "Symbol" },
        { Row: 1, Col: 0, RowSpan: 1, ColSpan: 1, FmtValue: "BTC-USDT" }
      ]
    };
    expect(reportRowsFromCells(reportGridCells(pascal))).toEqual([
      ["Symbol"],
      ["BTC-USDT"]
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

    const pascal = {
      Cols: [{
        ID: "1004",
        Name: "Amount",
        PrpType: 2,
        QueryTypes: [{ ID: "1", Name: "原值" }],
        CompareTypes: [{ ID: "7", Name: "包含" }],
        States: [{ ShowName: "Open", DBName: "0" }]
      }]
    };
    const col = reportModelColumns(pascal)[0];
    expect(reportModelColumnId(col)).toBe("1004");
    expect(reportModelColumnName(col)).toBe("Amount");
    expect(reportModelOptionName(reportModelQueryTypes(col)[0])).toBe("原值");
    expect(reportModelOptionName(reportModelCompareTypes(col)[0])).toBe("包含");
    expect(reportModelStateText(reportModelStates(col)[0])).toBe("Open");
    expect(buildReportColsFromModel(reportModelColumns(pascal))).toEqual([
      { colName: "Amount[原值]", colId: "1004", selectedTypeId: "1", index: 0, orderType: "2" }
    ]);
  });

});
