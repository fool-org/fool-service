import type {
  ListDataItem,
  ListDataValue,
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

export function rowObjectId(row: ListDataItem, columns: TableColumnInfo[] = []) {
  const firstColumnKey = columns[0] ? columnKey(columns[0]) : "";
  return displayValue(row.id || (firstColumnKey ? row.values?.[firstColumnKey] : "") || row.items?.[0]?.objId || row.rowIndex);
}

export function rowValue(row: ListDataItem, column: TableColumnInfo) {
  const key = columnKey(column);
  return row.items?.find((item) => item.prpId === key)?.fmtValue || displayValue(row.values?.[key]);
}

export function rowFormatClass(row: ListDataItem) {
  return displayValue(row.rowFmt).trim();
}

export function fieldKey(field: ListDataValue) {
  return field.prpId || field.prpShowName || "";
}

export function fieldTitle(field: ListDataValue) {
  return field.prpShowName || field.prpId || "";
}

export function fieldModelId(field: ListDataValue) {
  return field.prpModelId || 0;
}

export function isEnumField(field: ListDataValue) {
  return String(field.prpType || "").toLowerCase() === "enum" && fieldModelId(field) > 0;
}

export function isLookupField(field: ListDataValue) {
  const type = String(field.prpType || "").toLowerCase();
  return (type === "businessobject" || type === "16") && fieldModelId(field) > 0;
}

export function isReadonlyField(field: ListDataValue) {
  return field.readOnly === true || String(field.editType || "").toLowerCase() === "readonly";
}

export function fieldDraftValue(field: ListDataValue) {
  const value = field.objId === undefined || field.objId === null || field.objId === "" ? field.fmtValue : field.objId;
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
      drafts[itemKey(group, item)] = buildFieldDrafts(item.values || []);
    }
  }
  return drafts;
}

export function itemKey(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
  return `${group.prpId || group.name || "items"}:${item.dataId || ""}`;
}

export function groupColumns(group: QueryDataDetailItemGroup) {
  return group.properties?.length ? group.properties : group.items?.[0]?.values || [];
}

export function itemValue(item: QueryDataDetailDataItem, field: ListDataValue) {
  const key = fieldKey(field);
  return item.values?.find((value) => fieldKey(value) === key)?.fmtValue || "";
}

export function groupKey(group: QueryDataDetailItemGroup) {
  return group.prpId || group.name || "items";
}

export function selectedChildViewId(group: QueryDataDetailItemGroup) {
  return group.selectedView || group.listViewId || 0;
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
        itemId: item.dataId,
        isExist: true,
        propertyies: buildItemPropertyies(item.values || [], drafts)
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
        itemId: item.dataId,
        isExist: true,
        propertyies: buildItemPropertyies(item.values || [], buildFieldDrafts(item.values || []))
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
  return fields.reduce<Record<string, string>>((drafts, field, index) => {
    const key = fieldKey(field);
    if (!key) {
      return drafts;
    }
    const byKey = row.items?.find((item) => fieldKey(item) === key);
    const byIndex = row.items?.[index];
    const columnKeyAtIndex = columns[index] ? columnKey(columns[index]) : "";
    drafts[key] = byKey?.objId || byKey?.fmtValue || (columnKeyAtIndex ? displayValue(row.values?.[columnKeyAtIndex]) : "") || byIndex?.objId || byIndex?.fmtValue || "";
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
