package org.fool.framework.log;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * @author geyunfei
 */
public class GuaziThreadContextHelper {

    private static ThreadLocal<String> uuidThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<String> businessCodeThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Long> startTimeLocal = new ThreadLocal<>();

    private static final String UUID_STRING = "uuid";
    private static final String TID_STRING = "tid";
    private static final String FILTER_STRING = "filter";

    /****
     * 如果要日志中打印出uuid和tid，需要调用此方法。
     * 因为这个是线程相关的，每一个线程都要调用。
     * 返回随机生成的uuid。
     * **/
    public static synchronized String setThreadContext() {
        String uuid = getUUID();
        if (!StringUtils.isEmpty(uuid)) {
            return uuid;
        }
        uuid = UUID.randomUUID().toString().replace("-", "");
        return setThreadContext(uuid);
    }

    public static synchronized String setThreadContext(String uuid) {
        uuidThreadLocal.set(uuid);
        MDC.put(UUID_STRING, uuid);
        MDC.put(TID_STRING, String.valueOf(Thread.currentThread().getId()));
        return uuid;
    }

    public static synchronized void setFilter(String filter) {
        MDC.put(FILTER_STRING, filter);
    }

    /****
     * 要手动清除。
     * **/

    public static synchronized void clearThreadContext() {
        MDC.remove(UUID_STRING);
        MDC.remove(TID_STRING);
        MDC.remove(FILTER_STRING);
        uuidThreadLocal.remove();
        businessCodeThreadLocal.remove();
        startTimeLocal.remove();
    }

    /**
     * 如果没有set，会返回""，而不是null
     ***/
    public static String getUUID() {
        String ret = uuidThreadLocal.get();
        if (StringUtils.isEmpty(ret)) {
            return "";
        }
        return ret;
    }

    /**
     * 如果没有set，会返回""，而不是null
     ***/
    public static String getBusinessCode() {
        String ret = businessCodeThreadLocal.get();
        if (StringUtils.isEmpty(ret)) {
            return "";
        }
        return ret;
    }

    public static void setBusinessCode(String value) {
        businessCodeThreadLocal.set(value);
    }

    public static Long getStartTime() {
        return startTimeLocal.get();
    }

    public static void setStartTime(Long value) {
        startTimeLocal.set(value);
    }
}
