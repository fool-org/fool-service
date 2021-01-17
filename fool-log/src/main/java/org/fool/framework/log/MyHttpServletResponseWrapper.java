package org.fool.framework.log;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.output.TeeOutputStream;
import org.springframework.mock.web.DelegatingServletOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * @author geyunfei
 */
public class MyHttpServletResponseWrapper extends HttpServletResponseWrapper {

    public MyHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Getter
    @Setter
    private ByteArrayOutputStream byteArrayOutputStream;


    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new DelegatingServletOutputStream(
                new TeeOutputStream(super.getOutputStream(), loggingOutputStream())
        );
    }

    private ByteArrayOutputStream loggingOutputStream() {
        return this.byteArrayOutputStream;
    }
}
