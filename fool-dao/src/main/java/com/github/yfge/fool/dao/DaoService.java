package com.github.yfge.fool.dao;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DaoService {


    @Autowired
    private SqlScriptGenerator sqlScriptGenerator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ConcurrentHashMap<String, Mapper> concurrentHashMap = new ConcurrentHashMap<>();

    private <T> Mapper<T> getMapper(Class<T> clazz) {
        String name = clazz.getName();
        if (!concurrentHashMap.containsKey(name)) {
            concurrentHashMap.put(name, new Mapper<T>(clazz));
        }
        return concurrentHashMap.get(name);
    }

    /**
     * 所有列表
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> getAllList(Class<T> clazz) {
        var maper = getMapper(clazz);
        String sql = this.sqlScriptGenerator.generateSelect(maper);
        return this.jdbcTemplate.query(sql, maper);
    }

    /**
     * 查到一个
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getOneByKey(Object key, Class<T> clazz) {
        var mapper = getMapper(clazz);
        var queryAndArgs = this.sqlScriptGenerator.generateSelectOne(mapper, key);
        var items = this.jdbcTemplate.query(queryAndArgs.getSql(), mapper, key);
        if (items != null && items.size() > 0) {
            return items.get(0);
        }
        return null;
    }

    public <T> PageResult<T> getByPage(PageNavigator pageNavigator) {
        return null;
    }


    /**
     * 保存
     *
     * @param object
     * @param <T>
     */
    public <T> boolean save(T object) {
        var mapper = getMapper(object.getClass());
        var queryAndArgs = this.sqlScriptGenerator.generateUpdate(mapper, object);
        if (queryAndArgs == null) {
            return false;
        }
        int result = this.jdbcTemplate.update(queryAndArgs.getSql(), new ArgumentPreparedStatementSetter(queryAndArgs.getArgs()));
        if (result > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除
     *
     * @param object
     * @param <T>
     */

    public <T> void delete(T object) {

    }


}
