<script setup lang="ts">
import { computed, ref } from "vue";
import {
  type AuthItem,
  type CheckCodeRequest,
  type CheckCodeResult,
  type CommonResponse,
  type GetEnumResult,
  type GetMessageResult,
  type GetNotifyResult,
  type InputQueryResult,
  type LegacyAppResult,
  type LegacyInitAppResult,
  type LegacyLoginResult,
  type LegacyMainResult,
  type LegacyRunOperationResult,
  type LegacySubMenuResult,
  type LegacyUserInfoResult,
  type QueryDataDetailResult,
  type ListDataItem,
  type ListViewInfo,
  type ListViewResult,
  type LoginVo,
  type ReadItemViewInfo,
  type ReportGridResult,
  type ReportModelResult,
  type TableColumnInfo,
  type TreeNode,
  type UserDTO,
  postApi
} from "./api";
import {
  buildGetEnumRequest,
  buildInitNewRequest,
  buildInputQueryRequest,
  buildLegacyListViewRequest,
  buildLegacyQueryDataRequest,
  buildLegacyReadItemViewRequest,
  buildMakeReportRequest,
  buildQueryDataDetailRequest,
  buildQueryRequest,
  buildRunOperationRequest,
  buildSaveObjRequest,
  buildSaveNewObjRequest,
  buildTokenRequest,
  type VisibleFilterInput
} from "./payload";

const token = ref(localStorage.getItem("fool-service-token") || "");
const userId = ref("admin");
const password = ref("");
const legacyAppId = ref("fool-service");
const legacyAppKey = ref("fool-service");
const legacyDbId = ref("car_wash");
const viewName = ref("OrderList");
const legacyListViewId = ref(100);
const readItemViewId = ref(100);
const pageIndex = ref(1);
const pageSize = ref(20);
const filterJson = ref("{}");
const keyword = ref("");
const quickFilterProperty = ref("orderId");
const quickFilterMode = ref<"equals" | "range">("range");
const quickFilterValue = ref("");
const quickFilterFrom = ref("");
const quickFilterTo = ref("");
const legacyQueryViewId = ref(100);
const legacyQueryPageIndex = ref(1);
const legacyQueryPageSize = ref(10);
const legacyQueryFilter = ref('order_state="0"');
const reportViewId = ref(100);
const reportCurrentPage = ref(1);
const reportPageSize = ref(10);
const reportQueryFilter = ref('order_state="0"');
const reportColsJson = ref('[{"colName":"Symbol","index":1},{"colName":"State","index":2}]');
const reportName = ref("Order Daily");
const inputQueryViewItemId = ref("symbol");
const inputQueryText = ref("BTC");
const inputQueryObjId = ref("");
const inputQueryOwnerId = ref("");
const inputQueryIsAdded = ref(false);
const detailViewId = ref(100);
const detailObjId = ref("1001");
const detailIdExp = ref("");
const initNewViewId = ref(100);
const initNewParentObjId = ref("");
const enumModelId = ref("102");
const saveViewId = ref("100");
const saveObjId = ref("1001");
const savePropertyiesJson = ref('[{"key":"symbol","value":"BTC-USDT"},{"key":"state","value":"0"}]');
const saveItempropertiesJson = ref("");
const saveNewViewId = ref("100");
const saveNewObjId = ref("9001");
const saveNewPropertyiesJson = ref('[{"key":"symbol","value":"SOL-USDT"},{"key":"state","value":"0"}]');
const saveNewOwnerViewId = ref("");
const saveNewOwnerId = ref("");
const saveNewProperty = ref("items");
const operationObjectId = ref("1001");
const operationViewId = ref(100);
const operationId = ref(7001);
const checkCodeKey = ref("");
const checkCodeValue = ref("");
const subMenuParentAuthCode = ref("");
const activeSection = ref("orders");
const selectedOrderId = ref("");
const editableSymbol = ref("");
const editableState = ref("0");

const loginResponse = ref<CommonResponse<LoginVo> | null>(null);
const initAppResponse = ref<CommonResponse<LegacyInitAppResult> | null>(null);
const legacyLoginResponse = ref<CommonResponse<LegacyLoginResult> | null>(null);
const profileResponse = ref<CommonResponse<UserDTO> | null>(null);
const legacyUserInfoResponse = ref<CommonResponse<LegacyUserInfoResult> | null>(null);
const mainInfoResponse = ref<CommonResponse<LegacyMainResult> | null>(null);
const appInfoResponse = ref<CommonResponse<LegacyAppResult> | null>(null);
const checkCodeResponse = ref<CommonResponse<CheckCodeResult> | null>(null);
const checkCodeValidationResponse = ref<CommonResponse<boolean> | null>(null);
const subMenuResponse = ref<CommonResponse<LegacySubMenuResult> | null>(null);
const logoutResponse = ref<CommonResponse<void> | null>(null);
const menuResponse = ref<CommonResponse<TreeNode<AuthItem>[]> | null>(null);
const viewResponse = ref<CommonResponse<ListViewInfo> | null>(null);
const readItemViewResponse = ref<CommonResponse<ReadItemViewInfo> | null>(null);
const dataResponse = ref<CommonResponse<ListViewResult> | null>(null);
const detailResponse = ref<CommonResponse<QueryDataDetailResult> | null>(null);
const initNewResponse = ref<CommonResponse<QueryDataDetailResult> | null>(null);
const enumResponse = ref<CommonResponse<GetEnumResult> | null>(null);
const inputQueryResponse = ref<CommonResponse<InputQueryResult> | null>(null);
const saveObjResponse = ref<CommonResponse<void> | null>(null);
const saveNewObjResponse = ref<CommonResponse<void> | null>(null);
const runOperationResponse = ref<CommonResponse<LegacyRunOperationResult> | null>(null);
const reportResponse = ref<CommonResponse<ReportGridResult> | null>(null);
const reportModelResponse = ref<CommonResponse<ReportModelResult> | null>(null);
const saveReportResponse = ref<CommonResponse<void> | null>(null);
const messageResponse = ref<CommonResponse<GetMessageResult> | null>(null);
const notifyResponse = ref<CommonResponse<GetNotifyResult> | null>(null);
const backendSmokeResponse = ref<CommonResponse<Record<string, unknown>[]> | null>(null);
const errorMessage = ref("");
const pendingAction = ref("");

