package org.fool.framework.app.autoconfigure;

import org.fool.framework.app.AppBootstrapPlan;
import org.fool.framework.app.AppInstallGateway;
import org.fool.framework.app.AppInstaller;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "fool.app", name = "enable", havingValue = "true", matchIfMissing = true)
@Configuration
@ComponentScan(value = {"org.fool.framework.app"})
@EnableConfigurationProperties(AppInitializationProperties.class)
public class AppManageAutoConfigure {
    @Bean
    @ConditionalOnMissingBean
    public AppBootstrapPlan appBootstrapPlan() {
        return AppBootstrapPlan.legacyDefaults();
    }

    @Bean
    @ConditionalOnMissingBean
    public AppInstaller appInstaller(AppInstallGateway gateway, AppBootstrapPlan plan) {
        return new AppInstaller(gateway, plan);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "fool.app.initialization",
            name = "enabled",
            havingValue = "true")
    public AppInitializationRunner appInitializationRunner(
            AppInstaller installer,
            AppInitializationProperties properties) {
        return new AppInitializationRunner(installer, properties);
    }
}
