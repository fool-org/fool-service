package org.fool.framework.model.service;


import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.data.SubItemList;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.*;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.CommandsType;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelTriggerType;
import org.fool.framework.model.model.MultiDbMap;
import org.fool.framework.model.model.Operation;
import org.fool.framework.model.model.OperationBaseType;
import org.fool.framework.model.model.OperationCommand;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.model.PropertyTrigger;
import org.fool.framework.model.model.PropertyTriggerType;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.model.RelationType;
import org.fool.framework.model.model.Trigger;
import org.fool.framework.model.sqlscript.SqlGenerator;
import org.fool.framework.query.CompareFilter;
import org.fool.framework.query.CompareOp;
import org.fool.framework.query.IQueryFilter;
import org.fool.framework.query.SimpleFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Component
@Slf4j
public class ModelDataService {
    public record OrderColumn(String column, boolean descending) {
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;


    private ConcurrentHashMap<String, Mapper> concurrentHashMap = new ConcurrentHashMap<>();
    @Autowired
    private DaoService daoService;
    @Autowired
    private SqlGenerator sqlGenerator;
    @Autowired(required = false)
    private TransactionOperations transactionOperations;

    private final OperationCommandValueResolver commandValueResolver = new OperationCommandValueResolver();

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
        hydrateOperations(model);
        hydratePropertyTriggers(model);
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

    private void hydrateOperations(Model model) {
        if (model == null || model.getId() == null) {
            return;
        }
        List<Operation> operations = daoService.selectList(
                Operation.class,
                "SELECT `SysId`,`SW_SYS_MODEL_OperationsMODEL_ID`,`SW_MODEL_OPERATION_NAME`,"
                        + "`SW_MODEL_OPERATION_FILTER`,`SW_MODEL_OPERATION_BASETYPE`,"
                        + "`SW_MODEL_OPERATION_ARGMODEL`,`SW_MODEL_OPERATION_ARGFILTER`,"
                        + "`SW_MODEL_OPERATION_INVOKEDLL`,`SW_MODEL_OPERATION_INVOKECLASS`,"
                        + "`SW_MODEL_OPERATION_INVOKEMETHOD`,`SW_MODEL_OPERATION_RETURNMODEL` "
                        + "FROM `SW_SYS_OPERATION` "
                        + "WHERE `SW_SYS_MODEL_OperationsMODEL_ID` = ? ORDER BY `SysId`",
                model.getId());
        for (Operation operation : operations) {
            operation.setCommands(daoService.selectList(
                    OperationCommand.class,
                    "SELECT `SysId`,`SW_SYS_OPERATION_CommandsSysId`,`SW_SYS_COMMAND_TYPE`,"
                            + "`SW_SYS_COMMAND_PROPERTY`,`SW_SYS_COMMAND_EXP`,"
                            + "`SW_SYS_COMMAND_ARGMODEL`,`SW_SYS_COMMAND_ARGEXP`,`SW_SYS_COMMAND_ARGID`,"
                            + "`SW_SYS_COMMAND_INDEX`,`SW_SYS_COMMAND_PROPERTY_EXP`,"
                            + "`SW_SYS_COMMAND_TEMPVALUE` FROM `SW_SYS_COMMANDS` "
                            + "WHERE `SW_SYS_OPERATION_CommandsSysId` = ? "
                            + "ORDER BY `SW_SYS_COMMAND_INDEX`, `SysId`",
                    operation.getId()));
        }
        model.setOperations(operations);
    }

