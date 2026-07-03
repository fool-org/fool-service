package org.fool.framework.auth.business.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.auth.utils.RedisUtils;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CheckCodeServiceTest {
    @Test
    public void createStoresGeneratedCodeForLegacyValidation() throws Exception {
        RedisUtils redisUtils = mock(RedisUtils.class);
        CheckCodeService service = new CheckCodeService();
        setField(service, "redisUtils", redisUtils);

        CheckCodeService.CheckCodeResult result = service.create();

        assertTrue(result.getKey().length() > 10);
        assertTrue(result.getCode().matches("[2345689ABCDEFGHJKLMNPRSTWXY]{4}"));
        assertTrue(result.getChkCodeImg().length() > 20);
        verify(redisUtils).set(startsWith("CHECK_CODE:"), eq(result.getCode()), eq(60000L));
    }

    @Test
    public void validateMatchesStoredCodeIgnoringCaseAndWhitespace() throws Exception {
        RedisUtils redisUtils = mock(RedisUtils.class);
        when(redisUtils.get("CHECK_CODE:key-1")).thenReturn("AbC2");
        CheckCodeService service = new CheckCodeService();
        setField(service, "redisUtils", redisUtils);

        assertTrue(service.validate(request("key-1", " abc2 ")));
        assertFalse(service.validate(request("key-1", "wrong")));
    }

    @Test
    public void requestAcceptsLegacyCapitalizedFields() throws Exception {
        CheckCodeService.CheckCodeRequest request = new ObjectMapper().readValue(
                "{\"Key\":\"key-1\",\"Code\":\"A2BC\",\"ChkCodeImg\":\"image\"}",
                CheckCodeService.CheckCodeRequest.class);

        assertEquals("key-1", request.getKey());
        assertEquals("A2BC", request.getCode());
        assertEquals("image", request.getChkCodeImg());
    }

    private static CheckCodeService.CheckCodeRequest request(String key, String code) {
        CheckCodeService.CheckCodeRequest request = new CheckCodeService.CheckCodeRequest();
        request.setKey(key);
        request.setCode(code);
        return request;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
