import { beforeEach, describe, expect, it, vi } from "vitest";
import { approveHighAction, executeApprovedAction, prepareHighAction, previewSummary, withoutBodyToken } from "./actionWorkflow";
import { postApi } from "./api";
import type { ActionRequestView } from "./api";

vi.mock("./api", async (importOriginal) => ({
  ...await importOriginal<typeof import("./api")>(),
  postApi: vi.fn()
}));

describe("action workflow", () => {
  beforeEach(() => vi.mocked(postApi).mockReset());
  it("shows the server-owned risk, scope, count, rollback and expiry", () => {
    const request = {
      action: "data.update",
      resourceKey: "app:a:db:d:view:100",
      riskLevel: "MEDIUM",
      riskReasons: ["ACTION_CATALOG_FLOOR"],
      expiresAt: "2026-07-15T10:15:00Z",
      preview: { affectedObjectCount: 1, rollbackStrategy: "restore prior version" }
    } as ActionRequestView;
    const summary = previewSummary(request);
    expect(summary).toContain("影响对象：1");
    expect(summary).toContain("MEDIUM");
    expect(summary).toContain("restore prior version");
    expect(summary).toContain("app:a:db:d:view:100");
  });

  it("never copies compatibility body tokens into an action payload", () => {
    expect(withoutBodyToken({ token: "secret", Token: "legacy", saveObj: { id: "1" } }))
      .toEqual({ saveObj: { id: "1" } });
  });

  it("requires step-up before creating and previewing a HIGH request", async () => {
    vi.mocked(postApi)
      .mockResolvedValueOnce({ code: 0, message: "", data: { stepUpAt: "now", expiresAt: "later" } })
      .mockResolvedValueOnce({ code: 0, message: "", data: { actionRequestId: "a1" } as ActionRequestView })
      .mockResolvedValueOnce({ code: 0, message: "", data: {
        actionRequestId: "a1", riskLevel: "HIGH", status: "AWAITING_APPROVAL"
      } as ActionRequestView });

    await prepareHighAction({
      schemaVersion: 1,
      action: "data.delete",
      resource: { type: "view", id: "100" },
      arguments: { objectId: "1" },
      rationale: "reviewed deletion"
    }, "reauth-secret");

    expect(vi.mocked(postApi).mock.calls.map((call) => call[0])).toEqual([
      "/api/v1/auth/step-up", "/api/v1/actions", "/api/v1/actions/a1/preview"
    ]);
  });

  it("keeps independent approval and owner execution as explicit commands", async () => {
    vi.mocked(postApi).mockResolvedValue({ code: 0, message: "", data: {} as ActionRequestView });
    await approveHighAction("a1", "APPROVE", "scope reviewed");
    await executeApprovedAction("a1");
    expect(vi.mocked(postApi).mock.calls[0]?.[0]).toBe("/api/v1/actions/a1/approvals");
    expect(vi.mocked(postApi).mock.calls[1]?.[0]).toBe("/api/v1/actions/a1/execute");
  });
});
