package org.fool.framework.log;

import org.fool.framework.log.config.LogingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author geyunfei
 */
@Component
public class CachingRequestBodyFilter extends GenericFilterBean {

    @Autowired
    private GuaziWebMvcConfigurerAdapter myConfiguration;


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest currentRequest = (HttpServletRequest) servletRequest;

        String path = currentRequest.getServletPath();
        for(var prefix : LogingConfig.NO_LOG_PATH){
            if(path.startsWith(prefix)){
                chain.doFilter(servletRequest, servletResponse);
                return;
            }
        }
        RequestWrapper wrappedRequest = new RequestWrapper(currentRequest);
        HttpServletResponse responseWrapper = loggingResponseWrapper((HttpServletResponse) servletResponse, myConfiguration.getByteArrayOutputStream());
        chain.doFilter(wrappedRequest, responseWrapper);
        return;

    }

    private HttpServletResponse loggingResponseWrapper(HttpServletResponse response, ByteArrayOutputStream byteArrayOutputStream) {
        MyHttpServletRequestWrapper wrapper = new MyHttpServletRequestWrapper(response);
        wrapper.setByteArrayOutputStream(byteArrayOutputStream);
        return wrapper;
    }
}