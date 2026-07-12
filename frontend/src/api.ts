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

export interface LegacyUserInfo {
  loginName?: string;
  LoginName?: string;
  userName?: string;
  UserName?: string;
  userId?: number;
  UserId?: number;
  companyName?: string;
  CompanyName?: string;
  departmentName?: string;
  DepartmentName?: string;
  userAvtarUrl?: string;
  UserAvtarUrl?: string;
}

export interface LegacyUserInfoResult {
  token?: string;
  Token?: string;
  user?: LegacyUserInfo;
  User?: LegacyUserInfo;
}

export interface LegacyAuthItem {
  text?: string;
  Text?: string;
  note?: string;
  Note?: string;
  imageUrl?: string;
  ImageUrl?: string;
  authType?: number;
  AuthType?: number;
  viewId?: number;
  ViewId?: number;
  notifyCount?: number;
  NotifyCount?: number;
  viewType?: number;
  ViewType?: number;
  index?: number;
  Index?: number;
  authNo?: string;
  AuthNo?: string;
}

export interface LegacySubMenuResult {
  token?: string;
  Token?: string;
  items?: LegacyAuthItem[];
  Items?: LegacyAuthItem[];
}

export interface LegacyAppInfo {
  appName?: string;
  AppName?: string;
  appVer?: string;
  AppVer?: string;
  appNote?: string;
  AppNote?: string;
  appPowerBy?: string;
  AppPowerBy?: string;
  appPowerUrl?: string;
  AppPowerUrl?: string;
  appLogoUrl?: string;
  AppLogoUrl?: string;
  defaultViewId?: number;
  DefaultViewId?: number;
  appId?: string;
  AppId?: string;
}

export interface LegacyMainResult {
  token?: string;
  Token?: string;
  user?: LegacyUserInfo;
  User?: LegacyUserInfo;
  app?: LegacyAppInfo;
  App?: LegacyAppInfo;
  topMenu?: LegacyAuthItem[];
  TopMenu?: LegacyAuthItem[];
}

export interface LegacyAppResult {
  token?: string;
  Token?: string;
  app?: LegacyAppInfo;
  App?: LegacyAppInfo;
}

export interface CheckCodeResult {
  key?: string;
  Key?: string;
  code?: string;
  Code?: string;
  chkCodeImg?: string;
  ChkCodeImg?: string;
}

export interface CheckCodeRequest {
  key?: string;
  code?: string;
  chkCodeImg?: string;
}

export interface LegacyStoreBaseInfo {
  dbId?: string;
  DbId?: string;
  dbName?: string;
  DbName?: string;
}

export interface LegacyInitAppResult {
  appTitle?: string;
  AppTitle?: string;
  appName?: string;
  AppName?: string;
  appImg?: string;
  AppImg?: string;
  appVersion?: string;
  AppVersion?: string;
  appPowerBy?: string;
  AppPowerBy?: string;
  appUrl?: string;
  AppUrl?: string;
  checkCode?: CheckCodeResult;
  CheckCode?: CheckCodeResult;
  dbs?: LegacyStoreBaseInfo[];
  Dbs?: LegacyStoreBaseInfo[];
  error?: {
    code?: number;
    message?: string;
  };
}

export interface LoginVo {
  user?: UserDTO;
  token?: string;
}

export interface LegacyLoginResult {
  token?: string;
  Token?: string;
  loginSucess?: boolean;
  LoginSucess?: boolean;
  IsLogin?: boolean;
  user?: LegacyUserInfo;
  User?: LegacyUserInfo;
  app?: LegacyAppInfo;
  App?: LegacyAppInfo;
  error?: {
    code?: number;
    Code?: number;
    message?: string;
    Message?: string;
  };
  Error?: {
    code?: number;
    Code?: number;
    message?: string;
    Message?: string;
  };
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
  ID?: number;
  name?: string;
  Name?: string;
  title?: string;
  property?: string;
  propertyName?: string;
  PropertyName?: string;
  showIndex?: number;
  ShowIndex?: number;
  width?: number;
  Width?: number;
  format?: string;
  Format?: string;
  isReadOnly?: boolean;
  IsReadOnly?: boolean;
  editType?: string | number;
  EditType?: string | number;
  propertyId?: number;
  PropertyId?: number;
  listViewId?: number;
  ListViewId?: number;
  listViewType?: number;
  ListViewType?: number;
  editViewId?: number;
  EditViewId?: number;
  editExp?: number;
  EditExp?: number;
  propertyType?: string | number;
  PropertyType?: string | number;
  propertyModel?: number;
  PropertyModel?: number;
  viewFile?: string | null;
  ViewFile?: string | null;
}

