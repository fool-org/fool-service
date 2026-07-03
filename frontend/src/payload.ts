import type {
  GetEnumRequest,
  InputQueryRequest,
  LegacyQueryDataRequest,
  LegacyQueryDataDetailRequest,
  SaveItemProperty,
  SaveKeypair,
  SaveObject,
  SaveObjRequest,
  ViewDataRequest
} from "./api";

export interface QueryRequestInput {
  token: string;
  viewName: string;
  pageIndex: number;
  pageSize: number;
  filterJson: string;
  keyword?: string;
  visibleFilters?: VisibleFilterInput[];
}

export interface VisibleFilterInput {
  property: string;
  value?: string;
  values?: string[];
}

export interface InputQueryRequestInput {
  token: string;
  viewName: string;
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

export interface QueryDataDetailRequestInput {
  token: string;
  viewId: number;
  objId: string;
  idExp?: string;
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
  orderByItem?: number;
  orderByType?: number;
}

export interface QueryRequest {
  token: string;
  viewName: string;
  pageInfo: {
    pageIndex: number;
    pageSize: number;
  };
  filter: Record<string, unknown>;
  keyword?: string;
}

export function buildQueryRequest(input: QueryRequestInput): QueryRequest {
  const filter = parseFilter(input.filterJson);
  const visibleFilter = buildVisibleFilter(input.visibleFilters || []);
  const keyword = input.keyword?.trim();

  const request: QueryRequest = {
    token: input.token,
    viewName: input.viewName,
    pageInfo: {
      pageIndex: input.pageIndex,
      pageSize: input.pageSize
    },
    filter: {
      ...filter,
      ...visibleFilter
    }
  };
  if (keyword) {
    request.keyword = keyword;
  }
  return request;
}

export function buildInputQueryRequest(input: InputQueryRequestInput): InputQueryRequest {
  const request: InputQueryRequest = {
    token: input.token,
    viewName: input.viewName.trim(),
    viewItemId: input.viewItemId.trim(),
    text: input.text?.trim() || "",
    isAdded: Boolean(input.isAdded)
  };
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
  if (input.orderByItem !== undefined) {
    request.orderByItem = input.orderByItem;
  }
  if (input.orderByType !== undefined) {
    request.orderByType = input.orderByType;
  }
  return request;
}

function parseFilter(filterJson: string): Record<string, unknown> {
  const trimmed = filterJson.trim();
  if (!trimmed) {
    return {};
  }

  const parsed = JSON.parse(trimmed) as unknown;
  if (!parsed || Array.isArray(parsed) || typeof parsed !== "object") {
    throw new Error("Filter JSON must be an object.");
  }

  return parsed as Record<string, unknown>;
}

function buildVisibleFilter(filters: VisibleFilterInput[]): Record<string, unknown> {
  return filters.reduce<Record<string, unknown>>((result, filter) => {
    const property = filter.property.trim();
    if (!property) {
      return result;
    }

    const rangeValues = compactValues(filter.values || []);
    if (rangeValues.length === 2) {
      result[property] = {
        property,
        values: rangeValues
      };
      return result;
    }

    const value = filter.value?.trim();
    if (value) {
      result[property] = {
        property,
        value
      };
    }

    return result;
  }, {});
}

function compactValues(values: string[]): string[] {
  return values.map((value) => value.trim()).filter(Boolean);
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
