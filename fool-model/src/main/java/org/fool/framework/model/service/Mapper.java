package org.fool.framework.model.service;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.AbstratMapper;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.EnumValue;
import org.fool.framework.model.model.MultiDbMap;
import org.fool.framework.model.model.Property;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 动态数据 的映射
 */
@Slf4j
public class Mapper extends AbstratMapper<IDynamicData> {

    private final Model model;

    /**
     * 公共类型
     *
     * @param model
     */
    public Mapper(Model model) {
        this.model = model;
    }

    Model getModel() {
        return this.model;
    }

    /**
     * 暂时只处理简单类型
     *
     * @param resultSet
     * @param row
     * @return
     */
    @Override
    public IDynamicData mapRow(ResultSet resultSet, int row) {
        try {

            DbMysqlDynamic mysqlDynamic = new DbMysqlDynamic(this.model);
            for (var property : this.model.getProperties().stream().filter(p -> p.getIsCollection() == false).collect(Collectors.toList())) {
                if (Boolean.TRUE.equals(property.getMultiMap())) {
                    mapMultiDbMapProperty(mysqlDynamic, property, resultSet);
                } else if (PropertyType.BusinessObject.equals(property.getPropertyType())) {
                    mapBusinessObjectProperty(mysqlDynamic, property, resultSet);
                } else if (!StringUtils.isEmpty(property.getColumn())) {
                    try {
                        mysqlDynamic.set(property.getName(), simpleColumnValue(property, resultSet));
                    } catch (SQLException ex) {
                        mysqlDynamic.set(property.getName(), defaultValue(property));
                    }
                }
            }
            return mysqlDynamic;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object simpleColumnValue(Property property, ResultSet resultSet) throws SQLException {
        Object value = resultSet.getObject(property.getColumn());
        return value == null ? defaultValue(property) : value;
    }

    private void mapMultiDbMapProperty(DbMysqlDynamic owner, Property property, ResultSet resultSet) {
        if (property.getPropertyModel() == null || property.getDbMaps() == null) {
            return;
        }
        DbMysqlDynamic target = new DbMysqlDynamic(property.getPropertyModel());
        boolean mapped = false;
        for (MultiDbMap dbMap : property.getDbMaps()) {
            if (dbMap == null || !StringUtils.hasText(dbMap.getColumnName())) {
                continue;
            }
            Property targetProperty = mappedProperty(property.getPropertyModel(), dbMap.getPropertyName());
            if (targetProperty == null) {
                continue;
            }
            Object value = multiDbMapValue(property, dbMap, resultSet);
            if (value != null) {
                target.set(targetProperty.getName(), value);
                mapped = true;
            }
        }
        if (mapped) {
            owner.set(property.getName(), target);
        }
    }

    private Object multiDbMapValue(Property property, MultiDbMap dbMap, ResultSet resultSet) {
        // ponytail: raw column fallback keeps older non-aliased DBMap selects working.
        String alias = property.getName() + "_"
                + (StringUtils.hasText(dbMap.getPropertyName()) ? dbMap.getPropertyName() : dbMap.getColumnName());
        for (String column : List.of(alias, dbMap.getColumnName())) {
            try {
                Object value = resultSet.getObject(column);
                if (value != null) {
                    return value;
                }
            } catch (SQLException ex) {
            }
        }
        return null;
    }

    private void mapBusinessObjectProperty(DbMysqlDynamic owner, Property property, ResultSet resultSet) {
        Model targetModel = property.getPropertyModel();
        if (targetModel == null) {
            return;
        }
        DbMysqlDynamic target = new DbMysqlDynamic(targetModel);
        boolean mapped = false;
        mapped = mapAliasedBusinessObjectProperty(target, property, targetModel.getIdProperty(), resultSet) || mapped;
        mapped = mapAliasedBusinessObjectProperty(
                target,
                property,
                ModelDisplayProperties.displayProperty(targetModel),
                resultSet) || mapped;
        if (mapped) {
            owner.set(property.getName(), target);
        }
    }

    private boolean mapAliasedBusinessObjectProperty(
            DbMysqlDynamic target,
            Property ownerProperty,
            Property targetProperty,
            ResultSet resultSet) {
        if (targetProperty == null || !StringUtils.hasText(targetProperty.getColumn())) {
            return false;
        }
        try {
            Object value = resultSet.getObject(ownerProperty.getName() + "_" + targetProperty.getColumn());
            if (value == null) {
                return false;
            }
            target.set(targetProperty.getName(), value);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    private Property mappedProperty(Model model, String propertyName) {
        for (Property property : safeProperties(model)) {
            if (propertyName != null
                    && (propertyName.equals(property.getName()) || propertyName.equalsIgnoreCase(property.getName()))) {
                return property;
            }
        }
        return null;
    }

    private List<Property> safeProperties(Model model) {
        return model.getProperties() == null ? List.of() : model.getProperties();
    }

    static Object defaultValue(Property property) {
        if (property.getPropertyType() == null) {
            return null;
        }
        return switch (property.getPropertyType()) {
            case Boolean -> false;
            case Byte, Int, UInt -> 0;
            case Char -> '\0';
            case Decimal -> BigDecimal.ZERO;
            case Double -> 0D;
            case Float -> 0F;
            case Long, ULong, IdentifyId -> 0L;
            case String, SerialNo -> "";
            case Time -> LocalTime.MIN;
            case Enum -> enumDefaultValue(property);
            case Date, DateTime, BusinessObject, MD5, Radom, RadomDECS, Guid -> null;
        };
    }

    private static Object enumDefaultValue(Property property) {
        Model enumModel = property.getPropertyModel();
        if (enumModel == null || enumModel.getEnumValues() == null || enumModel.getEnumValues().isEmpty()) {
            return null;
        }
        EnumValue first = enumModel.getEnumValues().get(0);
        if (first == null || first.getValue() == null) {
            return null;
        }
        try {
            return Integer.parseInt(first.getValue());
        } catch (NumberFormatException ex) {
            return first.getValue();
        }
    }
}
