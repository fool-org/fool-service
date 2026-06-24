package org.fool.framework.log;

import org.fool.framework.log.config.LogingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author geyunfei
 */
@Configuration
public class GuaziWebMvcConfigurerAdapter implements WebMvcConfigurer {

    @Autowired
    private CommonLogInterceptor commonLogInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        var interecptor = registry.addInterceptor(this.commonLogInterceptor)
                .addPathPatterns("/**");
        for (var prefix : LogingConfig.NO_LOG_PATH) {
            interecptor = interecptor.excludePathPatterns(prefix);
        }
    }
}
