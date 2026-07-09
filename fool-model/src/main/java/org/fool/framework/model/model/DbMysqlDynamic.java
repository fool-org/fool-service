package org.fool.framework.model.model;

import org.fool.framework.common.dynamic.IDynamicData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * sql查询的动态结果
 */
public class DbMysqlDynamic implements IDynamicData {


    private final Model model;
    private final Map<String, Object> propertyMaps;
    private final Map<String, Object> oldPropertyMaps;
    private IDynamicData owner;


    public DbMysqlDynamic(Model model) {
        this.model = model;
        this.propertyMaps = new LinkedHashMap<>();
        this.oldPropertyMaps = new LinkedHashMap<>();
    }

    public Model getModel() {
        return this.model;
    }

    public IDynamicData getOwner() {
        return owner;
    }

    public void setOwner(IDynamicData owner) {
        this.owner = owner;
    }

    @Override
    public Object get(String field) {
        return this.propertyMaps.getOrDefault(field, null);
    }

    @Override
    public void set(String field, Object value) {
        if (this.propertyMaps.containsKey(field)
                && !this.oldPropertyMaps.containsKey(field)
                && !Objects.equals(this.propertyMaps.get(field), value)) {
            this.oldPropertyMaps.put(field, this.propertyMaps.get(field));
        }
        this.propertyMaps.put(field, value);
    }

    public Object getOld(String field) {
        return this.oldPropertyMaps.get(field);
    }

    public boolean hasOld(String field) {
        return this.oldPropertyMaps.containsKey(field);
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

        if (model != null && model.getIdProperty() != null) {
            Object id = this.propertyMaps.get(model.getIdProperty().getName());
            return id == null ? null : id.toString();
        }
        Object sysid = this.propertyMaps.get("SYSID");
        return sysid == null ? null : sysid.toString();
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
