package org.fool.framework.error;

import org.fool.framework.dto.CommonException;
import org.fool.framework.dto.CommonResponse;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class CommonExceptionHandler implements ExceptionHandler<CommonException> {
    @Override
    public CommonResponse handle(CommonException e) {

        return CommonResponse.builder().code(e.getCode()).message(e.getMessage()).build();
    }
}
