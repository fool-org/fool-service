package org.fool.framework.dao;


import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DaoService {


    private static ConcurrentHashMap<String, Mapper> concurrentHashMap = new ConcurrentHashMap<>();
    @Autowired
    private SqlScriptGenerator sqlScriptGenerator;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    static <T> Mapper<T> getMapper(Class<T> clazz) {
        String name = clazz.getName();
        if (!concurrentHashMap.containsKey(name)) {
            concurrentHashMap.put(name, new Mapper<T>(clazz));
            return concurrentHashMap.get(name);
        }
        return concurrentHashMap.get(name);
    }


    /**
     * 得到列表
     *
     * @param clazz
     * @param sql
     * @param <T>
     * @return
     */
    public <T> List<T> selectList(Class<T> clazz, String sql) {
        var mapper = getMapper(clazz);
        return this.jdbcTemplate.query(sql, mapper);
    }


    /**
     * 得到列表
     *
     * @param clazz
     * @param sql
     * @param args
     * @param <T>
     * @return
     */
    public <T> List<T> selectList(Class<T> clazz, String sql, Object... args) {
        var mapper = this.getMapper(clazz);
        return this.jdbcTemplate.query(sql, mapper, args);

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
    public <T> T getOneByKey(Class<T> clazz, Object key) {
        var mapper = getMapper(clazz);
        var queryAndArgs = this.sqlScriptGenerator.generateSelectOne(mapper, key);
        var items = this.jdbcTemplate.query(queryAndArgs.getSql(), queryAndArgs.getArgs(), mapper);
        if (items != null && items.size() > 0) {
            return items.get(0);
        }
        return null;
    }


    /**
     * 查询数据,同时得到子项数据
     *
     * @param clazz
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getOneDetailByKey(Class<T> clazz, Object key) {
        var mapper = getMapper(clazz);
        var queryAndArgs = this.sqlScriptGenerator.generateSelectOne(mapper, key);
        var items = this.jdbcTemplate.query(queryAndArgs.getSql(), queryAndArgs.getArgs(), mapper);
        if (items != null && items.size() > 0) {
            T result = items.get(0);
            fillItems(clazz, result, mapper, key);
            return result;
        }
        return null;
    }

    /**
     * 填充列表项
     *
     * @param clazz
     * @param result
     * @param mapper
     * @param key
     * @param <T>
     */
    private <T> void fillItems(Class<T> clazz, T result, Mapper<?> mapper, Object key) {
        var fieldList = mapper.getMapFields();
        var collectionFields = fieldList.stream().filter(p -> p.isCollection()).collect(Collectors.toList());
        for (var field : collectionFields) {
            /**
             * 得到集合类型
             */
            ParameterizedType t = (ParameterizedType) field.getField().getGenericType();
            Class<?> itemClazz = (Class<?>) t.getActualTypeArguments()[0];
            log.info("result:{}", result);
            Object foreignKey = getKey(result, mapper);
            var itemMapper = getMapper(itemClazz);
            var varQueryAndArgs = this.sqlScriptGenerator.generateSelectItems(itemMapper, field.getColumnName(), foreignKey);
            var items = this.jdbcTemplate.query(varQueryAndArgs.getSql(), itemMapper, foreignKey);
            try {
                field.getField().set(result, items);
            } catch (IllegalAccessException e) {
                log.info("set {} of {} error", field.getField().getName(), clazz.getName(), e);
            }
        }

    }

    private <T> Object getKey(T result, Mapper<?> mapper) {
        try {
            return mapper.getPrimaryField().getField().get(result);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private String getForeignColumn(MapField field) {
        return null;
    }


    public <T> PageResult<T> getWithPageBySimpleFilter(Class<T> clazz, PageNavigator pageNavigator, Map<String, Object[]> filter) {
        var mapper = getMapper(clazz);
        var queryAndArgs = this.sqlScriptGenerator.generateSelectithPageBySimpleFilter(mapper, pageNavigator, filter);
        PageResult<T> result = new PageResult<>();
        result.setItems(
                this.jdbcTemplate.query(queryAndArgs.getSql(), queryAndArgs.getArgs(), mapper));
        return result;
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
