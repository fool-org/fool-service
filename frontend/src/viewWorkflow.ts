import type {
  ListDataItem,
  ListDataValue,
  ListViewInfo,
  ListViewResult,
  OperationInfo,
  QueryDataDetailDataItem,
  QueryDataDetailItemGroup,
  ReportCell,
  ReportCol,
  ReportModelColumn,
  SaveItemProperty,
  SaveKeypair,
  TableColumnInfo
} from "./api";

export function columnKey(column: TableColumnInfo) {
  return column.property || column.propertyName || column.name || String(column.id || "");
}

export function columnTitle(column: TableColumnInfo) {
  return column.title || column.name || column.propertyName || column.property || String(column.id || "");
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
        editType: field.editType ?? field.EditType,
        propertyType: field.prpType ?? field.PrpType,
        propertyModel: field.prpModelId ?? field.PrpModelId
      };
    })
    .filter((column) => column.property || column.title);
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
  return operations.filter((operation) => operation.requireSelect === false && Number(operation.viewId || 0) > 0);
}

export function rowOperations(operations: OperationInfo[] = []) {
  return operations.filter((operation) => operation.requireSelect === true);
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

export function listRows(result?: ListViewResult) {
  return firstList(result?.items, result?.data, result?.Data);
}

export function viewDetailViewId(view: ListViewInfo | undefined, fallback = 0) {
  return Number(view?.detailViewId || fallback || 0);
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

export function buildReportColsFromModel(cols: ReportModelColumn[] = []): ReportCol[] {
  return cols
    .map((col, index) => {
      const queryType = col.queryTypes?.[0];
      const name = col.name || col.id || "";
      const queryName = queryType?.name || queryType?.id || "";
      return {
        colName: queryName ? `${name}[${queryName}]` : name,
        colId: col.id,
        selectedTypeId: queryType?.id,
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
    column.property,
    column.name,
    column.title,
    column.propertyId,
    column.id
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

function firstList<T>(...lists: Array<T[] | undefined>): T[] {
  return lists.find((list) => Array.isArray(list) && list.length > 0) ?? lists.find(Array.isArray) ?? [];
}

function firstDisplayValue(values: unknown[]) {
  const value = values.find((item) => displayValue(item) !== "");
  return displayValue(value);
}
