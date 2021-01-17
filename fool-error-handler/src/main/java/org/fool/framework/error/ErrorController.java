package org.fool.framework.error;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.dto.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author geyunfei
 * 基本的错误处理类
 */
@Controller
@Slf4j
public class ErrorController extends BasicErrorController {


    private ErrorAttributes errorAttributes;
    @Autowired
    private ExceptionDealerService dealerService;

    public ErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes, new ErrorProperties());
        this.errorAttributes = errorAttributes;
    }

    /**
     * 任何情况下都返回200
     * 是不是有点激进.....
     *
     * @param request
     * @return
     */
    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = this.getStatus(request);
        if (status == HttpStatus.NO_CONTENT) {
            return new ResponseEntity(status);
        } else {
            Map<String, Object> body = this.getErrorAttributes(request, this.isIncludeStackTrace(request, MediaType.ALL));
            CommonResponse commonResponse;
            Throwable throwable = this.errorAttributes.getError(new ServletWebRequest(request));
            if (throwable == null) {
                log.error("发生没有处理的异常:{}", body);
                commonResponse = CommonResponse.builder()
                        .code(CommonResponse.UNION_ERR_CODE)
                        .message("发生未处理的错误:" + body.getOrDefault("error", ""))
                        .build();

            } else {
                commonResponse = dealerService.handle(throwable);
                if (commonResponse == null) {
                    commonResponse = CommonResponse.builder()
                            .code(CommonResponse.UNION_ERR_CODE)
                            .message("发生未处理的错误:" + body.getOrDefault("error", ""))
                            .build();
                }
            }
            return new ResponseEntity(commonResponse
                    , HttpStatus.OK);
        }
    }


}