const services = computed(() => [
  { label: "Docker Backend", value: "8080", state: "ready" },
  { label: "MySQL", value: "car_wash", state: "ready" },
  { label: "Redis", value: "6379", state: "ready" }
]);

const navItems = [
  { id: "orders", label: "Orders" },
  { id: "tools", label: "API Tools" },
  { id: "migration", label: "Migration" }
];

const resultColumns = computed<TableColumnInfo[]>(() => {
  const declared = viewResponse.value?.data?.tableColumn || [];
  if (declared.length > 0) {
    return declared;
  }

  const first = dataResponse.value?.data?.items?.[0]?.values;
  if (!first) {
    return [];
  }

  return Object.keys(first).map((property) => ({ property, title: property }));
});

const resultRows = computed<ListDataItem[]>(() => dataResponse.value?.data?.items || []);
const selectedOrder = computed(() => resultRows.value.find((row) => getOrderId(row) === selectedOrderId.value));
const detailRows = computed(() => detailResponse.value?.data?.data?.simpleData || []);
const orderStateOptions = computed(() => {
  const enums = enumResponse.value?.data?.enumValues || [];
  if (enums.length > 0) {
    return enums.map((item) => ({ label: item.name || String(item.value), value: String(item.value ?? "") }));
  }
  return [
    { label: "Open", value: "0" },
    { label: "Filled", value: "1" }
  ];
});

const reportRows = computed(() => {
  const cells = reportResponse.value?.data?.cells || [];
  const maxRow = cells.reduce((max, cell) => Math.max(max, cell.row), -1);
  const maxCol = cells.reduce((max, cell) => Math.max(max, cell.col), -1);
  if (maxRow < 0 || maxCol < 0) {
    return [];
  }

  const byPosition = new Map(cells.map((cell) => [`${cell.row}:${cell.col}`, cell.fmtValue || ""]));
  return Array.from({ length: maxRow + 1 }, (_, row) =>
    Array.from({ length: maxCol + 1 }, (_, col) => byPosition.get(`${row}:${col}`) || "")
  );
});

const quickFilterOptions = computed(() => {
  const declared = viewResponse.value?.data?.tableColumn || [];
  const defaults = [
    { property: "orderId", title: "Order ID" },
    { property: "symbol", title: "Symbol" },
    { property: "state", title: "State" }
  ];
  const seen = new Set<string>();

  return [...declared, ...defaults].filter((column) => {
    const property = column.property || "";
    if (!property || seen.has(property)) {
      return false;
    }
    seen.add(property);
    return true;
  });
});

const visibleFilters = computed<VisibleFilterInput[]>(() => {
  if (quickFilterMode.value === "range") {
    return [
      {
        property: quickFilterProperty.value,
        values: [quickFilterFrom.value, quickFilterTo.value]
      }
    ];
  }

  return [
    {
      property: quickFilterProperty.value,
      value: quickFilterValue.value
    }
  ];
});

async function runAction<T>(label: string, action: () => Promise<CommonResponse<T>>) {
  pendingAction.value = label;
  errorMessage.value = "";

  try {
    return await action();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : String(error);
    return null;
  } finally {
    pendingAction.value = "";
  }
}

async function login() {
  const response = await runAction("login", () =>
    postApi<LoginVo>("/api/v1/auth/login", {
      userId: userId.value,
      password: password.value
    })
  );

  if (response) {
    loginResponse.value = response;
    token.value = response.data?.token || token.value;
    if (token.value) {
      localStorage.setItem("fool-service-token", token.value);
    }
  }
}

async function initApp() {
  const response = await runAction("initapp", () =>
    postApi<LegacyInitAppResult>("/api/v1/auth/initapp", {
      AppId: legacyAppId.value,
      AppKey: legacyAppKey.value
    })
  );

  if (response) {
    initAppResponse.value = response;
    checkCodeKey.value = response.data?.checkCode?.key || checkCodeKey.value;
    checkCodeValue.value = response.data?.checkCode?.code || checkCodeValue.value;
    legacyDbId.value = response.data?.dbs?.[0]?.dbId || legacyDbId.value;
  }
}

async function loginV2() {
  const response = await runAction("loginv2", () =>
    postApi<LegacyLoginResult>("/api/v1/auth/loginv2", {
      UserId: userId.value,
      PassWord: password.value,
      DbId: legacyDbId.value,
      CheckCode: checkCodeValue.value,
      AppId: legacyAppId.value,
      AppKey: legacyAppKey.value,
      CheckCodeKey: checkCodeKey.value
    })
  );

  if (response) {
    legacyLoginResponse.value = response;
    token.value = response.data?.token || token.value;
    if (token.value) {
      localStorage.setItem("fool-service-token", token.value);
    }
  }
}

async function loadProfile() {
  const response = await runAction("profile", () =>
    postApi<UserDTO>("/api/v1/auth/profile", {
      token: token.value
    })
  );
  if (response) {
    profileResponse.value = response;
  }
}

async function loadLegacyUserInfo() {
  const response = await runAction("getuserinfo", () =>
    postApi<LegacyUserInfoResult>("/api/v1/auth/getuserinfo", buildTokenRequest(token.value))
  );
  if (response) {
    legacyUserInfoResponse.value = response;
  }
}

