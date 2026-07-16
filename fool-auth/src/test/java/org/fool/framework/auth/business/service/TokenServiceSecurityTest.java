package org.fool.framework.auth.business.service;

import org.fool.framework.auth.business.common.RedisKeyPrefix;
import org.fool.framework.auth.utils.RedisUtils;
import org.junit.Test;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TokenServiceSecurityTest {
    @Test
    public void issuedTokenIsRandomAndOnlyItsHashIsStored() throws Exception {
        RedisUtils redisUtils = mock(RedisUtils.class);
        TokenService tokenService = service(redisUtils, 1_000L, 5_000L);

        String token = tokenService.getTokenByUid("admin");
        String hash = TokenService.tokenHash(token);

        assertTrue(token.length() >= 43);
        assertFalse(token.equals(hash));
        verify(redisUtils).set(RedisKeyPrefix.USER_TOKEN_PREFIX + hash, "admin", 1_000L);
        verify(redisUtils).set(eq(RedisKeyPrefix.TOKEN_ABSOLUTE_PREFIX + hash), anyString(), eq(5_000L));
        verify(redisUtils).set(RedisKeyPrefix.USER_ID_TOKEN_PREFIX + "admin", hash, 5_000L);
    }

    @Test
    public void authenticationUsesHashAndRefreshesOnlyIdleLifetime() throws Exception {
        RedisUtils redisUtils = mock(RedisUtils.class);
        TokenService tokenService = service(redisUtils, 30_000L, 300_000L);
        String token = "opaque-token";
        String hash = TokenService.tokenHash(token);
        when(redisUtils.get(RedisKeyPrefix.TOKEN_ABSOLUTE_PREFIX + hash)).thenReturn("1784044800000");
        when(redisUtils.get(RedisKeyPrefix.USER_TOKEN_PREFIX + hash)).thenReturn("admin");
        when(redisUtils.ttl(RedisKeyPrefix.TOKEN_ABSOLUTE_PREFIX + hash)).thenReturn(120L);

        TokenService.AuthenticatedToken authenticated = tokenService.authenticate(token);

        assertEquals("admin", authenticated.userId());
        assertEquals(hash, authenticated.tokenHash());
        assertTrue(authenticated.sessionId().startsWith("auth:"));
        verify(redisUtils).set(RedisKeyPrefix.USER_TOKEN_PREFIX + hash, "admin", 30_000L);
    }

    @Test
    public void stepUpProofIsShortLivedAndRevokedWithTheToken() throws Exception {
        RedisUtils redisUtils = mock(RedisUtils.class);
        TokenService tokenService = service(redisUtils, 30_000L, 300_000L);
        String sessionId = "auth:012345678901234567890123";

        tokenService.recordStepUp(sessionId);
        verify(redisUtils).set(eq(RedisKeyPrefix.STEP_UP_SESSION_PREFIX + sessionId), anyString(), eq(300_000L));

        when(redisUtils.get(RedisKeyPrefix.STEP_UP_SESSION_PREFIX + sessionId))
                .thenReturn(Long.toString(Instant.parse("2026-07-15T10:00:00Z").toEpochMilli()));
        assertEquals(Instant.parse("2026-07-15T10:00:00Z"), tokenService.stepUpAt(sessionId));
    }

    private static TokenService service(RedisUtils redisUtils, long idleTtl, long absoluteTtl) throws Exception {
        TokenService service = new TokenService();
        setField(service, "redisUtils", redisUtils);
        setField(service, "idleTtlMillis", idleTtl);
        setField(service, "absoluteTtlMillis", absoluteTtl);
        return service;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
