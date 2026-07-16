package org.fool.framework.error;

import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.dto.CommonResponse;
import org.springframework.stereotype.Component;

@Component
public class ControlledActionExceptionHandler implements ExceptionHandler<ControlledActionException> {
    @Override
    public CommonResponse<Void> handle(ControlledActionException exception) {
        return CommonResponse.<Void>builder()
                .code(exception.status())
                .message(exception.getMessage())
                .build();
    }
}