async function loadMainInfo() {
  const response = await runAction("getmain", () => postApi<LegacyMainResult>("/api/v1/auth/getmain", token.value));
  if (response) {
    mainInfoResponse.value = response;
  }
}

async function loadAppInfo() {
  const response = await runAction("getapp", () =>
    postApi<LegacyAppResult>("/api/v1/auth/getapp", buildTokenRequest(token.value))
  );
  if (response) {
    appInfoResponse.value = response;
  }
}

async function loadCheckCode() {
  const response = await runAction("getcheckcode", () => postApi<CheckCodeResult>("/api/v1/auth/getcheckcode", {}));
  if (response) {
    checkCodeResponse.value = response;
    checkCodeKey.value = response.data?.key || "";
    checkCodeValue.value = response.data?.code || "";
  }
}

async function validateCheckCode() {
  const request: CheckCodeRequest = {
    key: checkCodeKey.value,
    code: checkCodeValue.value
  };
  const response = await runAction("checkcode", () => postApi<boolean>("/api/v1/auth/checkcode", request));
  if (response) {
    checkCodeValidationResponse.value = response;
  }
}

async function loadMenus() {
  const response = await runAction("menus", () =>
    postApi<TreeNode<AuthItem>[]>("/api/v1/auth/auth-menus", {
      token: token.value
    })
  );
  if (response) {
    menuResponse.value = response;
  }
}

async function loadSubMenu() {
  const response = await runAction("getsubmenu", () =>
    postApi<LegacySubMenuResult>("/api/v1/auth/getsubmenu", {
      token: token.value,
      ParentAuthCode: subMenuParentAuthCode.value.trim()
    })
  );
  if (response) {
    subMenuResponse.value = response;
  }
}

async function loadMessages() {
  const response = await runAction("messages", () =>
    postApi<GetMessageResult>("/api/v1/message/getmsg", buildTokenRequest(token.value))
  );
  if (response) {
    messageResponse.value = response;
  }
}

async function loadNotify() {
  const response = await runAction("notify", () =>
    postApi<GetNotifyResult>("/api/v1/message/getnotify", buildTokenRequest(token.value))
  );
  if (response) {
    notifyResponse.value = response;
  }
}

async function logout() {
  const response = await runAction("logout", () => postApi<void>("/api/v1/auth/logout", buildTokenRequest(token.value)));
  if (response) {
    logoutResponse.value = response;
    token.value = "";
    localStorage.removeItem("fool-service-token");
  }
}

async function loadView() {
  const response = await runAction("view", () =>
    postApi<ListViewInfo>("/api/v1/view/get-view", {
      token: token.value,
      viewName: viewName.value
    })
  );
  if (response) {
    viewResponse.value = response;
  }
}

async function loadLegacyListView() {
  const request = buildLegacyListViewRequest({
    token: token.value,
    viewId: Number(legacyListViewId.value)
  });

  const response = await runAction("legacy-list-view", () => postApi<ListViewInfo>("/api/v1/view/getlistview", request));
  if (response) {
    viewResponse.value = response;
  }
}

async function loadReadItemView() {
  const request = buildLegacyReadItemViewRequest({
    token: token.value,
    viewId: Number(readItemViewId.value)
  });

  const response = await runAction("read-item-view", () =>
    postApi<ReadItemViewInfo>("/api/v1/view/getreaditemview", request)
  );
  if (response) {
    readItemViewResponse.value = response;
  }
}

async function queryData() {
  const request = buildQueryRequest({
    token: token.value,
    viewName: viewName.value,
    pageIndex: Number(pageIndex.value),
    pageSize: Number(pageSize.value),
    filterJson: filterJson.value,
    keyword: keyword.value,
    visibleFilters: visibleFilters.value
  });

  const response = await runAction("query", () => postApi<ListViewResult>("/api/v1/data/query-list", request));
  if (response) {
    dataResponse.value = response;
  }
}

async function queryLegacyData() {
  const request = buildLegacyQueryDataRequest({
    token: token.value,
    viewId: Number(legacyQueryViewId.value),
    pageIndex: Number(legacyQueryPageIndex.value),
    pageSize: Number(legacyQueryPageSize.value),
    queryFilter: legacyQueryFilter.value
  });

  const response = await runAction("legacy-query", () => postApi<ListViewResult>("/api/v1/data/querydata", request));
  if (response) {
    dataResponse.value = response;
  }
}

async function runReport(action: string, path: string) {
  const request = buildMakeReportRequest({
    token: token.value,
    viewId: Number(reportViewId.value),
    currentPage: Number(reportCurrentPage.value),
    pageSize: Number(reportPageSize.value),
    queryFilter: reportQueryFilter.value,
    reportColsJson: reportColsJson.value
  });

  const response = await runAction(action, () => postApi<ReportGridResult>(path, request));
  if (response) {
    reportResponse.value = response;
  }
}

async function makeReport() {
  await runReport("makereport", "/api/v1/report/makereport");
}

async function getReport() {
  await runReport("getrpt", "/api/v1/report/getrpt");
}

async function loadReportColumns() {
  const request = buildLegacyListViewRequest({
    token: token.value,
    viewId: Number(reportViewId.value)
  });

  const response = await runAction("report-columns", () =>
    postApi<ReportModelResult>("/api/v1/report/getmkqview", request)
  );
  if (response) {
    reportModelResponse.value = response;
  }
}

