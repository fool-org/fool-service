package org.fool.framework.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class AgentChatProviderServiceTest {
    @Test
    public void providersExposeConfigurationWithoutSecrets() {
        AgentProviderProperties properties = new AgentProviderProperties();
        properties.getProviders().get("openai").setApiKey("secret-key");
        properties.setDefaultProvider("openai");
        AgentChatProviderService service = service(properties, new RestTemplate());

        List<AgentChatProviderService.ProviderInfo> providers = service.providers();

        assertEquals(2, providers.size());
        assertFalse(providers.get(0).isConfigured());
        assertTrue(providers.get(1).isConfigured());
        assertTrue(providers.get(1).isDefaultProvider());
        assertEquals("gpt-5-mini", providers.get(1).getModel());
    }

    @Test
    public void replyUsesOpenAiCompatibleChatCompletion() {
        AgentProviderProperties properties = new AgentProviderProperties();
        AgentProviderProperties.Provider openai = properties.getProviders().get("openai");
        openai.setApiKey("secret-key");
        openai.setBaseUrl("https://provider.test/v1/");
        openai.setModel("test-model");
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo("https://provider.test/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer secret-key"))
                .andExpect(jsonPath("$.model").value("test-model"))
                .andExpect(jsonPath("$.stream").value(false))
                .andExpect(jsonPath("$.messages[0].role").value("system"))
                .andExpect(jsonPath("$.messages[1].content").value("会话已创建"))
                .andExpect(jsonPath("$.messages[2].content").value("帮我配置订单报表"))
                .andRespond(withSuccess(
                        "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"先确认 ViewId。\"}}]}",
                        MediaType.APPLICATION_JSON));
        AgentChatProviderService service = service(properties, restTemplate);
        AgentSession session = session();
        AgentDraft draft = new AgentDraft(
                AgentCapabilityType.REPORT_QUERY,
                "已生成草案",
                "fool-report,fool-query",
                "low-read-only",
                Map.of("ViewId", 100),
                List.of("compare metadata"));

        AgentChatProviderService.Reply reply = service.reply(
                "openai", session, AgentCapabilityType.REPORT_QUERY, "帮我配置订单报表", draft);

        assertEquals("先确认 ViewId。", reply.getContent());
        assertEquals("openai", reply.getProvider());
        assertEquals("test-model", reply.getModel());
        server.verify();
    }

    @Test
    public void missingKeysKeepLocalFallbackButRejectExplicitProvider() {
        AgentProviderProperties properties = new AgentProviderProperties();
        AgentChatProviderService service = service(properties, new RestTemplate());
        AgentDraft draft = new AgentDraft(
                AgentCapabilityType.REPORT_QUERY,
                "本地草案",
                "fool-report,fool-query",
                "low-read-only",
                Map.of(),
                List.of());

        AgentChatProviderService.Reply local = service.reply(
                null, session(), AgentCapabilityType.REPORT_QUERY, "hello", draft);

        assertEquals("local", local.getProvider());
        assertEquals("本地草案", local.getContent());
        assertEquals("local", service.reply(
                "local", session(), AgentCapabilityType.REPORT_QUERY, "hello", draft).getProvider());
        assertThrows(IllegalStateException.class, () -> service.reply(
                "deepseek", session(), AgentCapabilityType.REPORT_QUERY, "hello", draft));
    }

    private AgentChatProviderService service(AgentProviderProperties properties, RestTemplate restTemplate) {
        return new AgentChatProviderService(properties, new ObjectMapper(), restTemplate);
    }

    private AgentSession session() {
        AgentSession session = new AgentSession(
                "session-1",
                "token-1",
                "配置订单报表",
                AgentCapabilityType.REPORT_QUERY,
                Instant.parse("2026-07-15T00:00:00Z"));
        session.addMessage(new AgentMessage(
                "message-1",
                AgentMessageRole.SYSTEM,
                AgentCapabilityType.REPORT_QUERY,
                "会话已创建",
                Instant.parse("2026-07-15T00:00:00Z")));
        return session;
    }
}
