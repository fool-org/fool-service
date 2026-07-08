import type {
  CheckCodeResult,
  GetEnumResult,
  GetEnumValue,
  GetMessageResult,
  GetNotifyResult,
  InputQueryItem,
  InputQueryResult,
  LegacyAuthItem,
  LegacyInitAppResult,
  LegacySubMenuResult,
  ListDataItem,
  ListDataValue,
  ListViewInfo,
  ListViewResult,
  OperationInfo,
  MessageInfo,
  NotifyInfo,
  QueryDataDetailDataItem,
  QueryDataDetailItemGroup,
  QueryDataDetailResult,
  ReadItemViewInfo,
  ReadItemViewItemInfo,
  ReportCell,
  ReportCol,
  ReportModelColumn,
  ReportModelOption,
  ReportModelResult,
  ReportModelState,
  SaveItemProperty,
  SaveKeypair,
  TableColumnInfo
} from "./api";

export function columnKey(column: TableColumnInfo) {
  return firstDisplayValue([
    column.property,
    column.propertyName,
    column.PropertyName,
    column.name,
    column.Name,
    column.id,
    column.ID
  ]);
}

export function columnTitle(column: TableColumnInfo) {
  return firstDisplayValue([
    column.title,
    column.name,
    column.Name,
    column.propertyName,
    column.PropertyName,
    column.property,
    column.id,
    column.ID
  ]);
}

export function rowObjectId(row: ListDataItem, _columns: TableColumnInfo[] = []) {
  const items = rowItems(row);
  const firstItemWithId = items.find((item) => displayValue(valueObjId(item)));
  return firstDisplayValue([row.id, row.Id, firstItemWithId && valueObjId(firstItemWithId), valueFmtValue(items[0])]);
}

export function rowRenderKey(row: ListDataItem, index = 0) {
  return rowObjectId(row) || firstDisplayValue([row.rowIndex, row.RowIndex]) || String(index);
}

export function rowValue(row: ListDataItem, column: TableColumnInfo) {
  const item = rowItemForColumn(row, column);
  return firstDisplayValue([item && valueFmtValue(item), item && valueObjId(item)]);
}

export function rowFormatClass(row: ListDataItem) {
  return displayValue(row.rowFmt ?? row.RowFmt).trim();
}

export function fieldKey(field: ListDataValue) {
  return field.prpId || field.PrpId || field.prpShowName || field.PrpShowName || "";
}

export function fieldTitle(field: ListDataValue) {
  return field.prpShowName || field.PrpShowName || field.prpId || field.PrpId || "";
}

export function fieldType(field: ListDataValue) {
  return field.prpType ?? field.PrpType;
}

export function fieldEditType(field: ListDataValue) {
  return field.editType ?? field.EditType;
}

export function fieldDisplayValue(field: ListDataValue) {
  return firstDisplayValue([field.fmtValue, field.FmtValue, field.objId, field.ObjId]);
}

export function readViewId(view: ReadItemViewInfo | undefined, fallback = 0) {
  return Number(view?.viewId ?? view?.ViewId ?? fallback) || 0;
}

export function legacyAppDefaultViewId(source?: unknown) {
  const record = objectRecord(source);
  const app = objectRecord(record.app || record.App || source);
  return Number(app.defaultViewId ?? app.DefaultViewId ?? 0) || 0;
}

export function legacyInitAppCheckCode(source?: LegacyInitAppResult) {
  return source?.checkCode ?? source?.CheckCode;
}

export function legacyInitAppDbId(source?: LegacyInitAppResult) {
  const db = firstList(source?.dbs, source?.Dbs)[0];
  return firstDisplayValue([db?.dbId, db?.DbId]);
}

export function legacyCheckCodeKey(source?: CheckCodeResult) {
  return firstDisplayValue([source?.key, source?.Key]);
}

export function legacyCheckCodeCode(source?: CheckCodeResult) {
  return firstDisplayValue([source?.code, source?.Code]);
}

export function legacyCheckCodeImage(source?: CheckCodeResult) {
  return firstDisplayValue([source?.chkCodeImg, source?.ChkCodeImg]);
}

export function legacySubMenuItems(source?: LegacySubMenuResult) {
  return firstList(source?.items, source?.Items);
}

export function legacyAuthText(item: LegacyAuthItem) {
  return firstDisplayValue([item.text, item.Text]);
}