async function saveReport() {
  const request = buildMakeReportRequest({
    token: token.value,
    viewId: Number(reportViewId.value),
    currentPage: Number(reportCurrentPage.value),
    pageSize: Number(reportPageSize.value),
    queryFilter: reportQueryFilter.value,
    reportColsJson: reportColsJson.value,
    reportName: reportName.value
  });

  const response = await runAction("saverpt", () => postApi<void>("/api/v1/report/saverpt", request));
  if (response) {
    saveReportResponse.value = response;
  }
}

async function inputQuery() {
  const request = buildInputQueryRequest({
    token: token.value,
    viewName: viewName.value,
    viewItemId: inputQueryViewItemId.value,
    text: inputQueryText.value,
    objID: inputQueryObjId.value,
    ownerId: inputQueryOwnerId.value,
    isAdded: inputQueryIsAdded.value
  });

  const response = await runAction("inputquery", () =>
    postApi<InputQueryResult>("/api/v1/data/inputquery", request)
  );
  if (response) {
    inputQueryResponse.value = response;
  }
}

async function queryDetail() {
  const request = buildQueryDataDetailRequest({
    token: token.value,
    viewId: Number(detailViewId.value),
    objId: detailObjId.value,
    idExp: detailIdExp.value
  });

  const response = await runAction("detail", () =>
    postApi<QueryDataDetailResult>("/api/v1/data/querydatadetail", request)
  );
  if (response) {
    detailResponse.value = response;
  }
}

async function initNew() {
  const request = buildInitNewRequest({
    token: token.value,
    viewId: Number(initNewViewId.value),
    parentObjId: initNewParentObjId.value
  });

  const response = await runAction("initnew", () =>
    postApi<QueryDataDetailResult>("/api/v1/data/initnew", request)
  );
  if (response) {
    initNewResponse.value = response;
  }
}

async function loadEnums() {
  const request = buildGetEnumRequest({
    token: token.value,
    modelId: enumModelId.value
  });

  const response = await runAction("getenums", () => postApi<GetEnumResult>("/api/v1/data/getenums", request));
  if (response) {
    enumResponse.value = response;
  }
}

async function saveObj() {
  const request = buildSaveObjRequest({
    token: token.value,
    id: saveObjId.value,
    viewID: saveViewId.value,
    propertyiesJson: savePropertyiesJson.value,
    itempropertiesJson: saveItempropertiesJson.value
  });

  const response = await runAction("saveobj", () => postApi<void>("/api/v1/data/saveobj", request));
  if (response) {
    saveObjResponse.value = response;
  }
}

async function saveNewObj() {
  const request = buildSaveNewObjRequest({
    token: token.value,
    id: saveNewObjId.value,
    viewID: saveNewViewId.value,
    propertyiesJson: saveNewPropertyiesJson.value,
    ownerViewId: saveNewOwnerViewId.value,
    ownerId: saveNewOwnerId.value,
    property: saveNewProperty.value
  });

  const response = await runAction("savenewobj", () => postApi<void>("/api/v1/data/savenewobj", request));
  if (response) {
    saveNewObjResponse.value = response;
  }
}

async function runOperation() {
  const request = buildRunOperationRequest({
    token: token.value,
    objectId: operationObjectId.value,
    viewId: Number(operationViewId.value),
    operationId: Number(operationId.value)
  });

  const response = await runAction("runoperation", () =>
    postApi<LegacyRunOperationResult>("/api/v1/data/runoperation", request)
  );
  if (response) {
    runOperationResponse.value = response;
  }
}

async function loadBackendSmoke() {
  const response = await runAction("backend-smoke", async () => {
    const backendResponse = await fetch("/test");
    const data = (await backendResponse.json().catch(() => null)) as Record<string, unknown>[] | null;
    if (!backendResponse.ok) {
      throw new Error(`GET /test failed with HTTP ${backendResponse.status}`);
    }
    if (!Array.isArray(data)) {
      throw new Error("GET /test returned an unexpected payload.");
    }
    return {
      code: 0,
      message: "OK",
      data
    };
  });
  if (response) {
    backendSmokeResponse.value = response;
  }
}

function clearQuickFilter() {
  quickFilterValue.value = "";
  quickFilterFrom.value = "";
  quickFilterTo.value = "";
}

async function loadOrdersWorkflow() {
  await loadLegacyListView();
  await loadEnums();
  await queryData();
  const firstOrder = resultRows.value[0];
  if (firstOrder) {
    await selectOrder(firstOrder);
  }
}

async function selectOrder(row: ListDataItem) {
  const orderId = getOrderId(row);
  if (!orderId) {
    return;
  }
  selectedOrderId.value = orderId;
  editableSymbol.value = formatValue(row.values?.symbol);
  editableState.value = formatValue(row.values?.state) || "0";
  detailObjId.value = orderId;
  saveObjId.value = orderId;
  operationObjectId.value = orderId;
  await queryDetail();
}

async function saveSelectedOrder() {
  if (!selectedOrderId.value) {
    errorMessage.value = "Select an order first.";
    return;
  }
  saveObjId.value = selectedOrderId.value;
  savePropertyiesJson.value = JSON.stringify([
    { key: "symbol", value: editableSymbol.value },
    { key: "state", value: editableState.value }
  ]);
  await saveObj();
  await queryData();
  await queryDetail();
}

function getOrderId(row: ListDataItem) {
  return formatValue(row.values?.orderId || row.id);
}

