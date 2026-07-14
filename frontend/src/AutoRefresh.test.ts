import { describe, expect, it } from "vitest";
import appSource from "./App.vue?raw";
import viewListPanelSource from "./ViewListPanel.vue?raw";

describe("legacy View auto-refresh", () => {
  it("keeps the legacy one-second ticker during other requests", () => {
    const source = appSource.slice(
      appSource.indexOf("function scheduleAutoRefresh"),
      appSource.indexOf("function openListObject")
    );

    expect(source).toContain("window.setInterval");
    expect(source).toContain("autoRefreshInterval === seconds");
    expect(source).toContain("let elapsed = 0");
    expect(source).toContain("if (elapsed === seconds)");
    expect(source).toContain("if (viewTableVisible.value)");
    expect(source).toContain("elapsed += 1");
    expect(source).toContain("}, 1000)");
    expect(source).toContain("void queryCurrentViewData()");
    expect(source).not.toContain("pendingAction");
    expect(viewListPanelSource).toContain('activePane.value === "table"');
    expect(viewListPanelSource).toContain('emit("tableVisibility", visible)');
  });
});
