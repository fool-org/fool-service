package org.fool.framework.view.autoconfigure;

import org.fool.framework.common.authz.ControlledActionExecutionGuard;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "fool.view", name = "enable", havingValue = "true", matchIfMissing = true)
@Configuration
@ComponentScan(value = {"org.fool.framework.view"})
public class FoolViewAutoConfigure {
    @Bean
    @ConditionalOnMissingBean(ControlledActionExecutionGuard.class)
    public ControlledActionExecutionGuard controlledActionExecutionGuard() {
        return new ControlledActionExecutionGuard();
    }
}