    private void hydratePropertyTriggers(Model model) {
        if (model == null || model.getProperties() == null || model.getProperties().isEmpty()) {
            return;
        }
        List<Long> propertyIds = model.getProperties().stream()
                .map(Property::getId)
                .filter(Objects::nonNull)
                .toList();
        if (propertyIds.isEmpty()) {
            return;
        }
        String placeholders = propertyIds.stream().map(id -> "?").collect(Collectors.joining(","));
        List<PropertyTrigger> triggers = daoService.selectList(
                PropertyTrigger.class,
                "SELECT `SysId`,`SW_SYS_PROPERTY_TriggersSysId`,`SW_PROPERTY_TRIGGER_ARGFILTER`,"
                        + "`SW_PROPERTY_TRIGGER_ARGMODEL`,`SW_PROPERTY_TRIGGER_FILTER`,"
                        + "`SW_PROPERTY_TRIGGER_TYPE`,`SW_PROPERTY_TRIGGER_NAME`,"
                        + "`SW_PROPERTY_TRIGGER_PROPERTY`,`SW_PROPERTY_TRIGGER_BASETYPE`,"
                        + "`SW_MODEL_TRIGGER_INVOKEDLL`,`SW_MODEL_TRIGGER_INVOKECLASS`,"
                        + "`SW_MODEL_TRIGGER_INVOKEMETHOD` FROM `SW_SYS_PROPERTY_TRIGGER` "
                        + "WHERE `SW_SYS_PROPERTY_TriggersSysId` IN (" + placeholders + ") ORDER BY `SysId`",
                propertyIds.toArray());
        for (PropertyTrigger trigger : triggers) {
            trigger.setCommands(daoService.selectList(
                    OperationCommand.class,
                    "SELECT `SysId`,`SW_SYS_PROPERTY_TRIGGER_CommandsSysId` AS `SW_SYS_OPERATION_CommandsSysId`,"
                            + "`SW_SYS_COMMAND_TYPE`,`SW_SYS_COMMAND_PROPERTY`,`SW_SYS_COMMAND_EXP`,"
                            + "`SW_SYS_COMMAND_ARGMODEL`,`SW_SYS_COMMAND_ARGEXP`,`SW_SYS_COMMAND_ARGID`,"
                            + "`SW_SYS_COMMAND_INDEX`,`SW_SYS_COMMAND_PROPERTY_EXP`,"
                            + "`SW_SYS_COMMAND_TEMPVALUE` FROM `SW_SYS_PROPERTY_TRIGGER_COMMANDS` "
                            + "WHERE `SW_SYS_PROPERTY_TRIGGER_CommandsSysId` = ? "
                            + "ORDER BY `SW_SYS_COMMAND_INDEX`, `SysId`",
                    trigger.getId()));
        }
        Map<Long, List<PropertyTrigger>> byProperty = triggers.stream()
                .collect(Collectors.groupingBy(PropertyTrigger::getOwnerPropertyId));
        model.getProperties().forEach(property ->
                property.setTriggerList(byProperty.getOrDefault(property.getId(), List.of())));
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
        return getDataListWithPageInfo(
                modelId,
                filter,
                properties,
                pageNavigator,
                orderColumn == null || orderColumn.isBlank()
                        ? List.of()
                        : List.of(new OrderColumn(orderColumn, orderDescending)));
    }

