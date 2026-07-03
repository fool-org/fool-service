import type { InputQueryRequest } from "./api";

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
