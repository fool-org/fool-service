package org.fool.framework.model.service;


import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.*;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.sqlscript.SqlGenerator;
import org.fool.framework.query.CompareFilter;
import org.fool.framework.query.CompareOp;
import org.fool.framework.query.IQueryFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Component
@Slf4j
public class ModelDataService {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    private ConcurrentHashMap<String, Mapper> concurrentHashMap = new ConcurrentHashMap<>();
    @Autowired
    private DaoService daoService;
    @Autowired
    private SqlGenerator sqlGenerator;

    private Mapper getMapper(String modelId) {
        if (!concurrentHashMap.containsKey(modelId)) {
            Model model = daoService.getOneDetailByKey(Model.class, modelId);
            concurrentHashMap.put(modelId, new Mapper(model));
        }
        return concurrentHashMap.get(modelId);
    }

    /**
     * 得到模型
     *
     * @param modelId
     * @return
     */
    public Model getModel(String modelId) {
        return daoService.getOneDetailByKey(Model.class, modelId);
    }

    public IDynamicData getOneData(String modelId, String dataId) {
        if (dataId == null || dataId.isBlank()) {
            return null;
        }
        var mapper = getMapper(modelId);
        Model model = mapper.getModel();
        String idColumn = model.getIdProperty() != null
                && model.getIdProperty().getColumn() != null
                && !model.getIdProperty().getColumn().isBlank()
                ? model.getIdProperty().getColumn()
                : "SYSID";
        QueryAndArgs queryAndArgs = sqlGenerator.generateSelect(
                model,
                model.getProperties(),
                new CompareFilter(idColumn, CompareOp.EQUAL, dataId));
        var items = this.jdbcTemplate.query(queryAndArgs.getSql(), queryAndArgs.getArgs(), mapper);
        return items.isEmpty() ? null : items.get(0);
    }

    /**
     * 返回结果数据
     *
     * @param modelId
     * @param filter
     * @param properties
     * @return
     */
    public List<IDynamicData> getDataList(String modelId, IQueryFilter filter, List<Property> properties) {
        var mapper = getMapper(modelId);
        QueryAndArgs queryAndArgs = sqlGenerator.generateSelect(mapper.getModel(), properties, filter);
        var items = this.jdbcTemplate.query(queryAndArgs.getSql(), queryAndArgs.getArgs(), mapper);
        if (items.size() > 0) {
            var ids = items.stream().map(IDynamicData::getId).collect(Collectors.toList());
            for (var property : properties.stream().filter(p -> p.getIsCollection()).collect(Collectors.toList())) {
                Map<String, List> infos = new LinkedHashMap<>();
                for (var id : ids) {
                    infos.put(id, new LinkedList());
                }
                QueryAndArgs propertyArgs = sqlGenerator.generateItems(mapper.getModel(), property, ids);
                var itemsMapper = getMapper(property.getPropertyModel().getId().toString());
                this.jdbcTemplate.query(propertyArgs.getSql(), propertyArgs.getArgs(), new RowMapper<IDynamicData>() {
                    @Override
                    public IDynamicData mapRow(ResultSet resultSet, int i) throws SQLException {
                        var item = itemsMapper.mapRow(resultSet, i);
                        var id = resultSet.getString(SqlGenerator.ITEM_PARENT_ID_COLUMN);
                        if (infos.containsKey(id)) {
                            infos.get(id).add(item);
                        }
                        return item;
                    }
                });
                for (var item : items) {
                    item.set(property.getName(), infos.get(item.getId()));
                }
            }
        }

        return items;
    }


    /**
     * 得到翻页结果
     *
     * @param modelId
     * @param filter
     * @param properties
     * @param pageNavigator
     * @return
     */
    public PageResult<IDynamicData> getDataListWithPageInfo(String modelId, IQueryFilter filter, List<Property> properties, PageNavigator pageNavigator) {
        return getDataListWithPageInfo(modelId, filter, properties, pageNavigator, null, false);
    }