export interface OperationInfo {
  id?: number;
  ID?: number;
  name?: string;
  Name?: string;
  text?: string;
  type?: string;
  viewName?: string;
  viewId?: number;
  ViewID?: number;
  requireSelect?: boolean;
  RequireSelect?: boolean;
  location?: number;
  params?: OperationParamInfo[];
  Params?: OperationParamInfo[];
}

export interface OperationParamInfo {
  id?: number;
  ID?: number;
  name?: string;
  Name?: string;
  index?: number;
  paramId?: number;
  ParamId?: number;
  paramName?: string;
  ParamName?: string;
  viewId?: number;
  filter?: string;
  value?: string;
}

export interface ListViewInfo {
  id?: number;
  ID?: number;
  viewId?: number;
  ViewId?: number;
  ViewID?: number;
  viewName?: string;
  ViewName?: string;
  name?: string;
  Name?: string;
  viewTitle?: string;
  browserTitle?: string;
  viewType?: string;
  type?: string;
  Type?: string;
  showType?: string;
  ShowType?: string;
  tempFile?: string;
  TempFile?: string;
  detailViewId?: number;
  DetailViewId?: number;
  autoFreshTime?: number;
  AutoFreshTime?: number;
  inputInfo?: ViewInputInfo[];
  tableColumn?: TableColumnInfo[];
  Items?: TableColumnInfo[];
  operations?: OperationInfo[];
  Operations?: OperationInfo[];
}

export interface ViewDataRequest {
  token?: string;
  viewId?: number;
}

export interface ReadItemViewItemInfo {
  name?: string;
  Name?: string;
  prpType?: string | number;
  PrpType?: string | number;
  index?: number;
  Index?: number;
  prpId?: string;
  PrpId?: string;
  prpModelId?: number;
  PrpModelId?: number;
  id?: string;
  ID?: string;
  prpShowName?: string;
  PrpShowName?: string;
  readOnly?: boolean;
  ReadOnly?: boolean;
  editType?: string | number;
  EditType?: string | number;
}

export interface ReadItemViewDetailInfo extends ReadItemViewItemInfo {
  items?: ReadItemViewItemInfo[];
  Items?: ReadItemViewItemInfo[];
}

export interface ReadItemViewInfo {
  viewName?: string;
  ViewName?: string;
  viewId?: number;
  ViewId?: number;
  items?: ReadItemViewItemInfo[];
  Items?: ReadItemViewItemInfo[];
  detailViews?: ReadItemViewDetailInfo[];
  DetailViews?: ReadItemViewDetailInfo[];
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
  items?: ListDataValue[];
  rowFmt?: string;
  operation?: OperationInfo[];
  Id?: string;
  RowIndex?: number;
  Items?: ListDataValue[];
  RowFmt?: string;
}

export interface ListDataValue {
  objId?: string;
  prpId?: string;
  fmtValue?: string;
  prpShowName?: string;
  prpType?: string | number;
  prpModelId?: number;
  readOnly?: boolean;
  editType?: string | number;
  ObjId?: string;
  PrpId?: string;
  FmtValue?: string;
  PrpShowName?: string;
  PrpType?: string | number;
  PrpModelId?: number;
  ReadOnly?: boolean;
  EditType?: string | number;
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
  TotalItem?: number;
  TotalPage?: number;
  PageIndex?: number;
  Cols?: string[];
  FreshTime?: string;
  AutoFreshTime?: number;
  Data?: ListDataItem[];
}

export interface GetEnumRequest {
  token?: string;
  modelId?: string;
}

export interface GetEnumValue {
  name?: string;
  Name?: string;
  value?: number;
  Value?: number;
}

export interface GetEnumResult {
  enumValues?: GetEnumValue[];
  EnumValues?: GetEnumValue[];
}

export interface InputQueryRequest {
  token?: string;
  text?: string;
  viewItemId?: string;
  viewId?: number;
  viewName?: string;
  modelID?: string;
  objID?: string;
  isAdded?: boolean;
  ownerId?: string;
}

export interface InputQueryItem {
  id?: string;
  Id?: string;
  text?: string;
  Text?: string;
}

export interface InputQueryResult {
  items?: InputQueryItem[];
  Items?: InputQueryItem[];
}

export interface MessageInfo {
  messageID?: string;
  MessageID?: string;
  gernerationTime?: string;
  GernerationTime?: string;
  messageContent?: string;
  MessageContent?: string;
  resultView?: number;
  ResultView?: number;
  objId?: string;
  ObjId?: string;
  resultViewType?: string;
  ResultViewType?: string;
  resultKey?: string;
  ResultKey?: string;
  read?: boolean;
  Read?: boolean;
  timeOut?: boolean;
  TimeOut?: boolean;
  readDateTime?: string;
  ReadDateTime?: string;
}

export interface GetMessageResult {
  messages?: MessageInfo[];
  Messages?: MessageInfo[];
}

export interface NotifyInfo {
  count?: number;
  Count?: number;
  authNo?: string;
  AuthNo?: string;
}

