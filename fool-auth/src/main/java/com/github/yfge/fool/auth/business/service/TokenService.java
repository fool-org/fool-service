package com.github.yfge.fool.auth.business.service;


import com.github.yfge.fool.auth.business.common.BusinessErrorCode;
import com.github.yfge.fool.auth.business.common.RedisKeyPrefix;
import com.github.yfge.fool.auth.utils.RedisUtils;
import com.github.yfge.fool.dto.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@Slf4j
public class TokenService {


    @Autowired
    private RedisUtils redisUtils;

    /***
     * 根据token得到 userId
     * 如果为空表示未登录
     * @param token
     * @return
     */
    public String getUidByToken(String token) {
        if (org.springframework.util.StringUtils.isEmpty(token)) {
            throw new CommonException(BusinessErrorCode.TOKEN_IS_EMPTY, "token 不能为空");
        }
        String tokenKey = RedisKeyPrefix.USER_TOKEN_PREFIX + token;
        return redisUtils.get(tokenKey);
    }


    /**
     * 根据userId生成 token
     *
     * @param userId
     * @return
     */
    public String getTokenByUid(String userId) {
        String tokenKey = RedisKeyPrefix.USER_ID_TOKEN_PREFIX + userId;
        String token = redisUtils.get(tokenKey);

        log.info("the token is :{}", token);
        if (StringUtils.isEmpty(token)) {
            token = UUID.randomUUID().toString();
            redisUtils.set(tokenKey, token);
            String userTokeKey = RedisKeyPrefix.USER_TOKEN_PREFIX + token;
            redisUtils.set(userTokeKey, userId);
        } else {
            String userTokeKey = RedisKeyPrefix.USER_TOKEN_PREFIX + token;
            String uid = redisUtils.get(userTokeKey);
            log.info("the uid is :{}", uid);
            if (StringUtils.isEmpty(uid)) {
                redisUtils.del(tokenKey);
                return getTokenByUid(userId);
            }
        }
        return token;
    }

    public void updateToken(String token, String userId) {
        String tokenKey = RedisKeyPrefix.USER_ID_TOKEN_PREFIX + token;
        if (StringUtils.isEmpty(token)) {
            token = UUID.randomUUID().toString();
        }
        redisUtils.set(tokenKey, token);
        String userTokeKey = RedisKeyPrefix.USER_TOKEN_PREFIX + token;
        redisUtils.set(userTokeKey, userId);

    }
}
