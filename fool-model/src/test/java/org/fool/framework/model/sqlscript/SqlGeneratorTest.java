package org.fool.framework.model.sqlscript;

import org.fool.framework.common.PropertyType;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.QueryAndArgs;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.Property;
import org.fool.framework.query.IQueryFilter;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SqlGeneratorTest {

    @Test
    public void generateSelectPlacesLegacyDefaultOrderBeforePagination() {
        Model order = model("Order", "market_order");
        Property orderId = property("orderId", "order_id");
        Property symbol = property("symbol", "order_symbol");
        PageNavigator pageNavigator = new PageNavigator();
        pageNavigator.setPageIndex(2);
        pageNavigator.setPageSize(10);

        QueryAndArgs query = new SqlGenerator().generateSelect(
                order,
                List.of(orderId, symbol),
                IQueryFilter.init(),
                pageNavigator,
                "order_id",
                true);

        assertEquals(
                "SELECT order_id,order_symbol FROM `market_order` WHERE 1=1  AND  1=1  ORDER BY `order_id` DESC LIMIT ? OFFSET ?",
                query.getSql());
        assertArrayEquals(new Object[]{10, 10}, query.getArgs());
    }

    private static Model model(String name, String tableName) {
        Model model = new Model();
        model.setName(name);
        model.setText(name);
        model.setClassName("example." + name);
        model.setModelType(ModelType.DYNAMIC);
        model.setTableName(tableName);
        model.setAutoSysId(false);
        return model;
    }

    private static Property property(String name, String column) {
        Property property = new Property();
        property.setName(name);
        property.setRemark(name);
        property.setColumn(column);
        property.setPropertyType(PropertyType.String);
        property.setIsCollection(false);
        property.setAllowDbNull(false);
        property.setCheck(false);
        return property;
    }
}
