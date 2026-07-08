import { describe, expect, it } from "vitest";
import nginxConfig from "../nginx.conf?raw";
import viteConfig from "../vite.config.ts?raw";
import appSource from "./App.vue?raw";
import listDataTableSource from "./ListDataTable.vue?raw";
import metadataFieldEditorSource from "./MetadataFieldEditor.vue?raw";
import resultsPanelSource from "./ResultsPanel.vue?raw";
import {
  buildGetEnumRequest,
  buildInputQueryRequest,
  buildLegacyListViewRequest,
  buildLegacyQueryDataRequest,
  buildLegacyReadItemViewRequest,
  buildMakeReportRequest,
  buildInitNewRequest,
  buildQueryDataDetailRequest,
  buildRunOperationRequest,
  buildSaveObjRequest,
  buildSaveNewObjRequest,
  buildTokenRequest
} from "./payload";

describe("App defaults", () => {
  it("opens with a metadata-driven view workflow before API tools", () => {
    expect(appSource).toContain('const activeSection = ref("views")');
    expect(appSource).toContain("onMounted");
    expect(appSource).toContain("void loadViewWorkflow()");
    expect(appSource).toContain("View workflow");
    expect(appSource).toContain("Load View");
    expect(appSource).toContain("await loadLegacyListView()");
    expect(appSource).toContain("await queryCurrentViewData()");
    expect(appSource).toContain('v-model.number="legacyListViewId"');
    expect(appSource).toContain("New Row");
    expect(appSource).toContain("Create Row");
    expect(appSource).toContain("Save Row");
    expect(appSource).toContain("async function selectObject");
    expect(appSource).toContain("async function startNewObject");
    expect(appSource).toContain("async function addDetailItem");
    expect(appSource).toContain("async function updateDetailItem");
    expect(appSource).toContain("async function deleteDetailItem");
    expect(appSource).toContain("resultColumns");
    expect(appSource).toContain("detailDrafts");
    expect(appSource).toContain("childDrafts");
    expect(listDataTableSource).toContain("rowValue(row, column)");
  });

  it("routes every view row table through the shared metadata renderer", () => {
    expect(appSource).toContain("const noRowOperations");
    expect(appSource).toContain('default-action-label="Select"');
    expect(resultsPanelSource).toContain(':show-default-action="false"');
    expect(appSource).not.toContain("rowValue(row, column)");
    expect(listDataTableSource).toContain("defaultActionLabel");
    expect(listDataTableSource).toContain("showDefaultAction");
  });

  it("renders detail View operations from the loaded detail payload", () => {
    expect(appSource).toContain("View Operations");
    expect(appSource).toContain("dataOperations(detailResponse.value?.data)");
    expect(appSource).toContain("detailViewOperations");
    expect(appSource).toContain("runViewOperation(operation)");
    expect(appSource).toContain("operation.params");
  });

  it("renders row operations through their target detail View id", () => {
    expect(appSource).toContain("listRowOperations");
    expect(appSource).toContain(':row-operations="listRowOperations"');
    expect(listDataTableSource).toContain("rowOperations");
    expect(listDataTableSource).toContain("emit('select', row, operationTargetViewId(operation))");
    expect(listDataTableSource).toContain("operationTargetViewId(operation) <= 0");
  });

  it("renders list paging from legacy querydata totals", () => {
    expect(appSource).toContain("resultTotalItems");
    expect(appSource).toContain("resultFreshTime");
    expect(appSource).toContain("loadResultPage(resultPageIndex + 1)");
    expect(appSource).toContain("Page {{ resultPageIndex }} / {{ resultTotalPages || 1 }}");
    expect(appSource).toContain("Updated {{ resultFreshTime }}");
  });

  it("resets the main View search to the first page", () => {
    expect(appSource).toContain("async function loadViewWorkflow(resetPage = false)");
    expect(appSource).toContain("pageIndex.value = 1");
    expect(appSource).toContain('@click="loadViewWorkflow(true)"');
  });

  it("keeps the Vue workspace on view-id driven legacy view and data APIs", () => {
    expect(appSource).toContain("/api/v1/view/getlistview");
    expect(appSource).toContain("/api/v1/data/querydata");
    expect(appSource).toContain("viewDetailViewId(view, loadedViewId)");
    expect(appSource).toContain("async function selectObject(row: ListDataItem, viewId = Number(detailViewId.value))");
    expect(appSource).toContain("async function startNewObject(viewId = Number(detailViewId.value))");
    expect(appSource).toContain("await queryDetail(Number(detailViewId.value))");
    expect(appSource).toContain("saveViewId.value = String(detailViewId.value)");
    expect(appSource).toContain("columnsFromListResult(dataResponse.value?.data)");
    expect(appSource).toContain("columnsFromRowItems(first)");
    expect(appSource).not.toContain("Object.keys(first)");
    expect(appSource).not.toContain("viewName: viewName.value");
    expect(appSource).not.toContain("/api/v1/view/get-view");
    expect(appSource).not.toContain("/api/v1/data/query-list");
    expect(appSource).not.toContain("buildQueryRequest");
  });

  it("passes the main View filter as legacy querydata QueryFilter", () => {
    const querySource = appSource.slice(
      appSource.indexOf("async function queryCurrentViewData"),
      appSource.indexOf("async function loadResultPage")
    );

    expect(appSource).toContain('v-model="legacyQueryFilter"');
    expect(querySource).toContain("queryFilter: legacyQueryFilter.value");
    expect(querySource).not.toContain("keyword:");
  });

  it("uses querydata Cols before row Items when View columns are absent", () => {
    const columnsSource = appSource.slice(
      appSource.indexOf("const resultColumns = computed"),
      appSource.indexOf("const resultRows = computed")
    );
    const childSource = appSource.slice(
      appSource.indexOf("async function loadExistingDetailItems"),
      appSource.indexOf("async function addExistingDetailItem")
    );

    expect(columnsSource.indexOf("columnsFromListResult")).toBeLessThan(columnsSource.indexOf("columnsFromRowItems"));
    expect(childSource).toContain("declaredColumns.length ? declaredColumns : resultColumns");
  });

  it("refreshes the main View workflow from legacy AutoFreshTime", () => {
    expect(appSource).toContain("listAutoFreshTime");
    expect(appSource).toContain("scheduleAutoRefresh(response.data)");
    expect(appSource).toContain("window.setInterval");
    expect(appSource).toContain("pageIndex.value = 1");
    expect(appSource).toContain('activeSection.value === "views"');
    expect(appSource).toContain("onUnmounted(stopAutoRefresh)");
  });

  it("keeps metadata lookup tied to the rendered view id", () => {
    expect(metadataFieldEditorSource).toContain("viewId: props.viewId");
    expect(metadataFieldEditorSource).not.toContain("viewName");
  });

  it("loads the View definition before the API-tool data query", () => {
    const querySource = appSource.slice(
      appSource.indexOf("async function queryLegacyData"),
      appSource.indexOf("async function queryCurrentViewData")
    );

    expect(querySource.indexOf("await loadLegacyListView()")).toBeGreaterThanOrEqual(0);
    expect(querySource.indexOf("await loadLegacyListView()")).toBeLessThan(querySource.indexOf("/api/v1/data/querydata"));
    expect(querySource).toContain("viewId: Number(currentViewId.value)");
    expect(querySource).not.toContain("viewId: Number(legacyQueryViewId.value)");
  });

  it("loads the rendered View before the current data query", () => {
    const querySource = appSource.slice(
      appSource.indexOf("async function queryCurrentViewData"),
      appSource.indexOf("async function loadResultPage")
    );

    expect(querySource).toContain("viewId(viewResponse.value.data) !== Number(legacyListViewId.value)");
    expect(querySource.indexOf("await loadLegacyListView()")).toBeGreaterThanOrEqual(0);
    expect(querySource.indexOf("await loadLegacyListView()")).toBeLessThan(querySource.indexOf("/api/v1/data/querydata"));
    expect(querySource).toContain("viewId: loadedViewId");
  });

  it("does not bootstrap View/data rendering from the seeded business View", () => {
    expect(appSource).not.toContain("ref(100)");
    expect(appSource).toContain("const currentViewId = computed(() => viewId(viewResponse.value?.data))");
    expect(appSource).not.toContain("viewId(viewResponse.value?.data, legacyListViewId.value)");
  });

  it("loads the default first-screen View from the legacy app shell", () => {
    const workflowSource = appSource.slice(
      appSource.indexOf("async function loadViewWorkflow"),
      appSource.indexOf("onMounted(()")
    );

    expect(appSource).toContain('const password = ref("admin")');
    expect(appSource).toContain("async function ensureLegacySession");
    expect(workflowSource.indexOf("await ensureLegacySession()")).toBeGreaterThanOrEqual(0);
    expect(workflowSource.indexOf("await ensureLegacySession()")).toBeLessThan(workflowSource.indexOf("await loadMainInfo()"));
    expect(appSource).toContain("legacyAppDefaultViewId");
    expect(appSource).toContain("applyDefaultAppView(response.data)");
    expect(workflowSource.indexOf("await loadMainInfo()")).toBeGreaterThanOrEqual(0);
    expect(workflowSource.indexOf("await loadMainInfo()")).toBeLessThan(workflowSource.indexOf("await loadLegacyListView()"));
  });

  it("retries the first-screen legacy shell after a stale stored token", () => {
    const workflowSource = appSource.slice(
      appSource.indexOf("async function loadViewWorkflow"),
      appSource.indexOf("onMounted(()")
    );

    expect(workflowSource).toContain("if (!(await loadMainInfo()))");
    expect(workflowSource).toContain("token.value = \"\"");
    expect(workflowSource).toContain('localStorage.removeItem("fool-service-token")');
    expect(workflowSource).toContain("if (!(await ensureLegacySession()) || !(await loadMainInfo())) return");
  });

  it("renders legacy shell menu entries and opens their View ids", () => {
    const menuSource = appSource.slice(
      appSource.indexOf("async function openShellMenu"),
      appSource.indexOf("async function loadLegacyListView")
    );

    expect(appSource).toContain("legacyMainMenuItems(mainInfoResponse.value?.data)");
    expect(appSource).toContain("shellMenuItems");
    expect(appSource).toContain('@click="openShellMenu(item)"');
    expect(menuSource).toContain("legacyAuthViewId(item)");
    expect(menuSource).toContain("legacyListViewId.value = itemViewId");
    expect(menuSource).not.toContain("legacyQueryViewId.value = itemViewId");
    expect(menuSource).toContain("await loadViewWorkflow(true)");
    expect(menuSource).toContain("subMenuParentAuthCode.value = authNo");
    expect(menuSource).toContain("await loadSubMenu()");
  });

  it("does not prefill business-specific data DTO fields by default", () => {
    expect(appSource).toContain('const enumModelId = ref("102")');
    expect(appSource).toContain('const legacyQueryFilter = ref("")');
    expect(appSource).toContain('const detailObjId = ref("")');
    expect(appSource).toContain('const saveObjId = ref("")');
    expect(appSource).toContain('const operationObjectId = ref("")');
    expect(appSource).toContain("const operationId = ref(0)");
    expect(appSource).toContain('const savePropertyiesJson = ref("[]")');
    expect(appSource).toContain('const saveNewPropertyiesJson = ref("[]")');
    expect(appSource).not.toContain('order_state="0"');
    expect(appSource).not.toContain("BTC-USDT");
    expect(appSource).not.toContain("const operationId = ref(7001)");
    expect(appSource).not.toContain('const detailObjId = ref("1001")');
    expect(appSource).not.toContain('const saveObjId = ref("1001")');
    expect(appSource).not.toContain('const operationObjectId = ref("1001")');
  });

  it("does not expose the backend seed DTO smoke route in the Vue workspace", () => {
    expect(appSource).not.toContain("Backend Smoke");
    expect(appSource).not.toContain('fetch("/test")');
    expect(appSource).not.toContain("backendSmokeResponse");
  });

  it("exposes the legacy report grid route in the Vue console", () => {
    expect(appSource).toContain("Report Grid");
    expect(appSource).toContain("/api/v1/report/makereport");
    expect(appSource).toContain("/api/v1/report/getrpt");
    expect(appSource).toContain("Get Report");
    expect(appSource).toContain("reportResponse");
  });

  it("exposes the legacy report column candidate route in the Vue console", () => {
    expect(appSource).toContain("Report Columns");
    expect(appSource).toContain("/api/v1/report/getmkqview");
    expect(appSource).toContain("reportModelResponse");
    expect(appSource).toContain("buildReportColsFromModel");
    expect(appSource).toContain("reportColsJson.value = JSON.stringify");
  });

  it("exposes the legacy save report definition route in the Vue console", () => {
    expect(appSource).toContain("Save Report Definition");
    expect(appSource).toContain("/api/v1/report/saverpt");
    expect(appSource).toContain("saveReportResponse");
  });

  it("exposes the legacy message polling route in the Vue console", () => {
    expect(appSource).toContain("Messages");
    expect(appSource).toContain("/api/v1/message/getmsg");
    expect(appSource).toContain("messageResponse");
  });

  it("exposes the legacy notify count route in the Vue console", () => {
    expect(appSource).toContain("Notify Counts");
    expect(appSource).toContain("/api/v1/message/getnotify");
    expect(appSource).toContain("notifyResponse");
  });

  it("exposes the legacy user info route in the Vue console", () => {
    expect(appSource).toContain("Legacy User Info");
    expect(appSource).toContain("/api/v1/auth/getuserinfo");
    expect(appSource).toContain("legacyUserInfoResponse");
  });

  it("exposes the legacy checkcode routes in the Vue console", () => {
    expect(appSource).toContain("Check Code");
    expect(appSource).toContain("/api/v1/auth/getcheckcode");
    expect(appSource).toContain("/api/v1/auth/checkcode");
    expect(appSource).toContain("checkCodeResponse");
  });

  it("exposes the legacy loginv2 route in the Vue console", () => {
    expect(appSource).toContain("Legacy Login V2");
    expect(appSource).toContain("/api/v1/auth/loginv2");
    expect(appSource).toContain("legacyLoginResponse");
  });

  it("exposes the legacy initapp route in the Vue console", () => {
    expect(appSource).toContain("Init App");
    expect(appSource).toContain("/api/v1/auth/initapp");
    expect(appSource).toContain("initAppResponse");
  });

  it("exposes the legacy submenu route in the Vue console", () => {
    expect(appSource).toContain("Sub Menu");
    expect(appSource).toContain("/api/v1/auth/getsubmenu");
    expect(appSource).toContain("subMenuResponse");
  });

  it("exposes the legacy main-info route in the Vue console", () => {
    expect(appSource).toContain("Main Info");
    expect(appSource).toContain("/api/v1/auth/getmain");
    expect(appSource).toContain("mainInfoResponse");
  });

  it("exposes the legacy app-info route in the Vue console", () => {
    expect(appSource).toContain("App Info");
    expect(appSource).toContain("/api/v1/auth/getapp");
    expect(appSource).toContain("appInfoResponse");
  });

  it("exposes the legacy initnew route in the Vue console", () => {
    expect(appSource).toContain("Init New Object");
    expect(appSource).toContain("/api/v1/data/initnew");
    expect(appSource).toContain("initNewResponse");
  });

  it("exposes the legacy savenewobj route in the Vue console", () => {
    expect(appSource).toContain("Save New Object");
    expect(appSource).toContain("/api/v1/data/savenewobj");
    expect(appSource).toContain("saveNewObjResponse");
  });

  it("exposes the legacy runoperation route in the Vue console", () => {
    expect(appSource).toContain("Run Operation");
    expect(appSource).toContain("/api/v1/data/runoperation");
    expect(appSource).toContain("runOperationResponse");
  });

  it("keeps the frontend proxy surface on migrated API routes", () => {
    expect(viteConfig).not.toContain('"/test"');
    expect(nginxConfig).not.toContain("location /test");
    expect(viteConfig).toContain('"/api"');
    expect(nginxConfig).toContain("location /api/");
  });

});