export interface GetNotifyResult {
  notifies?: NotifyInfo[];
  Notifies?: NotifyInfo[];
}

export interface LegacyQueryDataRequest {
  token?: string;
  viewId?: number;
  pageSize?: number;
  pageIndex?: number;
  orderByItem?: number;
  orderByType?: number;
  queryFilter?: string;
  keyword?: string;
}

export interface ReportCol {
  colName?: string;
  colId?: string;
  selectedTypeId?: string;
  index?: number;
  orderType?: string;
}

export interface ReportFilterExp {
  col?: ReportModelOption;
  compareOp?: ReportModelOption;
  valueExp?: string;
  valueFmt?: string;
  firstExp?: ReportFilterExp;
  sequences?: ReportFilterSequence[];
}

export interface ReportFilterSequence {
  boolOp?: {
    dbName?: string;
    showName?: string;
  };
  addedExp?: ReportFilterExp;
}

export interface MakeReportRequest {
  token?: string;
  viewId?: number;
  reportCols?: ReportCol[];
  currentPage?: number;
  pageSize?: number;
  queryFilter?: string;
  filterExp?: ReportFilterExp;
  reportName?: string;
}

export interface ReportCell {
  col?: number;
  Col?: number;
  row?: number;
  Row?: number;
  colSpan?: number;
  ColSpan?: number;
  rowSpan?: number;
  RowSpan?: number;
  fmtValue?: string;
  FmtValue?: string;
}

export interface ReportGridResult {
  viewId?: number;
  ViewId?: number;
  currentPage?: number;
  CurrentPage?: number;
  pageSize?: number;
  PageSize?: number;
  totalRecords?: number;
  TotalRecords?: number;
  totalPages?: number;
  TotalPages?: number;
  cells?: ReportCell[];
  Cells?: ReportCell[];
}

export interface ReportModelOption {
  id?: string;
  ID?: string;
  name?: string;
  Name?: string;
}

export interface ReportModelState {
  showName?: string;
  ShowName?: string;
  dbName?: string;
  DBName?: string;
}

export interface ReportModelColumn {
  id?: string;
  ID?: string;
  name?: string;
  Name?: string;
  prpType?: number;
  PrpType?: number;
  modelId?: number;
  ModelId?: number;
  states?: ReportModelState[];
  States?: ReportModelState[];
  compareTypes?: ReportModelOption[];
  CompareTypes?: ReportModelOption[];
  queryTypes?: ReportModelOption[];
  QueryTypes?: ReportModelOption[];
}

export interface ReportModelResult {
  cols?: ReportModelColumn[];
  Cols?: ReportModelColumn[];
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
  ObjId?: string;
  name?: string;
  Name?: string;
  simpleData?: ListDataValue[];
  SimpleData?: ListDataValue[];
  items?: QueryDataDetailItemGroup[];
  Items?: QueryDataDetailItemGroup[];
  model?: string;
  Model?: string;
  parentId?: string;
  ParentId?: string;
}

export interface QueryDataDetailItemGroup {
  properties?: ListDataValue[];
  Properties?: ListDataValue[];
  items?: QueryDataDetailDataItem[];
  Items?: QueryDataDetailDataItem[];
  listViewId?: number;
  ListViewId?: number;
  detailViewId?: number;
  DetailViewId?: number;
  name?: string;
  Name?: string;
  prpId?: string;
  PrpId?: string;
  selectFromExists?: boolean;
  SelectFromExists?: boolean;
  itemName?: string;
  ItemName?: string;
  selectedView?: number;
  SelectedView?: number;
}

export interface QueryDataDetailDataItem {
  dataId?: string;
  values?: ListDataValue[];
  DataId?: string;
  Values?: ListDataValue[];
}

export interface QueryDataDetailResult {
  data?: QueryDataDetail;
  Data?: QueryDataDetail;
  autoFreshTime?: number;
  AutoFreshTime?: number;
  canEdit?: boolean;
  CanEdit?: boolean;
  operations?: OperationInfo[];
  Operations?: OperationInfo[];
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

export interface LegacySaveNewObjRequest extends SaveObjRequest {
  ownerViewId?: string;
  ownerId?: string;
  property?: string;
}

export interface LegacyRunOperationRequest {
  token?: string;
  objectId?: string;
  operationId?: number;
  viewId?: number;
}

export interface LegacyRunOperationResult {
  value?: ListDataValue[];
  Value?: ListDataValue[];
  success?: boolean;
  IsSuccess?: boolean;
  returnObjId?: string;
  ReturnObjId?: string;
  returnViewId?: number;
  ReturnViewId?: number;
  returnMsg?: string;
  ReturnMsg?: string;
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
  if (body.code !== 0) {
    throw new Error(body.message || `API code ${body.code}`);
  }

  return body;
}
