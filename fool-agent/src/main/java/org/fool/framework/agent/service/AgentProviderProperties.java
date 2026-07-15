package org.fool.framework.agent.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "fool.agent.chat")
public class AgentProviderProperties {
    private String defaultProvider = "deepseek";
    private final Map<String, Provider> providers = new LinkedHashMap<>();

    public AgentProviderProperties() {
        providers.put("deepseek", new Provider(
                "DeepSeek", "https://api.deepseek.com", "deepseek-v4-flash"));
        providers.put("openai", new Provider(
                "OpenAI", "https://api.openai.com/v1", "gpt-5-mini"));
    }

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public Map<String, Provider> getProviders() {
        return providers;
    }

    public static class Provider {
        private String displayName;
        private String baseUrl;
        private String apiKey;
        private String model;

        public Provider() {
        }

        public Provider(String displayName, String baseUrl, String model) {
            this.displayName = displayName;
            this.baseUrl = baseUrl;
            this.model = model;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }
}
