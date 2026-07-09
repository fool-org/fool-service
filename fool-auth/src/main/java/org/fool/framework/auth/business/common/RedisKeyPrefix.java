package org.fool.framework.auth.business.common;

public class RedisKeyPrefix {
    public static final String USER_TOKEN_PREFIX = "fool:auth:token";
    public static final String USER_ID_TOKEN_PREFIX = "fool:auth:user:token";
    public static final String LEGACY_APP_TOKEN_PREFIX = "fool:auth:legacy:app:";
    public static final String LEGACY_DB_TOKEN_PREFIX = "fool:auth:legacy:db:";
}
