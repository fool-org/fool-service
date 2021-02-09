package org.fool.framework.model.model;

import org.fool.framework.common.dynamic.IDynamicData;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * sql查询的动态结果
 */
public class DbMysqlDynamic implements IDynamicData {


    private final Model model;
    private final Map<String, Object> propertyMaps;


    public DbMysqlDynamic(Model model) {
        this.model = model;
        this.propertyMaps = new LinkedHashMap<>();
    }

    Model getModel() {
        return this.model;
    }

    @Override
    public Object get(String field) {
        return this.propertyMaps.getOrDefault(field, null);
    }

    @Override
    public void set(String field, Object value) {
        this.propertyMaps.put(field, value);
    }

    @Override
    public Object invokeWithReturn(String methodName, Object... args) {
        return null;
    }

    @Override
    public void invoke(String methodName, Object... args) {

    }

    @Override
    public Map<String, Object> toMap() {
        return this.propertyMaps;
    }

    @Override
    public String getId() {

        if (model.getIdProperty() != null) {
            return (String) this.propertyMaps.get(model.getIdProperty().getName());
        }
        return null;
    }

    @Override
    public String toString() {
        if (this.propertyMaps != null)
            return this.propertyMaps.toString();
        else {
            return "empty";
        }
    }
}
