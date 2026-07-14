import { describe, expect, it } from "vitest";
import appSource from "./App.vue?raw";

describe("legacy View auto-refresh", () => {
  it("runs on schedule during other requests", () => {
    const source = appSource.slice(
      appSource.indexOf("function scheduleAutoRefresh"),
      appSource.indexOf("function openListObject")
    );

    expect(source).toContain("window.setInterval");
    expect(source).toContain("void queryCurrentViewData()");
    expect(source).not.toContain("pendingAction");
  });
});
