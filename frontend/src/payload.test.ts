import { describe, expect, it } from "vitest";
import { buildInputQueryRequest, buildQueryRequest } from "./payload";

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
