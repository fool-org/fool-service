package com.github.yfge.fool.dao;


import org.springframework.beans.factory.annotation.Autowired;
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

    public <T> List<T> getAllList(Class<T> clazz) {
        var maper = getMapper(clazz);
        String sql = this.sqlScriptGenerator.generateSelect(maper);
        return this.jdbcTemplate.query(sql, maper);
    }

    public <T> PageResult<T> getByPage(PageNavigator pageNavigator) {
        return null;
    }

    public <T> T get(Object id) {
        return null;
    }


}