export function legacyAuthNo(item: LegacyAuthItem) {
  return firstDisplayValue([item.authNo, item.AuthNo]);
}

export function legacyAuthViewId(item: LegacyAuthItem) {
  return Number(item.viewId ?? item.ViewId ?? 0) || 0;
}

export function legacyAuthIndex(item: LegacyAuthItem) {
  return Number(item.index ?? item.Index ?? 0) || 0;
}

export function legacyMessages(source?: GetMessageResult) {
  return firstList(source?.messages, source?.Messages);
}

export function legacyMessageId(message: MessageInfo) {
  return firstDisplayValue([message.messageID, message.MessageID]);
}

export function legacyMessageContent(message: MessageInfo) {
  return firstDisplayValue([message.messageContent, message.MessageContent]);
}

export function legacyMessageResultView(message: MessageInfo) {
  return Number(message.resultView ?? message.ResultView ?? 0) || 0;
}

export function legacyMessageResultKey(message: MessageInfo) {
  return firstDisplayValue([message.resultKey, message.ResultKey]);
}

export function legacyNotifies(source?: GetNotifyResult) {
  return firstList(source?.notifies, source?.Notifies);
}

export function legacyNotifyAuthNo(item: NotifyInfo) {
  return firstDisplayValue([item.authNo, item.AuthNo]);
}

export function legacyNotifyCount(item: NotifyInfo) {
  return Number(item.count ?? item.Count ?? 0) || 0;
}

export function legacyEnumValues(source?: GetEnumResult) {
  return firstList(source?.enumValues, source?.EnumValues);
}

export function legacyEnumName(item: GetEnumValue) {
  return firstDisplayValue([item.name, item.Name]);
}

export function legacyEnumValue(item: GetEnumValue) {
  return firstDisplayValue([item.value, item.Value]);
}

export function legacyInputQueryItems(source?: InputQueryResult) {
  return firstList(source?.items, source?.Items);
}

export function inputQueryItemId(item: InputQueryItem) {
  return firstDisplayValue([item.id, item.Id]);
}

export function inputQueryItemText(item: InputQueryItem) {
  return firstDisplayValue([item.text, item.Text]);
}

export function readViewItems(view: ReadItemViewInfo | undefined) {
  return firstList(view?.items, view?.Items);
}

export function readViewDetailViews(view: ReadItemViewInfo | undefined) {
  return firstList(view?.detailViews, view?.DetailViews);
}

export function readViewFields(view: ReadItemViewInfo | undefined) {
  return detailFieldsFromReadView(view, []);
}

export function detailFieldsFromReadView(
  view: ReadItemViewInfo | undefined,
  dataFields: ListDataValue[] = []
): ListDataValue[] {
  const metadata = readViewItems(view);
  if (!metadata.length) {
    return dataFields;
  }
  const valuesByKey = new Map<string, ListDataValue>();
  for (const field of dataFields) {
    const key = fieldKey(field);
    if (key) {
      valuesByKey.set(key, field);
    }
  }
  return metadata.map((item) => {
    const field = fieldFromReadItem(item);
    return mergeFieldValue(field, valuesByKey.get(fieldKey(field)));
  });
}

export function detailGroupsFromReadView(
  view: ReadItemViewInfo | undefined,
  dataGroups: QueryDataDetailItemGroup[] = []
): QueryDataDetailItemGroup[] {
  const detailViews = readViewDetailViews(view);
  if (!detailViews.length) {
    return dataGroups;
  }

  const dataGroupsByKey = new Map<string, QueryDataDetailItemGroup>();
  for (const group of dataGroups) {
    for (const key of detailGroupKeys(group)) {
      dataGroupsByKey.set(key, group);
    }
  }

  const used = new Set<QueryDataDetailItemGroup>();
  const groups: QueryDataDetailItemGroup[] = detailViews.map((detail) => {
    const field = fieldFromReadItem(detail);
    const dataGroup = readDetailKeys(detail, field)
      .map((key) => dataGroupsByKey.get(key))
      .find((group): group is QueryDataDetailItemGroup => Boolean(group));
    if (dataGroup) {
      used.add(dataGroup);
    }
    const properties = firstList(detail.items, detail.Items).map(fieldFromReadItem);
    return {
      ...dataGroup,
      name: firstDisplayValue([detail.name, detail.Name, dataGroup?.name]),
      itemName: firstDisplayValue([detail.name, detail.Name, dataGroup?.itemName]),
      prpId: fieldKey(field) || dataGroup?.prpId,
      properties: properties.length ? properties : dataGroup?.properties || [],
      items: dataGroup?.items || []
    };
  });

  return groups.concat(dataGroups.filter((group) => !used.has(group)));
}

