import { describe, expect, it } from "vitest";
import appSource from "./App.vue?raw";
import centerSource from "./ActionCenterPage.vue?raw";

describe("ActionCenterPage integration", () => {
  it("uses only the subject-scoped action view to expose approval and execution commands", () => {
    expect(centerSource).toContain("request.approvable");
    expect(centerSource).toContain("request.executable");
    expect(centerSource).toContain("request.cancellable");
    expect(centerSource).toContain("当前用户是发起人；必须由独立审批人完成审批");
    expect(centerSource).toContain("/approvals");
    expect(centerSource).toContain("/execute");
  });

  it("registers the authenticated action center in desktop and mobile navigation", () => {
    expect(appSource).toContain('window.location.pathname === "/actions"');
    expect(appSource).toContain('<ActionCenterPage v-if="showActionCenter" />');
    expect(appSource.match(/>动作中心<\/button>/g)).toHaveLength(2);
  });
});
