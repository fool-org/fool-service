package org.fool.framework.auth.business.service;

import org.fool.framework.auth.business.common.RedisKeyPrefix;
import org.fool.framework.auth.utils.RedisUtils;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TokenServiceLogoutTest {
    @Test
    public void logoutTokenDeletesTokenAndUserTokenKeys() throws Exception {
        RedisUtils redisUtils = mock(RedisUtils.class);
        String hash = TokenService.tokenHash("token-1");
        when(redisUtils.get(RedisKeyPrefix.USER_TOKEN_PREFIX + hash)).thenReturn("admin");
        when(redisUtils.get(RedisKeyPrefix.USER_ID_TOKEN_PREFIX + "admin")).thenReturn(hash);
        TokenService tokenService = new TokenService();
        setField(tokenService, "redisUtils", redisUtils);

        tokenService.logoutToken("token-1");

        verify(redisUtils).del(RedisKeyPrefix.USER_TOKEN_PREFIX + hash);
        verify(redisUtils).del(RedisKeyPrefix.TOKEN_ABSOLUTE_PREFIX + hash);
        verify(redisUtils).del(RedisKeyPrefix.USER_ID_TOKEN_PREFIX + "admin");
        verify(redisUtils).del(RedisKeyPrefix.LEGACY_APP_TOKEN_PREFIX + hash);
        verify(redisUtils).del(RedisKeyPrefix.LEGACY_DB_TOKEN_PREFIX + hash);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
