package org.fool.framework.error;

import org.fool.framework.common.authz.AuthorizationDeniedException;
import org.fool.framework.dto.CommonResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationDeniedExceptionHandler implements ExceptionHandler<AuthorizationDeniedException> {
    @Override
    public CommonResponse<Void> handle(AuthorizationDeniedException exception) {
        return CommonResponse.<Void>builder()
                .code(403)
                .message(exception.reasonCode())
                .build();
    }
}
