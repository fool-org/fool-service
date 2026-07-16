package org.fool.framework.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.common.authz.DataClassification;
import org.fool.framework.common.authz.DataPolicy;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

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

    @Test
    public void providerHttpFailureIsContainedAsAStableServiceError() {
        AgentProviderProperties properties = new AgentProviderProperties();
        AgentProviderProperties.Provider openai = properties.getProviders().get("openai");
        openai.setApiKey("secret-key");
        openai.setBaseUrl("https://provider.test/v1");
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo("https://provider.test/v1/chat/completions"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        AgentChatProviderService service = service(properties, restTemplate);
        AgentDraft draft = new AgentDraft(AgentCapabilityType.REPORT_QUERY, "local draft",
                "fool-report", "low-read-only", Map.of(), List.of());

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.reply("openai", session(), AgentCapabilityType.REPORT_QUERY, "hello", draft));

        assertTrue(error.getMessage().contains("HTTP 503"));
        server.verify();
    }

    @Test
    public void providerRequestNeverContainsSecretsOrUnauthorizedFields() {
        AgentProviderProperties properties = new AgentProviderProperties();
        AgentProviderProperties.Provider openai = properties.getProviders().get("openai");
        openai.setApiKey("server-only-key");
        openai.setBaseUrl("https://provider.test/v1");
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo("https://provider.test/v1/chat/completions"))
                .andExpect(content().string(allOf(
                        not(containsString("raw-token-1234567890")),
                        not(containsString("database-password")),
                        not(containsString("jdbc:mysql://private")),
                        not(containsString("privateAmount")),
                        not(containsString("DangerousExecutor")))))
                .andRespond(withSuccess(
                        "{\"choices\":[{\"message\":{\"content\":\"安全草案\"}}]}",
                        MediaType.APPLICATION_JSON));
        DataPolicy dataPolicy = new DataPolicy(
                List.of(new DataPolicy.RowRule("ALL", Map.of())),
                Set.of("publicName"),
                Set.of(), Set.of(), Set.of(), Set.of(), Set.of("publicName"),
                Map.of(), Map.of("publicName", DataClassification.PUBLIC), Set.of("openai"), 10, 10);
        AgentDraft draft = new AgentDraft(
                AgentCapabilityType.REPORT_QUERY,
                "已生成草案",
                "fool-report",
                "low-read-only",
                Map.of(
                        "fields", List.of(
                                Map.of("PropertyName", "publicName", "Value", "visible"),
                                Map.of("PropertyName", "privateAmount", "Value", "99")),
                        "Password", "database-password",
                        "ConnectionString", "jdbc:mysql://private",
                        "ClassName", "DangerousExecutor"),
                List.of());

        AgentChatProviderService.Reply reply = service(properties, restTemplate).reply(
                "openai",
                session(),
                AgentCapabilityType.REPORT_QUERY,
                "Authorization: Bearer raw-token-1234567890",
                draft,
                dataPolicy);

        assertEquals("安全草案", reply.getContent());
        server.verify();
    }

    private AgentChatProviderService service(AgentProviderProperties properties, RestTemplate restTemplate) {
        return new AgentChatProviderService(properties, new ObjectMapper(), restTemplate);
    }

    private AgentSession session() {
        AgentSession session = new AgentSession(
                "session-1",
                "user-1",
                "fool-service",
                "car_wash",
                "auth-session-1",
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
