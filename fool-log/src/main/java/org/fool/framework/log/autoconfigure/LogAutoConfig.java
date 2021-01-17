package org.fool.framework.log.autoconfigure;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author geyunfei
 */
@Slf4j
@ConditionalOnProperty(prefix = "fool.log", name = "enable", havingValue = "true", matchIfMissing = true)
@Configuration
@ComponentScan(value = {"org.fool.framework.log"})
public class LogAutoConfig {
    @PostConstruct
    public void init() {
        log.info("the fool framework log auto configured.");
    }
}
