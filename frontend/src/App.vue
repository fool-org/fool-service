<script setup lang="ts">
import { computed, ref } from "vue";
import {
  type AuthItem,
  type CommonResponse,
  type GetEnumResult,
  type InputQueryResult,
  type QueryDataDetailResult,
  type ListDataItem,
  type ListViewInfo,
  type ListViewResult,
  type LoginVo,
  type TableColumnInfo,
  type TreeNode,
  type UserDTO,
  postApi
} from "./api";
import {
  buildGetEnumRequest,
  buildInputQueryRequest,
  buildLegacyListViewRequest,
  buildQueryDataDetailRequest,
  buildQueryRequest,
  buildSaveObjRequest,
  type VisibleFilterInput
} from "./payload";

const token = ref(localStorage.getItem("fool-service-token") || "");
const userId = ref("admin");
const password = ref("");
const viewName = ref("OrderList");
const legacyListViewId = ref(100);
const pageIndex = ref(1);
const pageSize = ref(20);
const filterJson = ref("{}");
const keyword = ref("");
const quickFilterProperty = ref("orderId");
const quickFilterMode = ref<"equals" | "range">("range");
const quickFilterValue = ref("");
const quickFilterFrom = ref("1001");
const quickFilterTo = ref("1002");
const inputQueryViewItemId = ref("symbol");
const inputQueryText = ref("BTC");
const inputQueryObjId = ref("");
const inputQueryOwnerId = ref("");
const inputQueryIsAdded = ref(false);
const detailViewId = ref(100);
const detailObjId = ref("1001");
const detailIdExp = ref("");
const enumModelId = ref("100");
const saveViewId = ref("100");
const saveObjId = ref("1001");
const savePropertyiesJson = ref('[{"key":"symbol","value":"BTC-USDT"},{"key":"state","value":"OPEN"}]');
const saveItempropertiesJson = ref("");
const activeSection = ref("auth");

const loginResponse = ref<CommonResponse<LoginVo> | null>(null);
const profileResponse = ref<CommonResponse<UserDTO> | null>(null);
const menuResponse = ref<CommonResponse<TreeNode<AuthItem>[]> | null>(null);
const viewResponse = ref<CommonResponse<ListViewInfo> | null>(null);
const dataResponse = ref<CommonResponse<ListViewResult> | null>(null);
const detailResponse = ref<CommonResponse<QueryDataDetailResult> | null>(null);
const enumResponse = ref<CommonResponse<GetEnumResult> | null>(null);
const inputQueryResponse = ref<CommonResponse<InputQueryResult> | null>(null);
const saveObjResponse = ref<CommonResponse<void> | null>(null);
const errorMessage = ref("");
const pendingAction = ref("");

const services = computed(() => [
  { label: "Docker Backend", value: "8080", state: "ready" },
  { label: "MySQL", value: "car_wash", state: "ready" },
  { label: "Redis", value: "6379", state: "ready" }
]);

const navItems = [
  { id: "auth", label: "Auth" },
  { id: "menus", label: "Menus" },
  { id: "views", label: "Views" },
  { id: "data", label: "Data Query" },
  { id: "migration", label: "Migration Map" }
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

function clearQuickFilter() {
  quickFilterValue.value = "";
  quickFilterFrom.value = "";
  quickFilterTo.value = "";
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
          <h1>Migration Console</h1>
          <p>Vue operator surface for the migrated Spring APIs.</p>
        </div>
        <div class="status-strip">
          <div v-for="service in services" :key="service.label" class="status-item">
            <span class="status-dot" :class="service.state"></span>
            <span>{{ service.label }}</span>
            <strong>{{ service.value }}</strong>
          </div>
        </div>
      </header>

      <section class="grid auth-grid" aria-labelledby="auth-heading">
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
          <button class="primary" type="button" :disabled="pendingAction === 'login'" @click="login">
            Login
          </button>
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
            <button type="button" :disabled="pendingAction === 'menus'" @click="loadMenus">Menus</button>
          </div>
        </article>
      </section>

      <section class="grid work-grid" aria-label="View and data tools">
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
      </section>

      <section class="panel results-panel" aria-label="Results">
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
                profile: profileResponse,
                menus: menuResponse,
                view: viewResponse,
                data: dataResponse,
                detail: detailResponse,
                enums: enumResponse,
                inputQuery: inputQueryResponse,
                saveObj: saveObjResponse
              },
              null,
              2
            )
          }}</pre>
        </div>
      </section>

      <section class="migration-band" aria-label="Migration map">
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