    public PageResult<IDynamicData> getDataListWithPageInfo(
            String modelId,
            IQueryFilter filter,
            List<Property> properties,
            PageNavigator pageNavigator,
            List<OrderColumn> orderColumns) {
        var mapper = getMapper(modelId);
        QueryAndArgs queryAndArgs = sqlGenerator.generateSelect(
                mapper.getModel(), properties, filter, pageNavigator, sqlOrderColumns(orderColumns));
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

    private List<SqlGenerator.OrderColumn> sqlOrderColumns(List<OrderColumn> orderColumns) {
        if (orderColumns == null) {
            return List.of();
        }
        return orderColumns.stream()
                .filter(Objects::nonNull)
                .filter(orderColumn -> orderColumn.column() != null && !orderColumn.column().isBlank())
                .map(orderColumn -> new SqlGenerator.OrderColumn(orderColumn.column(), orderColumn.descending()))
                .toList();
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
        Map<String, IDynamicData> owners = items.stream()
                .filter(item -> item.getId() != null && !item.getId().isBlank())
                .collect(Collectors.toMap(IDynamicData::getId, item -> item, (first, second) -> first, LinkedHashMap::new));
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
                        attachOwner(item, owners.get(id));
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
        Boolean saved = inTransaction(() -> saveData(data, null, null, true, true, false));
        if (Boolean.TRUE.equals(saved) && data instanceof DbMysqlDynamic dynamicData) {
            executeModelTriggers(dynamicData.getModel(), data, ModelTriggerType.SAVE);
        }
        return saved;
    }

    private Boolean saveData(IDynamicData data, String extraColumn, Object extraValue, boolean writeCollections) {
        return saveData(data, extraColumn, extraValue, writeCollections, true);
    }

    private Boolean saveData(
            IDynamicData data,
            String extraColumn,
            Object extraValue,
            boolean writeCollections,
            boolean runTriggers) {
        return saveData(data, extraColumn, extraValue, writeCollections, runTriggers, runTriggers);
    }

    private Boolean saveData(
            IDynamicData data,
            String extraColumn,
            Object extraValue,
            boolean writeCollections,
            boolean runTriggers,
            boolean runModelTriggers) {
        if (!(data instanceof DbMysqlDynamic dynamicData)) {
            return false;
        }
        Model model = dynamicData.getModel();
        if (model == null || model.getTableName() == null || model.getTableName().isBlank()
                || model.getProperties() == null) {
            return false;
        }
        hydratePropertyTriggers(model);
        hydrateModelTriggers(model);
        Property idProperty = model.getIdProperty();
        String idColumn = idProperty != null && idProperty.getColumn() != null && !idProperty.getColumn().isBlank()
                ? idProperty.getColumn()
                : "SYSID";
        Object idValue = lookupIdValue(dynamicData, idProperty);
        if (idValue == null) {
            return false;
        }
        if (runTriggers) {
            executePropertySetTriggers(model, dynamicData, false);
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
        if (saved && runTriggers && runModelTriggers) {
            executeModelTriggers(model, data, ModelTriggerType.SAVE);
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
        if (data instanceof DbMysqlDynamic dynamicData) {
            Model model = dynamicData.getModel();
            if (model != null) {
                hydrateModelTriggers(model);
                executeModelTriggers(model, data, ModelTriggerType.DELETE);
            }
        }
        return inTransaction(() -> deleteData(data, false));
    }

    private Boolean deleteData(IDynamicData data, boolean runTriggers) {
        if (!(data instanceof DbMysqlDynamic dynamicData)) {
            return false;
        }
        Model model = dynamicData.getModel();
        if (model == null || model.getTableName() == null || model.getTableName().isBlank()) {
            return false;
        }
        hydrateModelTriggers(model);
        Property idProperty = model.getIdProperty();
        String idColumn = idProperty != null && idProperty.getColumn() != null && !idProperty.getColumn().isBlank()
                ? idProperty.getColumn()
                : "SYSID";
        Object idValue = lookupIdValue(dynamicData, idProperty);
        if (idValue == null) {
            return false;
        }
        if (runTriggers) {
            executeModelTriggers(model, data, ModelTriggerType.DELETE);
        }
        String sql = "DELETE FROM `" + model.getTableName() + "` WHERE `" + idColumn + "` = ?";
        return jdbcTemplate.update(sql, idValue) > 0;
    }

    public Boolean createData(IDynamicData data) {
        Boolean created = inTransaction(() -> createData(data, null, null, true, true, false));
        if (Boolean.TRUE.equals(created) && data instanceof DbMysqlDynamic dynamicData) {
            executeModelTriggers(dynamicData.getModel(), data, ModelTriggerType.CREATE);
        }
        return created;
    }

    public Boolean createData(IDynamicData data, String extraColumn, Object extraValue) {
        Boolean created = inTransaction(() -> createData(data, extraColumn, extraValue, false, true, false));
        if (Boolean.TRUE.equals(created) && data instanceof DbMysqlDynamic dynamicData) {
            executeModelTriggers(dynamicData.getModel(), data, ModelTriggerType.CREATE);
        }
        return created;
    }

    private Boolean createData(IDynamicData data, String extraColumn, Object extraValue, boolean writeCollections) {
        return createData(data, extraColumn, extraValue, writeCollections, true);
    }

    private Boolean createData(
            IDynamicData data,
            String extraColumn,
            Object extraValue,
            boolean writeCollections,
            boolean runTriggers) {
        return createData(data, extraColumn, extraValue, writeCollections, runTriggers, runTriggers);
    }

    private Boolean createData(
            IDynamicData data,
            String extraColumn,
            Object extraValue,
            boolean writeCollections,
            boolean runTriggers,
            boolean runModelTriggers) {
        if (!(data instanceof DbMysqlDynamic dynamicData)) {
            return false;
        }
        Model model = dynamicData.getModel();
        if (model == null || model.getTableName() == null || model.getTableName().isBlank()
                || model.getProperties() == null) {
            return false;
        }
        hydratePropertyTriggers(model);
        hydrateModelTriggers(model);
        if (runTriggers) {
            executePropertySetTriggers(model, dynamicData, true);
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
        if (created && runTriggers && runModelTriggers) {
            executeModelTriggers(model, data, ModelTriggerType.CREATE);
        }
        return created;
    }

    private <T> T inTransaction(Supplier<T> action) {
        if (transactionOperations == null) {
            return action.get();
        }
        return transactionOperations.execute(status -> action.get());
    }

    private void executeModelTriggers(Model model, IDynamicData data, ModelTriggerType triggerType) {
        if (model.getTriggers() == null || model.getTriggers().isEmpty()) {
            return;
        }
        model.getTriggers().stream()
                .filter(trigger -> trigger != null && trigger.getTriggerType() == triggerType)
                .forEach(trigger -> executeModelTrigger(model, data, trigger));
    }

    private void executeModelTrigger(Model model, IDynamicData data, Trigger trigger) {
        TriggerCommandValues values = executeTriggerCommands(model, data, trigger.getCommands());
        executeTriggerBaseOperation(
                trigger.getBaseOperationType(), data, values, trigger.getInvokeClass(), trigger.getInvokeMethod());
    }

    private void executePropertySetTriggers(Model model, DbMysqlDynamic data, boolean creating) {
        model.getProperties().stream()
                .filter(property -> property != null && propertyTouched(data, property, creating))
                .flatMap(property -> property.getTriggerList().stream())
                .filter(trigger -> trigger != null && trigger.getTriggerType() == PropertyTriggerType.SET)
                .forEach(trigger -> executePropertyTrigger(model, data, trigger));
    }

    private void executePropertyTrigger(Model model, IDynamicData data, PropertyTrigger trigger) {
        TriggerCommandValues values = executeTriggerCommands(model, data, trigger.getCommands());
        executeTriggerBaseOperation(
                trigger.getBaseOperationType(), data, values, trigger.getInvokeClass(), trigger.getInvokeMethod());
    }

    private void executeTriggerBaseOperation(
            OperationBaseType type,
            IDynamicData data,
            TriggerCommandValues values,
            String invokeClass,
            String invokeMethod) {
        if (type == OperationBaseType.UPDATE) {
            saveData(data, null, null, false, false);
        } else if (type == OperationBaseType.CREATE) {
            createData(data, null, null, false, false);
        } else if (type == OperationBaseType.DELETE) {
            deleteData(data, false);
        } else if (type == OperationBaseType.ASSEBMLY) {
            invokeLegacyAssembly(invokeClass, invokeMethod, data, values);
        }
    }

    private void invokeLegacyAssembly(
            String invokeClass, String invokeMethod, IDynamicData data, TriggerCommandValues values) {
        LegacyAssemblyInvoker.invoke(invokeClass, invokeMethod, data, values.constructorValues, values.params);
    }

    private boolean propertyTouched(DbMysqlDynamic data, Property property, boolean creating) {
        if (property.getName() == null || property.getName().isBlank()) {
            return false;
        }
        return creating ? data.toMap().containsKey(property.getName()) : data.hasOld(property.getName());
    }

    private TriggerCommandValues executeTriggerCommands(Model model, IDynamicData data, List<OperationCommand> commands) {
        return executeTriggerCommands(model, data, data, commands);
    }

    private TriggerCommandValues executeTriggerCommands(
            Model model, IDynamicData data, IDynamicData valueSource, List<OperationCommand> commands) {
        TriggerCommandValues values = new TriggerCommandValues();
        if (commands == null) {
            return values;
        }
        commands.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(command -> command.getIndex() == null ? 0 : command.getIndex()))
                .forEach(command -> executeTriggerCommand(model, data, valueSource, command, values));
        return values;
    }

    private void executeTriggerCommand(
            Model model, IDynamicData data, IDynamicData valueSource, OperationCommand command, TriggerCommandValues values) {
        if (command.getCommandType() == CommandsType.SET_VALUE) {
            property(model, command.getPropertyId())
                    .ifPresent(property -> data.set(
                            property.getName(),
                            triggerCommandValue(property, valueSource, command.getExpression())));
        } else if (command.getCommandType() == CommandsType.FILTER) {
            checkFilterCommand(model, data, command);
        } else if (command.getCommandType() == CommandsType.EXUTE_PROPRTY_MODEL_METHOD) {
            property(model, command.getPropertyId())
                    .ifPresent(property -> invokePropertyModelMethod(data, property, command.getExpression()));
        } else if (command.getCommandType() == CommandsType.EXUTE_LIST_METHOD) {
            property(model, command.getPropertyId())
                    .ifPresent(property -> invokeListMethod(data, property, command.getExpression()));
        } else if (command.getCommandType() == CommandsType.EXUTE_OUT_MODEL_METHOD) {
            IDynamicData result = executeOutModelCommand(model, data, command);
            if (result != null) {
                property(model, command.getPropertyId())
                        .ifPresent(property -> data.set(
                                property.getName(),
                                triggerCommandValue(property, result, command.getArgExpression())));
            }
        } else if (command.getCommandType() == CommandsType.SET_PARAM_VALUE) {
            values.params.add(triggerCommandValue(
                    property(model, command.getPropertyId()).orElse(null), valueSource, command.getExpression()));
        } else if (command.getCommandType() == CommandsType.SET_CON_STR_VALUE) {
            values.constructorValues.add(triggerCommandValue(
                    property(model, command.getPropertyId()).orElse(null), valueSource, command.getExpression()));
        }
    }

    private IDynamicData executeOutModelCommand(Model sourceModel, IDynamicData sourceData, OperationCommand command) {
        if (command.getArgModelId() == null) {
            return null;
        }
        Model targetModel = getModel(command.getArgModelId().toString());
        if (targetModel == null || targetModel.getName() == null || targetModel.getName().isBlank()) {
            return null;
        }
        Object targetId = command.getArgSourceIdExpression() == null || command.getArgSourceIdExpression().isBlank()
                ? ""
                : triggerCommandValue(
                        property(sourceModel, command.getPropertyId()).orElse(null),
                        sourceData,
                        command.getArgSourceIdExpression());
        Operation targetOperation = modelOperation(targetModel, command.getExpression());
        if (targetOperation == null) {
            return getOneData(targetModel.getName(), targetId == null ? null : String.valueOf(targetId));
        }
        OperationBaseType type = targetOperation.getBaseOperationType();
        IDynamicData targetData;
        if (type == OperationBaseType.CREATE) {
            targetData = new DbMysqlDynamic(targetModel);
        } else if (type == OperationBaseType.UPDATE || type == OperationBaseType.DELETE) {
            targetData = getOneData(targetModel.getName(), targetId == null ? null : String.valueOf(targetId));
        } else {
            return null;
        }
        if (targetData == null) {
            return null;
        }
        executeTriggerCommands(targetModel, targetData, sourceData, targetOperation.getCommands());
        boolean success;
        if (type == OperationBaseType.CREATE) {
            success = Boolean.TRUE.equals(createData(targetData));
        } else if (type == OperationBaseType.UPDATE) {
            success = Boolean.TRUE.equals(saveData(targetData));
        } else {
            success = Boolean.TRUE.equals(deleteData(targetData));
        }
        return success ? targetData : null;
    }

    private Operation modelOperation(Model model, String operationName) {
        if (model == null || model.getOperations() == null || operationName == null || operationName.isBlank()) {
            return null;
        }
        String name = operationName.trim().toUpperCase(Locale.ROOT);
        return model.getOperations().stream()
                .filter(operation -> operation != null && operation.getName() != null)
                .filter(operation -> operation.getName().trim().toUpperCase(Locale.ROOT).equals(name))
                .findFirst()
                .orElse(null);
    }

    private void invokePropertyModelMethod(IDynamicData data, Property property, String methodName) {
        if (data == null || property == null || property.getName() == null || methodName == null || methodName.isBlank()) {
            return;
        }
        Object value = data.get(property.getName());
        if (Boolean.TRUE.equals(property.getIsCollection()) && value instanceof Iterable<?> items) {
            items.forEach(item -> invokeDynamic(item, methodName));
            return;
        }
        invokeDynamic(value, methodName);
    }

    private void invokeListMethod(IDynamicData data, Property property, String methodName) {
        if (data == null || property == null || property.getName() == null || methodName == null || methodName.isBlank()) {
            return;
        }
        Object value = data.get(property.getName());
        if (value instanceof IDynamicData dynamicData) {
            dynamicData.invoke(methodName);
            return;
        }
        if (value == null) {
            return;
        }
        try {
            value.getClass().getMethod(methodName).invoke(value);
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private void invokeDynamic(Object value, String methodName) {
        if (value instanceof IDynamicData dynamicData) {
            dynamicData.invoke(methodName);
        }
    }

    private void checkFilterCommand(Model model, IDynamicData data, OperationCommand command) {
        if (model == null || data == null || command.getExpression() == null || command.getExpression().isBlank()) {
            return;
        }
        String idColumn = model.getIdProperty() != null
                && model.getIdProperty().getColumn() != null
                && !model.getIdProperty().getColumn().isBlank()
                ? model.getIdProperty().getColumn()
                : "SYSID";
        Object idValue = dynamicId(data, model);
        IQueryFilter filter = new CompareFilter(idColumn, CompareOp.EQUAL, idValue == null ? "" : String.valueOf(idValue))
                .and(rawFilter(command.getExpression()));
        List<IDynamicData> matched = getDataList(
                model.getName(),
                filter,
                model.getProperties() == null ? List.of() : model.getProperties());
        if (matched.isEmpty()) {
            throw new IllegalStateException(command.getPropertyExpression() == null ? "" : command.getPropertyExpression());
        }
    }

    private IQueryFilter rawFilter(String filter) {
        return new SimpleFilter() {
            @Override
            public QueryAndArgs generateSql() {
                QueryAndArgs queryAndArgs = new QueryAndArgs();
                queryAndArgs.setSql(filter);
                queryAndArgs.setArgs(new Object[]{});
                return queryAndArgs;
            }
        };
    }

    private java.util.Optional<Property> property(Model model, Long propertyId) {
        if (model == null || model.getProperties() == null || propertyId == null) {
            return java.util.Optional.empty();
        }
        return model.getProperties().stream()
                .filter(property -> Objects.equals(propertyId, property.getId()))
                .findFirst();
    }

    private Object triggerCommandValue(Property property, IDynamicData data, String expression) {
        return commandValueResolver.resolve(property, data, expression, this::businessObjectValue);
    }

    private Object businessObjectValue(Property property, String value) {
        return property.getPropertyModel() == null
                || property.getPropertyModel().getName() == null
                || property.getPropertyModel().getName().isBlank()
                ? value
                : getOneData(property.getPropertyModel().getName(), value);
    }

    private static class TriggerCommandValues {
        private final List<Object> params = new LinkedList<>();
        private final List<Object> constructorValues = new LinkedList<>();
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
                writeOwnedCollection(relation, data.get(relation.getProperty().getName()), parentId, data);
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
                    attachOwner(itemData, data);
                    Object itemId = dynamicId(itemData, relation.getProperty().getPropertyModel());
                    if (itemId != null && !relationExists(relation, parentId, itemId)) {
                        executePropertyItemTriggers(relation.getProperty(), itemData, PropertyTriggerType.ITEMS_ADD);
                        insertRelation(relation, parentId, itemId);
                    }
                }
            }
            if (value instanceof SubItemList<?> subItems) {
                for (Object item : subItems.getDeleteList()) {
                    if (item instanceof IDynamicData itemData) {
                        attachOwner(itemData, data);
                        Object itemId = dynamicId(itemData, relation.getProperty().getPropertyModel());
                        if (itemId != null) {
                            executePropertyItemTriggers(relation.getProperty(), itemData, PropertyTriggerType.ITEMS_DELETE);
                            deleteRelation(relation, parentId, itemId);
                        }
                    }
                }
            }
        }
    }

    private void writeOwnedCollection(Relation relation, Object value, Object parentId, IDynamicData owner) {
        if (value instanceof Iterable<?> items) {
            for (Object item : items) {
                if (item instanceof IDynamicData itemData) {
                    attachOwner(itemData, owner);
                    Model itemModel = dynamicModel(itemData, relation.getProperty().getPropertyModel());
                    if (dataExists(itemData, itemModel)) {
                        saveData(itemData, relation.getTargetColumn(), parentId, false);
                    } else {
                        executePropertyItemTriggers(relation.getProperty(), itemData, PropertyTriggerType.ITEMS_ADD);
                        createData(itemData, relation.getTargetColumn(), parentId, false);
                    }
                }
            }
        }
        if (value instanceof SubItemList<?> subItems) {
            for (Object item : subItems.getDeleteList()) {
                if (item instanceof IDynamicData itemData) {
                    attachOwner(itemData, owner);
                    executePropertyItemTriggers(relation.getProperty(), itemData, PropertyTriggerType.ITEMS_DELETE);
                    deleteData(itemData);
                }
            }
        }
    }

    private void attachOwner(IDynamicData itemData, IDynamicData owner) {
        if (itemData instanceof DbMysqlDynamic dynamicData) {
            dynamicData.setOwner(owner);
        }
    }

    private void executePropertyItemTriggers(Property property, IDynamicData itemData, PropertyTriggerType triggerType) {
        if (property == null || property.getTriggerList() == null || itemData == null) {
            return;
        }
        Model itemModel = dynamicModel(itemData, property.getPropertyModel());
        property.getTriggerList().stream()
                .filter(trigger -> trigger != null && trigger.getTriggerType() == triggerType)
                .forEach(trigger -> executePropertyTrigger(itemModel, itemData, trigger));
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

    private boolean relationExists(Relation relation, Object parentId, Object itemId) {
        Object propertyValue = relation.getRelationType() == RelationType.Recurve ? parentId : itemId;
        Object targetValue = relation.getRelationType() == RelationType.Recurve ? itemId : parentId;
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `" + relation.getRelationTable() + "` WHERE `"
                        + relation.getPropertyColumn() + "` = ? AND `" + relation.getTargetColumn() + "` = ?",
                Integer.class,
                propertyValue,
                targetValue);
        return count != null && count > 0;
    }

    private void insertRelation(Relation relation, Object parentId, Object itemId) {
        Object propertyValue = relation.getRelationType() == RelationType.Recurve ? parentId : itemId;
        Object targetValue = relation.getRelationType() == RelationType.Recurve ? itemId : parentId;
        jdbcTemplate.update(
                "INSERT INTO `" + relation.getRelationTable() + "` (`"
                        + relation.getPropertyColumn() + "`,`" + relation.getTargetColumn() + "`) VALUES (?,?)",
                propertyValue,
                targetValue);
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
            if (!data.toMap().containsKey(property.getName())) {
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
        if (PropertyType.Boolean.equals(property.getPropertyType()) && value instanceof String text) {
            return Boolean.valueOf(text.trim());
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
