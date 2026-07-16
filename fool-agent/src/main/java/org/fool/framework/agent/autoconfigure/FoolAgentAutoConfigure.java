package org.fool.framework.agent.autoconfigure;

import org.fool.framework.agent.service.AgentSessionStore;
import org.fool.framework.agent.service.AgentProviderProperties;
import org.fool.framework.agent.service.DataSourceMetadataProvider;
import org.fool.framework.agent.service.EventAutomationMetadataProvider;
import org.fool.framework.agent.service.InMemoryAgentSessionStore;
import org.fool.framework.agent.service.JdbcDataSourceMetadataProvider;
import org.fool.framework.agent.service.JdbcAgentSessionStore;
import org.fool.framework.agent.service.JdbcEventAutomationMetadataProvider;
import org.fool.framework.agent.service.JdbcModelMetadataProvider;
import org.fool.framework.agent.service.JdbcReportQueryMetadataProvider;
import org.fool.framework.agent.service.JdbcTableSchemaProvider;
import org.fool.framework.agent.service.ModelMetadataProvider;
import org.fool.framework.agent.service.ReportQueryMetadataProvider;
import org.fool.framework.agent.service.TableSchemaProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;

@ConditionalOnProperty(prefix = "fool.agent", name = "enable", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(JdbcTemplateAutoConfiguration.class)
@Configuration
@EnableConfigurationProperties(AgentProviderProperties.class)
@ComponentScan(value = {"org.fool.framework.agent"})
public class FoolAgentAutoConfigure {
    @Bean
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean(AgentSessionStore.class)
    public AgentSessionStore jdbcAgentSessionStore(JdbcTemplate jdbcTemplate) {
        return new JdbcAgentSessionStore(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(AgentSessionStore.class)
    public AgentSessionStore inMemoryAgentSessionStore() {
        return new InMemoryAgentSessionStore();
    }

    @Bean
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean(ReportQueryMetadataProvider.class)
    public ReportQueryMetadataProvider jdbcReportQueryMetadataProvider(JdbcTemplate jdbcTemplate) {
        return new JdbcReportQueryMetadataProvider(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(ReportQueryMetadataProvider.class)
    public ReportQueryMetadataProvider unavailableReportQueryMetadataProvider() {
        return ReportQueryMetadataProvider.unavailable();
    }

    @Bean
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean(ModelMetadataProvider.class)
    public ModelMetadataProvider jdbcModelMetadataProvider(JdbcTemplate jdbcTemplate) {
        return new JdbcModelMetadataProvider(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(ModelMetadataProvider.class)
    public ModelMetadataProvider unavailableModelMetadataProvider() {
        return ModelMetadataProvider.unavailable();
    }

    @Bean
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean(DataSourceMetadataProvider.class)
    public DataSourceMetadataProvider jdbcDataSourceMetadataProvider(JdbcTemplate jdbcTemplate) {
        return new JdbcDataSourceMetadataProvider(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(DataSourceMetadataProvider.class)
    public DataSourceMetadataProvider unavailableDataSourceMetadataProvider() {
        return DataSourceMetadataProvider.unavailable();
    }

    @Bean
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean(EventAutomationMetadataProvider.class)
    public EventAutomationMetadataProvider jdbcEventAutomationMetadataProvider(JdbcTemplate jdbcTemplate) {
        return new JdbcEventAutomationMetadataProvider(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(EventAutomationMetadataProvider.class)
    public EventAutomationMetadataProvider unavailableEventAutomationMetadataProvider() {
        return EventAutomationMetadataProvider.unavailable();
    }

    @Bean
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean(TableSchemaProvider.class)
    public TableSchemaProvider jdbcTableSchemaProvider(JdbcTemplate jdbcTemplate) {
        return new JdbcTableSchemaProvider(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(TableSchemaProvider.class)
    public TableSchemaProvider unavailableTableSchemaProvider() {
        return TableSchemaProvider.unavailable();
    }
}
