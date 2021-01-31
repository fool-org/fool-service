package org.fool.framework.auth.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

@ConditionalOnProperty(prefix = "fool.auth", value = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(value = "org.fool.framework.auth",basePackages = "org.fool.framework.auth")
public class AuthAutoConfigure {
}
