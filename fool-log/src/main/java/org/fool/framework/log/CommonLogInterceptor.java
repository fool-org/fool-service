package org.fool.framework.log;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 增加统一的 requestId
 *
 * @Author geyunfei
 */
@Slf4j
@Component
public class CommonLogInterceptor implements HandlerInterceptor {
    /**
     * request-id的设置
     * 在med-usa下,request-id的key是x-request-id
     */
    private static final String REQUEST_ID = "x-request-id";

    @Autowired
    private GuaziWebMvcConfigurerAdapter guaziWebMvcConfigurerAdapter;


    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        String traceId = httpServletRequest.getHeader(REQUEST_ID);
        if (StringUtils.isEmpty(traceId)) {
            GuaziThreadContextHelper.setThreadContext();
        } else {
            GuaziThreadContextHelper.setThreadContext(traceId);
        }
        GuaziThreadContextHelper.setBusinessCode(httpServletRequest.getRequestURI());
        //设置起始时间
        Date now = new Date();
        GuaziThreadContextHelper.setStartTime(now.getTime());
        RequestWrapper wrappedRequest = new RequestWrapper(httpServletRequest);
        //
        String path = wrappedRequest.getServletPath();
        GuaziThreadContextHelper.setFilter(path);
        //
        log.debug("ServiceRequest [{}] {}", wrappedRequest.getRequestURL(), IOUtils.toString(wrappedRequest.getInputStream(), "UTF-8"));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {

    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) throws Exception {
        String body = this.guaziWebMvcConfigurerAdapter.getByteArrayOutputStream().toString();
        this.guaziWebMvcConfigurerAdapter.getByteArrayOutputStream().reset();
        //
        long latency = -1;
        Long startTime = GuaziThreadContextHelper.getStartTime();
        if (startTime != null) {
            Date now = new Date();
            latency = now.getTime() - startTime;
        }

        log.info("ServiceResponse [{}]( latency: {} ms ). {} . With Request {} .", request.getRequestURL(), latency, body, IOUtils.toString(request.getInputStream(), "UTF-8"));

        GuaziThreadContextHelper.clearThreadContext();
    }


}
