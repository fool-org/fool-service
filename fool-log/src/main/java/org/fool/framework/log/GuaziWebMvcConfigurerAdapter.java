package org.fool.framework.log;

import lombok.Getter;
import org.fool.framework.log.config.LogingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.ByteArrayOutputStream;

/**
 * @author geyunfei
 */
@Configuration
public class GuaziWebMvcConfigurerAdapter implements WebMvcConfigurer {

    /**
     * 通过teeoutputstream来读输出
     ***/
    @Getter
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


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