import { describe, expect, it } from "vitest";
import {
  buildGetEnumRequest,
  buildInputQueryRequest,
  buildLegacyListViewRequest,
  buildLegacyQueryDataRequest,
  buildLegacyReadItemViewRequest,
  buildQueryDataDetailRequest,
  buildQueryRequest,
  buildSaveObjRequest
} from "./payload";

describe("buildQueryRequest", () => {
  it("matches the Spring QueryDataRequest DTO shape", () => {
    const request = buildQueryRequest({
      token: "token-1",
      viewName: "OrderList",
      pageIndex: 2,
      pageSize: 25,
      filterJson: "{\"status\":{\"property\":\"status\",\"value\":\"OPEN\"}}"
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
          value: "OPEN"
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
      filterJson: "{\"state\":{\"property\":\"state\",\"value\":\"OPEN\"}}",
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
        value: "OPEN"
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
      propertyiesJson: "[{\"key\":\"symbol\",\"value\":\"BTC-USDT\"},{\"key\":\"state\",\"value\":\"OPEN\"}]",
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
          { key: "state", value: "OPEN" }
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
      queryFilter: " order_state=\"OPEN\" ",
      orderByItem: 1001,
      orderByType: 1
    });

    expect(request).toEqual({
      token: "token-1",
      viewId: 100,
      pageSize: 10,
      pageIndex: 2,
      queryFilter: "order_state=\"OPEN\"",
      orderByItem: 1001,
      orderByType: 1
    });
  });
});
