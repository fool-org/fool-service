import { afterEach, describe, expect, it, vi } from "vitest";
import { ref } from "vue";
import type { CommonResponse } from "./api";
import { useViewDataWorkflow } from "./useViewDataWorkflow";

describe("useViewDataWorkflow", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("loads View metadata before querydata and renders columns from the View", async () => {
    const calls: { path: string; payload: Record<string, unknown> }[] = [];
    vi.stubGlobal("fetch", vi.fn(async (path: string, init?: RequestInit) => {
      const payload = JSON.parse(String(init?.body || "{}")) as Record<string, unknown>;
      calls.push({ path, payload });
      if (path === "/api/v1/view/getlistview") {
        return jsonResponse({
          ID: 44,
          Name: "Orders",
          DetailViewId: 55,
          Items: [{ Name: "Symbol", PropertyName: "symbol" }]
        });
      }
      return jsonResponse({
        Cols: ["DTO Only"],
        Data: [{ Items: [{ PrpId: "symbol", FmtValue: "BTC-USDT" }] }],
        TotalItem: 1,
        TotalPage: 1,
        PageIndex: 2
      });
    }));

    const workflow = useViewDataWorkflow(workflowRefs({ listViewId: ref(42), pageIndex: ref(2), pageSize: ref(25) }));

    await workflow.queryCurrentViewData();

    expect(calls.map((call) => call.path)).toEqual([
      "/api/v1/view/getlistview",
      "/api/v1/data/querydata"
    ]);
    expect(calls[0].payload).toMatchObject({ viewId: 42 });
    expect(calls[1].payload).toMatchObject({ viewId: 44, pageIndex: 2, pageSize: 25 });
    expect(workflow.currentViewId.value).toBe(44);
    expect(workflow.resultColumns.value).toEqual([{ Name: "Symbol", PropertyName: "symbol" }]);
    expect(workflow.resultRows.value).toEqual([{ Items: [{ PrpId: "symbol", FmtValue: "BTC-USDT" }] }]);
  });

  it("caches read-item View metadata by the rendered View id", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => jsonResponse({
      ViewId: 60,
      Items: [{ PrpId: "name", PrpShowName: "Name" }]
    })));
    const workflow = useViewDataWorkflow(workflowRefs({ readItemViewId: ref(55) }));

    const response = await workflow.loadReadItemView();

    expect(response?.data.ViewId).toBe(60);
    expect(workflow.readItemViewFor(60)).toEqual(response?.data);
  });
});

function workflowRefs(overrides: Partial<ReturnType<typeof baseRefs>> = {}) {
  const refs = {
    ...baseRefs(),
    ...overrides
  };
  return {
    ...refs,
    runAction: async <T>(_label: string, action: () => Promise<CommonResponse<T>>) => action()
  };
}

function baseRefs() {
  return {
    token: ref("token-1"),
    listViewId: ref(100),
    readItemViewId: ref(0),
    queryViewId: ref(0),
    queryPageIndex: ref(1),
    queryPageSize: ref(10),
    pageIndex: ref(1),
    pageSize: ref(20),
    queryFilter: ref("state=0"),
    reportViewId: ref(0),
    detailViewId: ref(0),
    initNewViewId: ref(0),
    operationViewId: ref(0),
    saveViewId: ref(""),
    saveNewViewId: ref("")
  };
}

function jsonResponse<T>(data: T) {
  return new Response(JSON.stringify({ code: 0, message: "OK", data }), { status: 200 });
}
