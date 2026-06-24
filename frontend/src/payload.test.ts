import { describe, expect, it } from "vitest";
import { buildQueryRequest } from "./payload";

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
});