    public PageResult<IDynamicData> getDataListWithPageInfo(
            String modelId,
            IQueryFilter filter,
            List<Property> properties,
            PageNavigator pageNavigator,
            String orderColumn,
            boolean orderDescending) {
        var mapper = getMapper(modelId);
        QueryAndArgs queryAndArgs = sqlGenerator.generateSelect(
                mapper.getModel(), properties, filter, pageNavigator, orderColumn, orderDescending);
        PageResult<IDynamicData> result = new PageResult<>();
        var items = this.jdbcTemplate.query(queryAndArgs.getSql(), queryAndArgs.getArgs(), mapper);
        if (items.size() > 0) {
            var ids = items.stream().map(IDynamicData::getId).collect(Collectors.toList());
            for (var property : properties.stream().filter(p -> p.getIsCollection()).collect(Collectors.toList())) {
                Map<String, List> infos = new LinkedHashMap<>();
                for (var id : ids) {
                    infos.put(id, new LinkedList());
                }
                QueryAndArgs propertyArgs = sqlGenerator.generateItems(mapper.getModel(), property, ids);
                var itemsMapper = getMapper(property.getPropertyModel().getId().toString());
                this.jdbcTemplate.query(propertyArgs.getSql(), propertyArgs.getArgs(), new RowMapper<IDynamicData>() {
                    @Override
                    public IDynamicData mapRow(ResultSet resultSet, int i) throws SQLException {
                        var item = itemsMapper.mapRow(resultSet, i);
                        var id = resultSet.getString(SqlGenerator.ITEM_PARENT_ID_COLUMN);
                        if (infos.containsKey(id)) {
                            infos.get(id).add(item);
                        }
                        return item;
                    }
                });
                for (var item : items) {
                    item.set(property.getName(), infos.get(item.getId()));
                }
            }
        }
        result.setItems(items);
        result.setPageInfo(new PageNavigatorResult());
        result.getPageInfo().setPageSize(pageNavigator.getPageSize());
        result.getPageInfo().setPageIndex(pageNavigator.getPageIndex());
        var countArgs = sqlGenerator.generateSelectCount(mapper.getModel(), filter, properties);
        var rowset =
                this.jdbcTemplate.queryForRowSet(countArgs.getSql(), countArgs.getArgs());
        if (rowset.next()) {
            result.getPageInfo().setTotal(rowset.getInt(1));
            result.getPageInfo().setPageCount(result.getPageInfo().getTotal() / result.getPageInfo().getPageSize() + (result.getPageInfo().getTotal() % result.getPageInfo().getPageSize() > 0 ? 1 : 0));
        }
        return result;
    }

    public Boolean saveData(IDynamicData data) {
        if (!(data instanceof DbMysqlDynamic dynamicData)) {
            return false;
        }
        Model model = dynamicData.getModel();
        if (model == null || model.getTableName() == null || model.getTableName().isBlank()
                || model.getProperties() == null) {
            return false;
        }
        Property idProperty = model.getIdProperty();
        String idColumn = idProperty != null && idProperty.getColumn() != null && !idProperty.getColumn().isBlank()
                ? idProperty.getColumn()
                : "SYSID";
        Object idValue = idProperty != null ? data.get(idProperty.getName()) : data.getId();
        if (idValue == null) {
            return false;
        }
        List<Property> properties = model.getProperties().stream()
                .filter(property -> !Boolean.TRUE.equals(property.getIsCollection()))
                .filter(property -> !Boolean.TRUE.equals(property.getMultiMap()))
                .filter(property -> property.getColumn() != null && !property.getColumn().isBlank())
                .filter(property -> !property.getColumn().equals(idColumn))
                .toList();
        if (properties.isEmpty()) {
            return false;
        }
        String assignments = properties.stream()
                .map(property -> "`" + property.getColumn() + "` = ?")
                .collect(Collectors.joining(","));
        List<Object> args = new LinkedList<>();
        properties.forEach(property -> args.add(data.get(property.getName())));
        args.add(idValue);
        String sql = "UPDATE `" + model.getTableName() + "` SET " + assignments + " WHERE `" + idColumn + "` = ?";
        return jdbcTemplate.update(sql, args.toArray()) > 0;
    }

    public Boolean saveDataList(List<IDynamicData> dataList) {
        return true;
    }

    public Boolean deleteData(IDynamicData data) {
        if (!(data instanceof DbMysqlDynamic dynamicData)) {
            return false;
        }
        Model model = dynamicData.getModel();
        if (model == null || model.getTableName() == null || model.getTableName().isBlank()) {
            return false;
        }
        Property idProperty = model.getIdProperty();
        String idColumn = idProperty != null && idProperty.getColumn() != null && !idProperty.getColumn().isBlank()
                ? idProperty.getColumn()
                : "SYSID";
        Object idValue = idProperty != null ? data.get(idProperty.getName()) : data.getId();
        if (idValue == null) {
            return false;
        }
        String sql = "DELETE FROM `" + model.getTableName() + "` WHERE `" + idColumn + "` = ?";
        return jdbcTemplate.update(sql, idValue) > 0;
    }

    public Boolean createData(IDynamicData data) {
        if (!(data instanceof DbMysqlDynamic dynamicData)) {
            return false;
        }
        Model model = dynamicData.getModel();
        if (model == null || model.getTableName() == null || model.getTableName().isBlank()
                || model.getProperties() == null) {
            return false;
        }
        List<Property> properties = model.getProperties().stream()
                .filter(property -> !Boolean.TRUE.equals(property.getIsCollection()))
                .filter(property -> !Boolean.TRUE.equals(property.getMultiMap()))
                .filter(property -> property.getColumn() != null && !property.getColumn().isBlank())
                .toList();
        if (properties.isEmpty()) {
            return false;
        }
        String columns = properties.stream()
                .map(property -> "`" + property.getColumn() + "`")
                .collect(Collectors.joining(","));
        String values = properties.stream()
                .map(property -> "?")
                .collect(Collectors.joining(","));
        Object[] args = properties.stream()
                .map(property -> data.get(property.getName()))
                .toArray();
        String sql = "INSERT INTO `" + model.getTableName() + "` (" + columns + ") VALUES (" + values + ")";
        return jdbcTemplate.update(sql, args) > 0;
    }

    public IDynamicData initData(Model model) {
        return null;
    }

}
