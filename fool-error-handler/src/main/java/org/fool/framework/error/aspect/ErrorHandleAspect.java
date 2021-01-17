package org.fool.framework.error.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.error.ExceptionDealerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Slf4j
public class ErrorHandleAspect {


    @Autowired
    private ExceptionDealerService dealerService;

    /**
     * 所有 http 的接口
     */
    @Pointcut(value = "@annotation(org.springframework.web.bind.annotation.PostMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.RequestMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void pointCut() {
    }

    /**
     * 错误处理
     *
     * @param joinPoint
     * @return
     */
    @Around(value = "pointCut()")
    public Object arroundPointCut(ProceedingJoinPoint joinPoint) {
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            result = dealerService.handle(ex);
            if (result == null) {
                result = CommonResponse.UNOWN_ERROR_RESPONSE;
            }
        }
        return result;
    }
}

