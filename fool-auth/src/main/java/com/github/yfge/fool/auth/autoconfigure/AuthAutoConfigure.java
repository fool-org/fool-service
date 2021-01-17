package com.github.yfge.fool.auth.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

@ConditionalOnProperty(prefix = "fool-frame.auth", value = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(value = "com.github.yfge.fool.auth",basePackages = "com.github.yfge.fool.auth")
public class AuthAutoConfigure {
}
