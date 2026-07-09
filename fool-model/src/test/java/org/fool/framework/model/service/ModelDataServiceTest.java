package org.fool.framework.model.service;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.data.SubItemList;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.model.Application;
import org.fool.framework.model.model.CommandsType;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelTriggerType;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.MultiDbMap;
import org.fool.framework.model.model.OperationBaseType;
import org.fool.framework.model.model.OperationCommand;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.model.PropertyTrigger;
import org.fool.framework.model.model.PropertyTriggerType;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.model.RelationType;
import org.fool.framework.model.model.Trigger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = Application.class)
public class ModelDataServiceTest {

    @Autowired
    private ModelDataService modelDataService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getModel() {

        String modelName = "user";
        var model = modelDataService.getModel(modelName);
        log.info("the model is :{}", model);
    }


    @Test
    public void getDataListWithPageInfo(){

    }

    @Test
    public void getModelRehydratesRuntimeEnumValuesFromDetailRows() {
        long modelId = 91001L;
        String modelName = "RuntimeEnumState";
        cleanupRuntimeEnumModel(modelId, modelName);
        try {
            jdbcTemplate.update(
                    "INSERT INTO `fool_sys_model` "
                            + "(`id`,`name`,`text`,`remark`,`model_type`,`class_name`,`table_name`,`auto_sys_id`,`id_property`) "
                            + "VALUES (?,?,?,?,?,?,?,?,?)",
                    modelId,
                    modelName,
                    modelName,
                    "runtime enum rehydration test",
                    ModelType.ENUM.code(),
                    "example.RuntimeEnumState",
                    null,
                    false,
                    null);
            jdbcTemplate.update(
                    "INSERT INTO `fool_sys_model_enum` (`name`,`value`,`remark`,`owner`) VALUES (?,?,?,?)",
                    "OPEN",
                    "0",
                    "open state",
                    modelId);
            jdbcTemplate.update(
                    "INSERT INTO `fool_sys_model_enum` (`name`,`value`,`remark`,`owner`) VALUES (?,?,?,?)",
                    "CLOSED",
                    "1",
                    "closed state",
                    modelId);

            Model model = modelDataService.getModel(modelName);

            assertEquals(ModelType.ENUM, model.getModelType());
            assertEquals(
                    List.of("OPEN:0", "CLOSED:1"),
                    model.getEnumValues().stream()
                            .map(value -> value.getName() + ":" + value.getValue())
                            .toList());
        } finally {
            cleanupRuntimeEnumModel(modelId, modelName);
        }
    }

    @Test
    public void getModelRehydratesLegacyRelationsForCollectionProperties() {
        long orderModelId = 91501L;
        long itemModelId = 91502L;
        long orderIdPropertyId = 91503L;
        long itemsPropertyId = 91504L;
        long itemIdPropertyId = 91505L;
        String orderModelName = "RuntimeRelationOrder";
        String itemModelName = "RuntimeRelationItem";
        cleanupRuntimeRelationModel(orderModelId, itemModelId, orderModelName, itemModelName);
        try {
            createRuntimeRelationModel(
                    orderModelId,
                    itemModelId,
                    orderIdPropertyId,
                    itemsPropertyId,
                    itemIdPropertyId,
                    orderModelName,
                    itemModelName);

            Model model = modelDataService.getModel(orderModelName);

            assertNotNull(model);
            assertNotNull(model.getRelations());
            assertFalse(model.getRelations().isEmpty());
            Relation relation = model.getRelations().get(0);
            assertEquals(RelationType.One2Many, relation.getRelationType());
            assertEquals("items", relation.getProperty().getName());
            assertEquals("RuntimeRelationItem", relation.getProperty().getPropertyModel().getName());
            assertEquals("runtime_relation_item", relation.getRelationTable());
            assertEquals("ORDER_ID", relation.getTargetColumn());
        } finally {
            cleanupRuntimeRelationModel(orderModelId, itemModelId, orderModelName, itemModelName);
        }
    }

    @Test
    public void getOneDataLoadsLegacyDetailByModelIdAndDataId() {
        long modelId = 92001L;
        long idPropertyId = 92002L;
        long namePropertyId = 92003L;
        String modelName = "RuntimeDetailOrder";
        String tableName = "runtime_detail_order";
        cleanupRuntimeDetailModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "1001",
                    "Legacy detail");

            IDynamicData data = modelDataService.getOneData(modelName, "1001");

