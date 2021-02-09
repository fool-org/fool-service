package org.fool.framework.dao;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class CacheService {


    private static ConcurrentHashMap<String, Object> cacheMapper = new ConcurrentHashMap<>();
    private static CacheService ins;
    @Autowired
    private DaoService daoService;

    public static CacheService getIns() {
        return ins;
    }

    @PostConstruct
    public void init() {
        ins = this;
    }

    public <T> T get(Class<T> clazz, Object key) {
        if (ObjectUtils.isEmpty(key)) {
            return null;
        }
        String cacheKey = clazz.getName().concat(key.toString());
        if (cacheMapper.containsKey(cacheKey)) {
            return (T) cacheMapper.get(cacheKey);
        } else {

            var item = daoService.getOneDetailByKey(clazz, key);
            cacheMapper.put(cacheKey, item);
            return (T) cacheMapper.get(cacheKey);
        }
    }

    /**
     * @param clazz
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getOrInit(Class<T> clazz, Object key) {
        if (ObjectUtils.isEmpty(key)) {
            return null;
        }
        String cacheKey = clazz.getName().concat(key.toString());
        if (!cacheMapper.containsKey(cacheKey)) {
            T object = null;
            try {
                object = clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.error("create error:");
                return null;
            }
            T last = (T) cacheMapper.putIfAbsent(cacheKey, object);
            if (last != null) {
                cacheMapper.put(cacheKey, last);
                return last;
            } else {
                return object;
            }
        } else {
            return (T) cacheMapper.get(cacheKey);
        }
    }
}
