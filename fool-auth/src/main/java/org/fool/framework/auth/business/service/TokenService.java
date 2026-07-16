package org.fool.framework.auth.business.service;


import org.fool.framework.auth.business.common.BusinessErrorCode;
import org.fool.framework.auth.business.common.RedisKeyPrefix;
import org.fool.framework.auth.utils.RedisUtils;
import org.fool.framework.dto.CommonException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class TokenService {
    @Autowired
    private RedisUtils redisUtils;

    @Value("${fool.auth.token.idle-ttl-ms:1800000}")
    private long idleTtlMillis;

    @Value("${fool.auth.token.absolute-ttl-ms:28800000}")
    private long absoluteTtlMillis;

    private final SecureRandom secureRandom = new SecureRandom();

    /***
     * 根据token得到 userId
     * 如果为空表示未登录
     * @param token
     * @return
     */
    public String getUidByToken(String token) {
        return authenticate(token).userId();
    }


    /**
     * 根据userId生成 token
     *
     * @param userId
     * @return
     */
    public synchronized String getTokenByUid(String userId) {
        String userKey = RedisKeyPrefix.USER_ID_TOKEN_PREFIX + userId;
        String previousHash = redisUtils.get(userKey);
        if (StringUtils.hasText(previousHash)) {
            revokeHash(previousHash);
        }

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String hash = tokenHash(token);
        long issuedAt = System.currentTimeMillis();
        redisUtils.set(RedisKeyPrefix.USER_TOKEN_PREFIX + hash, userId,
                Math.min(idleTtlMillis, absoluteTtlMillis));
        redisUtils.set(RedisKeyPrefix.TOKEN_ABSOLUTE_PREFIX + hash,
                Long.toString(issuedAt), absoluteTtlMillis);
        redisUtils.set(userKey, hash, absoluteTtlMillis);
        return token;
    }

    public AuthenticatedToken authenticate(String token) {
        if (!StringUtils.hasText(token)) {
            throw new CommonException(BusinessErrorCode.TOKEN_IS_EMPTY, "token 不能为空");
        }
        String hash = tokenHash(token.trim());
        String issuedAtValue = redisUtils.get(RedisKeyPrefix.TOKEN_ABSOLUTE_PREFIX + hash);
        String userId = redisUtils.get(RedisKeyPrefix.USER_TOKEN_PREFIX + hash);
        Long absoluteTtlSeconds = redisUtils.ttl(RedisKeyPrefix.TOKEN_ABSOLUTE_PREFIX + hash);
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(issuedAtValue)
                || absoluteTtlSeconds == null || absoluteTtlSeconds <= 0) {
            revokeHash(hash);
            throw new CommonException(BusinessErrorCode.TOKEN_IS_VALID, "token无效");
        }
        long refreshMillis = Math.min(idleTtlMillis, absoluteTtlSeconds * 1000L);
        redisUtils.set(RedisKeyPrefix.USER_TOKEN_PREFIX + hash, userId, refreshMillis);
        try {
            return new AuthenticatedToken(
                    userId,
                    "auth:" + hash.substring(0, 24),
                    Instant.ofEpochMilli(Long.parseLong(issuedAtValue)),
                    hash);
        } catch (NumberFormatException ex) {
            revokeHash(hash);
            throw new CommonException(BusinessErrorCode.TOKEN_IS_VALID, "token无效");
        }
    }

    public void setLegacyAppId(String token, String appId) {
        if (!StringUtils.hasText(token) || !StringUtils.hasText(appId)) {
            return;
        }
        setScope(RedisKeyPrefix.LEGACY_APP_TOKEN_PREFIX, token, appId);
    }

    public String getLegacyAppId(String token) {
        if (!StringUtils.hasText(token)) {
            return "";
        }
        String appId = redisUtils.get(RedisKeyPrefix.LEGACY_APP_TOKEN_PREFIX + tokenHash(token.trim()));
        return appId == null ? "" : appId;
    }

    public void setLegacyDbId(String token, String dbId) {
        if (!StringUtils.hasText(token) || !StringUtils.hasText(dbId)) {
            return;
        }
        setScope(RedisKeyPrefix.LEGACY_DB_TOKEN_PREFIX, token, dbId);
    }

    public String getLegacyDbId(String token) {
        if (!StringUtils.hasText(token)) {
            return "";
        }
        String dbId = redisUtils.get(RedisKeyPrefix.LEGACY_DB_TOKEN_PREFIX + tokenHash(token.trim()));
        return dbId == null ? "" : dbId;
    }

    public void logoutToken(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        String hash = tokenHash(token.trim());
        String userId = redisUtils.get(RedisKeyPrefix.USER_TOKEN_PREFIX + hash);
        revokeHash(hash);
        if (StringUtils.hasText(userId)) {
            String userKey = RedisKeyPrefix.USER_ID_TOKEN_PREFIX + userId;
            if (hash.equals(redisUtils.get(userKey))) {
                redisUtils.del(userKey);
            }
        }
    }

    public synchronized void revokeUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            return;
        }
        String userKey = RedisKeyPrefix.USER_ID_TOKEN_PREFIX + userId;
        String hash = redisUtils.get(userKey);
        if (StringUtils.hasText(hash)) {
            revokeHash(hash);
        }
        redisUtils.del(userKey);
    }

    public void recordStepUp(String sessionId) {
        if (!StringUtils.hasText(sessionId) || !sessionId.startsWith("auth:")) {
            throw new CommonException(BusinessErrorCode.TOKEN_IS_VALID, "token无效");
        }
        redisUtils.set(RedisKeyPrefix.STEP_UP_SESSION_PREFIX + sessionId,
                Long.toString(System.currentTimeMillis()), 5 * 60 * 1000L);
    }

    public Instant stepUpAt(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return null;
        }
        String value = redisUtils.get(RedisKeyPrefix.STEP_UP_SESSION_PREFIX + sessionId);
        try {
            return StringUtils.hasText(value) ? Instant.ofEpochMilli(Long.parseLong(value)) : null;
        } catch (NumberFormatException ex) {
            redisUtils.del(RedisKeyPrefix.STEP_UP_SESSION_PREFIX + sessionId);
            return null;
        }
    }

    private void setScope(String prefix, String token, String value) {
        String hash = tokenHash(token.trim());
        Long absoluteTtlSeconds = redisUtils.ttl(RedisKeyPrefix.TOKEN_ABSOLUTE_PREFIX + hash);
        if (absoluteTtlSeconds == null || absoluteTtlSeconds <= 0) {
            throw new CommonException(BusinessErrorCode.TOKEN_IS_VALID, "token无效");
        }
        redisUtils.set(prefix + hash, value, absoluteTtlSeconds * 1000L);
    }

    private void revokeHash(String hash) {
        redisUtils.del(RedisKeyPrefix.USER_TOKEN_PREFIX + hash);
        redisUtils.del(RedisKeyPrefix.TOKEN_ABSOLUTE_PREFIX + hash);
        redisUtils.del(RedisKeyPrefix.LEGACY_APP_TOKEN_PREFIX + hash);
        redisUtils.del(RedisKeyPrefix.LEGACY_DB_TOKEN_PREFIX + hash);
        redisUtils.del(RedisKeyPrefix.STEP_UP_SESSION_PREFIX + "auth:" + hash.substring(0, 24));
    }

    static String tokenHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }

    public record AuthenticatedToken(String userId,
                                     String sessionId,
                                     Instant authenticatedAt,
                                     String tokenHash) {
    }
}
