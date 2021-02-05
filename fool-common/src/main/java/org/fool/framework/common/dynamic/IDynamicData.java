package org.fool.framework.common.dynamic;


import java.util.Map;

/**
 * 表示一个动态的数据
 * 可能是硬编码的反射
 * 也可能是数据库中查询得到的数据
 */
public interface IDynamicData {


    /**
     * 得到特定值
     *
     * @param field 属性名称
     * @return
     */
    Object get(String field);

    /**
     * 设置属性
     *
     * @param field 属性
     * @param value 值
     */
    void set(String field, Object value);


    /**
     * 调用方法
     *
     * @param methodName 名称
     * @param args       参数
     * @return
     */
    Object invokeWithReturn(String methodName, Object... args);


    /**
     * 调用方法（无返回）
     *
     * @param methodName 名称
     * @param args       参数
     */
    void invoke(String methodName, Object... args);


    Map<String, Object> toMap();

    /**
     * ID  暂时固定为String
     *
     * @return
     */
    String getId();
}