describe("buildTokenRequest", () => {
  it("matches the common token-only request DTO shape", () => {
    const request = buildTokenRequest(" token-1 ");

    expect(request).toEqual({
      token: "token-1"
    });
  });
});

describe("buildInputQueryRequest", () => {
  it("matches the legacy inputquery DTO shape", () => {
    const request = buildInputQueryRequest({
      token: "token-1",
      viewName: "RecordView",
      viewItemId: "name",
      text: "  Ada  ",
      objID: "1001",
      ownerId: "5001",
      isAdded: true
    });

    expect(request).toEqual({
      token: "token-1",
      viewName: "RecordView",
      viewItemId: "name",
      text: "Ada",
      objID: "1001",
      ownerId: "5001",
      isAdded: true
    });
  });

  it("includes the current view id for view-driven lookup", () => {
    const request = buildInputQueryRequest({
      token: "token-1",
      viewId: 100,
      viewName: " ",
      viewItemId: "customer",
      text: "Ad"
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      viewItemId: "customer",
      text: "Ad",
      isAdded: false
    });
  });
});

describe("buildSaveObjRequest", () => {
  it("matches the legacy saveobj DTO shape", () => {
    const request = buildSaveObjRequest({
      token: "token-1",
      id: " 1001 ",
      viewID: " 100 ",
      propertyiesJson: "[{\"key\":\"name\",\"value\":\"Sample\"},{\"key\":\"state\",\"value\":\"0\"}]",
      itempropertiesJson:
        "[{\"key\":\"children\",\"items\":[{\"itemId\":\"2001\",\"isExist\":true,\"propertyies\":[{\"key\":\"childName\",\"value\":\"Updated child\"}]}]}]"
    });

    expect(request).toEqual({
      token: "token-1",
      saveObj: {
        id: "1001",
        viewID: "100",
        propertyies: [
          { key: "name", value: "Sample" },
          { key: "state", value: "0" }
        ],
        itemproperties: [
          {
            key: "children",
            items: [
              {
                itemId: "2001",
                isExist: true,
                propertyies: [{ key: "childName", value: "Updated child" }]
              }
            ]
          }
        ]
      }
    });
  });

});

