package org.fool.framework.app;

import org.fool.framework.common.PropertyType;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.CommandsType;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.Operation;
import org.fool.framework.model.model.OperationBaseType;
import org.fool.framework.model.model.OperationCommand;
import org.fool.framework.model.model.OperationParam;
import org.fool.framework.model.model.Property;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DaoAppInstallGatewayOperationTest {
    @Test
    public void installModuleSourcePersistsLegacyModelOperationsAndCommands() {
        RecordingDaoService daoService = new RecordingDaoService();
        DaoAppInstallGateway gateway = new DaoAppInstallGateway(daoService);
        Model order = legacyModel("Order", "SW_ORDER");
        Property state = legacyProperty("state", "ORDER_STATE", PropertyType.String);
        Operation approve = new Operation();
        approve.setName("审批");
        approve.setFilter("state=0");
        approve.setBaseOperationType(OperationBaseType.UPDATE);
        OperationCommand setState = new OperationCommand();
        setState.setCommandType(CommandsType.SET_VALUE);
        setState.setPropertyId(901L);
        setState.setExpression("$1");
        setState.setIndex(1);
        approve.setCommands(List.of(setState));
        OperationParam reason = new OperationParam();
        reason.setName("reason");
        reason.setViewId(910L);
        reason.setFilter("state=0");
        reason.setValue("$reason");
        approve.setParams(List.of(reason));
        order.setProperties(List.of(state));
        order.setOperations(List.of(approve));

        gateway.installModuleSource(
                "sys-con",
                "work-con",
                new StaticAppModuleSource(List.of(AppModuleDefinition.legacy(
                        "MKT01",
                        "example.OrderModule",
                        "2.0.0",
                        List.of(order)))));

        assertEquals(6, daoService.created.size());
        AppInstalledModel installedModel = (AppInstalledModel) daoService.created.get(1);
        AppInstalledOperation operation = (AppInstalledOperation) daoService.created.get(3);
        AppInstalledOperationCommand command = (AppInstalledOperationCommand) daoService.created.get(4);
        AppInstalledOperationParam param = (AppInstalledOperationParam) daoService.created.get(5);
        assertEquals(installedModel.getModelId(), operation.getOwnerModelId());
        assertEquals("审批", operation.getName());
        assertEquals(Integer.valueOf(OperationBaseType.UPDATE.code()), operation.getBaseType());
        assertEquals(operation.getOperationId(), approve.getId());
        assertEquals(operation.getOperationId(), command.getOwnerOperationId());
        assertEquals(Integer.valueOf(CommandsType.SET_VALUE.code()), command.getCommandType());
        assertEquals(Long.valueOf(901L), command.getPropertyId());
        assertEquals("$1", command.getExpression());
        assertEquals(command.getCommandId(), setState.getId());
        assertEquals(operation.getOperationId(), param.getOwnerOperationId());
        assertEquals("reason", param.getName());
        assertEquals(Long.valueOf(910L), param.getViewId());
        assertEquals("state=0", param.getFilter());
        assertEquals("$reason", param.getValue());
        assertEquals(param.getParamId(), reason.getId());
        assertEquals(operation.getOperationId(), reason.getOwnerOperationId());
    }

    private static Model legacyModel(String name, String tableName) {
        Model model = new Model();
        model.setName(name);
        model.setText(name);
        model.setClassName("example." + name);
        model.setModelType(ModelType.DYNAMIC);
        model.setTableName(tableName);
        return model;
    }

    private static Property legacyProperty(String name, String column, PropertyType type) {
        Property property = new Property();
        property.setName(name);
        property.setColumn(column);
        property.setPropertyType(type);
        return property;
    }

    private static final class RecordingDaoService extends DaoService {
        private final List<Object> created = new ArrayList<>();
        private long nextModelId = 5000;
        private long nextPropertyId = 6000;
        private long nextOperationId = 7000;
        private long nextCommandId = 8000;
        private long nextParamId = 9000;

        @Override
        public <T> void create(T object) {
            if (object instanceof AppInstalledModel model && model.getModelId() == null) {
                model.setModelId(nextModelId++);
            } else if (object instanceof AppInstalledProperty property && property.getPropertyId() == null) {
                property.setPropertyId(nextPropertyId++);
            } else if (object instanceof AppInstalledOperation operation && operation.getOperationId() == null) {
                operation.setOperationId(nextOperationId++);
            } else if (object instanceof AppInstalledOperationCommand command && command.getCommandId() == null) {
                command.setCommandId(nextCommandId++);
            } else if (object instanceof AppInstalledOperationParam param && param.getParamId() == null) {
                param.setParamId(nextParamId++);
            }
            created.add(object);
        }

        @Override
        public <T> List<T> selectList(Class<T> clazz, String sql, Object... args) {
            return List.of();
        }
    }
}
