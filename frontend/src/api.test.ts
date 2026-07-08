import { afterEach, describe, expect, it, vi } from "vitest";
import { postApi } from "./api";

describe("postApi", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("rejects non-zero API response codes", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async () => new Response(JSON.stringify({ code: 1, message: "token invalid", data: null }), { status: 200 }))
    );

    await expect(postApi("/api/test", {})).rejects.toThrow("token invalid");
  });
});
