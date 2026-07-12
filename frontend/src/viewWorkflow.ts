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
  LegacyLoginResult,
  LegacyMainResult,
  LegacyRunOperationResult,
  LegacySubMenuResult,
  LegacyUserInfoResult,
  ListDataItem,
  ListDataValue,
  ListViewInfo,
  ListViewResult,
  OperationInfo,
  OperationParamInfo,
  MessageInfo,
  NotifyInfo,
  QueryDataDetailDataItem,
  QueryDataDetailItemGroup,
  QueryDataDetailResult,
  ReadItemViewInfo,
  ReadItemViewItemInfo,
  ReportCell,
  ReportCol,
  ReportGridResult,
  ReportModelColumn,
  ReportModelOption,
  ReportModelResult,
  ReportModelState,
  SaveItem,
  SaveItemProperty,
  SaveKeypair,
  TableColumnInfo
} from "./api";

type ViewDisplaySource = Pick<ListViewInfo, "viewName" | "ViewName" | "name" | "Name" | "viewTitle">;

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

export function rowObjectId(row: ListDataItem, columns: TableColumnInfo[] = []) {
  const items = rowItems(row);
  const firstColumnItemWithId = columns
    .map((column) => rowItemForColumn(row, column))
    .find((item) => displayValue(valueObjId(item)));
  const firstItemWithId = items.find((item) => displayValue(valueObjId(item)));
  return firstDisplayValue([
    row.id,
    row.Id,
    firstColumnItemWithId && valueObjId(firstColumnItemWithId),
    firstItemWithId && valueObjId(firstItemWithId),
    valueFmtValue(items[0])
  ]);
}

