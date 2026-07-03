export interface CommonResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface CommonRequest {
  token?: string;
}

export interface UserDTO {
  id?: string;
  name?: string;
  mobile?: string;
}

export interface LoginVo {
  user?: UserDTO;
  token?: string;
}

export interface TreeNode<T> {
  data?: T;
  item?: T;
  value?: T;
  children?: TreeNode<T>[];
  [key: string]: unknown;
}

export interface AuthItem {
  id?: string;
  name?: string;
  text?: string;
  title?: string;
  [key: string]: unknown;
}

export interface ViewInputInfo {
  property?: string;
  text?: string;
  legend?: string;
  inputType?: string;
  option?: unknown[];
}

export interface TableColumnInfo {
  id?: number;
  name?: string;
  title?: string;
  property?: string;
  propertyName?: string;
  showIndex?: number;
  width?: number;
  format?: string;
  isReadOnly?: boolean;
  editType?: string;
  propertyId?: number;
  listViewId?: number;
  listViewType?: number;
  editViewId?: number;
  editExp?: number;
  propertyType?: string;
  propertyModel?: number;
  viewFile?: string | null;
}

export interface OperationInfo {
  id?: number;
  name?: string;
  text?: string;
  type?: string;
  viewName?: string;
  viewId?: number;
  requireSelect?: boolean;
  location?: number;
}

export interface ListViewInfo {
  id?: number;
  viewName?: string;
  name?: string;
  viewTitle?: string;
  browserTitle?: string;
  viewType?: string;
  type?: string;
  showType?: string;
  tempFile?: string;
  detailViewId?: number;
  autoFreshTime?: number;
  inputInfo?: ViewInputInfo[];
  tableColumn?: TableColumnInfo[];
  operations?: OperationInfo[];
}

export interface ViewDataRequest {
  token?: string;
  viewId?: number;
  viewName?: string;
}

export interface ReadItemViewItemInfo {
  name?: string;
  prpType?: string;
  index?: number;
  prpId?: string;
  prpModelId?: number;
  id?: string;
  prpShowName?: string;
  readOnly?: boolean;
  editType?: string;
}

export interface ReadItemViewDetailInfo extends ReadItemViewItemInfo {
  items?: ReadItemViewItemInfo[];
}

export interface ReadItemViewInfo {
  viewName?: string;
  viewId?: number;
  items?: ReadItemViewItemInfo[];
  detailViews?: ReadItemViewDetailInfo[];
}

export interface PageNavigatorResult {
  pageSize?: number;
  pageIndex?: number;
  total?: number;
  pageCount?: number;
}

export interface ListDataItem {
  id?: string;
  rowIndex?: number;
  values?: Record<string, unknown>;
  items?: ListDataValue[];
  rowFmt?: string;
  operation?: OperationInfo[];
}

export interface ListDataValue {
  objId?: string;
  prpId?: string;
  fmtValue?: string;
  prpShowName?: string;
  prpType?: string;
  prpModelId?: number;
  readOnly?: boolean;
  editType?: string;
}

export interface ListViewResult {
  pageInfo?: PageNavigatorResult;
  totalItem?: number;
  totalPage?: number;
  pageIndex?: number;
  cols?: string[];
  freshTime?: string;
  autoFreshTime?: number;
  items?: ListDataItem[];
  data?: ListDataItem[];
}

export interface GetEnumRequest {
  token?: string;
  modelId?: string;
}

export interface GetEnumValue {
  name?: string;
  value?: number;
}

export interface GetEnumResult {
  enumValues?: GetEnumValue[];
}

export interface InputQueryRequest {
  token?: string;
  text?: string;
  viewItemId?: string;
  viewName?: string;
  modelID?: string;
  objID?: string;
  isAdded?: boolean;
  ownerId?: string;
}

export interface InputQueryItem {
  id?: string;
  text?: string;
}

export interface InputQueryResult {
  items?: InputQueryItem[];
}

export interface MessageInfo {
  messageID?: string;
  gernerationTime?: string;
  messageContent?: string;
  resultView?: number;
  objId?: string;
  resultViewType?: string;
  resultKey?: string;
  read?: boolean;
  timeOut?: boolean;
  readDateTime?: string;
}

export interface GetMessageResult {
  messages?: MessageInfo[];
}

export interface NotifyInfo {
  count?: number;
  authNo?: string;
}

export interface GetNotifyResult {
  notifies?: NotifyInfo[];
}

export interface LegacyQueryDataRequest {
  token?: string;
  viewId?: number;
  pageSize?: number;
  pageIndex?: number;
  orderByItem?: number;
  orderByType?: number;
  queryFilter?: string;
}

export interface ReportCol {
  colName?: string;
  colId?: string;
  selectedTypeId?: string;
  index?: number;
  orderType?: string;
}

export interface MakeReportRequest {
  token?: string;
  viewId?: number;
  reportCols?: ReportCol[];
  currentPage?: number;
  pageSize?: number;
  queryFilter?: string;
  reportName?: string;
}

export interface ReportCell {
  col: number;
  row: number;
  colSpan: number;
  rowSpan: number;
  fmtValue?: string;
}

export interface ReportGridResult {
  viewId: number;
  currentPage: number;
  pageSize: number;
  totalRecords: number;
  totalPages: number;
  cells: ReportCell[];
}

export interface ReportModelOption {
  id?: string;
  name?: string;
}

export interface ReportModelState {
  showName?: string;
  dbName?: string;
}

export interface ReportModelColumn {
  id?: string;
  name?: string;
  prpType?: number;
  modelId?: number;
  states?: ReportModelState[];
  compareTypes?: ReportModelOption[];
  queryTypes?: ReportModelOption[];
}

export interface ReportModelResult {
  cols?: ReportModelColumn[];
}

export interface LegacyQueryDataDetailRequest {
  token?: string;
  viewId?: number;
  objId?: unknown;
  idExp?: string;
}

export interface LegacyInitNewRequest {
  token?: string;
  viewId?: number;
  parentObjId?: string;
}

export interface QueryDataDetail {
  objId?: string;
  name?: string;
  simpleData?: ListDataValue[];
  items?: unknown[];
  model?: string;
  parentId?: string;
}

export interface QueryDataDetailResult {
  data?: QueryDataDetail;
  autoFreshTime?: number;
  canEdit?: boolean;
  operations?: OperationInfo[];
}

export interface SaveKeypair {
  key?: string;
  value?: unknown;
}

export interface SaveItem {
  isExist?: boolean;
  itemId?: string;
  propertyies?: SaveKeypair[];
}

export interface SaveItemProperty {
  key?: string;
  items?: SaveItem[];
  delteItems?: SaveItem[];
  addedItems?: SaveItem[];
}

export interface SaveObject {
  id?: string;
  propertyies?: SaveKeypair[];
  itemproperties?: SaveItemProperty[];
  viewID?: string;
  parentId?: string;
  model?: string;
}

export interface SaveObjRequest {
  token?: string;
  saveObj?: SaveObject;
}

export async function postApi<T>(path: string, payload: unknown): Promise<CommonResponse<T>> {
  const response = await fetch(path, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });

  const body = (await response.json().catch(() => null)) as CommonResponse<T> | null;
  if (!response.ok) {
    throw new Error(body?.message || `HTTP ${response.status}`);
  }

  if (!body) {
    throw new Error("Empty response body.");
  }

  return body;
}