describe("buildSaveNewObjRequest", () => {
  it("matches the legacy savenewobj DTO shape", () => {
    const request = buildSaveNewObjRequest({
      token: "token-1",
      id: " 2009 ",
      viewID: " 200 ",
      propertyiesJson: "[{\"key\":\"itemName\",\"value\":\"New child\"}]",
      ownerViewId: " 100 ",
      ownerId: " 1001 ",
      property: " items "
    });

    expect(request).toEqual({
      token: "token-1",
      saveObj: {
        id: "2009",
        viewID: "200",
        propertyies: [{ key: "itemName", value: "New child" }],
        itemproperties: []
      },
      ownerViewId: "100",
      ownerId: "1001",
      property: "items"
    });
  });
});

describe("buildRunOperationRequest", () => {
  it("matches the legacy runoperation DTO shape", () => {
    const request = buildRunOperationRequest({
      token: "token-1",
      objectId: " 1001 ",
      viewId: 100,
      operationId: 7001
    });

    expect(request).toEqual({
      token: "token-1",
      objectId: "1001",
      viewId: 100,
      operationId: 7001
    });
  });
});

describe("buildQueryDataDetailRequest", () => {
  it("matches the legacy querydatadetail DTO shape", () => {
    const request = buildQueryDataDetailRequest({
      token: "token-1",
      viewId: 100,
      objId: " 1001 ",
      idExp: " record_id "
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      objId: "1001",
      idExp: "record_id"
    });
  });
});

