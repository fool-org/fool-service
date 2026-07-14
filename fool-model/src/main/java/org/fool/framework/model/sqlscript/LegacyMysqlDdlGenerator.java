package org.fool.framework.model.sqlscript;

import org.fool.framework.common.PropertyType;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.MultiDbMap;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.model.RelationType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LegacyMysqlDdlGenerator {
    public String generateCreateTableSql(Model model) {
        if (model == null || model.getModelType() == ModelType.ENUM) {
            return "";
        }

        String table = normalizeIdentifier(model.getTableName() == null ? model.getName() : model.getTableName());
        List<String> definitions = new ArrayList<>();
        Map<String, List<String>> keyGroups = new LinkedHashMap<>();

        for (Property property : safeProperties(model)) {
            if (Boolean.TRUE.equals(property.getIsCollection())) {
                continue;
            }
            if (Boolean.TRUE.equals(property.getMultiMap())) {
                definitions.addAll(multiMapColumnSql(property));
                continue;
            }
            if (isBlank(property.getColumn())) {
                continue;
            }
            definitions.add(columnSql(property, false, property == model.getIdProperty()));
            if (Boolean.TRUE.equals(property.getCheck())) {
                String group = normalizeGroup(property.getIxGroup(), table);
                keyGroups.computeIfAbsent(group, ignored -> new ArrayList<>()).add(normalizeIdentifier(property.getColumn()));
            }
        }

        if (Boolean.TRUE.equals(model.getAutoSysId())) {
            definitions.add("`SysId` BIGINT NOT NULL AUTO_INCREMENT");
            definitions.add("PRIMARY KEY (`SysId`)");
        } else {
            addLegacyKeys(table, definitions, keyGroups);
        }

        return "CREATE TABLE IF NOT EXISTS `" + table + "` (\n  "
                + String.join(",\n  ", definitions)
                + "\n);";
    }

    public String generateRelationSql(Relation relation, Model sourceModel) {
        if (relation == null || relation.getRelationType() == null || sourceModel == null) {
            return "";
        }

        String relationTable = normalizeIdentifier(relation.getRelationTable());
        if (relation.getRelationType() == RelationType.One2Many) {
            return "ALTER TABLE `" + relationTable + "` ADD COLUMN `"
                    + normalizeIdentifier(relation.getTargetColumn()) + "` "
                    + modelKeyColumnSql(sourceModel, true)
                    + ";";
        }

        if (relation.getRelationType() == RelationType.Many2Many
                || relation.getRelationType() == RelationType.Recurve) {
            String targetSql = relation.getTargetProperty() == null
                    ? "BIGINT NOT NULL"
                    : modelKeyColumnSql(relation.getTargetProperty().getPropertyModel(), false);
            return "CREATE TABLE IF NOT EXISTS `" + relationTable + "` (\n  `"
                    + normalizeIdentifier(relation.getPropertyColumn()) + "` "
                    + modelKeyColumnSql(sourceModel, false)
                    + ",\n  `"
                    + normalizeIdentifier(relation.getTargetColumn()) + "` "
                    + targetSql
                    + "\n);";
        }

        return "";
    }

    private List<String> multiMapColumnSql(Property property) {
        List<String> columns = new ArrayList<>();
        for (MultiDbMap map : safeDbMaps(property)) {
            if (map == null || isBlank(map.getColumnName())) {
                continue;
            }
            Property mapProperty = mappedProperty(property, map.getPropertyName());
            Property column = new Property();
            column.setColumn(map.getColumnName());
            column.setAllowDbNull(property.getAllowDbNull());
            if (mapProperty != null) {
                column.setPropertyType(mapProperty.getPropertyType());
                column.setPropertyModel(mapProperty.getPropertyModel());
            } else {
                column.setPropertyType(PropertyType.String);
            }
            columns.add(columnSql(column, true, false));
        }
        return columns;
    }

    private Property mappedProperty(Property property, String propertyName) {
        if (property == null || property.getPropertyModel() == null || property.getPropertyModel().getProperties() == null) {
            return null;
        }
        return property.getPropertyModel().getProperties().stream()
                .filter(candidate -> propertyName != null
                        && (propertyName.equals(candidate.getName()) || propertyName.equalsIgnoreCase(candidate.getName())))
                .findFirst()
                .orElse(null);
    }

    private void addLegacyKeys(String table, List<String> definitions, Map<String, List<String>> keyGroups) {
        for (Map.Entry<String, List<String>> entry : keyGroups.entrySet()) {
            List<String> columns = entry.getValue();
            String columnList = columns.stream().map(column -> "`" + column + "`").collect(Collectors.joining(","));
            if (entry.getKey().equals(primaryGroup(table)) && columns.size() == 1) {
                definitions.add("PRIMARY KEY (" + columnList + ")");
            } else {
                definitions.add("UNIQUE KEY `IX" + entry.getKey() + "` (" + columnList + ")");
            }
        }
    }

    private String columnSql(Property property, boolean multiMap, boolean allowAutoIncrement) {
        String type = propertyTypeSql(property, multiMap);
        String nullable = Boolean.TRUE.equals(property.getAllowDbNull()) ? "NULL" : "NOT NULL";
        if (property.getPropertyType() == PropertyType.IdentifyId && !multiMap && allowAutoIncrement) {
            return "`" + normalizeIdentifier(property.getColumn()) + "` BIGINT NOT NULL AUTO_INCREMENT";
        }
        return "`" + normalizeIdentifier(property.getColumn()) + "` " + type + " " + nullable + defaultValueSql(property);
    }

    private String defaultValueSql(Property property) {
        if (property != null && !isBlank(property.getGenerationExpression())) {
            return " DEFAULT " + parenthesizedExpression(property.getGenerationExpression());
        }
        if (property == null || isBlank(property.getDefaultValue())) {
            return "";
        }
        return " DEFAULT '" + property.getDefaultValue().replace("'", "''") + "'";
    }

    private String parenthesizedExpression(String expression) {
        String normalized = expression.trim();
        if (normalized.startsWith("(") && normalized.endsWith(")")) {
            return normalized;
        }
        return "(" + normalized + ")";
    }

    private String propertyTypeSql(Property property, boolean multiMap) {
        PropertyType type = property.getPropertyType() == null ? PropertyType.String : property.getPropertyType();
        if (type == PropertyType.BusinessObject) {
            return businessObjectKeySql(property.getPropertyModel());
        }

        return switch (type) {
            case Boolean -> "BIT";
            case Byte, Enum -> "SMALLINT";
            case Char -> "NCHAR(1)";
            case Date -> "DATE";
            case DateTime -> "DATETIME";
            case Decimal -> "DECIMAL(18,2)";
            case Double, Float -> "FLOAT";
            case IdentifyId -> "BIGINT";
            case Int, UInt -> "INT";
            case Long, ULong -> "BIGINT";
            case String, SerialNo, MD5, Radom -> "VARCHAR(200)";
            case Time -> "TIME";
            case Guid -> "CHAR(36)";
            case RadomDECS -> "VARBINARY(8)";
            case BusinessObject -> "BIGINT";
        };
    }

    private String businessObjectKeySql(Model targetModel) {
        if (targetModel == null || Boolean.TRUE.equals(targetModel.getAutoSysId()) || targetModel.getIdProperty() == null) {
            return "BIGINT";
        }
        PropertyType targetType = targetModel.getIdProperty().getPropertyType();
        if (targetType == PropertyType.IdentifyId) {
            return "BIGINT";
        }
        Property keyProperty = new Property();
        keyProperty.setPropertyType(targetType);
        return propertyTypeSql(keyProperty, true);
    }

    private String modelKeyColumnSql(Model model, boolean nullable) {
        PropertyType type = PropertyType.Long;
        if (model != null && !Boolean.TRUE.equals(model.getAutoSysId()) && model.getIdProperty() != null) {
            type = model.getIdProperty().getPropertyType();
            if (type == PropertyType.IdentifyId) {
                type = PropertyType.Long;
            }
        }
        Property keyProperty = new Property();
        keyProperty.setPropertyType(type == null ? PropertyType.Long : type);
        return propertyTypeSql(keyProperty, true) + (nullable ? " NULL" : " NOT NULL");
    }

    private List<Property> safeProperties(Model model) {
        return model.getProperties() == null ? List.of() : model.getProperties();
    }

    private List<MultiDbMap> safeDbMaps(Property property) {
        return property.getDbMaps() == null ? List.of() : property.getDbMaps();
    }

    private String normalizeIdentifier(String identifier) {
        return identifier.replace("[", "").replace("]", "").replace("`", "").trim().toUpperCase();
    }

    private String normalizeGroup(String ixGroup, String table) {
        String group = isBlank(ixGroup) ? "" : ixGroup.trim().toUpperCase();
        return "_" + group + "_" + table;
    }

    private String primaryGroup(String table) {
        return "__" + table;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
