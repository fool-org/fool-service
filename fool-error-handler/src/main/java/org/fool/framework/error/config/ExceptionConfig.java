package org.fool.framework.error.config;

import lombok.Data;

/**
 * @author geyunfei
 * @date Jun 13r,2021
 * 异常处理的配置
 */
@Data
public class ExceptionConfig {
    private String logLevel;
    private String handler;
}
