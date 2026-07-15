import { describe, expect, it } from "vitest";
import appSource from "./App.vue?raw";
import chatSource from "./AgentChatPage.vue?raw";

describe("AgentChatPage integration", () => {
  it("loads providers and ordered capabilities before sending chat messages", () => {
    expect(chatSource).toContain('getApi<AgentProviderInfo[]>("/api/v1/agent/providers")');
    expect(chatSource).toContain('getApi<AgentCapability[]>("/api/v1/agent/capabilities")');
    expect(chatSource).toContain("provider: selectedProvider.value");
    expect(chatSource).toContain("response.data.provider");
  });

  it("registers the authenticated agent route in desktop and mobile navigation", () => {
    expect(appSource).toContain('window.location.pathname === "/agent"');
    expect(appSource).toContain('<AgentChatPage v-if="showAgentChat" :token="token" />');
    expect(appSource.match(/>AI 助手<\/button>/g)).toHaveLength(2);
  });
});