export function detailResultSimpleData(result: QueryDataDetailResult | undefined) {
  const data = detailResultData(result);
  return firstList(data?.simpleData, data?.SimpleData);
}

export function detailResultItems(result: QueryDataDetailResult | undefined) {
  const data = detailResultData(result);
  return firstList(data?.items, data?.Items);
}

export function columnsFromRowItems(row: ListDataItem | undefined): TableColumnInfo[] {
  return rowItems(row)
    .map((field) => {
      const key = fieldKey(field);
      return {
        property: key,
        propertyName: key,
        title: fieldTitle(field),
        name: fieldTitle(field),
        isReadOnly: field.readOnly ?? field.ReadOnly,
        editType: fieldEditType(field),
        propertyType: fieldType(field),
        propertyModel: field.prpModelId ?? field.PrpModelId
      };
    })
    .filter((column) => column.property || column.title);
}

export function columnsFromListResult(result: ListViewResult | undefined): TableColumnInfo[] {
  return firstList(result?.cols, result?.Cols)
    .map((col, index) => {
      const title = displayValue(col);
      return {
        id: index,
        property: title,
        propertyName: title,
        title,
        name: title
      };
    })
    .filter((column) => column.title);
}

export function fieldModelId(field: ListDataValue) {
  return field.prpModelId ?? field.PrpModelId ?? 0;
}

export function isEnumField(field: ListDataValue) {
  return String(field.prpType ?? field.PrpType ?? "").toLowerCase() === "enum" && fieldModelId(field) > 0;
}

export function isLookupField(field: ListDataValue) {
  const type = String(field.prpType ?? field.PrpType ?? "").toLowerCase();
  return (type === "businessobject" || type === "16") && fieldModelId(field) > 0;
}

export function isReadonlyField(field: ListDataValue) {
  return field.readOnly === true || field.ReadOnly === true || String(field.editType ?? field.EditType ?? "").toLowerCase() === "readonly";
}

export function fieldDraftValue(field: ListDataValue) {
  const objId = valueObjId(field);
  const value = objId === undefined || objId === null || objId === "" ? valueFmtValue(field) : objId;
  return displayValue(value);
}

export function buildFieldDrafts(fields: ListDataValue[]) {
  return fields.reduce<Record<string, string>>((drafts, field) => {
    const key = fieldKey(field);
    if (key) {
      drafts[key] = fieldDraftValue(field);
    }
    return drafts;
  }, {});
}

export function buildSavePropertyies(fields: ListDataValue[], drafts: Record<string, string>): SaveKeypair[] {
  return fields
    .filter((field) => !isReadonlyField(field))
    .map((field) => fieldKey(field))
    .filter(Boolean)
    .map((key) => ({ key, value: drafts[key] ?? "" }));
}

export function buildItemDrafts(groups: QueryDataDetailItemGroup[]) {
  const drafts: Record<string, Record<string, string>> = {};
  for (const group of groups) {
    for (const item of group.items || []) {
      drafts[itemKey(group, item)] = buildFieldDrafts(detailItemValues(item));
    }
  }
  return drafts;
}

export function itemKey(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
  return `${group.prpId || group.name || "items"}:${item.dataId || item.DataId || ""}`;
}

export function groupColumns(group: QueryDataDetailItemGroup) {
  return group.properties?.length ? group.properties : detailItemValues(group.items?.[0]);
}

export function itemValue(item: QueryDataDetailDataItem, field: ListDataValue) {
  const key = fieldKey(field);
  const value = detailItemValues(item).find((itemValue) => fieldKey(itemValue) === key);
  return value ? valueFmtValue(value) : "";
}

export function detailItemValues(item: QueryDataDetailDataItem | undefined) {
  return firstList(item?.values, item?.Values);
}

export function groupKey(group: QueryDataDetailItemGroup) {
  return group.prpId || group.name || "items";
}

export function selectedChildViewId(group: QueryDataDetailItemGroup) {
  return group.selectedView || group.listViewId || 0;
}

