import { describe, expect, it } from "vitest";
import nginxConfig from "../nginx.conf?raw";
import viteConfig from "../vite.config.ts?raw";
import appSource from "./App.vue?raw";
import {
  buildGetEnumRequest,
  buildInputQueryRequest,
  buildLegacyListViewRequest,
  buildLegacyQueryDataRequest,
  buildLegacyReadItemViewRequest,
  buildMakeReportRequest,
  buildQueryDataDetailRequest,
  buildQueryRequest,
  buildSaveObjRequest,
  buildTokenRequest
} from "./payload";

describe("App defaults", () => {
  it("loads the seeded order-state enum model by default", () => {
    expect(appSource).toContain('const enumModelId = ref("102")');
    expect(appSource).toContain('const legacyQueryFilter = ref(\'order_state="0"\')');
    expect(appSource).toContain('{"key":"state","value":"0"}');
  });

  it("exposes the Docker backend smoke route in the Vue console", () => {
    expect(appSource).toContain("Backend Smoke");
    expect(appSource).toContain('fetch("/test")');
    expect(appSource).toContain("backendSmokeResponse");
  });

  it("exposes the legacy report grid route in the Vue console", () => {
    expect(appSource).toContain("Report Grid");
    expect(appSource).toContain("/api/v1/report/makereport");
    expect(appSource).toContain("reportResponse");
  });

  it("exposes the legacy report column candidate route in the Vue console", () => {
    expect(appSource).toContain("Report Columns");
    expect(appSource).toContain("/api/v1/report/getmkqview");
    expect(appSource).toContain("reportModelResponse");
  });

  it("exposes the legacy save report definition route in the Vue console", () => {
    expect(appSource).toContain("Save Report Definition");
    expect(appSource).toContain("/api/v1/report/saverpt");
    expect(appSource).toContain("saveReportResponse");
  });

  it("proxies the backend smoke route in local and Compose frontends", () => {
    expect(viteConfig).toContain('"/test"');
    expect(nginxConfig).toContain("location /test");
  });
});

describe("buildQueryRequest", () => {
  it("matches the Spring QueryDataRequest DTO shape", () => {
    const request = buildQueryRequest({
      token: "token-1",
      viewName: "OrderList",
      pageIndex: 2,
      pageSize: 25,
      filterJson: "{\"status\":{\"property\":\"status\",\"value\":\"0\"}}"
    });

    expect(request).toEqual({
      token: "token-1",
      viewName: "OrderList",
      pageInfo: {
        pageIndex: 2,
        pageSize: 25
      },
      filter: {
        status: {
          property: "status",
          value: "0"
        }
      }
    });
  });

  it("builds visible equality and range filters as Spring QueryValue objects", () => {
    const request = buildQueryRequest({
      token: "token-1",
      viewName: "OrderList",
      pageIndex: 1,
      pageSize: 20,
      filterJson: "{\"state\":{\"property\":\"state\",\"value\":\"0\"}}",
      visibleFilters: [
        {
          property: "symbol",
          value: "BTC-USDT"
        },
        {
          property: "orderId",
          values: ["1001", "1002"]
        }
      ]
    });

    expect(request.filter).toEqual({
      state: {
        property: "state",
        value: "0"
      },
      symbol: {
        property: "symbol",
        value: "BTC-USDT"
      },
      orderId: {
        property: "orderId",
        values: ["1001", "1002"]
      }
    });
  });

  it("trims and sends the legacy keyword filter", () => {
    const request = buildQueryRequest({
      token: "token-1",
      viewName: "OrderList",
      pageIndex: 1,
      pageSize: 20,
      filterJson: "{}",
      keyword: "  USDT  "
    });

    expect(request.keyword).toBe("USDT");
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
      viewName: "OrderList",
      viewItemId: "symbol",
      text: "  BTC  ",
      objID: "1001",
      ownerId: "5001",
      isAdded: true
    });

    expect(request).toEqual({
      token: "token-1",
      viewName: "OrderList",
      viewItemId: "symbol",
      text: "BTC",
      objID: "1001",
      ownerId: "5001",
      isAdded: true
    });
  });
});

describe("buildSaveObjRequest", () => {
  it("matches the legacy saveobj DTO shape", () => {
    const request = buildSaveObjRequest({
      token: "token-1",
      id: " 1001 ",
      viewID: " 100 ",
      propertyiesJson: "[{\"key\":\"symbol\",\"value\":\"BTC-USDT\"},{\"key\":\"state\",\"value\":\"0\"}]",
      itempropertiesJson:
        "[{\"key\":\"items\",\"items\":[{\"itemId\":\"2001\",\"isExist\":true,\"propertyies\":[{\"key\":\"itemName\",\"value\":\"Updated item\"}]}]}]"
    });

    expect(request).toEqual({
      token: "token-1",
      saveObj: {
        id: "1001",
        viewID: "100",
        propertyies: [
          { key: "symbol", value: "BTC-USDT" },
          { key: "state", value: "0" }
        ],
        itemproperties: [
          {
            key: "items",
            items: [
              {
                itemId: "2001",
                isExist: true,
                propertyies: [{ key: "itemName", value: "Updated item" }]
              }
            ]
          }
        ]
      }
    });
  });
});

describe("buildQueryDataDetailRequest", () => {
  it("matches the legacy querydatadetail DTO shape", () => {
    const request = buildQueryDataDetailRequest({
      token: "token-1",
      viewId: 100,
      objId: " 1001 ",
      idExp: " order_id "
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      objId: "1001",
      idExp: "order_id"
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
      queryFilter: " order_state=\"0\" ",
      orderByItem: 1001,
      orderByType: 1
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      pageSize: 10,
      pageIndex: 2,
      queryFilter: "order_state=\"0\"",
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
      queryFilter: " order_state=\"0\" ",
      reportColsJson: "[{\"colName\":\"State\",\"index\":2},{\"colName\":\"Symbol\",\"index\":1}]",
      reportName: " Order Daily "
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      currentPage: 2,
      pageSize: 10,
      queryFilter: "order_state=\"0\"",
      reportName: "Order Daily",
      reportCols: [
        { colName: "State", index: 2 },
        { colName: "Symbol", index: 1 }
      ]
    });
  });
});
