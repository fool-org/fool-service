package com.github.yfge.fool.dao.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

@ConditionalOnProperty(prefix = "fool-frame.dao", value = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(value = {"com.github.yfge.fool.dao"})
public class DaoAutoConfigure {
}
