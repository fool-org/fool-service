package org.fool.framework.error;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.dto.CommonException;
import org.fool.framework.dto.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author geyunfei
 */
@ControllerAdvice
@Slf4j
public class ErrorHandleController {

    @Autowired
    private ExceptionDealerService dealerService;

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
