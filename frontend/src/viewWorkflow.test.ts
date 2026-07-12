import { describe, expect, it } from "vitest";
import type { ListDataItem } from "./api";
import {
  buildAddedDetailItem,
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
  createOperations,
  dataCanEdit,
  dataOperations,
  detailFieldsFromReadView,
  detailGroupsFromReadView,
  detailResultItems,
  detailResultSimpleData,
  draftFieldValue,
  listRenderColumns,
  fieldEditType,
  fieldKey,
  fieldDisplayValue,
  fieldInputChecked,
  fieldInputType,
  fieldInputValue,
  fieldModelId,
  fieldType,
  fieldTitle,
  groupColumns,
  groupDetailViewId,
  groupItems,
  groupKey,
  groupListViewId,
  groupSelectFromExists,
  groupSelectedViewId,
  groupTitle,
  isEnumField,
  isMultilineField,
  inputQueryItemId,
  inputQueryItemText,
  isLookupField,
  isReadonlyField,
  itemDataId,
  itemKey,
  legacyAppDefaultViewId,
  legacyAppName,
  legacyAppVersion,
  legacyAuthImageUrl,
  legacyAuthIndex,
  legacyAuthNo,
  legacyAuthText,
  legacyAuthViewId,
  legacyCheckCodeCode,
  legacyCheckCodeImage,
  legacyCheckCodeKey,
  legacyDetailHref,
  legacyDetailPath,
  legacyEnumName,
  legacyEnumValue,
  legacyEnumValues,
  legacyInitAppCheckCode,
  legacyInitAppDbId,
  legacyInputQueryItems,
  legacyItemViewPathId,
  legacyItemDetailFields,
  legacyMapMarkers,
  legacyMainMenuItems,
  legacyLoginErrorMessage,
  legacyMessageContent,
  legacyMessageId,
  legacyMessageTime,
  legacyMessageResultKey,
  legacyMessageResultView,
  legacyMessages,
  legacyChildNewHref,
  legacyNewPath,
  legacyNewHref,
  legacyNotifies,
  legacyNotifyAuthNo,
  legacyNotifyCount,
  legacyNotifyCountForAuth,
  legacyRunOperationSuccess,
  legacyRunOperationMessage,
  legacySubMenuItems,
  legacyUserName,
  legacyUserAvatar,
  legacyViewPathId,
  listAutoFreshTime,
  listFreshTime,
  listPageIndex,
  listRows,
  mergeItemPropertyChange,
  legacyChartData,
  listTotalItems,
  listTotalPages,
  operationParamKey,
  operationParamLabel,
  operationParams,
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
  reportModelStateValue,
  reportGridCells,
  reportGridPage,
  reportGridTotalPages,
  reportGridTotalRecords,
  reportRowsFromCells,
  rowFormatClass,
  rowObjectId,
  rowOperations,
  rowRenderKey,
  rowValue,
  removeAddedItemPropertyChange,
  sudokuPanelListViewType,
  readViewDetailViews,
  readViewFields,
  readViewId,
  readViewForId,
  renderedDetailFields,
  renderedDetailGroups,
  rememberReadView,
  sudokuPanelKind,
  viewColumns,
  viewDisplayName,
  viewDisplayTitle,
  viewDisplayType,
  viewDetailViewId,
  viewId,
  viewInputCount,
  viewTemplateName,
  viewUsesSudokuTemplate,
  viewUsesChartTemplate,
  viewTemplateKind,
  withGroupItems,
  withDraftFieldValue,
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
    expect(isEnumField({ prpId: "state", PrpType: 15, PrpModelId: 102 })).toBe(true);
    expect(fieldModelId({ prpId: "state", prpType: "Enum", prpModelId: 102 })).toBe(102);
    expect(isEnumField({ prpId: "name", prpType: "String", prpModelId: 0 })).toBe(false);
  });

  it("identifies lookup fields by metadata", () => {
    expect(isLookupField({ prpId: "customer", prpType: "BusinessObject", prpModelId: 200 })).toBe(true);
    expect(isLookupField({ prpId: "customer", prpType: "16", prpModelId: 200 })).toBe(true);
    expect(isLookupField({ prpId: "customer", PrpType: 16, PrpModelId: 200 })).toBe(true);
    expect(isLookupField({ prpId: "name", prpType: "String", prpModelId: 0 })).toBe(false);
  });

  it("identifies readonly fields by metadata", () => {
    expect(isReadonlyField({ prpId: "id", readOnly: true })).toBe(true);
    expect(isReadonlyField({ prpId: "id", editType: "ReadOnly" })).toBe(true);
    expect(isReadonlyField({ prpId: "id", editType: 0 })).toBe(true);
    expect(isReadonlyField({ prpId: "id", readOnly: false, editType: "ReadOnly" })).toBe(false);
    expect(isReadonlyField({ prpId: "id", ReadOnly: false, EditType: 0 })).toBe(false);
    expect(isReadonlyField({ prpId: "name", readOnly: false, editType: "TextBox" })).toBe(false);
  });

  it("uses RichTextBox as a multiline fallback only when property type is absent", () => {
    expect(isMultilineField({ prpId: "notes", EditType: "RichTextBox" })).toBe(true);
    expect(isMultilineField({ prpId: "notes", EditType: 5 })).toBe(true);
    expect(isMultilineField({ prpId: "notes", PrpType: "String", EditType: "RichTextBox" })).toBe(false);
    expect(isMultilineField({ prpId: "notes", PrpType: 11, EditType: 5 })).toBe(false);
    expect(isMultilineField({ prpId: "name", EditType: "TextBox" })).toBe(false);
  });

  it("maps metadata field types to native input types", () => {
    expect(fieldInputType({ prpId: "tradeDate", prpType: "Date" })).toBe("date");
    expect(fieldInputType({ prpId: "tradeDate", PrpType: 12 })).toBe("date");
    expect(fieldInputType({ prpId: "tradeTime", PrpType: "13" })).toBe("time");
    expect(fieldInputType({ prpId: "tradeTime", PrpType: 13 })).toBe("time");
    expect(fieldInputType({ prpId: "createdAt", PrpType: "14" })).toBe("datetime-local");
    expect(fieldInputType({ prpId: "createdAt", PrpType: 14 })).toBe("datetime-local");
    expect(fieldInputType({ prpId: "orderTime", prpType: "String" })).toBe("text");
    expect(fieldInputType({ prpId: "planned", EditType: "DatePicker" })).toBe("date");
    expect(fieldInputType({ prpId: "planned", EditType: "TimePicker" })).toBe("time");
    expect(fieldInputType({ prpId: "planned", EditType: "DateTimePicker" })).toBe("datetime-local");
    expect(fieldInputType({ prpId: "planned", EditType: 6 })).toBe("date");
    expect(fieldInputType({ prpId: "planned", EditType: 7 })).toBe("time");
    expect(fieldInputType({ prpId: "planned", EditType: 8 })).toBe("datetime-local");
    expect(fieldInputType({ prpId: "planned", prpType: "String", EditType: "DatePicker" })).toBe("text");
    expect(fieldInputType({ prpId: "planned", prpType: "String", EditType: 6 })).toBe("text");
    expect(fieldInputType({ prpId: "active", PrpType: "8" })).toBe("checkbox");
    expect(fieldInputType({ prpId: "active", PrpType: 8 })).toBe("checkbox");
    expect(fieldInputType({ prpId: "active", EditType: "CheckBox" })).toBe("checkbox");
    expect(fieldInputType({ prpId: "active", EditType: 2 })).toBe("checkbox");
    expect(fieldInputType({ prpId: "active", prpType: "String", EditType: "CheckBox" })).toBe("text");
    expect(fieldInputType({ prpId: "active", prpType: "String", EditType: 2 })).toBe("text");
    expect(fieldInputType({ prpId: "active", prpType: "String" })).toBe("text");
    expect(fieldInputType({ prpId: "amount", prpType: "Decimal" })).toBe("number");
    expect(fieldInputType({ prpId: "count", PrpType: "1" })).toBe("number");
    expect(fieldInputType({ prpId: "name", prpType: "String" })).toBe("text");
  });

  it("checks Boolean metadata values for native checkbox inputs", () => {
    expect(fieldInputChecked({ PrpType: "8" }, "true")).toBe(true);
    expect(fieldInputChecked({ prpType: "Boolean" }, "1")).toBe(true);
    expect(fieldInputChecked({ EditType: "CheckBox" }, "false")).toBe(false);
    expect(fieldInputChecked({ prpType: "String" }, "true")).toBe(false);
  });

  it("normalizes only DateTime metadata values for native inputs", () => {
    expect(fieldInputValue({ PrpType: "14" }, "2026-07-03 09:05:06.0")).toBe("2026-07-03T09:05:06");
    expect(fieldInputValue({ prpType: "DateTime" }, "2026-07-03T09:05:06.123")).toBe("2026-07-03T09:05:06");
    expect(fieldInputValue({ EditType: "DateTimePicker" }, "2026-07-03 09:05:06.0")).toBe(
      "2026-07-03T09:05:06"
    );
    expect(fieldInputValue({ prpType: "String", prpId: "createdAt" }, "2026-07-03 09:05:06.0")).toBe(
      "2026-07-03 09:05:06.0"
    );
  });

  it("renders list columns only from loaded View metadata", () => {
    const declared = [{ property: "name", title: "Name" }];
    const result = {
      Cols: ["DTO Only"],
      Data: [{ Items: [{ PrpId: "dtoOnly", FmtValue: "ignored" }] }]
    };

    expect(listRenderColumns(undefined)).toEqual([]);
    expect(listRenderColumns({ ID: 100, Items: [] })).toEqual([]);
    expect(listRenderColumns({ ID: 100, Items: declared })).toEqual(declared);
  });

  it("reads legacy view template names without data DTO fallback", () => {
    expect(viewTemplateName({ TempFile: "viewWithChart" })).toBe("viewWithChart");
    expect(viewTemplateName({ tempFile: "Sudoku" })).toBe("Sudoku");
    expect(viewTemplateName(undefined)).toBe("");
    expect(viewUsesChartTemplate({ TempFile: "viewWithChart" })).toBe(true);
    expect(viewUsesChartTemplate({ TempFile: "Sudoku" })).toBe(false);
    expect(viewUsesSudokuTemplate({ TempFile: "Sudoku" })).toBe(true);
    expect(viewUsesSudokuTemplate({ TempFile: "viewWithChart" })).toBe(false);
    expect(viewTemplateKind(undefined)).toBe("list");
    expect(viewTemplateKind({ TempFile: "view" })).toBe("list");
    expect(viewTemplateKind({ TempFile: "views/viewWithChart.jade" })).toBe("chart");
    expect(viewTemplateKind({ TempFile: "CustomDashboard" })).toBe("unsupported");
  });

  it("normalizes legacy Sudoku child ViewFile names", () => {
    expect(sudokuPanelKind({ ViewFile: "./includes/List" })).toBe("list");
    expect(sudokuPanelKind({ viewFile: "./includes/Group" })).toBe("group");
    expect(sudokuPanelKind({ ViewFile: "./includes/linechart" })).toBe("linechart");
    expect(sudokuPanelKind({ ViewFile: "./includes/Map" })).toBe("map");
    expect(sudokuPanelKind({ ViewFile: "./includes/Item" })).toBe("item");
    expect(sudokuPanelKind({ ViewFile: "" })).toBe("unknown");
    expect(sudokuPanelListViewType({ ListViewType: 1 })).toBe(1);
    expect(sudokuPanelListViewType({ listViewType: 0 })).toBe(0);
    expect(fieldTitle({ Name: "Group Detail" })).toBe("Group Detail");
  });

  it("builds chart data only from legacy chart edit types", () => {
    expect(legacyChartData([
      {
        Items: [
          { PrpShowName: "Day", FmtValue: "Mon", EditType: 11 },
          { PrpShowName: "Orders", FmtValue: "7", EditType: 12 },
          { PrpShowName: "Volume", FmtValue: "12", EditType: 13 },
          { PrpShowName: "Noise", FmtValue: "ignored", EditType: 0 }
        ]
      },
      {
        Items: [
          { PrpShowName: "Day", FmtValue: "Tue", EditType: "ChartAxis" },
          { PrpShowName: "Orders", FmtValue: "9", EditType: "ChartLine" },
          { PrpShowName: "Volume", FmtValue: "11", EditType: "ChartBar" }
        ]
      }
    ])).toEqual({
      labels: ["Mon", "Tue"],
      series: [
        { name: "Orders", type: "line", values: [7, 9] },
        { name: "Volume", type: "bar", values: [12, 11] }
      ]
    });
    expect(legacyChartData([{ Items: [{ PrpShowName: "DTO", FmtValue: "5", EditType: 0 }] }])).toEqual({
      labels: [],
      series: []
    });
  });

  it("builds map markers only from legacy map edit types", () => {
    expect(legacyMapMarkers([
      {
        Items: [
          { PrpShowName: "Longitude", ObjId: "116.32", EditType: "MapLongitude" },
          { PrpShowName: "Latitude", ObjId: "39.94917", EditType: "MapLatitude" },
          { PrpShowName: "Shop", FmtValue: "Main Store", EditType: "MapTitle" },
          { PrpShowName: "Address", FmtValue: "Beijing", EditType: 0 }
        ]
      },
      {
        Items: [
          { PrpShowName: "Longitude", ObjId: "", EditType: 16 },
          { PrpShowName: "Latitude", ObjId: "39", EditType: 17 }
        ]
      }
    ])).toEqual([
      {
        longitude: "116.32",
        latitude: "39.94917",
        title: { label: "Shop", text: "Main Store" },
        info: [{ label: "Address", text: "Beijing" }]
      }
    ]);
  });

  it("builds item panel fields from legacy detail SimpleData", () => {
    expect(legacyItemDetailFields({
      Data: {
        SimpleData: [
          { PrpShowName: "Name", FmtValue: "Main Store" },
          { PrpShowName: "State", FmtValue: "Open" }
        ]
      }
    })).toEqual([
      { label: "Name", text: "Main Store" },
      { label: "State", text: "Open" }
    ]);
    expect(legacyItemDetailFields({ Data: { Items: [] } })).toEqual([]);
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

  it("uses rendered View columns before DTO item order for fallback row identity", () => {
    const columns = [
      { property: "recordId", title: "Record ID" },
      { property: "state", title: "State" }
    ];
    const row = {
      items: [
        { prpId: "state", objId: "wrong-first-item", fmtValue: "Open" },
        { prpId: "recordId", objId: "view-column-id", fmtValue: "1001" }
      ]
    };

    expect(rowObjectId(row, columns)).toBe("view-column-id");
    expect(rowRenderKey(row, 0, columns)).toBe("view-column-id");
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
    expect(groupListViewId(groups[0])).toBe(101);
    expect(groupSelectedViewId(groups[0])).toBe(101);
    expect(groups[0].items).toBe(dataGroup.items);
    expect(groupColumns(groups[0]).map(fieldKey)).toEqual(["itemId", "itemName"]);
    expect(fieldTitle(groupColumns(groups[0])[0])).toBe("Item ID");
    expect(detailGroupsFromReadView(undefined, [dataGroup])).toEqual([]);
    expect(renderedDetailGroups(undefined, [dataGroup])).toEqual([]);
    expect(renderedDetailGroups(view, [dataGroup])).toEqual(groups);
  });

  it("does not render detail DTO fields before a read-item View definition is loaded", () => {
    const dataFields = [{ PrpId: "dtoOnly", ObjId: "ignored", FmtValue: "Ignored" }];
    const view = { ViewId: 102, Items: [{ PrpId: "name", PrpShowName: "Name" }] };

    expect(renderedDetailFields(undefined, dataFields)).toEqual([]);
    expect(detailFieldsFromReadView(undefined, dataFields)).toEqual([]);
    expect(renderedDetailFields({ ViewId: 102, Items: [] }, dataFields)).toEqual([]);
    expect(renderedDetailFields(view, [{ PrpId: "name", ObjId: "Ada", FmtValue: "Ada" }])).toMatchObject([
      { prpId: "name", objId: "Ada" }
    ]);
  });

  it("does not let detail DTO groups define child view columns", () => {
    const dataGroup = {
      name: "DTO Items",
      prpId: "items",
      properties: [{ prpId: "dtoOnly", prpShowName: "DTO Only" }],
      items: [
        {
          dataId: "2001",
          values: [{ prpId: "dtoOnly", objId: "ignored", fmtValue: "Ignored" }]
        }
      ]
    };

    expect(renderedDetailGroups({ ViewId: 102, DetailViews: [] }, [dataGroup])).toEqual([]);
    expect(groupColumns(renderedDetailGroups({
      ViewId: 102,
      DetailViews: [{ Name: "Items", PrpId: "items", Items: [] }]
    }, [dataGroup])[0])).toEqual([]);
  });

  it("does not append child groups missing from the rendered read-item View", () => {
    const view = {
      ViewId: 102,
      DetailViews: [
        { Name: "Items", PrpId: "items", Items: [{ PrpId: "itemId", PrpShowName: "Item ID" }] }
      ]
    };
    const declaredDataGroup = {
      prpId: "items",
      items: [{ dataId: "2001", values: [{ prpId: "itemId", objId: "2001" }] }]
    };
    const dtoOnlyDataGroup = {
      prpId: "dtoOnly",
      properties: [{ prpId: "dtoField", prpShowName: "DTO Field" }],
      items: [{ dataId: "9001", values: [{ prpId: "dtoField", objId: "leak" }] }]
    };

    const groups = renderedDetailGroups(view, [declaredDataGroup, dtoOnlyDataGroup]);

    expect(groups).toHaveLength(1);
    expect(groupKey(groups[0])).toBe("items");
    expect(groupColumns(groups[0]).map(fieldKey)).toEqual(["itemId"]);
  });

  it("keeps read-item Views keyed by requested and rendered View ids", () => {
    const detailView = { ViewId: 201, Items: [{ PrpId: "name", PrpShowName: "Name" }] };
    const createView = { ViewId: 301, Items: [{ PrpId: "symbol", PrpShowName: "Symbol" }] };
    const views = rememberReadView(
      rememberReadView({}, 200, detailView),
      300,
      createView
    );

    expect(readViewForId(views, 200)).toBe(detailView);
    expect(readViewForId(views, 201)).toBe(detailView);
    expect(readViewForId(views, 300)).toBe(createView);
    expect(readViewForId(views, 301)).toBe(createView);
    expect(readViewFields(readViewForId(views, 201)).map(fieldKey)).toEqual(["name"]);
    expect(readViewFields(readViewForId(views, 301)).map(fieldKey)).toEqual(["symbol"]);
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
    expect(buildAddedDetailItem(group, "2002", { itemId: "2002", itemName: "New item" })).toMatchObject({
      dataId: "2002",
      values: [{ prpId: "itemId", objId: "2002" }, { prpId: "itemName", objId: "New item" }]
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
    expect(mergeItemPropertyChange(
      [buildDeletedItemProperty(group, item)],
      buildDeletedItemProperty(group, { ...item, dataId: "2002" })
    )).toMatchObject([{
      key: "items",
      delteItems: [{ itemId: "2001" }, { itemId: "2002" }]
    }]);
    const firstUpdate = buildUpdatedItemProperty(group, item, { itemId: "2001", itemName: "First" });
    const latestUpdate = buildUpdatedItemProperty(group, item, { itemId: "2001", itemName: "Latest" });
    expect(mergeItemPropertyChange([firstUpdate], latestUpdate)[0].items).toEqual(latestUpdate.items);
    expect(mergeItemPropertyChange([firstUpdate], buildDeletedItemProperty(group, item))[0].items).toEqual([]);
    const added = buildAddedItemProperty(group, "2002", { itemId: "2002", itemName: "New item" });
    expect(removeAddedItemPropertyChange([added], "items", "2002")).toEqual([]);
  });

  it("reads child group render and save values through shared aliases", () => {
    const group = {
      PrpId: "lines",
      ItemName: "Order Lines",
      Properties: [
        { PrpId: "lineId", FmtValue: "", EditType: "ReadOnly" },
        { PrpId: "lineName", FmtValue: "" }
      ],
      Items: [
        {
          DataId: "9001",
          Values: [
            { PrpId: "lineId", ObjId: "9001", FmtValue: "9001", EditType: "ReadOnly" },
            { PrpId: "lineName", ObjId: "Old line", FmtValue: "Old line" }
          ]
        }
      ]
    };
    const item = group.Items[0];

    expect(groupKey(group)).toBe("lines");
    expect(groupTitle(group)).toBe("Order Lines");
    expect(groupItems(group)).toBe(group.Items);
    expect(itemDataId(item)).toBe("9001");
    expect(itemKey(group, item)).toBe("lines:9001");
    expect(buildAddedItemProperty(group, "9002", { lineId: "9002", lineName: "New line" })).toEqual({
      key: "lines",
      addedItems: [
        {
          itemId: "9002",
          isExist: true,
          propertyies: [{ key: "lineName", value: "New line" }]
        }
      ]
    });
    expect(buildUpdatedItemProperty(group, item, { lineId: "9001", lineName: "Updated line" })).toEqual({
      key: "lines",
      items: [
        {
          itemId: "9001",
          isExist: true,
          propertyies: [{ key: "lineName", value: "Updated line" }]
        }
      ]
    });
  });

  it("overrides both child-item aliases when a staged group becomes empty", () => {
    const group = { Items: [{ DataId: "2001" }] };
    expect(groupItems(withGroupItems(group, []))).toEqual([]);
  });

  it("builds existing child updates from rendered group columns instead of data DTO values", () => {
    const group = {
      prpId: "items",
      properties: [
        { prpId: "itemId", editType: "ReadOnly" },
        { prpId: "itemName" }
      ]
    };
    const item = {
      dataId: "2001",
      values: [
        { prpId: "itemId", objId: "2001", editType: "ReadOnly" },
        { prpId: "dtoOnly", objId: "leak" }
      ]
    };

    expect(buildUpdatedItemProperty(group, item, { itemName: "Updated item", dtoOnly: "leak" })).toEqual({
      key: "items",
      items: [
        {
          itemId: "2001",
          isExist: true,
          propertyies: [{ key: "itemName", value: "Updated item" }]
        }
      ]
    });
  });

  it("keeps child delete payloads to item id without data DTO propertyies", () => {
    const group = {
      prpId: "items",
      properties: [
        { prpId: "itemName" }
      ]
    };
    const item = {
      dataId: "2001",
      values: [
        { prpId: "itemName", objId: "Old item" },
        { prpId: "dtoOnly", objId: "leak" }
      ]
    };

    expect(buildDeletedItemProperty(group, item)).toEqual({
      key: "items",
      delteItems: [
        {
          itemId: "2001",
          isExist: true,
          propertyies: []
        }
      ]
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

  it("maps selected existing child rows from candidate View columns before DTO item keys", () => {
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
      items: [
        { prpId: "itemId", objId: "dto-id", fmtValue: "DTO id" },
        { prpId: "id", objId: "3001", fmtValue: "3001" },
        { prpId: "name", objId: "Existing item", fmtValue: "Existing item" }
      ]
    };

    expect(buildDraftsFromRow(group.properties, row, columns)).toEqual({
      itemId: "3001",
      itemName: "Existing item"
    });
    expect(buildSelectedExistingItemProperty(group, row, columns).addedItems?.[0].itemId).toBe("3001");
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
    expect(groupListViewId(group)).toBe(101);
    expect(groupSelectedViewId({ SelectedView: 201, ListViewId: 301 })).toBe(201);
    expect(groupListViewId({ SelectedView: 201, ListViewId: 301 })).toBe(301);
    expect(emptyGroupDraft(group)).toEqual({
      itemId: "",
      itemName: ""
    });
  });

  it("reads missing child drafts as empty and writes through a default draft", () => {
    const group = {
      prpId: "items",
      properties: [
        { prpId: "itemId", fmtValue: "" },
        { prpId: "itemName", fmtValue: "" }
      ]
    };

    expect(draftFieldValue({}, "items", group.properties[0])).toBe("");
    expect(withDraftFieldValue({}, "items", emptyGroupDraft(group), group.properties[0], "3001")).toEqual({
      items: {
        itemId: "3001",
        itemName: ""
      }
    });
    expect(withDraftFieldValue({ items: { itemId: "3001" } }, "items", emptyGroupDraft(group), group.properties[1], "New")).toEqual({
      items: {
        itemId: "3001",
        itemName: "New"
      }
    });
  });

  it("matches Pascal querydatadetail child groups to rendered read-item DetailViews", () => {
    const view = {
      DetailViews: [
        {
          Name: "Items",
          PrpId: "items",
          Items: [{ PrpId: "itemId", PrpShowName: "Item ID" }]
        }
      ]
    };
    const dataGroup = {
      PrpId: "items",
      ListViewId: 101,
      SelectedView: 103,
      Items: [{ DataId: "2001", Values: [{ PrpId: "itemId", ObjId: "2001" }] }]
    };

    const group = renderedDetailGroups(view, [dataGroup])[0];

    expect(groupKey(group)).toBe("items");
    expect(groupListViewId(group)).toBe(101);
    expect(groupSelectedViewId(group)).toBe(103);
    expect(group.items).toBe(dataGroup.Items);
    expect(groupColumns(group).map(fieldKey)).toEqual(["itemId"]);
  });

  it("reads select-existing flags from camel or Pascal child group aliases", () => {
    expect(groupSelectFromExists({ selectFromExists: true })).toBe(true);
    expect(groupSelectFromExists({ SelectFromExists: true })).toBe(true);
    expect(groupSelectFromExists({ selectFromExists: false, SelectFromExists: true })).toBe(false);
    expect(groupDetailViewId({ DetailViewId: 301 })).toBe(301);
    expect(groupDetailViewId({ detailViewId: 0, DetailViewId: 301 })).toBe(0);
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
    const open = {
      ID: 2,
      Name: "Open",
      RequireSelect: true,
      ViewID: 201,
      Params: [{ ID: 10, ParamId: 9001, ParamName: "Reason" }, { Name: "Fallback" }]
    };
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
    expect(viewDisplayName({ ViewName: "Order Detail" })).toBe("Order Detail");
    expect(viewDisplayTitle({ ViewName: "Order Detail" })).toBe("Order Detail");
    expect(viewDisplayType(view)).toBe("ListView");
    expect(viewInputCount(view)).toBe(1);
    expect(viewDetailViewId(view, 100)).toBe(300);
    expect(columnKey(viewColumns(view)[0])).toBe("orderId");
    expect(createOperations(viewOperations(view))).toEqual([create]);
    expect(rowOperations(viewOperations(view))).toEqual([open]);
    expect(operationTargetViewId(open)).toBe(201);
    expect(operationLabel(open)).toBe("Open");
    expect(operationParams(open)).toBe(open.Params);
    expect(operationParamKey(open.Params[0], 0)).toBe("10");
    expect(operationParamLabel(open.Params[0])).toBe("Reason");
    expect(operationParamKey(open.Params[1], 1)).toBe("Fallback");
    expect(dataOperations({ Operations: [open] })).toEqual([open]);
    expect(dataCanEdit({ CanEdit: true })).toBe(true);
    expect(dataCanEdit({ canEdit: false, CanEdit: true })).toBe(false);
    expect(dataCanEdit(undefined)).toBe(false);
  });

  it("reads legacy list paging from direct totals or pageInfo", () => {
    expect(listTotalItems({ totalItem: 8 })).toBe(8);
    expect(listTotalItems({ pageInfo: { total: 5 } })).toBe(5);
    expect(listTotalPages({ totalPage: 4 })).toBe(4);
    expect(listTotalPages({ pageInfo: { pageCount: 3 } })).toBe(3);
    expect(listPageIndex({ pageInfo: { pageIndex: 2 } }, 1)).toBe(2);
    expect(listAutoFreshTime({ autoFreshTime: 30 })).toBe(30);
    expect(listAutoFreshTime({ AutoFreshTime: 45 })).toBe(45);
    expect(listFreshTime({ freshTime: "2026-07-04T00:16:42" })).toBe(new Date("2026-07-04T00:16:42").toLocaleString());
    expect(listFreshTime({ FreshTime: "/Date(0)/" })).toBe(new Date(0).toLocaleString());
    expect(listFreshTime({ FreshTime: "server clock unavailable" })).toBe("server clock unavailable");
  });

  it("reads the default View id from legacy app shell payloads", () => {
    expect(legacyAppDefaultViewId({ app: { defaultViewId: 100 } })).toBe(100);
    expect(legacyAppDefaultViewId({ App: { DefaultViewId: 101 } })).toBe(101);
    expect(legacyAppDefaultViewId({ defaultViewId: 102 })).toBe(102);
    expect(legacyAppDefaultViewId({ App: { DefaultViewId: 0 } })).toBe(0);
    expect(legacyAppName({ App: { AppName: "Legacy App" } })).toBe("Legacy App");
    expect(legacyAppName({ app: { appName: "Camel App" } })).toBe("Camel App");
    expect(legacyAppVersion({ App: { AppVer: "2.1" } })).toBe("2.1");
  });

  it("reads the old FoolFrame Web list route View id", () => {
    expect(legacyViewPathId("/view100")).toBe(100);
    expect(legacyViewPathId("/view100/")).toBe(100);
    expect(legacyViewPathId("/view100/1001")).toBe(0);
    expect(legacyViewPathId("/new100")).toBe(0);
  });

  it("reads the old FoolFrame Web detail and new routes", () => {
    expect(legacyDetailPath("/view100/1001")).toEqual({ viewId: 100, objectId: "1001" });
    expect(legacyDetailPath("/view100")).toBeNull();
    expect(legacyDetailHref(100, "1001")).toBe("/view100/1001");
    expect(legacyDetailHref(0, "1001")).toBe("");
    expect(legacyItemViewPathId("/itemview100")).toBe(100);
    expect(legacyItemViewPathId("/itemview100/")).toBe(100);
    expect(legacyItemViewPathId("/view100")).toBe(0);
    expect(legacyNewPath("/new200")).toEqual({ viewId: 200, parentObjId: "", ownerViewId: "", property: "" });
    expect(legacyNewHref(200)).toBe("/new200");
    expect(legacyNewHref(0)).toBe("");
    expect(legacyChildNewHref(200, "1001", 100, "items")).toBe("/new200/1001&100&items");
    expect(legacyNewPath("/new200/1001&100&items")).toEqual({
      viewId: 200,
      parentObjId: "1001",
      ownerViewId: "100",
      property: "items"
    });
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
    const messages = { Messages: [{
      MessageID: "m1",
      GernerationTime: "/Date(0)/",
      MessageContent: "Ready",
      ResultView: 100,
      ResultKey: "1001"
    }] };
    const notifies = { Notifies: [{ AuthNo: "1", Count: 3 }, { AuthNo: "1", Count: 2 }] };
    const enums = { EnumValues: [{ Name: "Open", Value: 0 }] };
    const inputQuery = { Items: [{ Id: "1001", Text: "Ada" }] };

    expect(legacyMainMenuItems(main)).toHaveLength(1);
    expect(legacyAuthText(main.TopMenu[0])).toBe("Views");
    expect(legacyAuthImageUrl({ ImageUrl: "/menu/views.png" })).toBe("/menu/views.png");
    expect(legacySubMenuItems(menu)).toHaveLength(1);
    expect(legacyAuthNo(menu.Items[0])).toBe("1");
    expect(legacyAuthText(menu.Items[0])).toBe("Home");
    expect(legacyAuthViewId(menu.Items[0])).toBe(100);
    expect(legacyAuthIndex(menu.Items[0])).toBe(2);
    expect(legacyMessages(messages)).toHaveLength(1);
    expect(legacyMessageId(messages.Messages[0])).toBe("m1");
    expect(legacyMessageContent(messages.Messages[0])).toBe("Ready");
    expect(legacyMessageTime(messages.Messages[0])).toBe("1970-01-01 00:00:00");
    expect(legacyMessageResultView(messages.Messages[0])).toBe(100);
    expect(legacyMessageResultKey(messages.Messages[0])).toBe("1001");
    expect(legacyNotifies(notifies)).toHaveLength(2);
    expect(legacyNotifyAuthNo(notifies.Notifies[0])).toBe("1");
    expect(legacyNotifyCount(notifies.Notifies[0])).toBe(3);
    expect(legacyNotifyCountForAuth(notifies.Notifies, "1")).toBe(5);
    expect(legacyUserName({ User: { UserName: "Admin", LoginName: "admin" } })).toBe("Admin");
    expect(legacyUserAvatar({ User: { UserAvtarUrl: "/avatars/admin.png" } })).toBe("/avatars/admin.png");
    expect(legacyLoginErrorMessage({ Error: { Message: "Invalid login" } })).toBe("Invalid login");
    expect(legacyEnumValues(enums)).toHaveLength(1);
    expect(legacyEnumName(enums.EnumValues[0])).toBe("Open");
    expect(legacyEnumValue(enums.EnumValues[0])).toBe("0");
    expect(legacyInputQueryItems(inputQuery)).toHaveLength(1);
    expect(inputQueryItemId(inputQuery.Items[0])).toBe("1001");
    expect(inputQueryItemText(inputQuery.Items[0])).toBe("Ada");
    expect(inputQueryItemId(undefined as never)).toBe("");
    expect(inputQueryItemText(undefined as never)).toBe("");
  });

  it("reads runoperation success from camel or legacy result fields", () => {
    expect(legacyRunOperationSuccess({ success: true })).toBe(true);
    expect(legacyRunOperationSuccess({ IsSuccess: true })).toBe(true);
    expect(legacyRunOperationSuccess({ success: false, IsSuccess: false })).toBe(false);
    expect(legacyRunOperationMessage({ ReturnMsg: "执行成功" })).toBe("执行成功");
    expect(legacyRunOperationMessage({ returnMsg: "Done", ReturnMsg: "Legacy" })).toBe("Done");
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
    expect(reportGridPage({ CurrentPage: 2 }, 1)).toBe(2);
    expect(reportGridTotalPages({ TotalPages: 3 })).toBe(3);
    expect(reportGridTotalRecords({ TotalRecords: 21 })).toBe(21);
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
    expect(reportModelStateValue(reportModelStates(col)[0])).toBe("0");
    expect(buildReportColsFromModel(reportModelColumns(pascal))).toEqual([
      { colName: "Amount[原值]", colId: "1004", selectedTypeId: "1", index: 0, orderType: "2" }
    ]);
  });

});