export function createOperations(operations: OperationInfo[] = []) {
  return operations.filter((operation) => operationRequiresSelect(operation) === false && operationTargetViewId(operation) > 0);
}

export function rowOperations(operations: OperationInfo[] = []) {
  return operations.filter((operation) => operationRequiresSelect(operation) === true);
}

export function operationId(operation: OperationInfo) {
  return operation.id ?? operation.ID ?? 0;
}

export function operationKey(operation: OperationInfo) {
  return firstDisplayValue([operationId(operation), operation.name, operation.Name, operationTargetViewId(operation), "operation"]);
}

export function operationLabel(operation: OperationInfo) {
  return operation.text || operation.name || operation.Name || `Operation ${operationId(operation) || operationTargetViewId(operation)}`;
}

export function operationTargetViewId(operation: OperationInfo) {
  return Number(operation.viewId ?? operation.ViewID ?? 0) || 0;
}

export function viewColumns(view: ListViewInfo | undefined) {
  return firstList(view?.tableColumn, view?.Items);
}

export function viewId(view: ListViewInfo | undefined, fallback = 0) {
  return Number(view?.id ?? view?.ID ?? fallback) || 0;
}

export function viewOperations(view: ListViewInfo | undefined) {
  return firstList(view?.operations, view?.Operations);
}

export function dataOperations(result: QueryDataDetailResult | undefined) {
  return firstList(result?.operations, result?.Operations);
}

export function listTotalItems(result?: ListViewResult) {
  return result?.totalItem ?? result?.TotalItem ?? result?.pageInfo?.total ?? 0;
}

export function listTotalPages(result?: ListViewResult) {
  return result?.totalPage ?? result?.TotalPage ?? result?.pageInfo?.pageCount ?? 0;
}

export function listPageIndex(result: ListViewResult | undefined, fallback = 1) {
  return result?.pageIndex ?? result?.PageIndex ?? result?.pageInfo?.pageIndex ?? fallback;
}

export function listAutoFreshTime(result?: ListViewResult) {
  return Number(result?.autoFreshTime ?? result?.AutoFreshTime ?? 0) || 0;
}

export function listFreshTime(result?: ListViewResult) {
  return displayValue(result?.freshTime ?? result?.FreshTime);
}

export function listRows(result?: ListViewResult) {
  return firstList(result?.items, result?.data, result?.Data);
}

export function viewDetailViewId(view: ListViewInfo | undefined, fallback = 0) {
  return Number(view?.detailViewId ?? view?.DetailViewId ?? 0) || Number(fallback || 0);
}

export function reportRowsFromCells(cells: ReportCell[] = []) {
  const maxRow = cells.reduce((max, cell) => Math.max(max, cell.row), -1);
  const maxCol = cells.reduce((max, cell) => Math.max(max, cell.col), -1);
  if (maxRow < 0 || maxCol < 0) {
    return [];
  }

  const byPosition = new Map(cells.map((cell) => [`${cell.row}:${cell.col}`, cell.fmtValue || ""]));
  return Array.from({ length: maxRow + 1 }, (_, row) =>
    Array.from({ length: maxCol + 1 }, (_, col) => byPosition.get(`${row}:${col}`) || "")
  );
}

export function reportModelColumns(result: ReportModelResult | undefined) {
  return firstList(result?.cols, result?.Cols);
}

export function reportModelColumnId(col: ReportModelColumn) {
  return firstDisplayValue([col.id, col.ID]);
}

export function reportModelColumnName(col: ReportModelColumn) {
  return firstDisplayValue([col.name, col.Name, reportModelColumnId(col)]);
}

export function reportModelColumnType(col: ReportModelColumn) {
  return firstDisplayValue([col.prpType, col.PrpType]);
}

export function reportModelCompareTypes(col: ReportModelColumn) {
  return firstList(col.compareTypes, col.CompareTypes);
}

export function reportModelQueryTypes(col: ReportModelColumn) {
  return firstList(col.queryTypes, col.QueryTypes);
}

export function reportModelStates(col: ReportModelColumn) {
  return firstList(col.states, col.States);
}

export function reportModelOptionId(option: ReportModelOption) {
  return firstDisplayValue([option.id, option.ID]);
}