function formatValue(value: unknown) {
  if (value === null || value === undefined) {
    return "";
  }

  if (typeof value === "object") {
    return JSON.stringify(value);
  }

  return String(value);
}
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">F</span>
        <div>
          <strong>Fool Service</strong>
          <small>FoolFrame migration</small>
        </div>
      </div>

      <nav class="nav-list" aria-label="Main">
        <button
          v-for="item in navItems"
          :key="item.id"
          type="button"
          :class="{ active: activeSection === item.id }"
          @click="activeSection = item.id"
        >
          {{ item.label }}
        </button>
      </nav>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div>
          <h1>OrderList</h1>
          <p>Work with the Docker-seeded order view.</p>
        </div>
        <div class="status-strip">
          <div v-for="service in services" :key="service.label" class="status-item">
            <span class="status-dot" :class="service.state"></span>
            <span>{{ service.label }}</span>
            <strong>{{ service.value }}</strong>
          </div>
        </div>
      </header>

      <section v-if="activeSection === 'orders'" class="order-workflow" aria-label="OrderList workflow">
        <article class="panel order-list-panel">
          <div class="panel-heading">
            <h2>Orders</h2>
            <span>OrderList</span>
          </div>
          <div class="workflow-toolbar">
            <label>
              Keyword
              <input v-model="keyword" placeholder="symbol or visible text" />
            </label>
            <label>
              Page size
              <input v-model.number="pageSize" min="1" type="number" />
            </label>
            <button class="primary" type="button" :disabled="Boolean(pendingAction)" @click="loadOrdersWorkflow">
              Load Orders
            </button>
          </div>

          <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

          <div class="table-wrap orders-table">
            <table v-if="resultRows.length">
              <thead>
                <tr>
                  <th>Order ID</th>
                  <th>Symbol</th>
                  <th>State</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="row in resultRows"
                  :key="getOrderId(row)"
                  :class="{ selected: getOrderId(row) === selectedOrderId }"
                >
                  <td>{{ formatValue(row.values?.orderId) }}</td>
                  <td>{{ formatValue(row.values?.symbol) }}</td>
                  <td>{{ formatValue(row.values?.state) }}</td>
                  <td>
                    <button type="button" :disabled="Boolean(pendingAction)" @click="selectOrder(row)">Open</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">Load orders to start.</div>
          </div>
        </article>

        <article class="panel order-detail-panel">
          <div class="panel-heading">
            <h2>Order Detail</h2>
            <span>{{ selectedOrderId || "No order selected" }}</span>
          </div>

          <div v-if="selectedOrder" class="order-edit-grid">
            <label>
              Symbol
              <input v-model="editableSymbol" />
            </label>
            <label>
              State
              <select v-model="editableState">
                <option v-for="state in orderStateOptions" :key="state.value" :value="state.value">
                  {{ state.label }}
                </option>
              </select>
            </label>
            <button class="primary" type="button" :disabled="Boolean(pendingAction)" @click="saveSelectedOrder">
              Save Order
            </button>
          </div>
          <div v-else class="empty-state compact">Select an order from the list.</div>

          <div class="detail-fields">
            <div v-for="item in detailRows" :key="item.prpId || item.prpShowName">
              <span>{{ item.prpShowName || item.prpId }}</span>
              <strong>{{ item.fmtValue }}</strong>
            </div>
          </div>
        </article>
      </section>

      <section v-if="activeSection === 'tools'" class="grid auth-grid" aria-labelledby="auth-heading">
        <article class="panel">
          <div class="panel-heading">
            <h2 id="auth-heading">Auth Session</h2>
            <span>POST /api/v1/auth/login</span>
          </div>
          <label>
            User ID
            <input v-model="userId" autocomplete="username" />
          </label>
          <label>
            Password
            <input v-model="password" type="password" autocomplete="current-password" />
          </label>
          <div class="inline-fields">
            <label>
              App ID
              <input v-model="legacyAppId" />
            </label>
            <label>
              App Key
              <input v-model="legacyAppKey" />
            </label>
            <label>
              DB ID
              <input v-model="legacyDbId" />
            </label>
          </div>
          <button class="primary" type="button" :disabled="pendingAction === 'login'" @click="login">
            Login
          </button>
          <button type="button" :disabled="pendingAction === 'initapp'" @click="initApp">Init App</button>
          <button type="button" :disabled="pendingAction === 'loginv2'" @click="loginV2">Legacy Login V2</button>
        </article>

        <article class="panel">
          <div class="panel-heading">
            <h2>Token & Profile</h2>
            <span>profile / menus</span>
          </div>
          <label>
            Token
            <textarea v-model="token" rows="3" spellcheck="false"></textarea>
          </label>
          <div class="button-row">
            <button type="button" :disabled="pendingAction === 'profile'" @click="loadProfile">Profile</button>
            <button type="button" :disabled="pendingAction === 'getuserinfo'" @click="loadLegacyUserInfo">
              Legacy User Info
            </button>
            <button type="button" :disabled="pendingAction === 'getmain'" @click="loadMainInfo">Main Info</button>
            <button type="button" :disabled="pendingAction === 'getapp'" @click="loadAppInfo">App Info</button>
            <button type="button" :disabled="pendingAction === 'menus'" @click="loadMenus">Menus</button>
            <button type="button" :disabled="pendingAction === 'logout'" @click="logout">Logout</button>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Check Code</h2>
            <span>POST /api/v1/auth/getcheckcode</span>
          </div>
          <div class="inline-fields">
            <label>
              Key
              <input v-model="checkCodeKey" />
            </label>
            <label>
              Code
              <input v-model="checkCodeValue" />
            </label>
          </div>
          <div class="button-row">
            <button type="button" :disabled="pendingAction === 'getcheckcode'" @click="loadCheckCode">
              Load Check Code
            </button>
            <button type="button" :disabled="pendingAction === 'checkcode'" @click="validateCheckCode">
              Validate Code
            </button>
          </div>

          <div v-if="checkCodeResponse?.data" class="summary-list">
            <div><span>Image bytes</span><strong>{{ checkCodeResponse.data.chkCodeImg?.length || 0 }}</strong></div>
            <div><span>Valid</span><strong>{{ checkCodeValidationResponse?.data ?? "-" }}</strong></div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Sub Menu</h2>
            <span>POST /api/v1/auth/getsubmenu</span>
          </div>
          <label>
            Parent Auth Code
            <input v-model="subMenuParentAuthCode" placeholder="blank for top level" />
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'getsubmenu'" @click="loadSubMenu">
            Load Sub Menu
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="subMenuResponse?.data?.items?.length">
              <thead>
                <tr>
                  <th>Auth</th>
                  <th>Text</th>
                  <th>View</th>
                  <th>Index</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in subMenuResponse.data.items" :key="item.authNo || item.text">
                  <td>{{ item.authNo }}</td>
                  <td>{{ item.text }}</td>
                  <td>{{ item.viewId }}</td>
                  <td>{{ item.index }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No submenu items loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Messages</h2>
            <span>POST /api/v1/message/getmsg</span>
          </div>
          <button class="primary" type="button" :disabled="pendingAction === 'messages'" @click="loadMessages">
            Load Messages
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="messageResponse?.data?.messages?.length">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Content</th>
                  <th>View</th>
                  <th>Result</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="message in messageResponse.data.messages" :key="message.messageID">
                  <td>{{ message.messageID }}</td>
                  <td>{{ message.messageContent }}</td>
                  <td>{{ message.resultView }}</td>
                  <td>{{ message.resultKey }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No generated messages loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Notify Counts</h2>
            <span>POST /api/v1/message/getnotify</span>
          </div>
          <button class="primary" type="button" :disabled="pendingAction === 'notify'" @click="loadNotify">
            Load Notify Counts
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="notifyResponse?.data?.notifies?.length">
              <thead>
                <tr>
                  <th>Auth</th>
                  <th>Count</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in notifyResponse.data.notifies" :key="item.authNo || item.count">
                  <td>{{ item.authNo }}</td>
                  <td>{{ item.count }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No notify counts loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Backend Smoke</h2>
            <span>GET /test</span>
          </div>
          <button class="primary" type="button" :disabled="pendingAction === 'backend-smoke'" @click="loadBackendSmoke">
            Load Seed Data
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="backendSmokeResponse?.data?.length">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Order Price</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in backendSmokeResponse.data" :key="String(row.id || row.order_price)">
                  <td>{{ formatValue(row.id) }}</td>
                  <td>{{ formatValue(row.order_price) }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No backend smoke rows loaded.</div>
          </div>
        </article>
      </section>

      <section v-if="activeSection === 'tools'" class="grid work-grid" aria-label="View and data tools">
        <article class="panel">
          <div class="panel-heading">
            <h2>View Definition</h2>
            <span>POST /api/v1/view/get-view</span>
          </div>
          <label>
            View Name
            <input v-model="viewName" />
          </label>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model.number="legacyListViewId" min="1" type="number" />
            </label>
          </div>
          <div class="button-row">
            <button type="button" :disabled="pendingAction === 'view'" @click="loadView">Load View</button>
            <button type="button" :disabled="pendingAction === 'legacy-list-view'" @click="loadLegacyListView">
              Legacy List View
            </button>
          </div>

          <div v-if="viewResponse?.data" class="summary-list">
            <div><span>Title</span><strong>{{ viewResponse.data.viewTitle || "-" }}</strong></div>
            <div><span>Type</span><strong>{{ viewResponse.data.viewType || "-" }}</strong></div>
            <div><span>Columns</span><strong>{{ viewResponse.data.tableColumn?.length || 0 }}</strong></div>
            <div><span>Inputs</span><strong>{{ viewResponse.data.inputInfo?.length || 0 }}</strong></div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Read Item View</h2>
            <span>POST /api/v1/view/getreaditemview</span>
          </div>
          <label>
            View ID
            <input v-model.number="readItemViewId" min="1" type="number" />
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'read-item-view'" @click="loadReadItemView">
            Load Read Items
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="readItemViewResponse?.data?.items?.length">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Property</th>
                  <th>Type</th>
                  <th>Edit</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in readItemViewResponse.data.items" :key="item.prpId || item.name">
                  <td>{{ item.name }}</td>
                  <td>{{ item.prpId }}</td>
                  <td>{{ item.prpType }}</td>
                  <td>{{ item.editType }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No read items loaded.</div>
          </div>
        </article>

        <article class="panel">
          <div class="panel-heading">
            <h2>Data Query</h2>
            <span>POST /api/v1/data/query-list</span>
          </div>
          <div class="inline-fields">
            <label>
              Page
              <input v-model.number="pageIndex" min="1" type="number" />
            </label>
            <label>
              Size
              <input v-model.number="pageSize" min="1" type="number" />
            </label>
          </div>
          <div class="filter-builder">
            <div class="filter-toolbar">
              <label>
                Keyword
                <input v-model="keyword" />
              </label>
              <label>
                Property
                <select v-model="quickFilterProperty">
                  <option v-for="column in quickFilterOptions" :key="column.property" :value="column.property">
                    {{ column.title || column.property }}
                  </option>
                </select>
              </label>
              <div class="mode-toggle" aria-label="Filter mode">
                <button
                  type="button"
                  :class="{ active: quickFilterMode === 'equals' }"
                  @click="quickFilterMode = 'equals'"
                >
                  Equals
                </button>
                <button
                  type="button"
                  :class="{ active: quickFilterMode === 'range' }"
                  @click="quickFilterMode = 'range'"
                >
                  Range
                </button>
              </div>
            </div>
            <div v-if="quickFilterMode === 'equals'" class="inline-fields">
              <label>
                Value
                <input v-model="quickFilterValue" />
              </label>
              <button type="button" @click="clearQuickFilter">Clear</button>
            </div>
            <div v-else class="inline-fields">
              <label>
                From
                <input v-model="quickFilterFrom" />
              </label>
              <label>
                To
                <input v-model="quickFilterTo" />
              </label>
            </div>
          </div>
          <label>
            Filter JSON
            <textarea v-model="filterJson" rows="6" spellcheck="false"></textarea>
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'query'" @click="queryData">
            Query Data
          </button>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Legacy Query Data</h2>
            <span>POST /api/v1/data/querydata</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model.number="legacyQueryViewId" min="1" type="number" />
            </label>
            <label>
              Page
              <input v-model.number="legacyQueryPageIndex" min="1" type="number" />
            </label>
            <label>
              Size
              <input v-model.number="legacyQueryPageSize" min="1" type="number" />
            </label>
          </div>
          <label>
            QueryFilter
            <input v-model="legacyQueryFilter" />
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'legacy-query'" @click="queryLegacyData">
            Legacy Query Data
          </button>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Report Columns</h2>
            <span>POST /api/v1/report/getmkqview</span>
          </div>
          <label>
            View ID
            <input v-model.number="reportViewId" min="1" type="number" />
          </label>
          <button
            class="primary"
            type="button"
            :disabled="pendingAction === 'report-columns'"
            @click="loadReportColumns"
          >
            Load Columns
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="reportModelResponse?.data?.cols?.length">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>ID</th>
                  <th>Type</th>
                  <th>Compare</th>
                  <th>Select</th>
                  <th>States</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="col in reportModelResponse.data.cols" :key="col.id || col.name">
                  <td>{{ col.name }}</td>
                  <td>{{ col.id }}</td>
                  <td>{{ col.prpType }}</td>
                  <td>{{ col.compareTypes?.map((item) => item.name || item.id).join(", ") }}</td>
                  <td>{{ col.queryTypes?.map((item) => item.name || item.id).join(", ") }}</td>
                  <td>{{ col.states?.map((item) => item.showName || item.dbName).join(", ") }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No report columns loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Report Grid</h2>
            <span>POST /api/v1/report/makereport | /api/v1/report/getrpt</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model.number="reportViewId" min="1" type="number" />
            </label>
            <label>
              Page
              <input v-model.number="reportCurrentPage" min="1" type="number" />
            </label>
            <label>
              Size
              <input v-model.number="reportPageSize" min="1" type="number" />
            </label>
          </div>
          <label>
            QueryFilter
            <input v-model="reportQueryFilter" />
          </label>
          <label>
            Report Columns JSON
            <textarea v-model="reportColsJson" rows="3" spellcheck="false"></textarea>
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'makereport'" @click="makeReport">
            Make Report
          </button>
          <button type="button" :disabled="pendingAction === 'getrpt'" @click="getReport">
            Get Report
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="reportRows.length">
              <thead>
                <tr>
                  <th v-for="(cell, index) in reportRows[0]" :key="`report-head-${index}`">
                    {{ cell }}
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, rowIndex) in reportRows.slice(1)" :key="`report-row-${rowIndex}`">
                  <td v-for="(cell, colIndex) in row" :key="`report-cell-${rowIndex}-${colIndex}`">
                    {{ cell }}
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No report cells loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Save Report Definition</h2>
            <span>POST /api/v1/report/saverpt</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model.number="reportViewId" min="1" type="number" />
            </label>
            <label>
              Name
              <input v-model="reportName" />
            </label>
          </div>
          <label>
            QueryFilter
            <input v-model="reportQueryFilter" />
          </label>
          <label>
            Report Columns JSON
            <textarea v-model="reportColsJson" rows="3" spellcheck="false"></textarea>
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'saverpt'" @click="saveReport">
            Save Report
          </button>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Detail Data</h2>
            <span>POST /api/v1/data/querydatadetail</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model.number="detailViewId" min="1" type="number" />
            </label>
            <label>
              Object ID
              <input v-model="detailObjId" />
            </label>
          </div>
          <label>
            ID Exp
            <input v-model="detailIdExp" />
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'detail'" @click="queryDetail">
            Load Detail
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="detailResponse?.data?.data?.simpleData?.length">
              <thead>
                <tr>
                  <th>Property</th>
                  <th>Value</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in detailResponse.data.data.simpleData" :key="item.prpId || item.prpShowName">
                  <td>{{ item.prpShowName || item.prpId }}</td>
                  <td>{{ item.fmtValue }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No detail loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Init New Object</h2>
            <span>POST /api/v1/data/initnew</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model.number="initNewViewId" min="1" type="number" />
            </label>
            <label>
              Parent ID
              <input v-model="initNewParentObjId" />
            </label>
          </div>
          <button class="primary" type="button" :disabled="pendingAction === 'initnew'" @click="initNew">
            Init New
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="initNewResponse?.data?.data?.simpleData?.length">
              <thead>
                <tr>
                  <th>Property</th>
                  <th>Value</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in initNewResponse.data.data.simpleData" :key="item.prpId || item.prpShowName">
                  <td>{{ item.prpShowName || item.prpId }}</td>
                  <td>{{ item.fmtValue }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No new object initialized.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Enum Values</h2>
            <span>POST /api/v1/data/getenums</span>
          </div>
          <label>
            Model ID
            <input v-model="enumModelId" />
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'getenums'" @click="loadEnums">
            Load Enums
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="enumResponse?.data?.enumValues?.length">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Value</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in enumResponse.data.enumValues" :key="`${item.name}-${item.value}`">
                  <td>{{ item.name }}</td>
                  <td>{{ item.value }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No enums loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Input Query</h2>
            <span>POST /api/v1/data/inputquery</span>
          </div>
          <div class="inline-fields">
            <label>
              View Item
              <input v-model="inputQueryViewItemId" />
            </label>
            <label>
              Text
              <input v-model="inputQueryText" />
            </label>
          </div>
          <div class="inline-fields">
            <label>
              Obj ID
              <input v-model="inputQueryObjId" />
            </label>
            <label>
              Owner ID
              <input v-model="inputQueryOwnerId" />
            </label>
          </div>
          <label class="checkbox-row">
            <input v-model="inputQueryIsAdded" type="checkbox" />
            Added item
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'inputquery'" @click="inputQuery">
            Query Candidates
          </button>

          <div class="table-wrap input-query-results">
            <table v-if="inputQueryResponse?.data?.items?.length">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Text</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in inputQueryResponse.data.items" :key="item.id || item.text">
                  <td>{{ item.id }}</td>
                  <td>{{ item.text }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No candidates loaded.</div>
          </div>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Save Object</h2>
            <span>POST /api/v1/data/saveobj</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model="saveViewId" />
            </label>
            <label>
              Object ID
              <input v-model="saveObjId" />
            </label>
          </div>
          <label>
            Propertyies JSON
            <textarea v-model="savePropertyiesJson" rows="4" spellcheck="false"></textarea>
          </label>
          <label>
            Itemproperties JSON
            <textarea v-model="saveItempropertiesJson" rows="4" spellcheck="false"></textarea>
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'saveobj'" @click="saveObj">
            Save Object
          </button>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Save New Object</h2>
            <span>POST /api/v1/data/savenewobj</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model="saveNewViewId" />
            </label>
            <label>
              Object ID
              <input v-model="saveNewObjId" />
            </label>
          </div>
          <div class="inline-fields">
            <label>
              Owner View
              <input v-model="saveNewOwnerViewId" />
            </label>
            <label>
              Owner ID
              <input v-model="saveNewOwnerId" />
            </label>
            <label>
              Property
              <input v-model="saveNewProperty" />
            </label>
          </div>
          <label>
            Propertyies JSON
            <textarea v-model="saveNewPropertyiesJson" rows="4" spellcheck="false"></textarea>
          </label>
          <button class="primary" type="button" :disabled="pendingAction === 'savenewobj'" @click="saveNewObj">
            Save New
          </button>
        </article>

        <article class="panel lookup-panel">
          <div class="panel-heading">
            <h2>Run Operation</h2>
            <span>POST /api/v1/data/runoperation</span>
          </div>
          <div class="inline-fields">
            <label>
              View ID
              <input v-model.number="operationViewId" min="1" type="number" />
            </label>
            <label>
              Operation ID
              <input v-model.number="operationId" min="1" type="number" />
            </label>
            <label>
              Object ID
              <input v-model="operationObjectId" />
            </label>
          </div>
          <button class="primary" type="button" :disabled="pendingAction === 'runoperation'" @click="runOperation">
            Run Operation
          </button>
        </article>
      </section>

      <section v-if="activeSection === 'tools'" class="panel results-panel" aria-label="Results">
        <div class="panel-heading">
          <h2>Response & Result Set</h2>
          <span v-if="pendingAction">Running {{ pendingAction }}...</span>
          <span v-else>Ready</span>
        </div>

        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <div class="result-layout">
          <div class="table-wrap">
            <table v-if="resultColumns.length > 0">
              <thead>
                <tr>
                  <th
                    v-for="column in resultColumns"
                    :key="column.property || column.title"
                    :style="{ width: column.width ? `${column.width}px` : undefined }"
                  >
                    {{ column.title || column.property }}
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in resultRows" :key="row.id || JSON.stringify(row.values)">
                  <td
                    v-for="column in resultColumns"
                    :key="column.property || column.title"
                    :style="{ width: column.width ? `${column.width}px` : undefined }"
                  >
                    {{ formatValue(row.values?.[column.property || ""]) }}
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-else class="empty-state">No query rows loaded.</div>
          </div>

          <pre class="json-output">{{
            JSON.stringify(
              {
                login: loginResponse,
                initApp: initAppResponse,
                legacyLogin: legacyLoginResponse,
                profile: profileResponse,
                legacyUserInfo: legacyUserInfoResponse,
                mainInfo: mainInfoResponse,
                appInfo: appInfoResponse,
                checkCode: checkCodeResponse,
                checkCodeValidation: checkCodeValidationResponse,
                subMenu: subMenuResponse,
                logout: logoutResponse,
                menus: menuResponse,
                view: viewResponse,
                readItemView: readItemViewResponse,
                data: dataResponse,
                detail: detailResponse,
                initNew: initNewResponse,
                enums: enumResponse,
                inputQuery: inputQueryResponse,
                saveObj: saveObjResponse,
                saveNewObj: saveNewObjResponse,
                runOperation: runOperationResponse,
                reportModel: reportModelResponse,
                report: reportResponse,
                saveReport: saveReportResponse,
                messages: messageResponse,
                notify: notifyResponse,
                backendSmoke: backendSmokeResponse
              },
              null,
              2
            )
          }}</pre>
        </div>
      </section>

      <section v-if="activeSection === 'migration'" class="migration-band" aria-label="Migration map">
        <div>
          <strong>SCPB01-Soway.Data</strong>
          <span>fool-common</span>
        </div>
        <div>
          <strong>SCPB02-Soway.DB</strong>
          <span>fool-dao</span>
        </div>
        <div>
          <strong>SCPB05-Soway.Model</strong>
          <span>fool-model</span>
        </div>
        <div>
          <strong>SWDQ01-Soway.Query</strong>
          <span>fool-query</span>
        </div>
        <div>
          <strong>Soway.Server</strong>
          <span>fool-view</span>
        </div>
        <div>
          <strong>SWUA Auth</strong>
          <span>fool-auth</span>
        </div>
      </section>
    </main>
  </div>
</template>
