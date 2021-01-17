package org.fool.framework.log;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Getter
public class EnvConfig {

    private String profile;

    @Autowired
    ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        profile = applicationContext.getEnvironment().getActiveProfiles()[0];
    }
}
