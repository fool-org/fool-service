package com.github.yfge.fool.dao;


import com.github.yfge.fool.common.annotation.SqlGenerateConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
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


    @Transactional
    public <T> void create(T object) {
        var mapper = getMapper(object.getClass());
        var queryAndArgs = this.sqlScriptGenerator.generateOnInsert(mapper, object);
        int result = this.jdbcTemplate.update(queryAndArgs.getSql(), new ArgumentPreparedStatementSetter(queryAndArgs.getArgs()));
        if (result > 0) {
            var updateArgs = this.sqlScriptGenerator.generateAfterInsert(mapper, object);
            if (updateArgs != null) {
                this.jdbcTemplate.query(updateArgs.getSql(), updateArgs.getArgs(), rch -> {
                    var fields = mapper.getMapFields().stream().filter(
                            p -> p.getSqlGenerateConfig() == SqlGenerateConfig.INSERT_AND_UPDATE
                                    || p.getSqlGenerateConfig() == SqlGenerateConfig.INSERT
                                    || p.getSqlGenerateConfig() == SqlGenerateConfig.AUTO_INCREMENT
                    );
                    fields.forEach(p -> {
                        try {
                            p.getField().set(object,
                                    p.getGetFieldFunction().get(rch, p.getColumnName()));
                        } catch (IllegalAccessException | SQLException e) {
                            log.error("create failed:", e);
                        }
                    });
                });
            }
        }
    }

}
