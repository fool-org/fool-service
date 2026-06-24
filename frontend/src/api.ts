export interface CommonResponse<T> {
  code: number;
  message: string;
  data: T;
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
  title?: string;
  property?: string;
}

export interface ListViewInfo {
  id?: number;
  viewName?: string;
  viewTitle?: string;
  browserTitle?: string;
  viewType?: string;
  inputInfo?: ViewInputInfo[];
  tableColumn?: TableColumnInfo[];
}

export interface PageNavigatorResult {
  pageSize?: number;
  pageIndex?: number;
  total?: number;
  pageCount?: number;
}

export interface ListDataItem {
  id?: string;
  values?: Record<string, unknown>;
  operation?: unknown[];
}

export interface ListViewResult {
  pageInfo?: PageNavigatorResult;
  items?: ListDataItem[];
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
