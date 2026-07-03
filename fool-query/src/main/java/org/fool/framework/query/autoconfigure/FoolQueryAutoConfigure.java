package org.fool.framework.query.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "fool.query", name = "enable", havingValue = "true", matchIfMissing = true)
@Configuration
@ComponentScan(value = {"org.fool.framework.query"})
public class FoolQueryAutoConfigure {
}
