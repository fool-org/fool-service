package org.fool.framework.model.service;


import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.*;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.sqlscript.SqlGenerator;
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
        return null;
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
                QueryAndArgs propertyArgs = sqlGenerator.generateItems(property, ids);
                String propertyColumnt = property.getColumn();
                var itemsMapper = getMapper(property.getPropertyModel().getId().toString());
                this.jdbcTemplate.query(propertyArgs.getSql(), propertyArgs.getArgs(), new RowMapper<IDynamicData>() {
                    @Override
                    public IDynamicData mapRow(ResultSet resultSet, int i) throws SQLException {
                        var item = itemsMapper.mapRow(resultSet, i);
                        var id = resultSet.getString(propertyColumnt);
                        infos.get(id).add(item);
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
        var mapper = getMapper(modelId);
        QueryAndArgs queryAndArgs = sqlGenerator.generateSelect(mapper.getModel(), properties, filter, pageNavigator);
        PageResult<IDynamicData> result = new PageResult<>();
        var items = this.jdbcTemplate.query(queryAndArgs.getSql(), queryAndArgs.getArgs(), mapper);
        if (items.size() > 0) {
            var ids = items.stream().map(IDynamicData::getId).collect(Collectors.toList());
            for (var property : properties.stream().filter(p -> p.getIsCollection()).collect(Collectors.toList())) {
                Map<String, List> infos = new LinkedHashMap<>();
                for (var id : ids) {
                    infos.put(id, new LinkedList());
                }
                QueryAndArgs propertyArgs = sqlGenerator.generateItems(property, ids);
                String propertyColumnt = property.getColumn();
                var itemsMapper = getMapper(property.getPropertyModel().getId().toString());
                this.jdbcTemplate.query(propertyArgs.getSql(), propertyArgs.getArgs(), new RowMapper<IDynamicData>() {
                    @Override
                    public IDynamicData mapRow(ResultSet resultSet, int i) throws SQLException {
                        var item = itemsMapper.mapRow(resultSet, i);
                        var id = resultSet.getString(propertyColumnt);
                        infos.get(id).add(item);
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
        var countArgs = sqlGenerator.generateSelectCount(mapper.getModel(), filter);
        var rowset =
                this.jdbcTemplate.queryForRowSet(countArgs.getSql(), countArgs.getArgs());
        if (rowset.next()) {
            result.getPageInfo().setTotal(rowset.getInt(1));
            result.getPageInfo().setPageCount(result.getPageInfo().getTotal() / result.getPageInfo().getPageSize() + (result.getPageInfo().getTotal() % result.getPageInfo().getPageSize() > 0 ? 1 : 0));
        }
        return result;
    }

    public Boolean saveData(IDynamicData data) {
        return true;
    }

    public Boolean saveDataList(List<IDynamicData> dataList) {
        return true;
    }

    public Boolean deleteData(IDynamicData data) {
        return true;
    }

    public Boolean createData(IDynamicData data) {
        return true;
    }

    public IDynamicData initData(Model model) {
        return null;
    }

}
