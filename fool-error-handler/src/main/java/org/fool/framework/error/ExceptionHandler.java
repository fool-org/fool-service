package org.fool.framework.error;

import org.fool.framework.dto.CommonResponse;

/**
 * @author geyunfei
 */
public interface ExceptionHandler<T extends Throwable> {
    CommonResponse handle(T e);
}
