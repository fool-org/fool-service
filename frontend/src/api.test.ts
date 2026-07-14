import { afterEach, describe, expect, it, vi } from "vitest";
import { isTransportError, postApi } from "./api";

describe("postApi", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("rejects non-zero API response codes", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async () => new Response(JSON.stringify({ code: 1, message: "token invalid", data: null }), { status: 200 }))
    );

    await expect(postApi("/api/test", {})).rejects.toSatisfy(
      (error: unknown) => error instanceof Error && error.message === "token invalid" && !isTransportError(error)
    );
  });

  it("classifies HTTP and network failures as transport errors", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => new Response(null, { status: 502 })));
    await expect(postApi("/api/test", {})).rejects.toSatisfy(isTransportError);

    vi.stubGlobal("fetch", vi.fn(async () => {
      throw new TypeError("offline");
    }));
    await expect(postApi("/api/test", {})).rejects.toSatisfy(isTransportError);
  });
});
