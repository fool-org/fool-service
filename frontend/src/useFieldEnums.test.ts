import { afterEach, describe, expect, it, vi } from "vitest";
import { ref } from "vue";
import type { CommonResponse } from "./api";
import { useFieldEnums } from "./useFieldEnums";

describe("useFieldEnums", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("loads enum options by field model id and skips cached models", async () => {
    const requests: Record<string, unknown>[] = [];
    vi.stubGlobal("fetch", vi.fn(async (_path: string, init?: RequestInit) => {
      requests.push(JSON.parse(String(init?.body || "{}")) as Record<string, unknown>);
      return new Response(JSON.stringify({
        code: 0,
        message: "OK",
        data: { EnumValues: [{ Name: "Open", Value: 0 }, { Name: "Filled", Value: 1 }] }
      }), { status: 200 });
    }));
    const enums = useFieldEnums(ref("token-1"), async <T>(_label: string, action: () => Promise<CommonResponse<T>>) => action());

    await enums.loadFieldEnums([
      { PrpId: "state", PrpType: "Enum", PrpModelId: 300 },
      { PrpId: "name", PrpType: "String", PrpModelId: 0 },
      { PrpId: "stateAgain", PrpType: "Enum", PrpModelId: 300 }
    ]);
    await enums.loadFieldEnums([{ PrpId: "cached", PrpType: "Enum", PrpModelId: 300 }]);

    expect(requests).toEqual([{ token: "token-1", modelId: "300" }]);
    expect(enums.enumOptions.value["300"]).toEqual([
      { label: "Open", value: "0" },
      { label: "Filled", value: "1" }
    ]);
  });
});
