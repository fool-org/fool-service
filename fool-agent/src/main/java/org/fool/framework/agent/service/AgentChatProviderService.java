package org.fool.framework.agent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AgentChatProviderService {
    private static final int MAX_HISTORY_MESSAGES = 20;

    private final AgentProviderProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Autowired
    public AgentChatProviderService(AgentProviderProperties properties,
                                    ObjectMapper objectMapper,
                                    RestTemplateBuilder restTemplateBuilder) {
        this(properties, objectMapper, restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(90))
                .build());
    }

    AgentChatProviderService(AgentProviderProperties properties,
                             ObjectMapper objectMapper,
                             RestTemplate restTemplate) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    public List<ProviderInfo> providers() {
        String defaultId = defaultProviderId();
        return properties.getProviders().entrySet().stream()
                .map(entry -> new ProviderInfo(
                        entry.getKey(),
                        displayName(entry.getKey(), entry.getValue()),
                        entry.getValue().getModel(),
                        configured(entry.getValue()),
                        entry.getKey().equals(defaultId)))
                .toList();
    }

    public Reply reply(String requestedProvider,
                       AgentSession session,
                       AgentCapabilityType capability,
                       String userContent,
                       AgentDraft draft) {
        Map.Entry<String, AgentProviderProperties.Provider> selected = select(requestedProvider);
        if (selected == null) {
            return Reply.local(draft.getSummary());
        }

        AgentProviderProperties.Provider provider = selected.getValue();
        List<Map<String, String>> messages = messages(session, capability, userContent, draft);
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", provider.getModel());
        request.put("messages", messages);
        request.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(provider.getApiKey().trim());

        ResponseEntity<JsonNode> response;
        try {
            response = restTemplate.postForEntity(
                    completionUrl(provider.getBaseUrl()),
                    new HttpEntity<>(request, headers),
                    JsonNode.class);
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException(displayName(selected.getKey(), provider)
                    + " 请求失败（HTTP " + ex.getRawStatusCode() + "）。");
        } catch (RestClientException ex) {
            throw new IllegalStateException(displayName(selected.getKey(), provider) + " 暂时不可用。", ex);
        }

        String content = response.getBody() == null
                ? ""
                : response.getBody().path("choices").path(0).path("message").path("content").asText("");
        if (!StringUtils.hasText(content)) {
            throw new IllegalStateException(displayName(selected.getKey(), provider) + " 返回了空回复。");
        }
        return new Reply(content.trim(), selected.getKey(), provider.getModel());
    }

    private List<Map<String, String>> messages(AgentSession session,
                                                AgentCapabilityType capability,
                                                String userContent,
                                                AgentDraft draft) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", systemPrompt(capability, draft)));
        int first = Math.max(0, session.getMessages().size() - MAX_HISTORY_MESSAGES);
        session.getMessages().subList(first, session.getMessages().size()).stream()
                .map(existing -> message(role(existing.getRole()), existing.getContent()))
                .forEach(messages::add);
        messages.add(message("user", userContent));
        return messages;
    }

    private String systemPrompt(AgentCapabilityType capability, AgentDraft draft) {
        return "你是 Fool Service 的配置助手。当前阶段是“" + capability.getDisplayName() + "”。"
                + "请用中文直接回答用户，基于系统生成的只读草案给出清晰、简短、可执行的建议。"
                + "不要声称已经执行写入、DDL、操作或外部副作用；需要变更时明确要求人工确认。\n"
                + "草案摘要：" + draft.getSummary() + "\n"
                + "风险级别：" + draft.getRiskLevel() + "\n"
                + "草案数据：" + json(draft.getDraftPayload()) + "\n"
                + "验证步骤：" + json(draft.getValidationSteps());
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize agent draft context.", ex);
        }
    }

    private Map.Entry<String, AgentProviderProperties.Provider> select(String requestedProvider) {
        if (StringUtils.hasText(requestedProvider)) {
            if ("local".equalsIgnoreCase(requestedProvider.trim())) {
                return null;
            }
            Map.Entry<String, AgentProviderProperties.Provider> selected = properties.getProviders().entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(requestedProvider.trim()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown agent provider: " + requestedProvider));
            if (!configured(selected.getValue())) {
                throw new IllegalStateException(displayName(selected.getKey(), selected.getValue())
                        + " 尚未配置 API Key。请设置服务端环境变量后重启。");
            }
            return selected;
        }

        String defaultId = defaultProviderId();
        if (defaultId == null) {
            return null;
        }
        return Map.entry(defaultId, properties.getProviders().get(defaultId));
    }

    private String defaultProviderId() {
        String configuredDefault = properties.getDefaultProvider();
        if (StringUtils.hasText(configuredDefault)) {
            for (Map.Entry<String, AgentProviderProperties.Provider> entry : properties.getProviders().entrySet()) {
                if (entry.getKey().equalsIgnoreCase(configuredDefault.trim()) && configured(entry.getValue())) {
                    return entry.getKey();
                }
            }
        }
        return properties.getProviders().entrySet().stream()
                .filter(entry -> configured(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private boolean configured(AgentProviderProperties.Provider provider) {
        return provider != null
                && StringUtils.hasText(provider.getApiKey())
                && StringUtils.hasText(provider.getBaseUrl())
                && StringUtils.hasText(provider.getModel());
    }

    private String displayName(String id, AgentProviderProperties.Provider provider) {
        return StringUtils.hasText(provider.getDisplayName())
                ? provider.getDisplayName().trim()
                : id.toUpperCase(Locale.ROOT);
    }

    private String completionUrl(String baseUrl) {
        String normalized = baseUrl.trim().replaceAll("/+$", "");
        return normalized.endsWith("/chat/completions") ? normalized : normalized + "/chat/completions";
    }

    private Map<String, String> message(String role, String content) {
        return Map.of("role", role, "content", content);
    }

    private String role(AgentMessageRole role) {
        return switch (role) {
            case USER -> "user";
            case AGENT -> "assistant";
            case SYSTEM -> "system";
        };
    }

    public static class ProviderInfo {
        private final String id;
        private final String displayName;
        private final String model;
        private final boolean configured;
        private final boolean defaultProvider;

        ProviderInfo(String id, String displayName, String model, boolean configured, boolean defaultProvider) {
            this.id = id;
            this.displayName = displayName;
            this.model = model;
            this.configured = configured;
            this.defaultProvider = defaultProvider;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getModel() {
            return model;
        }

        public boolean isConfigured() {
            return configured;
        }

        public boolean isDefaultProvider() {
            return defaultProvider;
        }
    }

    public static class Reply {
        private final String content;
        private final String provider;
        private final String model;

        Reply(String content, String provider, String model) {
            this.content = content;
            this.provider = provider;
            this.model = model;
        }

        static Reply local(String content) {
            return new Reply(content, "local", "deterministic");
        }

        public String getContent() {
            return content;
        }

        public String getProvider() {
            return provider;
        }

        public String getModel() {
            return model;
        }
    }
}
