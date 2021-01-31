package org.fool.framework.model.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "fool.model", name = "enable", havingValue = "true", matchIfMissing = true)
@Configuration
@ComponentScan(value = {"org.fool.framework.model"})
public class FoolModelAutoConfigure {
}
