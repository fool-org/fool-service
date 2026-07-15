import { afterEach, describe, expect, it, vi } from "vitest";
import { getApi, isTransportError, postApi } from "./api";

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

  it("loads GET API catalogs without a request body", async () => {
    const fetchMock = vi.fn(async () => new Response(JSON.stringify({ code: 0, message: "success", data: ["openai"] })));
    vi.stubGlobal("fetch", fetchMock);

    await expect(getApi<string[]>("/api/v1/agent/providers")).resolves.toMatchObject({ data: ["openai"] });
    expect(fetchMock).toHaveBeenCalledWith("/api/v1/agent/providers", undefined);
  });
});
