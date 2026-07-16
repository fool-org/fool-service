package org.fool.framework.error;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.dto.CommonException;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.common.authz.AuthorizationDeniedException;
import org.fool.framework.common.authz.ControlledActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @author geyunfei
 */
@ControllerAdvice
@Slf4j
public class ErrorHandleController {

    @Autowired
    private ExceptionDealerService dealerService;

    @ResponseBody
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<CommonResponse<Void>> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(CommonResponse.<Void>builder()
                        .code(HttpStatus.FORBIDDEN.value())
                        .message(ex.reasonCode())
                        .build());
    }

    @ResponseBody
    @ExceptionHandler(ControlledActionException.class)
    public ResponseEntity<CommonResponse<Void>> handleControlledAction(ControlledActionException ex) {
        return ResponseEntity.status(ex.status())
                .body(CommonResponse.<Void>builder()
                        .code(ex.status())
                        .message(ex.getMessage())
                        .build());
    }

    @ResponseBody
    @ExceptionHandler(Throwable.class)
    public CommonResponse handleError(Throwable ex) {
        CommonResponse commonResponse = dealerService.handle(ex);
        if (commonResponse != null) {
            return commonResponse;
        }
        return CommonResponse.builder()
                .code(CommonResponse.UNION_ERR_CODE)
                .message("发生未处理的内部错误:" + ex.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(CommonException.class)
    public CommonResponse handleError(CommonException ex) {
        CommonResponse commonResponse = dealerService.handle(ex);
        if (commonResponse != null) {
            return commonResponse;
        }
        return CommonResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();
    }
}
