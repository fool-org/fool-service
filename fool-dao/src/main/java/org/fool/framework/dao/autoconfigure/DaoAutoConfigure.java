package org.fool.framework.dao.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

@ConditionalOnProperty(prefix = "fool-frame.dao", value = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(value = {"org.fool.framework.dao"})
public class DaoAutoConfigure {
}
