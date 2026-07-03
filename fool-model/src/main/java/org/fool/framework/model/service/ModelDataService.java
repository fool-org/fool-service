package org.fool.framework.model.service;


import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.data.SubItemList;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.*;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.MultiDbMap;
import org.fool.framework.model.model.OperationCommand;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.model.RelationType;
import org.fool.framework.model.model.Trigger;
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
        Model model = daoService.getOneDetailByKey(Model.class, modelId);
        hydrateRelations(model);
        hydrateModelTriggers(model);
        return model;
    }

    private void hydrateRelations(Model model) {
        if (model == null || model.getProperties() == null || model.getProperties().isEmpty()) {
            return;
        }
        List<Long> propertyIds = model.getProperties().stream()
                .map(Property::getId)
                .filter(id -> id != null)
                .toList();
        if (propertyIds.isEmpty()) {
            return;
        }
        String placeholders = propertyIds.stream().map(id -> "?").collect(Collectors.joining(","));
        model.setRelations(daoService.selectList(
                Relation.class,
                "SELECT `SW_SYS_RELATION_TYPE`,`SW_SYS_RELATION_SOURCEPROPERTY`,`SW_SYS_RELATION_TARGETPROPERTY`,"
                        + "`SW_SYS_RELATION_TABLE`,`SW_SYS_RELATION_SOURCECOL`,`SW_SYS_RELATION_TARGETCOL`,"
                        + "`SW_SYS_RELATION_CANBENULL` FROM `SW_SYS_RELATION` "
                        + "WHERE `SW_SYS_RELATION_SOURCEPROPERTY` IN (" + placeholders + ")",
                propertyIds.toArray()));
    }

    private void hydrateModelTriggers(Model model) {
        if (model == null || model.getId() == null) {
            return;
        }
        List<Trigger> triggers = daoService.selectList(
                Trigger.class,
                "SELECT `SysId`,`SW_MODEL_TRIGGER_ARGMODEL`,`SW_MODEL_TRIGGER_TYPE`,"
                        + "`SW_MODEL_TRIGGER_FILTER`,`SW_MODEL_TRIGGER_ARGFILTER`,"
                        + "`SW_MODEL_TRIGGER_OPERATIONTYPE`,`SW_MODEL_TRIGGER_INVOKEDLL`,"
                        + "`SW_MODEL_TRIGGER_INVOKECLASS`,`SW_MODEL_TRIGGER_INVOKEMETHOD` "
                        + "FROM `SW_SYS_MODEL_TRIGGER` "
                        + "WHERE `SW_SYS_MODEL_TriggersMODEL_ID` = ? ORDER BY `SysId`",
                model.getId());
        for (Trigger trigger : triggers) {
            trigger.setCommands(daoService.selectList(
                    OperationCommand.class,
                    "SELECT `SysId`,`SW_SYS_MODEL_TRIGGER_CommandsSysId` AS `SW_SYS_OPERATION_CommandsSysId`,"
                            + "`SW_SYS_COMMAND_TYPE`,`SW_SYS_COMMAND_PROPERTY`,`SW_SYS_COMMAND_EXP`,"
                            + "`SW_SYS_COMMAND_ARGMODEL`,`SW_SYS_COMMAND_ARGEXP`,`SW_SYS_COMMAND_ARGID`,"
                            + "`SW_SYS_COMMAND_Index` AS `SW_SYS_COMMAND_INDEX`,"
                            + "`SW_SYS_COMMAND_PROPERTY_EXP`,`SW_SYS_COMMAND_TEMPVALUE` "
                            + "FROM `SW_SYS_MODEL_TRIGGER_COMMANDS` "
                            + "WHERE `SW_SYS_MODEL_TRIGGER_CommandsSysId` = ? "
                            + "ORDER BY `SW_SYS_COMMAND_Index`, `SysId`",
                    trigger.getId()));
        }
        model.setTriggers(triggers);
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
        loadCollectionProperties(model, model.getProperties(), items);
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
        loadCollectionProperties(mapper.getModel(), properties, items);

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
        loadCollectionProperties(mapper.getModel(), properties, items);
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

    private void loadCollectionProperties(Model model, List<Property> properties, List<IDynamicData> items) {
        if (model == null || properties == null || items == null || items.isEmpty()) {
            return;
        }
        hydrateRelations(model);
        var ids = items.stream()
                .map(IDynamicData::getId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        for (var property : properties.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCollection()))
                .collect(Collectors.toList())) {
            if (property.getPropertyModel() == null || property.getPropertyModel().getId() == null) {
                continue;
            }
            Map<String, List<IDynamicData>> infos = new LinkedHashMap<>();
            for (var id : ids) {
                infos.put(id, new LinkedList<>());
            }
            QueryAndArgs propertyArgs = sqlGenerator.generateItems(model, property, ids);
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
                item.set(property.getName(), infos.getOrDefault(item.getId(), new LinkedList<>()));
            }
        }
    }

    public Boolean saveData(IDynamicData data) {
        return saveData(data, null, null, true);
    }

    private Boolean saveData(IDynamicData data, String extraColumn, Object extraValue, boolean writeCollections) {
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
        Object idValue = lookupIdValue(dynamicData, idProperty);
        if (idValue == null) {
            return false;
        }
        Map<String, Object> columnValues = writableColumnValues(model, data, idColumn);
        addColumnValue(columnValues, extraColumn, extraValue, idColumn);
        if (columnValues.isEmpty()) {
            return false;
        }
        String assignments = columnValues.keySet().stream()
                .map(column -> "`" + column + "` = ?")
                .collect(Collectors.joining(","));
        List<Object> args = new LinkedList<>();
        args.addAll(columnValues.values());
        args.add(idValue);
        String sql = "UPDATE `" + model.getTableName() + "` SET " + assignments + " WHERE `" + idColumn + "` = ?";
        boolean saved = jdbcTemplate.update(sql, args.toArray()) > 0;
        if (saved && writeCollections) {
            writeCollectionRelations(model, data);
        }
        return saved;
    }

    public Boolean saveDataList(List<IDynamicData> dataList) {
        if (dataList == null) {
            return false;
        }
        for (IDynamicData data : dataList) {
            if (!Boolean.TRUE.equals(saveData(data))) {
                return false;
            }
        }
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
        Object idValue = lookupIdValue(dynamicData, idProperty);
        if (idValue == null) {
            return false;
        }
        String sql = "DELETE FROM `" + model.getTableName() + "` WHERE `" + idColumn + "` = ?";
        return jdbcTemplate.update(sql, idValue) > 0;
    }

    public Boolean createData(IDynamicData data) {
        return createData(data, null, null, true);
    }

    public Boolean createData(IDynamicData data, String extraColumn, Object extraValue) {
        return createData(data, extraColumn, extraValue, false);
    }

    private Boolean createData(IDynamicData data, String extraColumn, Object extraValue, boolean writeCollections) {
        if (!(data instanceof DbMysqlDynamic dynamicData)) {
            return false;
        }
        Model model = dynamicData.getModel();
        if (model == null || model.getTableName() == null || model.getTableName().isBlank()
                || model.getProperties() == null) {
            return false;
        }
        Map<String, Object> columnValues = writableColumnValues(model, data, null);
        addColumnValue(columnValues, extraColumn, extraValue, null);
        if (columnValues.isEmpty()) {
            return false;
        }
        String columns = columnValues.keySet().stream()
                .map(column -> "`" + column + "`")
                .collect(Collectors.joining(","));
        String values = columnValues.keySet().stream()
                .map(property -> "?")
                .collect(Collectors.joining(","));
        String sql = "INSERT INTO `" + model.getTableName() + "` (" + columns + ") VALUES (" + values + ")";
        boolean created = jdbcTemplate.update(sql, columnValues.values().toArray()) > 0;
        if (created && writeCollections) {
            writeCollectionRelations(model, data);
        }
        return created;
    }

    private void writeCollectionRelations(Model model, IDynamicData data) {
        if (model.getRelations() == null) {
            return;
        }
        Object parentId = dynamicId(data, model);
        if (parentId == null) {
            return;
        }
        for (Relation relation : model.getRelations()) {
            if (isWritableOwnedRelation(relation)) {
                writeOwnedCollection(relation, data.get(relation.getProperty().getName()), parentId);
                continue;
            }
            if (!isWritableComplexRelation(relation)) {
                continue;
            }
            Object value = data.get(relation.getProperty().getName());
            if (!(value instanceof Iterable<?> items)) {
                continue;
            }
            for (Object item : items) {
                if (item instanceof IDynamicData itemData) {
                    Object itemId = dynamicId(itemData, relation.getProperty().getPropertyModel());
                    if (itemId != null) {
                        insertRelationIfMissing(relation, parentId, itemId);
                    }
                }
            }
            if (value instanceof SubItemList<?> subItems) {
                for (Object item : subItems.getDeleteList()) {
                    if (item instanceof IDynamicData itemData) {
                        Object itemId = dynamicId(itemData, relation.getProperty().getPropertyModel());
                        if (itemId != null) {
                            deleteRelation(relation, parentId, itemId);
                        }
                    }
                }
            }
        }
    }

    private void writeOwnedCollection(Relation relation, Object value, Object parentId) {
        if (value instanceof Iterable<?> items) {
            for (Object item : items) {
                if (item instanceof IDynamicData itemData) {
                    Model itemModel = dynamicModel(itemData, relation.getProperty().getPropertyModel());
                    if (dataExists(itemData, itemModel)) {
                        saveData(itemData, relation.getTargetColumn(), parentId, false);
                    } else {
                        createData(itemData, relation.getTargetColumn(), parentId, false);
                    }
                }
            }
        }
        if (value instanceof SubItemList<?> subItems) {
            for (Object item : subItems.getDeleteList()) {
                if (item instanceof IDynamicData itemData) {
                    deleteData(itemData);
                }
            }
        }
    }

    private boolean isWritableOwnedRelation(Relation relation) {
        return relation != null
                && relation.getProperty() != null
                && relation.getProperty().getName() != null
                && !relation.getProperty().getName().isBlank()
                && (relation.getRelationType() == RelationType.One2Many
                || relation.getRelationType() == RelationType.Many2One)
                && relation.getTargetColumn() != null
                && !relation.getTargetColumn().isBlank();
    }

    private boolean isWritableComplexRelation(Relation relation) {
        return relation != null
                && relation.getProperty() != null
                && relation.getProperty().getName() != null
                && !relation.getProperty().getName().isBlank()
                && (relation.getRelationType() == RelationType.Many2Many
                || relation.getRelationType() == RelationType.Recurve)
                && relation.getRelationTable() != null
                && !relation.getRelationTable().isBlank()
                && relation.getPropertyColumn() != null
                && !relation.getPropertyColumn().isBlank()
                && relation.getTargetColumn() != null
                && !relation.getTargetColumn().isBlank();
    }

    private Object dynamicId(IDynamicData data, Model model) {
        if (model != null && model.getIdProperty() != null
                && model.getIdProperty().getName() != null
                && !model.getIdProperty().getName().isBlank()) {
            Object id = data.get(model.getIdProperty().getName());
            if (id != null) {
                return id;
            }
        }
        return data.getId();
    }

    private boolean dataExists(IDynamicData data, Model model) {
        if (model == null || model.getTableName() == null || model.getTableName().isBlank()) {
            return false;
        }
        Property idProperty = model.getIdProperty();
        String idColumn = idProperty != null && idProperty.getColumn() != null && !idProperty.getColumn().isBlank()
                ? idProperty.getColumn()
                : "SYSID";
        Object idValue = data instanceof DbMysqlDynamic dynamicData
                ? lookupIdValue(dynamicData, idProperty)
                : (idProperty != null ? data.get(idProperty.getName()) : data.getId());
        if (idValue == null) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `" + model.getTableName() + "` WHERE `" + idColumn + "` = ?",
                Integer.class,
                idValue);
        return count != null && count > 0;
    }

    private Model dynamicModel(IDynamicData data, Model fallback) {
        if (data instanceof DbMysqlDynamic dynamicData && dynamicData.getModel() != null) {
            return dynamicData.getModel();
        }
        return fallback;
    }

    private Object lookupIdValue(DbMysqlDynamic data, Property idProperty) {
        if (idProperty != null) {
            String idName = idProperty.getName();
            if (idName != null && data.hasOld(idName)) {
                return data.getOld(idName);
            }
            return data.get(idName);
        }
        return data.getId();
    }

    private void insertRelationIfMissing(Relation relation, Object parentId, Object itemId) {
        Object propertyValue = relation.getRelationType() == RelationType.Recurve ? parentId : itemId;
        Object targetValue = relation.getRelationType() == RelationType.Recurve ? itemId : parentId;
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `" + relation.getRelationTable() + "` WHERE `"
                        + relation.getPropertyColumn() + "` = ? AND `" + relation.getTargetColumn() + "` = ?",
                Integer.class,
                propertyValue,
                targetValue);
        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO `" + relation.getRelationTable() + "` (`"
                            + relation.getPropertyColumn() + "`,`" + relation.getTargetColumn() + "`) VALUES (?,?)",
                    propertyValue,
                    targetValue);
        }
    }

    private void deleteRelation(Relation relation, Object parentId, Object itemId) {
        Object propertyValue = relation.getRelationType() == RelationType.Recurve ? parentId : itemId;
        Object targetValue = relation.getRelationType() == RelationType.Recurve ? itemId : parentId;
        jdbcTemplate.update(
                "DELETE FROM `" + relation.getRelationTable() + "` WHERE `"
                        + relation.getPropertyColumn() + "` = ? AND `" + relation.getTargetColumn() + "` = ?",
                propertyValue,
                targetValue);
    }

    private Map<String, Object> writableColumnValues(Model model, IDynamicData data, String excludedColumn) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Property property : model.getProperties()) {
            if (Boolean.TRUE.equals(property.getIsCollection())) {
                continue;
            }
            if (Boolean.TRUE.equals(property.getMultiMap())) {
                Object propertyValue = data.get(property.getName());
                if (propertyValue instanceof IDynamicData mappedData) {
                    for (MultiDbMap dbMap : safeDbMaps(property)) {
                        if (dbMap.getPropertyName() != null && !dbMap.getPropertyName().isBlank()) {
                            addColumnValue(values, dbMap.getColumnName(), mappedData.get(dbMap.getPropertyName()), excludedColumn);
                        }
                    }
                }
                continue;
            }
            addColumnValue(values, property.getColumn(), columnValue(property, data.get(property.getName())), excludedColumn);
        }
        return values;
    }

    private Object columnValue(Property property, Object value) {
        if (PropertyType.BusinessObject.equals(property.getPropertyType()) && value instanceof IDynamicData itemData) {
            return dynamicId(itemData, dynamicModel(itemData, property.getPropertyModel()));
        }
        return value;
    }

    private void addColumnValue(Map<String, Object> values, String column, Object value, String excludedColumn) {
        if (column == null || column.isBlank() || column.equals(excludedColumn)) {
            return;
        }
        values.put(column, value);
    }

    private List<MultiDbMap> safeDbMaps(Property property) {
        return property.getDbMaps() == null ? List.of() : property.getDbMaps();
    }

    public IDynamicData initData(Model model) {
        if (model == null) {
            return null;
        }
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        if (model.getProperties() == null) {
            return data;
        }
        model.getProperties().stream()
                .filter(property -> !Boolean.TRUE.equals(property.getMultiMap()))
                .forEach(property -> data.set(
                        property.getName(),
                        Boolean.TRUE.equals(property.getIsCollection())
                                ? new LinkedList<>()
                                : Mapper.defaultValue(property)));
        return data;
    }

}
