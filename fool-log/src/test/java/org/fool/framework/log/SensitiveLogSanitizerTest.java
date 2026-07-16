package org.fool.framework.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SensitiveLogSanitizerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SensitiveLogSanitizer sanitizer = new SensitiveLogSanitizer(objectMapper);

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

    @Test
    public void recursivelyRedactsCheckCodePayloads() throws Exception {
        JsonNode initApp = objectMapper.readTree(sanitizer.sanitize("""
                {"code":0,"message":"success","data":{"CheckCode":{
                 "key":"key-1","code":"A2BC","chkCodeImg":"image-1",
                 "Key":"key-2","Code":"B3CD","ChkCodeImg":"image-2",
                 "chkkey":"key-3","chkimg":"image-3"}}}
                """));
        JsonNode getCheckCode = objectMapper.readTree(sanitizer.sanitize("""
                {"code":0,"message":"success","data":{
                 "Key":"key-4","Code":"C4DE","ChkCodeImg":"image-4",
                 "chkkey":"key-5","chkimg":"image-5"}}
                """));

        assertEquals(0, initApp.path("code").asInt());
        assertEquals("success", initApp.path("message").asText());
        initApp.path("data").path("CheckCode").fields().forEachRemaining(
                field -> assertEquals("[REDACTED]", field.getValue().asText()));
        assertEquals(0, getCheckCode.path("code").asInt());
        getCheckCode.path("data").fields().forEachRemaining(
                field -> assertEquals("[REDACTED]", field.getValue().asText()));
    }

    @Test
    public void redactsLoginCheckCodeAliasesWithoutHidingNormalCodes() throws Exception {
        JsonNode sanitized = objectMapper.readTree(sanitizer.sanitize("""
                {"CheckCode":"A2BC","CheckCodeKey":"key-1","chk":"B3CD","chkid":"key-2",
                 "AppKey":"app-secret","data":{"code":"ORDER_READY","message":"visible"}}
                """));

        assertEquals("[REDACTED]", sanitized.path("CheckCode").asText());
        assertEquals("[REDACTED]", sanitized.path("CheckCodeKey").asText());
        assertEquals("[REDACTED]", sanitized.path("chk").asText());
        assertEquals("[REDACTED]", sanitized.path("chkid").asText());
        assertEquals("[REDACTED]", sanitized.path("AppKey").asText());
        assertEquals("ORDER_READY", sanitized.path("data").path("code").asText());
        assertEquals("visible", sanitized.path("data").path("message").asText());
    }
}
