package org.fool.framework.error.autoconfigure;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * @author geyunfei
 */
@Slf4j
@ConditionalOnProperty(prefix = "guazi.fin.error", name = "enable", havingValue = "true", matchIfMissing = true)
@Configuration
@ComponentScan(value = {"org.fool.framework.error"})
@Data
public class ErrorHandleAutoConfig {
    private Map<String, String> config;

    @PostConstruct
    public void init() {
        log.info("the guazi fin error handle is auto configured.");
    }
}