export function reportModelOptionName(option: ReportModelOption) {
  return firstDisplayValue([option.name, option.Name, reportModelOptionId(option)]);
}

export function reportModelStateText(state: ReportModelState) {
  return firstDisplayValue([state.showName, state.ShowName, state.dbName, state.DBName]);
}

export function buildReportColsFromModel(cols: ReportModelColumn[] = []): ReportCol[] {
  return cols
    .map((col, index) => {
      const queryType = reportModelQueryTypes(col)[0];
      const name = reportModelColumnName(col);
      const queryName = queryType ? reportModelOptionName(queryType) : "";
      return {
        colName: queryName ? `${name}[${queryName}]` : name,
        colId: reportModelColumnId(col),
        selectedTypeId: queryType && reportModelOptionId(queryType),
        index,
        orderType: "2"
      };
    })
    .filter((col) => col.colName || col.colId);
}

export function recordColumns(rows: Record<string, unknown>[] = []) {
  const columns: string[] = [];
  const seen = new Set<string>();
  for (const row of rows) {
    for (const key of Object.keys(row)) {
      if (!seen.has(key)) {
        seen.add(key);
        columns.push(key);
      }
    }
  }
  return columns;
}

export function recordRowKey(row: Record<string, unknown>, columns: string[] = [], index = 0) {
  const id = row.id ?? row.ID ?? row.Id;
  if (id !== undefined && id !== null && id !== "") {
    return displayValue(id);
  }
  const firstValue = columns.map((column) => row[column]).find((value) => value !== undefined && value !== null && value !== "");
  return firstValue === undefined ? String(index) : displayValue(firstValue);
}

export function emptyGroupDraft(group: QueryDataDetailItemGroup) {
  return groupColumns(group).reduce<Record<string, string>>((drafts, field) => {
    const key = fieldKey(field);
    if (key) {
      drafts[key] = "";
    }
    return drafts;
  }, {});
}

export function buildItemPropertyies(
  fields: ListDataValue[],
  drafts: Record<string, string>,
  includeReadonly = false
): SaveKeypair[] {
  return fields
    .filter((field) => includeReadonly || !isReadonlyField(field))
    .map((field) => fieldKey(field))
    .filter(Boolean)
    .map((key) => ({ key, value: drafts[key] ?? "" }));
}

export function buildUpdatedItemProperty(
  group: QueryDataDetailItemGroup,
  item: QueryDataDetailDataItem,
  drafts: Record<string, string>
): SaveItemProperty {
  return {
    key: group.prpId || "items",
    items: [
      {
        itemId: item.dataId || item.DataId,
        isExist: true,
        propertyies: buildItemPropertyies(detailItemValues(item), drafts)
      }
    ]
  };
}

export function buildDeletedItemProperty(
  group: QueryDataDetailItemGroup,
  item: QueryDataDetailDataItem
): SaveItemProperty {
  return {
    key: group.prpId || "items",
    delteItems: [
      {
        itemId: item.dataId || item.DataId,
        isExist: true,
        propertyies: buildItemPropertyies(detailItemValues(item), buildFieldDrafts(detailItemValues(item)))
      }
    ]
  };
}

export function buildAddedItemProperty(
  group: QueryDataDetailItemGroup,
  itemId: string,
  drafts: Record<string, string>,
  includeReadonly = false
): SaveItemProperty {
  return {
    key: group.prpId || "items",
    addedItems: [
      {
        itemId,
        isExist: true,
        propertyies: buildItemPropertyies(group.properties || [], drafts, includeReadonly)
      }
    ]
  };
}

export function buildDraftsFromRow(fields: ListDataValue[], row: ListDataItem, columns: TableColumnInfo[] = []) {
  const items = rowItems(row);
  return fields.reduce<Record<string, string>>((drafts, field, index) => {
    const key = fieldKey(field);
    if (!key) {
      return drafts;
    }
    const byKey = items.find((item) => fieldKey(item) === key);
    const byColumn = columns[index] ? rowItemForColumn(row, columns[index]) : undefined;
    const byIndex = items[index];
    drafts[key] = firstDisplayValue([
      byKey && valueObjId(byKey),
      byKey && valueFmtValue(byKey),
      byColumn && valueObjId(byColumn),
      byColumn && valueFmtValue(byColumn),
      byIndex && valueObjId(byIndex),
      byIndex && valueFmtValue(byIndex)
    ]);
    return drafts;
  }, {});
}

