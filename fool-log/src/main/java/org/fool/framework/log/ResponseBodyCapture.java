package org.fool.framework.log;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Stores the response body captured for the current request thread.
 */
@Component
public class ResponseBodyCapture {
    private final ThreadLocal<ByteArrayOutputStream> responseBody =
            ThreadLocal.withInitial(ByteArrayOutputStream::new);

    public ByteArrayOutputStream open() {
        ByteArrayOutputStream buffer = responseBody.get();
        buffer.reset();
        return buffer;
    }

    public String close() {
        try {
            return responseBody.get().toString(StandardCharsets.UTF_8);
        } finally {
            responseBody.remove();
        }
    }
}
