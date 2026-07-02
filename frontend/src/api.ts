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
  showIndex?: number;
}

export interface OperationInfo {
  id?: number;
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
  viewTitle?: string;
  browserTitle?: string;
  viewType?: string;
  autoFreshTime?: number;
  inputInfo?: ViewInputInfo[];
  tableColumn?: TableColumnInfo[];
  operations?: OperationInfo[];
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
  rowFmt?: string;
  operation?: OperationInfo[];
}

export interface ListViewResult {
  pageInfo?: PageNavigatorResult;
  cols?: string[];
  freshTime?: string;
  autoFreshTime?: number;
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
