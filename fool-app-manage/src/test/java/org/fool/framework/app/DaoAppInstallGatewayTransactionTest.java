package org.fool.framework.app;

import org.fool.framework.common.PropertyType;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.Property;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class DaoAppInstallGatewayTransactionTest {
    @Test
    public void installWorkUsesRoutedDaoTransactionBoundaries() {
        RecordingDaoService sysDao = new RecordingDaoService();
        RecordingDaoService workDao = new RecordingDaoService();
        DaoAppInstallGateway gateway = new DaoAppInstallGateway(
                connection -> "work-con".equals(connection) ? workDao : sysDao);
        Model order = legacyModel("Order", "SW_ORDER");
        order.setProperties(List.of(legacyProperty("id", "ORDER_ID", PropertyType.IdentifyId)));
        AppModuleDefinition module = AppModuleDefinition.legacy(
                "MKT01",
                "example.OrderModule",
                "2.0.0",
                List.of(order));

        gateway.installModuleSource("sys-con", "work-con", new StaticAppModuleSource(List.of(module)));
        gateway.installModelSchemas("sys-con", "work-con", List.of(order));
        gateway.installDefaultViews("sys-con", "work-con", List.of(order));

        assertEquals(2, sysDao.transactionCount);
        assertEquals(1, workDao.transactionCount);
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
        private final List<String> executedSql = new ArrayList<>();
        private int transactionCount;
        private long nextModelId = 5000;
        private long nextPropertyId = 6000;
        private long nextViewId = 7000;
        private long nextViewItemId = 8000;
        private long nextViewOperationId = 9000;
        private long nextOperationViewId = 10000;

        @Override
        public <T> void create(T object) {
            if (object instanceof AppInstalledModel model && model.getModelId() == null) {
                model.setModelId(nextModelId++);
            } else if (object instanceof AppInstalledProperty property && property.getPropertyId() == null) {
                property.setPropertyId(nextPropertyId++);
            } else if (object instanceof AppInstalledView view && view.getViewId() == null) {
                view.setViewId(nextViewId++);
            } else if (object instanceof AppInstalledViewItem item && item.getItemId() == null) {
                item.setItemId(nextViewItemId++);
            } else if (object instanceof AppInstalledViewOperation operation && operation.getViewOperationId() == null) {
                operation.setViewOperationId(nextViewOperationId++);
            } else if (object instanceof AppInstalledOperationView operationView
                    && operationView.getOperationViewId() == null) {
                operationView.setOperationViewId(nextOperationViewId++);
            }
        }

        @Override
        public <T> List<T> selectList(Class<T> clazz, String sql, Object... args) {
            return List.of();
        }

        @Override
        public void execute(String sql) {
            executedSql.add(sql);
        }

        @Override
        public <T> T inTransaction(Supplier<T> action) {
            transactionCount++;
            return action.get();
        }
    }
}
