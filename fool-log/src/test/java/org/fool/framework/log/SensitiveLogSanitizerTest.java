package org.fool.framework.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SensitiveLogSanitizerTest {
    private final SensitiveLogSanitizer sanitizer = new SensitiveLogSanitizer(new ObjectMapper());

    @Test
    public void recursivelyRedactsCredentialsAndTokens() {
        String sanitized = sanitizer.sanitize("""
                {"password":"plain-password","data":{"token":"opaque-token"},
                 "items":[{"connectionString":"jdbc:mysql://secret"}],"title":"safe"}
                """);

        assertFalse(sanitized.contains("plain-password"));
        assertFalse(sanitized.contains("opaque-token"));
        assertFalse(sanitized.contains("jdbc:mysql://secret"));
        assertTrue(sanitized.contains("safe"));
        assertTrue(sanitized.contains("[REDACTED]"));
    }

    @Test
    public void scalarAndUnparseableBodiesAreNeverLoggedRaw() {
        assertFalse(sanitizer.sanitize("\"opaque-token\"").contains("opaque-token"));
        assertFalse(sanitizer.sanitize("raw-secret-value").contains("raw-secret-value"));
    }
}