export function rowRenderKey(row: ListDataItem, index = 0, columns: TableColumnInfo[] = []) {
  return rowObjectId(row, columns) || firstDisplayValue([row.rowIndex, row.RowIndex]) || String(index);
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

export function fieldTitle(field: Partial<ListDataValue & TableColumnInfo>) {
  return firstDisplayValue([
    field.prpShowName,
    field.PrpShowName,
    field.name,
    field.Name,
    field.prpId,
    field.PrpId
  ]);
}

export function fieldType(field: ListDataValue) {
  return field.prpType ?? field.PrpType;
}

const numberFieldTypes = new Set([
  "identifyid", "int", "uint", "long", "ulong", "float", "double", "decimal", "byte",
  "0", "1", "2", "3", "4", "5", "6", "7", "10"
]);

export function fieldInputType(field: ListDataValue) {
  const type = normalizedPropertyType(field);
  if (type === "boolean" || type === "8") return "checkbox";
  if (type === "date" || type === "12") return "date";
  if (type === "time" || type === "13") return "time";
  if (type === "datetime" || type === "14") return "datetime-local";
  if (numberFieldTypes.has(type)) return "number";
  if (type) return "text";

  const editType = normalizedEditType(field);
  if (editType === "checkbox" || editType === "2") return "checkbox";
  if (editType === "datepicker" || editType === "6") return "date";
  if (editType === "timepicker" || editType === "7") return "time";
  if (editType === "datetimepicker" || editType === "8") return "datetime-local";
  return "text";
}

export function fieldInputChecked(field: ListDataValue, value: string) {
  if (fieldInputType(field) !== "checkbox") return false;
  const text = String(value ?? "").trim().toLowerCase();
  return text === "true" || text === "1";
}

export function fieldInputValue(field: ListDataValue, value: string) {
  const text = String(value ?? "");
  if (fieldInputType(field) !== "datetime-local") return text;
  const match = text.trim().match(/^(\d{4}-\d{2}-\d{2})[ T](\d{2}:\d{2}(?::\d{2})?)(?:\.\d+)?$/);
  return match ? `${match[1]}T${match[2]}` : text;
}

export function fieldEditType(field: ListDataValue) {
  return field.editType ?? field.EditType;
}

function normalizedEditType(field: ListDataValue) {
  return String(fieldEditType(field) ?? "").toLowerCase();
}

function normalizedPropertyType(field: ListDataValue) {
  return String(fieldType(field) ?? "").toLowerCase();
}

export function fieldDisplayValue(field: ListDataValue) {
  return firstDisplayValue([field.fmtValue, field.FmtValue, field.objId, field.ObjId]);
}

export function readViewId(view: ReadItemViewInfo | undefined, fallback = 0) {
  return Number(view?.viewId ?? view?.ViewId ?? fallback) || 0;
}

export function viewDisplayName(view: ViewDisplaySource | undefined, fallback = "") {
  return firstDisplayValue([view?.viewName, view?.ViewName, view?.name, view?.Name, fallback]);
}

export function viewDisplayTitle(view: ViewDisplaySource | undefined, fallback = "") {
  return firstDisplayValue([view?.viewTitle, viewDisplayName(view), fallback]);
}

export function viewDisplayType(view: ListViewInfo | undefined) {
  return firstDisplayValue([view?.viewType, view?.type, view?.Type, view?.showType, view?.ShowType]);
}

export function viewTemplateName(view: ListViewInfo | undefined) {
  return firstDisplayValue([view?.tempFile, view?.TempFile]);
}

export function viewTemplateKind(view: ListViewInfo | undefined) {
  const template = viewTemplateName(view).split("/").pop()?.replace(/\.jade$/i, "") || "view";
  if (template === "view") return "list";
  if (template === "viewWithChart") return "chart";
  if (template === "Sudoku") return "sudoku";
  return "unsupported";
}

export function viewUsesChartTemplate(view: ListViewInfo | undefined) {
  return viewTemplateKind(view) === "chart";
}

export function viewUsesSudokuTemplate(view: ListViewInfo | undefined) {
  return viewTemplateKind(view) === "sudoku";
}

export function sudokuPanelKind(column: Pick<TableColumnInfo, "viewFile" | "ViewFile">) {
  const name = firstDisplayValue([column.viewFile, column.ViewFile]).split("/").pop()?.toLowerCase() || "";
  if (["list", "group", "map", "item", "linechart"].includes(name)) {
    return name;
  }
  return "unknown";
}

export function sudokuPanelViewId(column: Pick<TableColumnInfo, "listViewId" | "ListViewId">) {
  return Number(column.listViewId ?? column.ListViewId ?? 0) || 0;
}

export function sudokuPanelListViewType(column: Pick<TableColumnInfo, "listViewType" | "ListViewType">) {
  return Number(column.listViewType ?? column.ListViewType ?? 0) || 0;
}

export function viewInputCount(view: ListViewInfo | undefined) {
  return firstList(view?.inputInfo).length;
}

export function legacyAppDefaultViewId(source?: unknown) {
  const record = objectRecord(source);
  const app = objectRecord(record.app || record.App || source);
  return Number(app.defaultViewId ?? app.DefaultViewId ?? 0) || 0;
}

export function legacyAppName(source?: unknown, fallback = "") {
  const record = objectRecord(source);
  const app = objectRecord(record.app || record.App || source);
  return firstDisplayValue([app.appName, app.AppName, fallback]);
}

export function legacyAppVersion(source?: unknown) {
  const record = objectRecord(source);
  const app = objectRecord(record.app || record.App || source);
  return firstDisplayValue([app.appVer, app.AppVer, app.appVersion, app.AppVersion]);
}

export function legacyInitAppCheckCode(source?: LegacyInitAppResult) {
  return source?.checkCode ?? source?.CheckCode;
}

export function legacyInitAppDbId(source?: LegacyInitAppResult) {
  const db = firstList(source?.dbs, source?.Dbs)[0];
  return firstDisplayValue([db?.dbId, db?.DbId]);
}

export function legacyLoginErrorMessage(source?: LegacyLoginResult) {
  const error = source?.error ?? source?.Error;
  return firstDisplayValue([error?.message, error?.Message]);
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

export function legacyMainMenuItems(source?: LegacyMainResult): LegacyAuthItem[] {
  return firstList(source?.topMenu, source?.TopMenu);
}

export function legacySubMenuItems(source?: LegacySubMenuResult): LegacyAuthItem[] {
  return firstList(source?.items, source?.Items);
}

export function legacyAuthText(item: LegacyAuthItem) {
  return firstDisplayValue([item.text, item.Text]);
}

export function legacyAuthImageUrl(item: LegacyAuthItem) {
  return firstDisplayValue([item.imageUrl, item.ImageUrl]);
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

export function legacyUserName(source?: LegacyUserInfoResult) {
  const user = source?.user ?? source?.User;
  return firstDisplayValue([user?.userName, user?.UserName, user?.loginName, user?.LoginName]);
}

export function legacyUserAvatar(source?: LegacyUserInfoResult) {
  const user = source?.user ?? source?.User;
  return firstDisplayValue([user?.userAvtarUrl, user?.UserAvtarUrl]);
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

export function legacyMessageTime(message: MessageInfo) {
  const raw = firstDisplayValue([message.gernerationTime, message.GernerationTime]);
  const millis = raw.match(/-?\d+/)?.[0];
  if (!millis) return raw;
  const date = new Date(Number(millis));
  return Number.isNaN(date.getTime()) ? raw : date.toISOString().slice(0, 19).replace("T", " ");
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

export function legacyNotifyCountForAuth(items: NotifyInfo[], authNo: string) {
  return items
    .filter((item) => legacyNotifyAuthNo(item) === authNo)
    .reduce((total, item) => total + legacyNotifyCount(item), 0);
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

export function inputQueryItemId(item?: InputQueryItem) {
  return firstDisplayValue([item?.id, item?.Id]);
}

export function inputQueryItemText(item?: InputQueryItem) {
  return firstDisplayValue([item?.text, item?.Text]);
}

export function legacyRunOperationSuccess(source?: LegacyRunOperationResult) {
  return source?.success === true || source?.IsSuccess === true;
}

export function legacyRunOperationMessage(source?: LegacyRunOperationResult) {
  return firstDisplayValue([source?.returnMsg, source?.ReturnMsg]);
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

export function rememberReadView(
  views: Record<number, ReadItemViewInfo>,
  requestedViewId: number,
  view: ReadItemViewInfo | undefined
) {
  const id = readViewId(view, requestedViewId);
  if (!id || !view) return views;
  return requestedViewId && requestedViewId !== id
    ? { ...views, [requestedViewId]: view, [id]: view }
    : { ...views, [id]: view };
}

export function readViewForId(views: Record<number, ReadItemViewInfo>, viewId: number) {
  return views[Number(viewId) || 0];
}

export function detailFieldsFromReadView(
  view: ReadItemViewInfo | undefined,
  dataFields: ListDataValue[] = []
): ListDataValue[] {
  if (!view) {
    return [];
  }
  const metadata = readViewItems(view);
  if (!metadata.length) {
    return [];
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

export function renderedDetailFields(
  view: ReadItemViewInfo | undefined,
  dataFields: ListDataValue[] = []
): ListDataValue[] {
  return view ? detailFieldsFromReadView(view, dataFields) : [];
}

export function detailGroupsFromReadView(
  view: ReadItemViewInfo | undefined,
  dataGroups: QueryDataDetailItemGroup[] = []
): QueryDataDetailItemGroup[] {
  if (!view) {
    return [];
  }
  const detailViews = readViewDetailViews(view);
  if (!detailViews.length) {
    return [];
  }

  const dataGroupsByKey = new Map<string, QueryDataDetailItemGroup>();
  for (const group of dataGroups) {
    for (const key of detailGroupKeys(group)) {
      dataGroupsByKey.set(key, group);
    }
  }

  const groups: QueryDataDetailItemGroup[] = detailViews.map((detail) => {
    const field = fieldFromReadItem(detail);
    const dataGroup = readDetailKeys(detail, field)
      .map((key) => dataGroupsByKey.get(key))
      .find((group): group is QueryDataDetailItemGroup => Boolean(group));
    const properties = firstList(detail.items, detail.Items).map(fieldFromReadItem);
    return {
      ...dataGroup,
      name: firstDisplayValue([detail.name, detail.Name, dataGroup?.name, dataGroup?.Name]),
      itemName: firstDisplayValue([detail.name, detail.Name, dataGroup?.itemName, dataGroup?.ItemName]),
      prpId: fieldKey(field) || dataGroup?.prpId || dataGroup?.PrpId,
      properties,
      items: firstList(dataGroup?.items, dataGroup?.Items)
    };
  });

  return groups;
}

export function renderedDetailGroups(
  view: ReadItemViewInfo | undefined,
  dataGroups: QueryDataDetailItemGroup[] = []
): QueryDataDetailItemGroup[] {
  return view ? detailGroupsFromReadView(view, dataGroups) : [];
}

export function detailResultSimpleData(result: QueryDataDetailResult | undefined) {
  const data = detailResultData(result);
  return firstList(data?.simpleData, data?.SimpleData);
}

export function detailResultObjectId(result: QueryDataDetailResult | undefined) {
  const data = detailResultData(result);
  return displayValue(data?.objId ?? data?.ObjId);
}

export function detailResultViewName(result: QueryDataDetailResult | undefined) {
  const data = detailResultData(result);
  return displayValue(data?.name ?? data?.Name);
}

export function detailResultItems(result: QueryDataDetailResult | undefined) {
  const data = detailResultData(result);
  return firstList(data?.items, data?.Items);
}

export function listRenderColumns(view: ListViewInfo | undefined): TableColumnInfo[] {
  if (!view) {
    return [];
  }
  return viewColumns(view);
}

export function fieldModelId(field: ListDataValue) {
  return field.prpModelId ?? field.PrpModelId ?? 0;
}

export function isEnumField(field: ListDataValue) {
  const type = normalizedPropertyType(field);
  return (type === "enum" || type === "15") && fieldModelId(field) > 0;
}

export function isLookupField(field: ListDataValue) {
  const type = normalizedPropertyType(field);
  return (type === "businessobject" || type === "16") && fieldModelId(field) > 0;
}

export function isMultilineField(field: ListDataValue) {
  if (normalizedPropertyType(field)) return false;
  const editType = normalizedEditType(field);
  return editType === "richtextbox" || editType === "5";
}

export function isReadonlyField(field: ListDataValue) {
  const readOnly = field.readOnly ?? field.ReadOnly;
  if (readOnly !== undefined) return readOnly === true;
  const editType = normalizedEditType(field);
  return editType === "readonly" || editType === "0";
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
    for (const item of groupItems(group)) {
      drafts[itemKey(group, item)] = buildGroupItemDrafts(group, item);
    }
  }
  return drafts;
}

export function buildGroupItemDrafts(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
  const valuesByKey = new Map<string, ListDataValue>();
  for (const value of detailItemValues(item)) {
    const key = fieldKey(value);
    if (key) {
      valuesByKey.set(key, value);
    }
  }
  return groupColumns(group).reduce<Record<string, string>>((drafts, field) => {
    const key = fieldKey(field);
    if (key) {
      drafts[key] = fieldDraftValue(mergeFieldValue(field, valuesByKey.get(key)));
    }
    return drafts;
  }, {});
}

export function draftFieldValue(
  drafts: Record<string, Record<string, string>>,
  draftKey: string,
  field: ListDataValue
) {
  return drafts[draftKey]?.[fieldKey(field)] ?? "";
}

export function withDraftFieldValue(
  drafts: Record<string, Record<string, string>>,
  draftKey: string,
  defaults: Record<string, string>,
  field: ListDataValue,
  value: string
) {
  const key = fieldKey(field);
  if (!key) {
    return drafts;
  }
  return {
    ...drafts,
    [draftKey]: {
      ...defaults,
      ...(drafts[draftKey] || {}),
      [key]: value
    }
  };
}

export function itemKey(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
  return `${groupKey(group)}:${itemDataId(item)}`;
}

export function groupColumns(group: QueryDataDetailItemGroup) {
  return firstList(group.properties, group.Properties);
}

export function groupItems(group: QueryDataDetailItemGroup) {
  return firstList(group.items, group.Items);
}

export function withGroupItems(group: QueryDataDetailItemGroup, items: QueryDataDetailDataItem[]): QueryDataDetailItemGroup {
  return { ...group, items, Items: items };
}

export function groupTitle(group: QueryDataDetailItemGroup) {
  return firstDisplayValue([group.itemName, group.ItemName, group.name, group.Name, group.prpId, group.PrpId]);
}

export function itemDataId(item: QueryDataDetailDataItem) {
  return firstDisplayValue([item.dataId, item.DataId]);
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
  return group.prpId || group.PrpId || group.name || group.Name || "items";
}

export function groupSelectedViewId(group: QueryDataDetailItemGroup) {
  return Number(group.selectedView ?? group.SelectedView ?? 0) || 0;
}

export function groupListViewId(group: QueryDataDetailItemGroup) {
  return Number(group.listViewId ?? group.ListViewId ?? 0) || 0;
}

export function groupSelectFromExists(group: QueryDataDetailItemGroup) {
  return group.selectFromExists ?? group.SelectFromExists ?? false;
}

export function groupDetailViewId(group: QueryDataDetailItemGroup) {
  return Number(group.detailViewId ?? group.DetailViewId ?? 0) || 0;
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

export function operationParams(operation: OperationInfo) {
  return firstList(operation.params, operation.Params);
}

export function operationParamKey(param: OperationParamInfo, index = 0) {
  return firstDisplayValue([param.id, param.ID, param.paramId, param.ParamId, param.name, param.Name, index]);
}

export function operationParamLabel(param: OperationParamInfo) {
  return firstDisplayValue([param.paramName, param.ParamName, param.name, param.Name, operationParamKey(param)]);
}

export function viewColumns(view: ListViewInfo | undefined) {
  return firstList(view?.tableColumn, view?.Items);
}

export function viewId(view: ListViewInfo | undefined, fallback = 0) {
  return Number(view?.viewId ?? view?.ViewId ?? view?.ViewID ?? view?.id ?? view?.ID ?? fallback) || 0;
}

export function legacyViewPathId(pathname: string) {
  const match = pathname.match(/^\/view(\d+)\/?$/);
  return match ? Number(match[1]) || 0 : 0;
}

export function legacyDetailPath(pathname: string) {
  const match = pathname.match(/^\/view(\d+)\/([^/]+)\/?$/);
  const viewId = match ? Number(match[1]) || 0 : 0;
  return viewId && match?.[2] ? { viewId, objectId: match[2] } : null;
}

export function legacyDetailHref(viewId: number, objectId: string) {
  const normalizedViewId = Number(viewId) || 0;
  const normalizedObjectId = String(objectId || "").trim();
  return normalizedViewId > 0 && normalizedObjectId ? `/view${normalizedViewId}/${normalizedObjectId}` : "";
}

export function legacyItemViewPathId(pathname: string) {
  const match = pathname.match(/^\/itemview(\d+)\/?$/);
  return match ? Number(match[1]) || 0 : 0;
}

export function legacyNewPath(pathname: string) {
  const match = pathname.match(/^\/new(\d+)(?:\/([^&/]*)&([^&/]*)&([^/]*))?\/?$/);
  const viewId = match ? Number(match[1]) || 0 : 0;
  return viewId ? { viewId, parentObjId: match?.[2] || "", ownerViewId: match?.[3] || "", property: match?.[4] || "" } : null;
}

export function legacyNewHref(viewId: number) {
  const normalizedViewId = Number(viewId) || 0;
  return normalizedViewId > 0 ? `/new${normalizedViewId}` : "";
}

export function legacyChildNewHref(viewId: number, parentObjId: string, ownerViewId: number, property: string) {
  const href = legacyNewHref(viewId);
  return href && parentObjId && ownerViewId && property
    ? `${href}/${parentObjId}&${ownerViewId}&${property}`
    : href;
}

export function viewOperations(view: ListViewInfo | undefined) {
  return firstList(view?.operations, view?.Operations);
}

export function dataOperations(result: QueryDataDetailResult | undefined) {
  return firstList(result?.operations, result?.Operations);
}

export function dataCanEdit(result: QueryDataDetailResult | undefined) {
  return result?.canEdit ?? result?.CanEdit ?? false;
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
  const value = displayValue(result?.freshTime ?? result?.FreshTime);
  if (!value) return "";
  const legacy = value.match(/^\/Date\((-?\d+)(?:[+-]\d{4})?\)\/$/);
  const date = legacy ? new Date(Number(legacy[1])) : new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

export function listRows(result?: ListViewResult) {
  return firstList(result?.items, result?.data, result?.Data);
}

export interface LegacyChartSeries {
  name: string;
  type: "line" | "bar" | "scatter";
  values: number[];
}

export interface LegacyChartData {
  labels: string[];
  series: LegacyChartSeries[];
}

export interface LegacyMapMarkerInfo {
  label: string;
  text: string;
}

export interface LegacyMapMarker {
  longitude: string;
  latitude: string;
  title?: LegacyMapMarkerInfo;
  info: LegacyMapMarkerInfo[];
}

export function legacyChartData(rows: ListDataItem[]): LegacyChartData {
  const labels: string[] = [];
  const series: LegacyChartSeries[] = [];

  for (const row of rows) {
    let seriesIndex = 0;
    for (const item of rowItems(row)) {
      const editType = String(item.editType ?? item.EditType ?? "").toLowerCase();
      if (editType === "11" || editType === "chartaxis") {
        labels.push(firstDisplayValue([item.fmtValue, item.FmtValue]));
        continue;
      }
      const type = chartSeriesType(editType);
      if (!type) {
        continue;
      }
      if (!series[seriesIndex]) {
        series.push({
          name: firstDisplayValue([item.prpShowName, item.PrpShowName, item.prpId, item.PrpId]),
          type,
          values: []
        });
      }
      const value = Number(firstDisplayValue([item.fmtValue, item.FmtValue, item.objId, item.ObjId]));
      if (Number.isFinite(value)) {
        series[seriesIndex].values.push(value);
      }
      seriesIndex += 1;
    }
  }

  return { labels, series };
}

export function legacyMapMarkers(rows: ListDataItem[]): LegacyMapMarker[] {
  const markers: LegacyMapMarker[] = [];
  for (const row of rows) {
    let longitude = "";
    let latitude = "";
    let title: LegacyMapMarkerInfo | undefined;
    const info: LegacyMapMarkerInfo[] = [];
    for (const item of rowItems(row)) {
      const editType = String(item.editType ?? item.EditType ?? "").toLowerCase();
      if (editType === "16" || editType === "maplongitude") {
        longitude = firstDisplayValue([item.objId, item.ObjId, item.fmtValue, item.FmtValue]);
      } else if (editType === "17" || editType === "maplatitude") {
        latitude = firstDisplayValue([item.objId, item.ObjId, item.fmtValue, item.FmtValue]);
      } else if (editType === "18" || editType === "maptitle") {
        title = { label: fieldTitle(item), text: fieldDisplayValue(item) };
      } else {
        const text = fieldDisplayValue(item);
        const label = fieldTitle(item);
        if (label || text) info.push({ label, text });
      }
    }
    if (longitude && latitude) {
      markers.push({ longitude, latitude, title, info });
    }
  }
  return markers;
}

export function legacyItemDetailFields(result: QueryDataDetailResult | undefined) {
  return detailResultSimpleData(result)
    .map((item) => ({ label: fieldTitle(item), text: fieldDisplayValue(item) }))
    .filter((item) => item.label || item.text);
}

function chartSeriesType(editType: string): LegacyChartSeries["type"] | "" {
  if (editType === "12" || editType === "chartline") return "line";
  if (editType === "13" || editType === "chartbar") return "bar";
  if (editType === "14" || editType === "chartscatter") return "scatter";
  return "";
}

export function viewDetailViewId(view: ListViewInfo | undefined, fallback = 0) {
  return Number(view?.detailViewId ?? view?.DetailViewId ?? 0) || Number(fallback || 0);
}

export function reportGridCells(result: ReportGridResult | undefined) {
  return firstList(result?.cells, result?.Cells);
}

export function reportGridPage(result: ReportGridResult | undefined, fallback = 1) {
  return Number(result?.currentPage ?? result?.CurrentPage ?? fallback) || fallback;
}

export function reportGridTotalPages(result: ReportGridResult | undefined) {
  return Number(result?.totalPages ?? result?.TotalPages ?? 0) || 0;
}

export function reportGridTotalRecords(result: ReportGridResult | undefined) {
  return Number(result?.totalRecords ?? result?.TotalRecords ?? 0) || 0;
}

export function reportRowsFromCells(cells: ReportCell[] = []) {
  const maxRow = cells.reduce((max, cell) => Math.max(max, reportCellRow(cell)), -1);
  const maxCol = cells.reduce((max, cell) => Math.max(max, reportCellCol(cell)), -1);
  if (maxRow < 0 || maxCol < 0) {
    return [];
  }

  const byPosition = new Map(cells.map((cell) => [`${reportCellRow(cell)}:${reportCellCol(cell)}`, reportCellValue(cell)]));
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

export function reportModelStateValue(state: ReportModelState) {
  return firstDisplayValue([state.dbName, state.DBName]);
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
    key: groupKey(group),
    items: [
      {
        itemId: itemDataId(item),
        isExist: true,
        propertyies: buildItemPropertyies(groupColumns(group), drafts)
      }
    ]
  };
}

export function buildDeletedItemProperty(
  group: QueryDataDetailItemGroup,
  item: QueryDataDetailDataItem
): SaveItemProperty {
  return {
    key: groupKey(group),
    delteItems: [
      {
        itemId: itemDataId(item),
        isExist: true,
        propertyies: []
      }
    ]
  };
}

export function mergeItemPropertyChange(
  changes: SaveItemProperty[],
  change: SaveItemProperty
): SaveItemProperty[] {
  const key = change.key || "";
  const index = changes.findIndex((item) => item.key === key);
  if (!key || index < 0) return [...changes, change];
  const current = changes[index];
  const deletedItemIds = new Set((change.delteItems || []).map((item) => item.itemId || ""));
  const merged = {
    key,
    items: mergeSaveItems(current.items, change.items)
      .filter((item) => !deletedItemIds.has(item.itemId || "")),
    delteItems: mergeSaveItems(current.delteItems, change.delteItems),
    addedItems: mergeSaveItems(current.addedItems, change.addedItems)
  };
  return changes.map((item, itemIndex) => itemIndex === index ? merged : item);
}

function mergeSaveItems(current: SaveItem[] = [], incoming: SaveItem[] = []) {
  return incoming.reduce<SaveItem[]>((items, item) => {
    const itemId = item.itemId || "";
    const index = itemId ? items.findIndex((candidate) => candidate.itemId === itemId) : -1;
    if (index < 0) return [...items, item];
    return items.map((candidate, candidateIndex) => candidateIndex === index ? item : candidate);
  }, [...current]);
}

export function buildAddedItemProperty(
  group: QueryDataDetailItemGroup,
  itemId: string,
  drafts: Record<string, string>,
  options: { includeReadonly?: boolean; isExist?: boolean } = {}
): SaveItemProperty {
  return {
    key: groupKey(group),
    addedItems: [
      {
        itemId,
        isExist: options.isExist === true,
        propertyies: buildItemPropertyies(groupColumns(group), drafts, options.includeReadonly)
      }
    ]
  };
}

export function buildAddedDetailItem(
  group: QueryDataDetailItemGroup,
  itemId: string,
  drafts: Record<string, string>
): QueryDataDetailDataItem {
  const values = groupColumns(group).map((field) => {
    const value = drafts[fieldKey(field)] ?? "";
    return { ...field, objId: value, fmtValue: value };
  });
  return { dataId: itemId, DataId: itemId, values, Values: values };
}

export function removeAddedItemPropertyChange(
  changes: SaveItemProperty[],
  key: string,
  itemId: string
): SaveItemProperty[] {
  return changes.flatMap((change) => {
    if (change.key !== key) return [change];
    const next = {
      ...change,
      addedItems: (change.addedItems || []).filter((item) => item.itemId !== itemId)
    };
    return next.items?.length || next.delteItems?.length || next.addedItems.length ? [next] : [];
  });
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
      byColumn && valueObjId(byColumn),
      byColumn && valueFmtValue(byColumn),
      byKey && valueObjId(byKey),
      byKey && valueFmtValue(byKey),
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
  return buildAddedItemProperty(group, itemId, buildDraftsFromRow(groupColumns(group), row, columns), {
    includeReadonly: true,
    isExist: true
  });
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

function reportCellRow(cell: ReportCell) {
  return finiteNumber(cell.row ?? cell.Row, -1);
}

function reportCellCol(cell: ReportCell) {
  return finiteNumber(cell.col ?? cell.Col, -1);
}

function reportCellValue(cell: ReportCell) {
  return firstDisplayValue([cell.fmtValue, cell.FmtValue]);
}

function finiteNumber(value: unknown, fallback: number) {
  const result = Number(value);
  return Number.isFinite(result) ? result : fallback;
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
  return normalizeKeys([group.prpId, group.PrpId, group.name, group.Name, group.itemName, group.ItemName]);
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
