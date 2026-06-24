package org.fool.framework.event.autoconfigure;

import org.fool.framework.event.EventSchedulerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "fool.event", name = "enable", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableConfigurationProperties(EventSchedulerProperties.class)
@ComponentScan(value = {"org.fool.framework.event"})
public class EventAutoConfigure {
}