describe("buildInitNewRequest", () => {
  it("matches the legacy initnew DTO shape", () => {
    const request = buildInitNewRequest({
      token: "token-1",
      viewId: 100,
      parentObjId: " 5001 "
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      parentObjId: "5001"
    });
  });
});

describe("buildGetEnumRequest", () => {
  it("matches the legacy getenums DTO shape", () => {
    const request = buildGetEnumRequest({
      token: "token-1",
      modelId: " 100 "
    });

    expect(request).toEqual({
      token: "token-1",
      modelId: "100"
    });
  });
});

describe("buildLegacyListViewRequest", () => {
  it("matches the legacy getlistview DTO shape", () => {
    const request = buildLegacyListViewRequest({
      token: "token-1",
      viewId: 100
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100
    });
  });
});

describe("buildLegacyReadItemViewRequest", () => {
  it("matches the legacy getreaditemview DTO shape", () => {
    const request = buildLegacyReadItemViewRequest({
      token: "token-1",
      viewId: 100
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100
    });
  });
});

describe("buildLegacyQueryDataRequest", () => {
  it("matches the legacy querydata DTO shape", () => {
    const request = buildLegacyQueryDataRequest({
      token: "token-1",
      viewId: 100,
      pageSize: 10,
      pageIndex: 2,
      queryFilter: " record_state=\"0\" ",
      keyword: "  Ada  ",
      orderByItem: 1001,
      orderByType: 1
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      pageSize: 10,
      pageIndex: 2,
      queryFilter: "record_state=\"0\"",
      keyword: "Ada",
      orderByItem: 1001,
      orderByType: 1
    });
  });
});

describe("buildMakeReportRequest", () => {
  it("matches the legacy makereport DTO shape", () => {
    const request = buildMakeReportRequest({
      token: "token-1",
      viewId: 100,
      currentPage: 2,
      pageSize: 10,
      queryFilter: " record_state=\"0\" ",
      reportColsJson: "[{\"colName\":\"State\",\"index\":2},{\"colName\":\"Name\",\"index\":1}]",
      reportName: " View Daily "
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      currentPage: 2,
      pageSize: 10,
      queryFilter: "record_state=\"0\"",
      reportName: "View Daily",
      reportCols: [
        { colName: "State", index: 2 },
        { colName: "Name", index: 1 }
      ]
    });
  });
});