export function buildSelectedExistingItemProperty(
  group: QueryDataDetailItemGroup,
  row: ListDataItem,
  columns: TableColumnInfo[] = []
): SaveItemProperty {
  const itemId = rowObjectId(row, columns);
  return buildAddedItemProperty(group, itemId, buildDraftsFromRow(group.properties || [], row, columns), true);
}

export function displayValue(value: unknown) {
  if (value === null || value === undefined) {
    return "";
  }
  if (typeof value === "object") {
    return JSON.stringify(value);
  }
  return String(value);
}

function rowItemForColumn(row: ListDataItem, column: TableColumnInfo) {
  const keys = new Set(columnMatchKeys(column));
  return rowItems(row).find((item) => rowItemKeys(item).some((key) => keys.has(key)));
}

function columnMatchKeys(column: TableColumnInfo) {
  return [
    column.propertyName,
    column.PropertyName,
    column.property,
    column.name,
    column.Name,
    column.title,
    column.propertyId,
    column.PropertyId,
    column.id,
    column.ID
  ].map(displayValue).filter(Boolean);
}

function rowItemKeys(item: ListDataValue) {
  return [item.prpId, item.PrpId, item.prpShowName, item.PrpShowName].map(displayValue).filter(Boolean);
}

function rowItems(row: ListDataItem | undefined) {
  return firstList(row?.items, row?.Items);
}

function valueObjId(value: ListDataValue | undefined) {
  return value?.objId ?? value?.ObjId;
}

function valueFmtValue(value: ListDataValue | undefined) {
  return value?.fmtValue ?? value?.FmtValue;
}

function fieldFromReadItem(item: ReadItemViewItemInfo): ListDataValue {
  const prpId = firstDisplayValue([item.prpId, item.PrpId, item.id, item.ID]);
  const prpShowName = firstDisplayValue([item.prpShowName, item.PrpShowName, item.name, item.Name, prpId]);
  return {
    prpId,
    PrpId: prpId,
    prpShowName,
    PrpShowName: prpShowName,
    prpType: item.prpType ?? item.PrpType,
    PrpType: item.PrpType ?? item.prpType,
    prpModelId: item.prpModelId ?? item.PrpModelId,
    PrpModelId: item.PrpModelId ?? item.prpModelId,
    readOnly: item.readOnly ?? item.ReadOnly,
    ReadOnly: item.ReadOnly ?? item.readOnly,
    editType: item.editType ?? item.EditType,
    EditType: item.EditType ?? item.editType
  };
}

function mergeFieldValue(field: ListDataValue, value: ListDataValue | undefined): ListDataValue {
  if (!value) {
    return field;
  }
  const objId = valueObjId(value);
  const fmtValue = valueFmtValue(value);
  return {
    ...field,
    objId,
    ObjId: objId,
    fmtValue,
    FmtValue: fmtValue
  };
}

function detailGroupKeys(group: QueryDataDetailItemGroup) {
  return normalizeKeys([group.prpId, group.name, group.itemName]);
}

function readDetailKeys(detail: ReadItemViewItemInfo, field: ListDataValue) {
  return normalizeKeys([
    field.prpId,
    field.PrpId,
    field.prpShowName,
    field.PrpShowName,
    detail.name,
    detail.Name,
    detail.id,
    detail.ID
  ]);
}

function normalizeKeys(values: unknown[]) {
  const keys = new Set<string>();
  for (const value of values) {
    const text = displayValue(value);
    if (text) {
      keys.add(text);
      keys.add(text.toLowerCase());
    }
  }
  return [...keys];
}

function objectRecord(value: unknown): Record<string, unknown> {
  return value && typeof value === "object" ? value as Record<string, unknown> : {};
}

function operationRequiresSelect(operation: OperationInfo) {
  return operation.requireSelect ?? operation.RequireSelect;
}

function detailResultData(result: QueryDataDetailResult | undefined) {
  return result?.data ?? result?.Data;
}

function firstList<T>(...lists: Array<T[] | undefined>): T[] {
  return lists.find((list) => Array.isArray(list) && list.length > 0) ?? lists.find(Array.isArray) ?? [];
}

function firstDisplayValue(values: unknown[]) {
  const value = values.find((item) => displayValue(item) !== "");
  return displayValue(value);
}
