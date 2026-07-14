import { afterEach, describe, expect, it, vi } from "vitest";
import { ref } from "vue";
import type { CommonResponse } from "./api";
import { type WorkflowActionOptions, useViewDataWorkflow } from "./useViewDataWorkflow";

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

  it("sends the main View search as keyword without the API-tool QueryFilter", async () => {
    const calls: { path: string; payload: Record<string, unknown> }[] = [];
    vi.stubGlobal("fetch", vi.fn(async (path: string, init?: RequestInit) => {
      const payload = JSON.parse(String(init?.body || "{}")) as Record<string, unknown>;
      calls.push({ path, payload });
      if (path === "/api/v1/view/getlistview") {
        return jsonResponse({ ViewId: 100, Items: [{ Name: "Symbol", PropertyName: "symbol" }] });
      }
      return jsonResponse({ Data: [] });
    }));
    const workflow = useViewDataWorkflow(workflowRefs({ keyword: ref(" BTC ") }));

    await workflow.queryCurrentViewData();

    expect(calls[1].payload).toMatchObject({ viewId: 100, keyword: "BTC" });
    expect(calls[1].payload).not.toHaveProperty("queryFilter");
  });

  it("uses legacy ViewId from rendered View metadata before querying data", async () => {
    const calls: { path: string; payload: Record<string, unknown> }[] = [];
    vi.stubGlobal("fetch", vi.fn(async (path: string, init?: RequestInit) => {
      const payload = JSON.parse(String(init?.body || "{}")) as Record<string, unknown>;
      calls.push({ path, payload });
      if (path === "/api/v1/view/getlistview") {
        return jsonResponse({
          ViewId: 144,
          Name: "Rendered View",
          Items: [{ Name: "Name", PropertyName: "name" }]
        });
      }
      return jsonResponse({
        Data: [{ Items: [{ PrpId: "name", FmtValue: "Alice" }] }]
      });
    }));

    const workflow = useViewDataWorkflow(workflowRefs({ listViewId: ref(42) }));

    await workflow.queryCurrentViewData();

    expect(calls.map((call) => call.path)).toEqual([
      "/api/v1/view/getlistview",
      "/api/v1/data/querydata"
    ]);
    expect(calls[1].payload).toMatchObject({ viewId: 144 });
    expect(workflow.currentViewId.value).toBe(144);
    expect(workflow.resultColumns.value).toEqual([{ Name: "Name", PropertyName: "name" }]);
  });

  it("does not query data when the loaded View cannot render columns", async () => {
    const calls: { path: string; payload: Record<string, unknown> }[] = [];
    vi.stubGlobal("fetch", vi.fn(async (path: string, init?: RequestInit) => {
      const payload = JSON.parse(String(init?.body || "{}")) as Record<string, unknown>;
      calls.push({ path, payload });
      return jsonResponse({
        ViewId: 144,
        Name: "Empty View",
        Items: []
      });
    }));

    const workflow = useViewDataWorkflow(workflowRefs({ listViewId: ref(42) }));

    const response = await workflow.queryCurrentViewData();

    expect(response).toBeNull();
    expect(calls.map((call) => call.path)).toEqual(["/api/v1/view/getlistview"]);
    expect(workflow.currentViewId.value).toBe(144);
    expect(workflow.resultColumns.value).toEqual([]);
    expect(workflow.resultRows.value).toEqual([]);
  });

  it("caches read-item View metadata by requested and rendered View ids", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => jsonResponse({
      ViewId: 60,
      Items: [{ PrpId: "name", PrpShowName: "Name" }]
    })));
    const workflow = useViewDataWorkflow(workflowRefs({ readItemViewId: ref(55) }));

    const response = await workflow.loadReadItemView();

    expect(response?.data.ViewId).toBe(60);
    expect(workflow.readItemViewFor(55)).toEqual(response?.data);
    expect(workflow.readItemViewFor(60)).toEqual(response?.data);
  });

  it("loads a child panel by getlistview before querydata", async () => {
    const calls: { path: string; payload: Record<string, unknown> }[] = [];
    vi.stubGlobal("fetch", vi.fn(async (path: string, init?: RequestInit) => {
      const payload = JSON.parse(String(init?.body || "{}")) as Record<string, unknown>;
      calls.push({ path, payload });
      if (path === "/api/v1/view/getlistview") {
        return jsonResponse({
          ViewId: 201,
          Name: "Child",
          Items: [{ Name: "Child Name", PropertyName: "childName" }]
        });
      }
      return jsonResponse({
        Data: [{ Items: [{ PrpId: "childName", FmtValue: "One" }] }]
      });
    }));
    const workflow = useViewDataWorkflow(workflowRefs());

    const response = await workflow.loadViewDataById(200, "sudoku-panel", 5);

    expect(calls.map((call) => call.path)).toEqual([
      "/api/v1/view/getlistview",
      "/api/v1/data/querydata"
    ]);
    expect(calls[0].payload).toMatchObject({ viewId: 200 });
    expect(calls[1].payload).toMatchObject({ viewId: 201, pageIndex: 1, pageSize: 5 });
    expect(calls[1].payload).not.toHaveProperty("queryFilter");
    expect(response?.view.ViewId).toBe(201);
    expect(response?.data?.Data).toEqual([{ Items: [{ PrpId: "childName", FmtValue: "One" }] }]);
  });

  it("forwards one action policy through panel View and data loads", async () => {
    const actionOptions: Array<WorkflowActionOptions | undefined> = [];
    vi.stubGlobal("fetch", vi.fn(async (path: string) => path === "/api/v1/view/getlistview"
      ? jsonResponse({ ViewId: 201, Items: [{ Name: "Name", PropertyName: "name" }] })
      : jsonResponse({ Data: [] })));
    const workflow = useViewDataWorkflow({
      ...baseRefs(),
      runAction: async <T>(
        _label: string,
        action: () => Promise<CommonResponse<T>>,
        options?: WorkflowActionOptions
      ) => {
        actionOptions.push(options);
        return action();
      }
    });

    await workflow.loadViewDataById(200, "sudoku-panel", 5, { silentTransport: true });

    expect(actionOptions).toEqual([{ silentTransport: true }, { silentTransport: true }]);
  });

  it("loads a child panel View without querying row data", async () => {
    const calls: { path: string; payload: Record<string, unknown> }[] = [];
    vi.stubGlobal("fetch", vi.fn(async (path: string, init?: RequestInit) => {
      const payload = JSON.parse(String(init?.body || "{}")) as Record<string, unknown>;
      calls.push({ path, payload });
      return jsonResponse({
        ViewId: 301,
        Name: "Item",
        Items: [{ Name: "Name", PropertyName: "name" }]
      });
    }));
    const workflow = useViewDataWorkflow(workflowRefs());

    const response = await workflow.loadViewById(300, "sudoku-item");

    expect(calls.map((call) => call.path)).toEqual(["/api/v1/view/getlistview"]);
    expect(calls[0].payload).toMatchObject({ viewId: 300 });
    expect(response?.data.ViewId).toBe(301);
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
    pageIndex: ref(1),
    pageSize: ref(20),
    keyword: ref(""),
    detailViewId: ref(0)
  };
}

function jsonResponse<T>(data: T) {
  return new Response(JSON.stringify({ code: 0, message: "OK", data }), { status: 200 });
}
