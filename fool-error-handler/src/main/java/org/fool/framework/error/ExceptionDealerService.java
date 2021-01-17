package org.fool.framework.error;


import lombok.extern.slf4j.Slf4j;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.error.autoconfigure.ErrorHandleAutoConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class ExceptionDealerService {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private ErrorHandleAutoConfig config;


    public CommonResponse handle(Throwable ex) {
        String finalExceptionClass = ex.getClass().getCanonicalName();
        if (StringUtils.isEmpty(finalExceptionClass)) {
            return null;
        }
        /**
         * 检查是否有专门的异常处理类
         *
         */
        try {
            Class exceptionClass = Class.forName(finalExceptionClass);
            while (true) {
                String[] beanNames = context.getBeanNamesForType(ResolvableType.forClassWithGenerics(ExceptionHandler.class, exceptionClass));
                if (beanNames != null && beanNames.length > 0) {
                    var handler = (ExceptionHandler) context.getBean(beanNames[0]);
                    return handler.handle(ex);
                }
                exceptionClass = exceptionClass.getSuperclass();
                if (exceptionClass == Object.class) {
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
        }
        String[] names = finalExceptionClass.split("\\.");
        if (config.getConfig() == null) {
            log.error("", ex);
            return null;
        }
        String logging = config.getConfig().getOrDefault(names[names.length - 1], "error");
        switch (logging) {
            case "info":
                log.info("", ex);
                break;
            case "debug":
                log.debug("", ex);
                break;
            case "warn":
                log.warn("", ex);
                break;
            case "trace":
                log.trace("", ex);
            default:
                log.error("", ex);
        }
        /**
         * 检查配置
         */
        return null;
    }
}