            assertNotNull(data);
            assertEquals("1001", data.getId());
            assertEquals("Legacy detail", data.get("orderName"));
        } finally {
            cleanupRuntimeDetailModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void getOneDataAttachesLegacyCollectionOwnerForChildExpressions() {
        long orderModelId = 92101L;
        long itemModelId = 92102L;
        long orderIdPropertyId = 92103L;
        long itemsPropertyId = 92104L;
        long itemIdPropertyId = 92105L;
        String orderModelName = "RuntimeOwnerExpressionOrder";
        String itemModelName = "RuntimeOwnerExpressionItem";
        cleanupRuntimeRelationDataTables();
        cleanupRuntimeRelationModel(orderModelId, itemModelId, orderModelName, itemModelName);
        try {
            createRuntimeRelationModel(
                    orderModelId,
                    itemModelId,
                    orderIdPropertyId,
                    itemsPropertyId,
                    itemIdPropertyId,
                    orderModelName,
                    itemModelName);
            jdbcTemplate.execute("CREATE TABLE `runtime_relation_order` ("
                    + "`ORDER_ID` varchar(64) NOT NULL,"
                    + "PRIMARY KEY (`ORDER_ID`))");
            jdbcTemplate.execute("CREATE TABLE `runtime_relation_item` ("
                    + "`ITEM_ID` varchar(64) NOT NULL,"
                    + "`ORDER_ID` varchar(64) DEFAULT NULL,"
                    + "PRIMARY KEY (`ITEM_ID`))");
            jdbcTemplate.update("INSERT INTO `runtime_relation_order` (`ORDER_ID`) VALUES (?)", "6101");
            jdbcTemplate.update(
                    "INSERT INTO `runtime_relation_item` (`ITEM_ID`,`ORDER_ID`) VALUES (?,?)",
                    "I1",
                    "6101");

            IDynamicData data = modelDataService.getOneData(orderModelName, "6101");

            assertNotNull(data);
            List<?> items = (List<?>) data.get("items");
            assertEquals(1, items.size());
            DbMysqlDynamic child = (DbMysqlDynamic) items.get(0);
            assertSame(data, child.getOwner());
        } finally {
            cleanupRuntimeRelationDataTables();
            cleanupRuntimeRelationModel(orderModelId, itemModelId, orderModelName, itemModelName);
        }
    }

    @Test
    public void getModelRehydratesLegacyModelTriggersWithCommands() {
        long modelId = 92501L;
        long idPropertyId = 92502L;
        long namePropertyId = 92503L;
        long triggerId = 92504L;
        long commandId = 92505L;
        String modelName = "RuntimeTriggerOrder";
        String tableName = "runtime_trigger_order";
        cleanupRuntimeTriggerModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `SW_SYS_MODEL_TRIGGER` "
                            + "(`SysId`,`SW_SYS_MODEL_TriggersMODEL_ID`,`SW_MODEL_TRIGGER_TYPE`,"
                            + "`SW_MODEL_TRIGGER_FILTER`,`SW_MODEL_TRIGGER_OPERATIONTYPE`) "
                            + "VALUES (?,?,?,?,?)",
                    triggerId,
                    modelId,
                    ModelTriggerType.SAVE.code(),
                    "`ORDER_NAME` IS NOT NULL",
                    OperationBaseType.UPDATE.code());
            jdbcTemplate.update(
                    "INSERT INTO `SW_SYS_MODEL_TRIGGER_COMMANDS` "
                            + "(`SysId`,`SW_SYS_MODEL_TRIGGER_CommandsSysId`,`SW_SYS_COMMAND_TYPE`,"
                            + "`SW_SYS_COMMAND_PROPERTY`,`SW_SYS_COMMAND_EXP`,`SW_SYS_COMMAND_Index`) "
                            + "VALUES (?,?,?,?,?,?)",
                    commandId,
                    triggerId,
                    CommandsType.SET_VALUE.code(),
                    namePropertyId,
                    "$triggered",
                    1);

            Model model = modelDataService.getModel(modelName);

            assertNotNull(model.getTriggers());
            assertEquals(1, model.getTriggers().size());
            Trigger trigger = model.getTriggers().get(0);
            assertEquals(Long.valueOf(triggerId), trigger.getId());
            assertEquals(ModelTriggerType.SAVE, trigger.getTriggerType());
            assertEquals("`ORDER_NAME` IS NOT NULL", trigger.getFilter());
            assertEquals(OperationBaseType.UPDATE, trigger.getBaseOperationType());
            assertEquals(1, trigger.getCommands().size());
            OperationCommand command = trigger.getCommands().get(0);
            assertEquals(Long.valueOf(commandId), command.getId());
            assertEquals(CommandsType.SET_VALUE, command.getCommandType());
            assertEquals(Long.valueOf(namePropertyId), command.getPropertyId());
            assertEquals("$triggered", command.getExpression());
        } finally {
            cleanupRuntimeTriggerModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void saveDataExecutesLegacySaveTriggerSetValue() {
        long modelId = 92601L;
        long idPropertyId = 92602L;
        long namePropertyId = 92603L;
        long triggerId = 92604L;
        long commandId = 92605L;
        String modelName = "RuntimeSaveTriggerOrder";
        String tableName = "runtime_save_trigger_order";
        cleanupRuntimeTriggerModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "2001",
                    "before trigger");
            jdbcTemplate.update(
                    "INSERT INTO `SW_SYS_MODEL_TRIGGER` "
                            + "(`SysId`,`SW_SYS_MODEL_TriggersMODEL_ID`,`SW_MODEL_TRIGGER_TYPE`,"
                            + "`SW_MODEL_TRIGGER_OPERATIONTYPE`) VALUES (?,?,?,?)",
                    triggerId,
                    modelId,
                    ModelTriggerType.SAVE.code(),
                    OperationBaseType.UPDATE.code());
            jdbcTemplate.update(
                    "INSERT INTO `SW_SYS_MODEL_TRIGGER_COMMANDS` "
                            + "(`SysId`,`SW_SYS_MODEL_TRIGGER_CommandsSysId`,`SW_SYS_COMMAND_TYPE`,"
                            + "`SW_SYS_COMMAND_PROPERTY`,`SW_SYS_COMMAND_EXP`,`SW_SYS_COMMAND_Index`) "
                            + "VALUES (?,?,?,?,?,?)",
                    commandId,
                    triggerId,
                    CommandsType.SET_VALUE.code(),
                    namePropertyId,
                    "$triggered",
                    1);
            Model model = modelDataService.getModel(modelName);
            DbMysqlDynamic data = new DbMysqlDynamic(model);
            data.set("orderId", "2001");
            data.set("orderName", "manual save");

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            String name = jdbcTemplate.queryForObject(
                    "SELECT `ORDER_NAME` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    String.class,
                    "2001");
            assertEquals("triggered", name);
        } finally {
            cleanupRuntimeTriggerModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void saveDataStopsLegacySaveTriggerWhenFilterCommandDoesNotMatch() {
        long modelId = 92621L;
        long idPropertyId = 92622L;
        long namePropertyId = 92623L;
        long triggerId = 92624L;
        long filterCommandId = 92625L;
        long setValueCommandId = 92626L;
        String modelName = "RuntimeSaveTriggerFilterOrder";
        String tableName = "runtime_save_trigger_filter_order";
        cleanupRuntimeTriggerModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "2001",
                    "before trigger");
            jdbcTemplate.update(
                    "INSERT INTO `SW_SYS_MODEL_TRIGGER` "
                            + "(`SysId`,`SW_SYS_MODEL_TriggersMODEL_ID`,`SW_MODEL_TRIGGER_TYPE`,"
                            + "`SW_MODEL_TRIGGER_OPERATIONTYPE`) VALUES (?,?,?,?)",
                    triggerId,
                    modelId,
                    ModelTriggerType.SAVE.code(),
                    OperationBaseType.UPDATE.code());
            jdbcTemplate.update(
                    "INSERT INTO `SW_SYS_MODEL_TRIGGER_COMMANDS` "
                            + "(`SysId`,`SW_SYS_MODEL_TRIGGER_CommandsSysId`,`SW_SYS_COMMAND_TYPE`,"
                            + "`SW_SYS_COMMAND_EXP`,`SW_SYS_COMMAND_PROPERTY_EXP`,`SW_SYS_COMMAND_Index`) "
                            + "VALUES (?,?,?,?,?,?)",
                    filterCommandId,
                    triggerId,
                    CommandsType.FILTER.code(),
                    "`ORDER_NAME` = 'allowed'",
                    "filter blocked",
                    1);
            jdbcTemplate.update(
                    "INSERT INTO `SW_SYS_MODEL_TRIGGER_COMMANDS` "
                            + "(`SysId`,`SW_SYS_MODEL_TRIGGER_CommandsSysId`,`SW_SYS_COMMAND_TYPE`,"
                            + "`SW_SYS_COMMAND_PROPERTY`,`SW_SYS_COMMAND_EXP`,`SW_SYS_COMMAND_Index`) "
                            + "VALUES (?,?,?,?,?,?)",
                    setValueCommandId,
                    triggerId,
                    CommandsType.SET_VALUE.code(),
                    namePropertyId,
                    "$triggered",
                    2);
            Model model = modelDataService.getModel(modelName);
            DbMysqlDynamic data = new DbMysqlDynamic(model);
            data.set("orderId", "2001");
            data.set("orderName", "blocked");

            IllegalStateException error = assertThrows(IllegalStateException.class, () -> modelDataService.saveData(data));

            assertEquals("filter blocked", error.getMessage());
            String name = jdbcTemplate.queryForObject(
                    "SELECT `ORDER_NAME` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    String.class,
                    "2001");
            assertEquals("blocked", name);
        } finally {
            cleanupRuntimeTriggerModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void saveDataExecutesLegacyPropertySetTriggerSetValue() {
        long modelId = 92701L;
        long idPropertyId = 92702L;
        long namePropertyId = 92703L;
        long triggerId = 92704L;
        long commandId = 92705L;
        String modelName = "RuntimePropertyTriggerOrder";
        String tableName = "runtime_property_trigger_order";
        cleanupRuntimePropertyTriggerModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "2001",
                    "before trigger");
            jdbcTemplate.update(
                    "INSERT INTO `SW_SYS_PROPERTY_TRIGGER` "
                            + "(`SysId`,`SW_SYS_PROPERTY_TriggersSysId`,`SW_PROPERTY_TRIGGER_TYPE`,"
                            + "`SW_PROPERTY_TRIGGER_NAME`,`SW_PROPERTY_TRIGGER_PROPERTY`,"
                            + "`SW_PROPERTY_TRIGGER_BASETYPE`) VALUES (?,?,?,?,?,?)",
                    triggerId,
                    namePropertyId,
                    PropertyTriggerType.SET.code(),
                    "set orderName",
                    namePropertyId,
                    OperationBaseType.NULL.code());
            jdbcTemplate.update(
                    "INSERT INTO `SW_SYS_PROPERTY_TRIGGER_COMMANDS` "
                            + "(`SysId`,`SW_SYS_PROPERTY_TRIGGER_CommandsSysId`,`SW_SYS_COMMAND_TYPE`,"
                            + "`SW_SYS_COMMAND_PROPERTY`,`SW_SYS_COMMAND_EXP`,`SW_SYS_COMMAND_INDEX`) "
                            + "VALUES (?,?,?,?,?,?)",
                    commandId,
                    triggerId,
                    CommandsType.SET_VALUE.code(),
                    namePropertyId,
                    "$property-triggered",
                    1);

            Model model = modelDataService.getModel(modelName);
            Property nameProperty = model.getProperties().stream()
                    .filter(property -> "orderName".equals(property.getName()))
                    .findFirst()
                    .orElseThrow();
            assertEquals(1, nameProperty.getTriggerList().size());
            PropertyTrigger trigger = nameProperty.getTriggerList().get(0);
            assertEquals(Long.valueOf(triggerId), trigger.getId());
            assertEquals(PropertyTriggerType.SET, trigger.getTriggerType());
            assertEquals(1, trigger.getCommands().size());

            IDynamicData data = modelDataService.getOneData(modelName, "2001");
            data.set("orderName", "manual save");

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            String name = jdbcTemplate.queryForObject(
                    "SELECT `ORDER_NAME` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    String.class,
                    "2001");
            assertEquals("property-triggered", name);
        } finally {
            cleanupRuntimePropertyTriggerModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void saveDataExecutesLegacyTriggerPropertyAndListMethods() {
        String tableName = "runtime_trigger_method_order";
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + tableName + "`");
        try {
            jdbcTemplate.execute("CREATE TABLE `" + tableName + "` ("
                    + "`ORDER_ID` varchar(64) NOT NULL,"
                    + "`ORDER_NAME` varchar(255) DEFAULT NULL,"
                    + "PRIMARY KEY (`ORDER_ID`))");
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "2001",
                    "before trigger");
            Property orderId = columnProperty("orderId", "ORDER_ID", PropertyType.String);
            Property orderName = columnProperty("orderName", "ORDER_NAME", PropertyType.String);
            Property child = simpleProperty("child", PropertyType.BusinessObject);
            child.setId(92713L);
            Property items = collectionProperty("items");
            items.setId(92714L);
            Property children = collectionProperty("children");
            children.setId(92715L);
            Model model = new Model();
            model.setTableName(tableName);
            model.setIdProperty(orderId);
            model.setProperties(List.of(orderId, orderName, child, items, children));
            Trigger trigger = new Trigger();
            trigger.setTriggerType(ModelTriggerType.SAVE);
            trigger.setBaseOperationType(OperationBaseType.NULL);
            trigger.setCommands(List.of(
                    triggerCommand(CommandsType.EXUTE_PROPRTY_MODEL_METHOD, child.getId(), "Close", 1),
                    triggerCommand(CommandsType.EXUTE_PROPRTY_MODEL_METHOD, children.getId(), "Close", 2),
                    triggerCommand(CommandsType.EXUTE_LIST_METHOD, items.getId(), "CloseAll", 3)));
            model.setTriggers(List.of(trigger));
            RecordingDynamic childData = new RecordingDynamic();
            RecordingDynamic childItem = new RecordingDynamic();
            RecordingList itemList = new RecordingList();
            DbMysqlDynamic data = new DbMysqlDynamic(model);
            data.set("orderId", "2001");
            data.set("orderName", "manual save");
            data.set("child", childData);
            data.set("items", itemList);
            data.set("children", List.of(childItem));

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            assertEquals("Close", childData.methodName);
            assertEquals("Close", childItem.methodName);
            assertEquals(true, itemList.closed);
        } finally {
            jdbcTemplate.execute("DROP TABLE IF EXISTS `" + tableName + "`");
        }
    }

    @Test
    public void saveDataExecutesLegacyModelTriggerAssembly() {
        String tableName = "runtime_trigger_assembly_order";
        RecordingAssemblyHandler.reset();
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + tableName + "`");
        try {
            jdbcTemplate.execute("CREATE TABLE `" + tableName + "` ("
                    + "`ORDER_ID` varchar(64) NOT NULL,"
                    + "`ORDER_NAME` varchar(255) DEFAULT NULL,"
                    + "PRIMARY KEY (`ORDER_ID`))");
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "2001",
                    "before trigger");
            Property orderId = columnProperty("orderId", "ORDER_ID", PropertyType.String);
            Property orderName = columnProperty("orderName", "ORDER_NAME", PropertyType.String);
            orderName.setId(92723L);
            Model model = new Model();
            model.setTableName(tableName);
            model.setIdProperty(orderId);
            model.setProperties(List.of(orderId, orderName));
            Trigger trigger = new Trigger();
            trigger.setTriggerType(ModelTriggerType.SAVE);
            trigger.setBaseOperationType(OperationBaseType.ASSEBMLY);
            trigger.setInvokeClass(RecordingAssemblyHandler.class.getName());
            trigger.setInvokeMethod("Run");
            trigger.setCommands(List.of(
                    triggerCommand(CommandsType.SET_CON_STR_VALUE, orderName.getId(), "$ctor", 1),
                    triggerCommand(CommandsType.SET_PARAM_VALUE, orderName.getId(), ".orderName", 2)));
            model.setTriggers(List.of(trigger));
            DbMysqlDynamic data = new DbMysqlDynamic(model);
            data.set("orderId", "2001");
            data.set("orderName", "manual save");

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            assertEquals("ctor", RecordingAssemblyHandler.constructorValue);
            assertSame(data, RecordingAssemblyHandler.receivedData);
            assertEquals("manual save", RecordingAssemblyHandler.receivedName);
        } finally {
            jdbcTemplate.execute("DROP TABLE IF EXISTS `" + tableName + "`");
            RecordingAssemblyHandler.reset();
        }
    }

    @Test
    public void saveDataExecutesLegacyPropertySetTriggerAssembly() {
        long modelId = 92731L;
        long idPropertyId = 92732L;
        long namePropertyId = 92733L;
        long triggerId = 92734L;
        long constructorCommandId = 92735L;
        long paramCommandId = 92736L;
        String modelName = "RuntimePropertyTriggerAssemblyOrder";
        String tableName = "runtime_property_trigger_assembly_order";
        RecordingAssemblyHandler.reset();
        cleanupRuntimePropertyTriggerModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "2001",
                    "before trigger");
            jdbcTemplate.update(
                    "INSERT INTO `SW_SYS_PROPERTY_TRIGGER` "
                            + "(`SysId`,`SW_SYS_PROPERTY_TriggersSysId`,`SW_PROPERTY_TRIGGER_TYPE`,"
                            + "`SW_PROPERTY_TRIGGER_NAME`,`SW_PROPERTY_TRIGGER_PROPERTY`,"
                            + "`SW_PROPERTY_TRIGGER_BASETYPE`,`SW_MODEL_TRIGGER_INVOKECLASS`,"
                            + "`SW_MODEL_TRIGGER_INVOKEMETHOD`) VALUES (?,?,?,?,?,?,?,?)",
                    triggerId,
                    namePropertyId,
                    PropertyTriggerType.SET.code(),
                    "set orderName assembly",
                    namePropertyId,
                    OperationBaseType.ASSEBMLY.code(),
                    RecordingAssemblyHandler.class.getName(),
                    "Run");
            jdbcTemplate.update(
                    "INSERT INTO `SW_SYS_PROPERTY_TRIGGER_COMMANDS` "
                            + "(`SysId`,`SW_SYS_PROPERTY_TRIGGER_CommandsSysId`,`SW_SYS_COMMAND_TYPE`,"
                            + "`SW_SYS_COMMAND_PROPERTY`,`SW_SYS_COMMAND_EXP`,`SW_SYS_COMMAND_INDEX`) "
                            + "VALUES (?,?,?,?,?,?)",
                    constructorCommandId,
                    triggerId,
                    CommandsType.SET_CON_STR_VALUE.code(),
                    namePropertyId,
                    "$ctor",
                    1);
            jdbcTemplate.update(
                    "INSERT INTO `SW_SYS_PROPERTY_TRIGGER_COMMANDS` "
                            + "(`SysId`,`SW_SYS_PROPERTY_TRIGGER_CommandsSysId`,`SW_SYS_COMMAND_TYPE`,"
                            + "`SW_SYS_COMMAND_PROPERTY`,`SW_SYS_COMMAND_EXP`,`SW_SYS_COMMAND_INDEX`) "
                            + "VALUES (?,?,?,?,?,?)",
                    paramCommandId,
                    triggerId,
                    CommandsType.SET_PARAM_VALUE.code(),
                    namePropertyId,
                    ".orderName",
                    2);
            IDynamicData data = modelDataService.getOneData(modelName, "2001");
            data.set("orderName", "manual save");

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            assertEquals("ctor", RecordingAssemblyHandler.constructorValue);
            assertSame(data, RecordingAssemblyHandler.receivedData);
            assertEquals("manual save", RecordingAssemblyHandler.receivedName);
        } finally {
            cleanupRuntimePropertyTriggerModel(modelId, modelName, tableName);
            RecordingAssemblyHandler.reset();
        }
    }

    @Test
    public void createDataInsertsLegacySimpleDynamicRow() {
        long modelId = 93001L;
        long idPropertyId = 93002L;
        long namePropertyId = 93003L;
        String modelName = "RuntimeCreateOrder";
        String tableName = "runtime_create_order";
        cleanupRuntimeDetailModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            Model model = modelDataService.getModel(modelName);
            DbMysqlDynamic data = new DbMysqlDynamic(model);
            data.set("orderId", "2001");
            data.set("orderName", "Created detail");

            assertEquals(Boolean.TRUE, modelDataService.createData(data));

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + tableName + "` WHERE `ORDER_ID` = ? AND `ORDER_NAME` = ?",
                    Integer.class,
                    "2001",
                    "Created detail");
            assertEquals(1, count.intValue());
        } finally {
            cleanupRuntimeDetailModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void deleteDataDeletesLegacySimpleDynamicRowById() {
        long modelId = 94001L;
        long idPropertyId = 94002L;
        long namePropertyId = 94003L;
        String modelName = "RuntimeDeleteOrder";
        String tableName = "runtime_delete_order";
        cleanupRuntimeDetailModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "3001",
                    "Delete detail");
            Model model = modelDataService.getModel(modelName);
            DbMysqlDynamic data = new DbMysqlDynamic(model);
            data.set("orderId", "3001");

            assertEquals(Boolean.TRUE, modelDataService.deleteData(data));

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    Integer.class,
                    "3001");
            assertEquals(0, count.intValue());
        } finally {
            cleanupRuntimeDetailModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void saveDataUpdatesLegacySimpleDynamicRowById() {
        long modelId = 95001L;
        long idPropertyId = 95002L;
        long namePropertyId = 95003L;
        String modelName = "RuntimeSaveOrder";
        String tableName = "runtime_save_order";
        cleanupRuntimeDetailModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "4001",
                    "Before save");
            Model model = modelDataService.getModel(modelName);
            DbMysqlDynamic data = new DbMysqlDynamic(model);
            data.set("orderId", "4001");
            data.set("orderName", "After save");

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            String name = jdbcTemplate.queryForObject(
                    "SELECT `ORDER_NAME` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    String.class,
                    "4001");
            assertEquals("After save", name);
        } finally {
            cleanupRuntimeDetailModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void saveDataCoercesLegacyBooleanStringByPropertyType() {
        long modelId = 95041L;
        long idPropertyId = 95042L;
        long namePropertyId = 95043L;
        long activePropertyId = 95044L;
        String modelName = "RuntimeSaveBooleanOrder";
        String tableName = "runtime_save_boolean_order";
        cleanupRuntimeDetailModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.execute("ALTER TABLE `" + tableName + "` ADD `ACTIVE` BIT DEFAULT NULL");
            insertRuntimeProperty(activePropertyId, "active", null, false, modelId, "ACTIVE", PropertyType.Boolean);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`,`ACTIVE`) VALUES (?,?,?)",
                    "4051",
                    "Before boolean save",
                    true);
            Model model = modelDataService.getModel(modelName);
            DbMysqlDynamic data = new DbMysqlDynamic(model);
            data.set("orderId", "4051");
            data.set("orderName", "After boolean save");
            data.set("active", "false");

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            Boolean active = jdbcTemplate.queryForObject(
                    "SELECT `ACTIVE` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    Boolean.class,
                    "4051");
            assertEquals(Boolean.FALSE, active);
        } finally {
            cleanupRuntimeDetailModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void saveDataUsesLegacyOldIdWhenIdPropertyChanged() {
        long modelId = 95101L;
        long idPropertyId = 95102L;
        long namePropertyId = 95103L;
        String modelName = "RuntimeSaveOldIdOrder";
        String tableName = "runtime_save_old_id_order";
        cleanupRuntimeDetailModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "4101",
                    "Before old id save");
            Model model = modelDataService.getModel(modelName);
            DbMysqlDynamic data = new DbMysqlDynamic(model);
            data.set("orderId", "4101");
            data.set("orderId", "4102");
            data.set("orderName", "After old id save");

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            String name = jdbcTemplate.queryForObject(
                    "SELECT `ORDER_NAME` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    String.class,
                    "4101");
            Integer changedIdCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    Integer.class,
                    "4102");
            assertEquals("After old id save", name);
            assertEquals(0, changedIdCount.intValue());
        } finally {
            cleanupRuntimeDetailModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void saveDataListUpdatesLegacySimpleDynamicRows() {
        long modelId = 96001L;
        long idPropertyId = 96002L;
        long namePropertyId = 96003L;
        String modelName = "RuntimeBatchSaveOrder";
        String tableName = "runtime_batch_save_order";
        cleanupRuntimeDetailModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "5001",
                    "Before save 1");
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "5002",
                    "Before save 2");
            Model model = modelDataService.getModel(modelName);
            DbMysqlDynamic first = new DbMysqlDynamic(model);
            first.set("orderId", "5001");
            first.set("orderName", "After save 1");
            DbMysqlDynamic second = new DbMysqlDynamic(model);
            second.set("orderId", "5002");
            second.set("orderName", "After save 2");

            assertEquals(Boolean.TRUE, modelDataService.saveDataList(List.of(first, second)));

            List<String> names = jdbcTemplate.queryForList(
                    "SELECT `ORDER_NAME` FROM `" + tableName + "` ORDER BY `ORDER_ID`",
                    String.class);
            assertEquals(List.of("After save 1", "After save 2"), names);
        } finally {
            cleanupRuntimeDetailModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void createDataWritesLegacyMultiDbMapColumns() {
        String tableName = "runtime_create_dbmaps_order";
        cleanupRuntimeDbMapsTable(tableName);
        try {
            createRuntimeDbMapsTable(tableName);
            Model order = dbMapsOrderModel(tableName);
            DbMysqlDynamic data = dbMapsOrderData(order, "7001", 42L, "Ada");

            assertEquals(Boolean.TRUE, modelDataService.createData(data));

            Long customerId = jdbcTemplate.queryForObject(
                    "SELECT `CUSTOMER_ID` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    Long.class,
                    "7001");
            String customerName = jdbcTemplate.queryForObject(
                    "SELECT `CUSTOMER_NAME` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    String.class,
                    "7001");
            assertEquals(Long.valueOf(42L), customerId);
            assertEquals("Ada", customerName);
        } finally {
            cleanupRuntimeDbMapsTable(tableName);
        }
    }

    @Test
    public void saveDataUpdatesLegacyMultiDbMapColumnsById() {
        String tableName = "runtime_save_dbmaps_order";
        cleanupRuntimeDbMapsTable(tableName);
        try {
            createRuntimeDbMapsTable(tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`CUSTOMER_ID`,`CUSTOMER_NAME`) VALUES (?,?,?)",
                    "8001",
                    1L,
                    "Before save");
            Model order = dbMapsOrderModel(tableName);
            DbMysqlDynamic data = dbMapsOrderData(order, "8001", 42L, "Ada");

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            Long customerId = jdbcTemplate.queryForObject(
                    "SELECT `CUSTOMER_ID` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    Long.class,
                    "8001");
            String customerName = jdbcTemplate.queryForObject(
                    "SELECT `CUSTOMER_NAME` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    String.class,
                    "8001");
            assertEquals(Long.valueOf(42L), customerId);
            assertEquals("Ada", customerName);
        } finally {
            cleanupRuntimeDbMapsTable(tableName);
        }
    }

    @Test
    public void createDataWritesLegacyManyToManyRelationRows() {
        String orderTable = "runtime_create_relation_order";
        String roleTable = "runtime_create_relation_role";
        String relationTable = "runtime_create_order_role";
        cleanupRuntimeManyToManyTables(orderTable, roleTable, relationTable);
        try {
            createRuntimeManyToManyTables(orderTable, roleTable, relationTable);
            jdbcTemplate.update("INSERT INTO `" + roleTable + "` (`ROLE_ID`,`ROLE_NAME`) VALUES (?,?)", "R1", "Admin");
            Model order = manyToManyOrderModel(orderTable, roleTable, relationTable);
            DbMysqlDynamic data = manyToManyOrderData(order, "9001", "Created relation", "R1", "Admin");

            assertEquals(Boolean.TRUE, modelDataService.createData(data));

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + relationTable + "` WHERE `ROLE_ID` = ? AND `ORDER_ID` = ?",
                    Integer.class,
                    "R1",
                    "9001");
            assertEquals(1, count.intValue());
        } finally {
            cleanupRuntimeManyToManyTables(orderTable, roleTable, relationTable);
        }
    }

    @Test
    public void saveDataWritesLegacyManyToManyRelationRows() {
        String orderTable = "runtime_save_relation_order";
        String roleTable = "runtime_save_relation_role";
        String relationTable = "runtime_save_order_role";
        cleanupRuntimeManyToManyTables(orderTable, roleTable, relationTable);
        try {
            createRuntimeManyToManyTables(orderTable, roleTable, relationTable);
            jdbcTemplate.update("INSERT INTO `" + orderTable + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)", "9101", "Before save");
            jdbcTemplate.update("INSERT INTO `" + roleTable + "` (`ROLE_ID`,`ROLE_NAME`) VALUES (?,?)", "R2", "Editor");
            Model order = manyToManyOrderModel(orderTable, roleTable, relationTable);
            DbMysqlDynamic data = manyToManyOrderData(order, "9101", "After save", "R2", "Editor");

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + relationTable + "` WHERE `ROLE_ID` = ? AND `ORDER_ID` = ?",
                    Integer.class,
                    "R2",
                    "9101");
            assertEquals(1, count.intValue());
        } finally {
            cleanupRuntimeManyToManyTables(orderTable, roleTable, relationTable);
        }
    }

    @Test
    public void saveDataDeletesLegacyManyToManyRelationRows() {
        String orderTable = "runtime_delete_relation_order";
        String roleTable = "runtime_delete_relation_role";
        String relationTable = "runtime_delete_order_role";
        cleanupRuntimeManyToManyTables(orderTable, roleTable, relationTable);
        try {
            createRuntimeManyToManyTables(orderTable, roleTable, relationTable);
            jdbcTemplate.update("INSERT INTO `" + orderTable + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)", "9401", "Before save");
            jdbcTemplate.update("INSERT INTO `" + roleTable + "` (`ROLE_ID`,`ROLE_NAME`) VALUES (?,?)", "R3", "Remove");
            jdbcTemplate.update("INSERT INTO `" + relationTable + "` (`ROLE_ID`,`ORDER_ID`) VALUES (?,?)", "R3", "9401");
            Model order = manyToManyOrderModel(orderTable, roleTable, relationTable);
            SubItemList<IDynamicData> roles = new SubItemList<>();
            IDynamicData removed = manyToManyRoleData(order, "R3", "Remove");
            roles.add(removed);
            roles.remove(removed);
            DbMysqlDynamic data = manyToManyOrderData(order, "9401", "After save", roles);

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + relationTable + "` WHERE `ROLE_ID` = ? AND `ORDER_ID` = ?",
                    Integer.class,
                    "R3",
                    "9401");
            assertEquals(0, count.intValue());
        } finally {
            cleanupRuntimeManyToManyTables(orderTable, roleTable, relationTable);
        }
    }

    @Test
    public void createDataWritesLegacyRecurveRelationRows() {
        String nodeTable = "runtime_create_recurve_node";
        String relationTable = "runtime_create_recurve_node_rel";
        cleanupRuntimeRecurveTables(nodeTable, relationTable);
        try {
            createRuntimeRecurveTables(nodeTable, relationTable);
            jdbcTemplate.update("INSERT INTO `" + nodeTable + "` (`NODE_ID`,`NODE_NAME`) VALUES (?,?)", "C1", "Child");
            Model node = recurveNodeModel(nodeTable, relationTable);
            DbMysqlDynamic data = recurveNodeData(node, "P1", "Parent", "C1", "Child");

            assertEquals(Boolean.TRUE, modelDataService.createData(data));

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + relationTable + "` WHERE `PARENT_ID` = ? AND `CHILD_ID` = ?",
                    Integer.class,
                    "P1",
                    "C1");
            assertEquals(1, count.intValue());
        } finally {
            cleanupRuntimeRecurveTables(nodeTable, relationTable);
        }
    }

    @Test
    public void saveDataDeletesLegacyRecurveRelationRows() {
        String nodeTable = "runtime_delete_recurve_node";
        String relationTable = "runtime_delete_recurve_node_rel";
        cleanupRuntimeRecurveTables(nodeTable, relationTable);
        try {
            createRuntimeRecurveTables(nodeTable, relationTable);
            jdbcTemplate.update("INSERT INTO `" + nodeTable + "` (`NODE_ID`,`NODE_NAME`) VALUES (?,?)", "P2", "Parent");
            jdbcTemplate.update("INSERT INTO `" + nodeTable + "` (`NODE_ID`,`NODE_NAME`) VALUES (?,?)", "C2", "Child");
            jdbcTemplate.update("INSERT INTO `" + relationTable + "` (`PARENT_ID`,`CHILD_ID`) VALUES (?,?)", "P2", "C2");
            Model node = recurveNodeModel(nodeTable, relationTable);
            SubItemList<IDynamicData> children = new SubItemList<>();
            IDynamicData removed = recurveChildData(node, "C2", "Child");
            children.add(removed);
            children.remove(removed);
            DbMysqlDynamic data = recurveNodeData(node, "P2", "After save", children);

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + relationTable + "` WHERE `PARENT_ID` = ? AND `CHILD_ID` = ?",
                    Integer.class,
                    "P2",
                    "C2");
            assertEquals(0, count.intValue());
        } finally {
            cleanupRuntimeRecurveTables(nodeTable, relationTable);
        }
    }

    @Test
    public void createDataWritesLegacyOneToManyChildRows() {
        String orderTable = "runtime_create_child_order";
        String itemTable = "runtime_create_child_order_item";
        cleanupRuntimeOneToManyTables(orderTable, itemTable);
        try {
            createRuntimeOneToManyTables(orderTable, itemTable);
            Model order = oneToManyOrderModel(orderTable, itemTable);
            DbMysqlDynamic data = oneToManyOrderData(order, "9201", "Created parent",
                    List.of(orderItemData(order, "I1", "Created child")));

            assertEquals(Boolean.TRUE, modelDataService.createData(data));

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + itemTable + "` WHERE `ITEM_ID` = ? AND `ITEM_NAME` = ? AND `ORDER_ID` = ?",
                    Integer.class,
                    "I1",
                    "Created child",
                    "9201");
            assertEquals(1, count.intValue());
        } finally {
            cleanupRuntimeOneToManyTables(orderTable, itemTable);
        }
    }

    @Test
    public void saveDataSyncsLegacyOneToManyChildRows() {
        String orderTable = "runtime_save_child_order";
        String itemTable = "runtime_save_child_order_item";
        cleanupRuntimeOneToManyTables(orderTable, itemTable);
        try {
            createRuntimeOneToManyTables(orderTable, itemTable);
            jdbcTemplate.update("INSERT INTO `" + orderTable + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)", "9301", "Before save");
            jdbcTemplate.update("INSERT INTO `" + itemTable + "` (`ITEM_ID`,`ITEM_NAME`,`ORDER_ID`) VALUES (?,?,?)", "I2", "Before child", "9301");
            jdbcTemplate.update("INSERT INTO `" + itemTable + "` (`ITEM_ID`,`ITEM_NAME`,`ORDER_ID`) VALUES (?,?,?)", "I4", "Remove child", "9301");
            Model order = oneToManyOrderModel(orderTable, itemTable);
            SubItemList<IDynamicData> items = new SubItemList<>();
            items.add(orderItemData(order, "I2", "After child"));
            items.add(orderItemData(order, "I3", "New child"));
            IDynamicData removed = orderItemData(order, "I4", "Remove child");
            items.add(removed);
            items.remove(removed);
            DbMysqlDynamic data = oneToManyOrderData(order, "9301", "After save", items);

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            List<String> rows = jdbcTemplate.queryForList(
                    "SELECT CONCAT(`ITEM_ID`, ':', `ITEM_NAME`, ':', `ORDER_ID`) FROM `" + itemTable + "` ORDER BY `ITEM_ID`",
                    String.class);
            assertEquals(List.of("I2:After child:9301", "I3:New child:9301"), rows);
        } finally {
            cleanupRuntimeOneToManyTables(orderTable, itemTable);
        }
    }

    @Test
    public void saveDataExecutesLegacyCollectionItemTriggers() {
        String orderTable = "runtime_collection_trigger_order";
        String itemTable = "runtime_collection_trigger_order_item";
        cleanupRuntimeOneToManyTables(orderTable, itemTable);
        try {
            createRuntimeOneToManyTables(orderTable, itemTable);
            jdbcTemplate.update("INSERT INTO `" + orderTable + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)", "9351", "Before save");
            jdbcTemplate.update("INSERT INTO `" + itemTable + "` (`ITEM_ID`,`ITEM_NAME`,`ORDER_ID`) VALUES (?,?,?)", "D1", "Before delete", "9351");
            Model order = oneToManyOrderModel(orderTable, itemTable);
            Property items = order.getProperties().get(2);
            Property itemName = items.getPropertyModel().getProperties().get(1);
            itemName.setId(93503L);
            items.setTriggerList(List.of(
                    propertyTrigger(PropertyTriggerType.ITEMS_ADD, itemName.getId(), "#.orderName"),
                    propertyTrigger(PropertyTriggerType.ITEMS_DELETE, itemName.getId(), "#.orderName")));

            SubItemList<IDynamicData> itemList = new SubItemList<>();
            itemList.add(orderItemData(order, "A1", "Before add"));
            IDynamicData removed = orderItemData(order, "D1", "Before delete");
            itemList.add(removed);
            itemList.remove(removed);
            DbMysqlDynamic data = oneToManyOrderData(order, "9351", "After save", itemList);

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            String addedName = jdbcTemplate.queryForObject(
                    "SELECT `ITEM_NAME` FROM `" + itemTable + "` WHERE `ITEM_ID` = ?",
                    String.class,
                    "A1");
            Integer deletedCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + itemTable + "` WHERE `ITEM_ID` = ?",
                    Integer.class,
                    "D1");
            assertEquals("After save", addedName);
            assertEquals("After save", removed.get("itemName"));
            assertEquals(0, deletedCount.intValue());
        } finally {
            cleanupRuntimeOneToManyTables(orderTable, itemTable);
        }
    }

    @Test
    public void initDataBuildsLegacySimpleDynamicDefaults() {
        Model model = new Model();
        model.setProperties(List.of(
                simpleProperty("active", PropertyType.Boolean),
                simpleProperty("count", PropertyType.Int),
                simpleProperty("total", PropertyType.Long),
                simpleProperty("code", PropertyType.String)));

        IDynamicData data = modelDataService.initData(model);

        assertNotNull(data);
        assertEquals(false, data.get("active"));
        assertEquals(0, data.get("count"));
        assertEquals(0L, data.get("total"));
        assertEquals("", data.get("code"));
    }

    @Test
    public void initDataBuildsLegacyCollectionDefaults() {
        Model model = new Model();
        model.setProperties(List.of(collectionProperty("items")));

        IDynamicData data = modelDataService.initData(model);

        assertNotNull(data);
        assertEquals(List.of(), data.get("items"));
    }

    private Model dbMapsOrderModel(String tableName) {
        Model customer = new Model();
        customer.setProperties(List.of(
                simpleProperty("customerId", PropertyType.Long),
                simpleProperty("displayName", PropertyType.String)));

        Model order = new Model();
        order.setTableName(tableName);
        Property orderId = columnProperty("orderId", "ORDER_ID", PropertyType.String);
        order.setIdProperty(orderId);
        Property customerSnapshot = simpleProperty("customer", PropertyType.BusinessObject);
        customerSnapshot.setPropertyModel(customer);
        customerSnapshot.setMultiMap(true);
        customerSnapshot.setDbMaps(List.of(
                new MultiDbMap("customerId", "CUSTOMER_ID"),
                new MultiDbMap("displayName", "CUSTOMER_NAME")));
        order.setProperties(List.of(orderId, customerSnapshot));
        return order;
    }

    private DbMysqlDynamic dbMapsOrderData(Model order, String orderId, Long customerId, String customerName) {
        DbMysqlDynamic customerData = new DbMysqlDynamic(order.getProperties().get(1).getPropertyModel());
        customerData.set("customerId", customerId);
        customerData.set("displayName", customerName);
        DbMysqlDynamic orderData = new DbMysqlDynamic(order);
        orderData.set("orderId", orderId);
        orderData.set("customer", customerData);
        return orderData;
    }

    private Model manyToManyOrderModel(String orderTable, String roleTable, String relationTable) {
        Model role = new Model();
        role.setTableName(roleTable);
        Property roleId = columnProperty("roleId", "ROLE_ID", PropertyType.String);
        role.setIdProperty(roleId);
        role.setProperties(List.of(roleId, columnProperty("roleName", "ROLE_NAME", PropertyType.String)));

        Model order = new Model();
        order.setTableName(orderTable);
        Property orderId = columnProperty("orderId", "ORDER_ID", PropertyType.String);
        order.setIdProperty(orderId);
        Property orderName = columnProperty("orderName", "ORDER_NAME", PropertyType.String);
        Property roles = collectionProperty("roles");
        roles.setPropertyModel(role);
        Relation relation = new Relation();
        relation.setProperty(roles);
        relation.setTargetProperty(roles);
        relation.setRelationType(RelationType.Many2Many);
        relation.setRelationTable(relationTable);
        relation.setPropertyColumn("ROLE_ID");
        relation.setTargetColumn("ORDER_ID");
        order.setProperties(List.of(orderId, orderName, roles));
        order.setRelations(List.of(relation));
        return order;
    }

    private DbMysqlDynamic manyToManyOrderData(
            Model order,
            String orderId,
            String orderName,
            String roleId,
            String roleName) {
        DbMysqlDynamic role = new DbMysqlDynamic(order.getProperties().get(2).getPropertyModel());
        role.set("roleId", roleId);
        role.set("roleName", roleName);
        DbMysqlDynamic data = new DbMysqlDynamic(order);
        data.set("orderId", orderId);
        data.set("orderName", orderName);
        data.set("roles", List.of(role));
        return data;
    }

    private DbMysqlDynamic manyToManyOrderData(
            Model order,
            String orderId,
            String orderName,
            List<? extends IDynamicData> roles) {
        DbMysqlDynamic data = new DbMysqlDynamic(order);
        data.set("orderId", orderId);
        data.set("orderName", orderName);
        data.set("roles", roles);
        return data;
    }

    private DbMysqlDynamic manyToManyRoleData(Model order, String roleId, String roleName) {
        DbMysqlDynamic role = new DbMysqlDynamic(order.getProperties().get(2).getPropertyModel());
        role.set("roleId", roleId);
        role.set("roleName", roleName);
        return role;
    }

    private Model recurveNodeModel(String nodeTable, String relationTable) {
        Model node = new Model();
        node.setTableName(nodeTable);
        Property nodeId = columnProperty("nodeId", "NODE_ID", PropertyType.String);
        node.setIdProperty(nodeId);
        Property nodeName = columnProperty("nodeName", "NODE_NAME", PropertyType.String);
        Property children = collectionProperty("children");
        children.setPropertyModel(node);
        Relation relation = new Relation();
        relation.setProperty(children);
        relation.setTargetProperty(children);
        relation.setRelationType(RelationType.Recurve);
        relation.setRelationTable(relationTable);
        relation.setPropertyColumn("PARENT_ID");
        relation.setTargetColumn("CHILD_ID");
        node.setProperties(List.of(nodeId, nodeName, children));
        node.setRelations(List.of(relation));
        return node;
    }

    private DbMysqlDynamic recurveNodeData(
            Model node,
            String parentId,
            String parentName,
            String childId,
            String childName) {
        DbMysqlDynamic child = new DbMysqlDynamic(node);
        child.set("nodeId", childId);
        child.set("nodeName", childName);
        DbMysqlDynamic data = new DbMysqlDynamic(node);
        data.set("nodeId", parentId);
        data.set("nodeName", parentName);
        data.set("children", List.of(child));
        return data;
    }

    private DbMysqlDynamic recurveNodeData(
            Model node,
            String parentId,
            String parentName,
            List<? extends IDynamicData> children) {
        DbMysqlDynamic data = new DbMysqlDynamic(node);
        data.set("nodeId", parentId);
        data.set("nodeName", parentName);
        data.set("children", children);
        return data;
    }

    private DbMysqlDynamic recurveChildData(Model node, String childId, String childName) {
        DbMysqlDynamic child = new DbMysqlDynamic(node);
        child.set("nodeId", childId);
        child.set("nodeName", childName);
        return child;
    }

    private Model oneToManyOrderModel(String orderTable, String itemTable) {
        Model item = new Model();
        item.setTableName(itemTable);
        Property itemId = columnProperty("itemId", "ITEM_ID", PropertyType.String);
        item.setIdProperty(itemId);
        item.setProperties(List.of(itemId, columnProperty("itemName", "ITEM_NAME", PropertyType.String)));

        Model order = new Model();
        order.setTableName(orderTable);
        Property orderId = columnProperty("orderId", "ORDER_ID", PropertyType.String);
        order.setIdProperty(orderId);
        Property orderName = columnProperty("orderName", "ORDER_NAME", PropertyType.String);
        Property items = collectionProperty("items");
        items.setPropertyModel(item);
        Relation relation = new Relation();
        relation.setProperty(items);
        relation.setTargetProperty(items);
        relation.setRelationType(RelationType.One2Many);
        relation.setTargetColumn("ORDER_ID");
        order.setProperties(List.of(orderId, orderName, items));
        order.setRelations(List.of(relation));
        return order;
    }

    private DbMysqlDynamic oneToManyOrderData(Model order, String orderId, String orderName, List<? extends IDynamicData> items) {
        DbMysqlDynamic data = new DbMysqlDynamic(order);
        data.set("orderId", orderId);
        data.set("orderName", orderName);
        data.set("items", items);
        return data;
    }

    private DbMysqlDynamic orderItemData(Model order, String itemId, String itemName) {
        DbMysqlDynamic item = new DbMysqlDynamic(order.getProperties().get(2).getPropertyModel());
        item.set("itemId", itemId);
        item.set("itemName", itemName);
        return item;
    }

    private PropertyTrigger propertyTrigger(PropertyTriggerType type, Long propertyId, String expression) {
        PropertyTrigger trigger = new PropertyTrigger();
        trigger.setTriggerType(type);
        trigger.setCommands(List.of(triggerCommand(CommandsType.SET_VALUE, propertyId, expression, 1)));
        return trigger;
    }

    private OperationCommand triggerCommand(CommandsType type, Long propertyId, String expression, int index) {
        OperationCommand command = new OperationCommand();
        command.setCommandType(type);
        command.setPropertyId(propertyId);
        command.setExpression(expression);
        command.setIndex(index);
        return command;
    }

    private Property simpleProperty(String name, PropertyType type) {
        Property property = new Property();
        property.setName(name);
        property.setPropertyType(type);
        property.setIsCollection(false);
        property.setMultiMap(false);
        return property;
    }

    private Property columnProperty(String name, String column, PropertyType type) {
        Property property = simpleProperty(name, type);
        property.setColumn(column);
        return property;
    }

    private Property collectionProperty(String name) {
        Property property = simpleProperty(name, PropertyType.BusinessObject);
        property.setIsCollection(true);
        return property;
    }

    private static class RecordingDynamic extends DbMysqlDynamic {
        private String methodName;

        RecordingDynamic() {
            super(new Model());
        }

        @Override
        public void invoke(String methodName, Object... args) {
            this.methodName = methodName;
        }
    }

    private static class RecordingList extends java.util.ArrayList<IDynamicData> {
        private boolean closed;

        public void CloseAll() {
            closed = true;
        }
    }

    public static class RecordingAssemblyHandler {
        private static String constructorValue;
        private static IDynamicData receivedData;
        private static String receivedName;

        public RecordingAssemblyHandler(String constructorValue) {
            RecordingAssemblyHandler.constructorValue = constructorValue;
        }

        public void Run(IDynamicData data, String name) {
            receivedData = data;
            receivedName = name;
        }

        private static void reset() {
            constructorValue = null;
            receivedData = null;
            receivedName = null;
        }
    }

    private void cleanupRuntimeEnumModel(long modelId, String modelName) {
        jdbcTemplate.update("DELETE FROM `fool_sys_model_enum` WHERE `owner` = ?", modelId);
        jdbcTemplate.update("DELETE FROM `fool_sys_model` WHERE `id` = ? OR `name` = ?", modelId, modelName);
    }

    private void createRuntimeDetailModel(
            long modelId,
            long idPropertyId,
            long namePropertyId,
            String modelName,
            String tableName) {
        jdbcTemplate.execute("CREATE TABLE `" + tableName + "` ("
                + "`ORDER_ID` varchar(64) NOT NULL,"
                + "`ORDER_NAME` varchar(255) DEFAULT NULL,"
                + "PRIMARY KEY (`ORDER_ID`))");
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model` "
                        + "(`id`,`name`,`text`,`remark`,`model_type`,`class_name`,`table_name`,`auto_sys_id`,`id_property`) "
                        + "VALUES (?,?,?,?,?,?,?,?,?)",
                modelId,
                modelName,
                modelName,
                "runtime detail test",
                ModelType.DYNAMIC.code(),
                "example." + modelName,
                tableName,
                false,
                idPropertyId);
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model_property` "
                        + "(`id`,`name`,`remark`,`property_model`,`is_collection`,`owner`,`filter`,`format`,`column`,"
                        + "`property_type`,`allow_db_null`,`is_check`,`ix_group`,`multi_map`) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                idPropertyId,
                "orderId",
                "order id",
                null,
                false,
                modelId,
                null,
                null,
                "ORDER_ID",
                PropertyType.String.code(),
                false,
                true,
                "",
                false);
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model_property` "
                        + "(`id`,`name`,`remark`,`property_model`,`is_collection`,`owner`,`filter`,`format`,`column`,"
                        + "`property_type`,`allow_db_null`,`is_check`,`ix_group`,`multi_map`) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                namePropertyId,
                "orderName",
                "order name",
                null,
                false,
                modelId,
                null,
                null,
                "ORDER_NAME",
                PropertyType.String.code(),
                true,
                false,
                null,
                false);
    }

    private void createRuntimeRelationModel(
            long orderModelId,
            long itemModelId,
            long orderIdPropertyId,
            long itemsPropertyId,
            long itemIdPropertyId,
            String orderModelName,
            String itemModelName) {
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model` "
                        + "(`id`,`name`,`text`,`remark`,`model_type`,`class_name`,`table_name`,`auto_sys_id`,`id_property`) "
                        + "VALUES (?,?,?,?,?,?,?,?,?)",
                orderModelId,
                orderModelName,
                orderModelName,
                "runtime relation test",
                ModelType.DYNAMIC.code(),
                "example." + orderModelName,
                "runtime_relation_order",
                false,
                orderIdPropertyId);
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model` "
                        + "(`id`,`name`,`text`,`remark`,`model_type`,`class_name`,`table_name`,`auto_sys_id`,`id_property`) "
                        + "VALUES (?,?,?,?,?,?,?,?,?)",
                itemModelId,
                itemModelName,
                itemModelName,
                "runtime relation item test",
                ModelType.DYNAMIC.code(),
                "example." + itemModelName,
                "runtime_relation_item",
                false,
                itemIdPropertyId);
        insertRuntimeProperty(orderIdPropertyId, "orderId", null, false, orderModelId, "ORDER_ID", PropertyType.String);
        insertRuntimeProperty(itemsPropertyId, "items", itemModelId, true, orderModelId, null, PropertyType.BusinessObject);
        insertRuntimeProperty(itemIdPropertyId, "itemId", null, false, itemModelId, "ITEM_ID", PropertyType.String);
        jdbcTemplate.update(
                "INSERT INTO `SW_SYS_RELATION` "
                        + "(`SW_SYS_RELATION_TYPE`,`SW_SYS_RELATION_SOURCEPROPERTY`,`SW_SYS_RELATION_TARGETPROPERTY`,"
                        + "`SW_SYS_RELATION_TABLE`,`SW_SYS_RELATION_SOURCECOL`,`SW_SYS_RELATION_TARGETCOL`,`SW_SYS_RELATION_CANBENULL`) "
                        + "VALUES (?,?,?,?,?,?,?)",
                RelationType.One2Many.code(),
                itemsPropertyId,
                itemIdPropertyId,
                "runtime_relation_item",
                "ITEM_ID",
                "ORDER_ID",
                false);
    }

    private void insertRuntimeProperty(
            long propertyId,
            String name,
            Long propertyModelId,
            boolean collection,
            long ownerModelId,
            String column,
            PropertyType type) {
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model_property` "
                        + "(`id`,`name`,`remark`,`property_model`,`is_collection`,`owner`,`filter`,`format`,`column`,"
                        + "`property_type`,`allow_db_null`,`is_check`,`ix_group`,`multi_map`) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                propertyId,
                name,
                name,
                propertyModelId,
                collection,
                ownerModelId,
                null,
                null,
                column,
                type.code(),
                true,
                false,
                null,
                false);
    }

    private void cleanupRuntimeDetailModel(long modelId, String modelName, String tableName) {
        jdbcTemplate.update("DELETE FROM `fool_sys_model_property` WHERE `owner` = ?", modelId);
        jdbcTemplate.update("DELETE FROM `fool_sys_model` WHERE `id` = ? OR `name` = ?", modelId, modelName);
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + tableName + "`");
    }

    private void cleanupRuntimeTriggerModel(long modelId, String modelName, String tableName) {
        jdbcTemplate.update(
                "DELETE FROM `SW_SYS_MODEL_TRIGGER_COMMANDS` WHERE `SW_SYS_MODEL_TRIGGER_CommandsSysId` IN "
                        + "(SELECT `SysId` FROM `SW_SYS_MODEL_TRIGGER` WHERE `SW_SYS_MODEL_TriggersMODEL_ID` = ?)",
                modelId);
        jdbcTemplate.update("DELETE FROM `SW_SYS_MODEL_TRIGGER` WHERE `SW_SYS_MODEL_TriggersMODEL_ID` = ?", modelId);
        cleanupRuntimeDetailModel(modelId, modelName, tableName);
    }

    private void cleanupRuntimePropertyTriggerModel(long modelId, String modelName, String tableName) {
        jdbcTemplate.update(
                "DELETE FROM `SW_SYS_PROPERTY_TRIGGER_COMMANDS` WHERE `SW_SYS_PROPERTY_TRIGGER_CommandsSysId` IN "
                        + "(SELECT `SysId` FROM `SW_SYS_PROPERTY_TRIGGER` WHERE "
                        + "`SW_SYS_PROPERTY_TriggersSysId` IN "
                        + "(SELECT `id` FROM `fool_sys_model_property` WHERE `owner` = ?))",
                modelId);
        jdbcTemplate.update(
                "DELETE FROM `SW_SYS_PROPERTY_TRIGGER` WHERE `SW_SYS_PROPERTY_TriggersSysId` IN "
                        + "(SELECT `id` FROM `fool_sys_model_property` WHERE `owner` = ?)",
                modelId);
        cleanupRuntimeTriggerModel(modelId, modelName, tableName);
    }

    private void cleanupRuntimeRelationModel(
            long orderModelId,
            long itemModelId,
            String orderModelName,
            String itemModelName) {
        jdbcTemplate.update(
                "DELETE FROM `SW_SYS_RELATION` WHERE `SW_SYS_RELATION_SOURCEPROPERTY` IN "
                        + "(SELECT `id` FROM `fool_sys_model_property` WHERE `owner` IN (?,?))",
                orderModelId,
                itemModelId);
        jdbcTemplate.update("DELETE FROM `fool_sys_model_property` WHERE `owner` IN (?,?)", orderModelId, itemModelId);
        jdbcTemplate.update(
                "DELETE FROM `fool_sys_model` WHERE `id` IN (?,?) OR `name` IN (?,?)",
                orderModelId,
                itemModelId,
                orderModelName,
                itemModelName);
    }

    private void cleanupRuntimeRelationDataTables() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS `runtime_relation_item`");
        jdbcTemplate.execute("DROP TABLE IF EXISTS `runtime_relation_order`");
    }

    private void createRuntimeDbMapsTable(String tableName) {
        jdbcTemplate.execute("CREATE TABLE `" + tableName + "` ("
                + "`ORDER_ID` varchar(64) NOT NULL,"
                + "`CUSTOMER_ID` bigint DEFAULT NULL,"
                + "`CUSTOMER_NAME` varchar(255) DEFAULT NULL,"
                + "PRIMARY KEY (`ORDER_ID`))");
    }

    private void cleanupRuntimeDbMapsTable(String tableName) {
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + tableName + "`");
    }

    private void createRuntimeManyToManyTables(String orderTable, String roleTable, String relationTable) {
        jdbcTemplate.execute("CREATE TABLE `" + orderTable + "` ("
                + "`ORDER_ID` varchar(64) NOT NULL,"
                + "`ORDER_NAME` varchar(255) DEFAULT NULL,"
                + "PRIMARY KEY (`ORDER_ID`))");
        jdbcTemplate.execute("CREATE TABLE `" + roleTable + "` ("
                + "`ROLE_ID` varchar(64) NOT NULL,"
                + "`ROLE_NAME` varchar(255) DEFAULT NULL,"
                + "PRIMARY KEY (`ROLE_ID`))");
        jdbcTemplate.execute("CREATE TABLE `" + relationTable + "` ("
                + "`ROLE_ID` varchar(64) NOT NULL,"
                + "`ORDER_ID` varchar(64) NOT NULL,"
                + "PRIMARY KEY (`ROLE_ID`,`ORDER_ID`))");
    }

    private void cleanupRuntimeManyToManyTables(String orderTable, String roleTable, String relationTable) {
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + relationTable + "`");
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + roleTable + "`");
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + orderTable + "`");
    }

    private void createRuntimeRecurveTables(String nodeTable, String relationTable) {
        jdbcTemplate.execute("CREATE TABLE `" + nodeTable + "` ("
                + "`NODE_ID` varchar(64) NOT NULL,"
                + "`NODE_NAME` varchar(255) DEFAULT NULL,"
                + "PRIMARY KEY (`NODE_ID`))");
        jdbcTemplate.execute("CREATE TABLE `" + relationTable + "` ("
                + "`PARENT_ID` varchar(64) NOT NULL,"
                + "`CHILD_ID` varchar(64) NOT NULL,"
                + "PRIMARY KEY (`PARENT_ID`,`CHILD_ID`))");
    }

    private void cleanupRuntimeRecurveTables(String nodeTable, String relationTable) {
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + relationTable + "`");
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + nodeTable + "`");
    }

    private void createRuntimeOneToManyTables(String orderTable, String itemTable) {
        jdbcTemplate.execute("CREATE TABLE `" + orderTable + "` ("
                + "`ORDER_ID` varchar(64) NOT NULL,"
                + "`ORDER_NAME` varchar(255) DEFAULT NULL,"
                + "PRIMARY KEY (`ORDER_ID`))");
        jdbcTemplate.execute("CREATE TABLE `" + itemTable + "` ("
                + "`ITEM_ID` varchar(64) NOT NULL,"
                + "`ITEM_NAME` varchar(255) DEFAULT NULL,"
                + "`ORDER_ID` varchar(64) DEFAULT NULL,"
                + "PRIMARY KEY (`ITEM_ID`))");
    }

    private void cleanupRuntimeOneToManyTables(String orderTable, String itemTable) {
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + itemTable + "`");
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + orderTable + "`");
    }
}
