package org.fool.framework.auth.business.common;

public class RedisKeyPrefix {
    public static final String USER_TOKEN_PREFIX = "fool:auth:token:sha256:";
    public static final String TOKEN_ABSOLUTE_PREFIX = "fool:auth:absolute:sha256:";
    public static final String USER_ID_TOKEN_PREFIX = "fool:auth:user:token-hash:";
    public static final String LEGACY_APP_TOKEN_PREFIX = "fool:auth:legacy:app:sha256:";
    public static final String LEGACY_DB_TOKEN_PREFIX = "fool:auth:legacy:db:sha256:";
    public static final String STEP_UP_SESSION_PREFIX = "fool:auth:step-up:session:";
}
