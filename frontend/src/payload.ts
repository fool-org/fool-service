import type {
  CommonRequest,
  GetEnumRequest,
  InputQueryRequest,
  LegacyInitNewRequest,
  LegacyRunOperationRequest,
  LegacySaveNewObjRequest,
  LegacyQueryDataRequest,
  LegacyQueryDataDetailRequest,
  MakeReportRequest,
  ReportCol,
  ReportFilterExp,
  SaveItemProperty,
  SaveKeypair,
  SaveObject,
  SaveObjRequest,
  ViewDataRequest
} from "./api";

export interface InputQueryRequestInput {
  token: string;
  viewId?: number;
  viewItemId: string;
  text?: string;
  modelID?: string;
  objID?: string;
  isAdded?: boolean;
  ownerId?: string;
}

export interface SaveObjRequestInput {
  token: string;
  id: string;
  viewID: string;
  propertyiesJson: string;
  itempropertiesJson?: string;
  parentId?: string;
  model?: string;
}

export interface SaveNewObjRequestInput extends SaveObjRequestInput {
  ownerViewId?: string;
  ownerId?: string;
  property?: string;
}

export interface RunOperationRequestInput {
  token: string;
  objectId: string;
  viewId: number;
  operationId: number;
}

export interface QueryDataDetailRequestInput {
  token: string;
  viewId: number;
  objId: string;
  idExp?: string;
}

export interface InitNewRequestInput {
  token: string;
  viewId: number;
  parentObjId?: string;
}

export interface GetEnumRequestInput {
  token: string;
  modelId: string;
}

export interface LegacyListViewRequestInput {
  token: string;
  viewId: number;
}

export interface LegacyQueryDataRequestInput {
  token: string;
  viewId: number;
  pageSize: number;
  pageIndex: number;
  queryFilter?: string;
  keyword?: string;
  orderByItem?: number;
  orderByType?: number;
}

export interface MakeReportRequestInput {
  token: string;
  viewId: number;
  currentPage: number;
  pageSize: number;
  queryFilter?: string;
  reportCols: ReportCol[];
  filterExp?: ReportFilterExp;
  reportName?: string;
}

export function buildTokenRequest(token: string): CommonRequest {
  return {
    token: token.trim()
  };
}

export function buildInputQueryRequest(input: InputQueryRequestInput): InputQueryRequest {
  const request: InputQueryRequest = {
    token: input.token,
    viewItemId: input.viewItemId.trim(),
    text: input.text?.trim() || "",
    isAdded: Boolean(input.isAdded)
  };
  if (input.viewId) {
    request.viewId = input.viewId;
  }
  addOptional(request, "modelID", input.modelID);
  addOptional(request, "objID", input.objID);
  addOptional(request, "ownerId", input.ownerId);
  return request;
}

export function buildSaveObjRequest(input: SaveObjRequestInput): SaveObjRequest {
  const saveObj: SaveObject = {
    id: input.id.trim(),
    viewID: input.viewID.trim(),
    propertyies: parseJsonArray<SaveKeypair>(input.propertyiesJson, "Propertyies JSON"),
    itemproperties: parseJsonArray<SaveItemProperty>(input.itempropertiesJson || "", "Itemproperties JSON")
  };
  const parentId = input.parentId?.trim();
  const model = input.model?.trim();
  if (parentId) {
    saveObj.parentId = parentId;
  }
  if (model) {
    saveObj.model = model;
  }
  return {
    token: input.token,
    saveObj
  };
}

export function buildSaveNewObjRequest(input: SaveNewObjRequestInput): LegacySaveNewObjRequest {
  const request: LegacySaveNewObjRequest = buildSaveObjRequest(input);
  const ownerViewId = input.ownerViewId?.trim();
  const ownerId = input.ownerId?.trim();
  const property = input.property?.trim();
  if (ownerViewId) {
    request.ownerViewId = ownerViewId;
  }
  if (ownerId) {
    request.ownerId = ownerId;
  }
  if (property) {
    request.property = property;
  }
  return request;
}

export function buildRunOperationRequest(input: RunOperationRequestInput): LegacyRunOperationRequest {
  return {
    token: input.token,
    objectId: input.objectId.trim(),
    viewId: input.viewId,
    operationId: input.operationId
  };
}

export function buildQueryDataDetailRequest(input: QueryDataDetailRequestInput): LegacyQueryDataDetailRequest {
  const request: LegacyQueryDataDetailRequest = {
    token: input.token,
    viewId: input.viewId,
    objId: input.objId.trim()
  };
  const idExp = input.idExp?.trim();
  if (idExp) {
    request.idExp = idExp;
  }
  return request;
}

export function buildInitNewRequest(input: InitNewRequestInput): LegacyInitNewRequest {
  const request: LegacyInitNewRequest = {
    token: input.token,
    viewId: input.viewId
  };
  const parentObjId = input.parentObjId?.trim();
  if (parentObjId) {
    request.parentObjId = parentObjId;
  }
  return request;
}

export function buildGetEnumRequest(input: GetEnumRequestInput): GetEnumRequest {
  return {
    token: input.token,
    modelId: input.modelId.trim()
  };
}

export function buildLegacyListViewRequest(input: LegacyListViewRequestInput): ViewDataRequest {
  return {
    token: input.token,
    viewId: input.viewId
  };
}

export function buildLegacyReadItemViewRequest(input: LegacyListViewRequestInput): ViewDataRequest {
  return buildLegacyListViewRequest(input);
}

export function buildLegacyQueryDataRequest(input: LegacyQueryDataRequestInput): LegacyQueryDataRequest {
  const request: LegacyQueryDataRequest = {
    token: input.token,
    viewId: input.viewId,
    pageSize: input.pageSize,
    pageIndex: input.pageIndex
  };
  const queryFilter = input.queryFilter?.trim();
  if (queryFilter) {
    request.queryFilter = queryFilter;
  }
  const keyword = input.keyword?.trim();
  if (keyword) {
    request.keyword = keyword;
  }
  if (input.orderByItem !== undefined) {
    request.orderByItem = input.orderByItem;
  }
  if (input.orderByType !== undefined) {
    request.orderByType = input.orderByType;
  }
  return request;
}

export function buildMakeReportRequest(input: MakeReportRequestInput): MakeReportRequest {
  const request: MakeReportRequest = {
    token: input.token,
    viewId: input.viewId,
    currentPage: input.currentPage,
    pageSize: input.pageSize,
    reportCols: input.reportCols
  };
  const queryFilter = input.queryFilter?.trim();
  if (queryFilter) {
    request.queryFilter = queryFilter;
  }
  if (input.filterExp) {
    request.filterExp = input.filterExp;
  }
  const reportName = input.reportName?.trim();
  if (reportName) {
    request.reportName = reportName;
  }
  return request;
}

function addOptional(request: InputQueryRequest, key: "modelID" | "objID" | "ownerId", value?: string) {
  const trimmed = value?.trim();
  if (trimmed) {
    request[key] = trimmed;
  }
}

function parseJsonArray<T>(json: string, label: string): T[] {
  const trimmed = json.trim();
  if (!trimmed) {
    return [];
  }
  const parsed = JSON.parse(trimmed) as unknown;
  if (!Array.isArray(parsed)) {
    throw new Error(`${label} must be an array.`);
  }
  return parsed as T[];
}
