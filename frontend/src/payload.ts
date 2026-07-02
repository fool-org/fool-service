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
