package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class JdbcCompareOpCatalog {
    public static final String SELECT_SQL = """
            SELECT
              `SE_COMPARETYPE`.`SE_COMPARESHOW`,
              `SE_COMPARETYPE`.`SE_COMPAREEXP`,
              `SE_COMPARETYPE`.`SysID` AS `SysID`
            FROM `SE_COMPARETYPE`
            JOIN `SE_COMPARETYPE_PROPERTYINDEX`
              ON `SE_COMPARETYPE`.`SysID` = `SE_COMPARETYPE_PROPERTYINDEX`.`COMPARETYPE_ID`
            WHERE `SE_COMPARETYPE_PROPERTYINDEX`.`PROPERTYTYPE_VALUE` = ?
            ORDER BY `SE_COMPARETYPE`.`SysID`
            """;

    private final Function<PropertyType, List<Map<String, Object>>> rowsByPropertyType;

    public JdbcCompareOpCatalog(JdbcTemplate jdbcTemplate) {
        this(propertyType -> jdbcTemplate.queryForList(SELECT_SQL, propertyType.ordinal()));
    }

    JdbcCompareOpCatalog(Function<PropertyType, List<Map<String, Object>>> rowsByPropertyType) {
        this.rowsByPropertyType = rowsByPropertyType;
    }

    public List<LegacyCompareOp> listFor(PropertyType propertyType) {
        return rowsByPropertyType.apply(propertyType).stream()
                .map(row -> mapRow(row, propertyType))
                .collect(Collectors.toList());
    }

    public List<LegacyCompareOp> listFor(PropertyType propertyType, long propertyId) {
        return listFor(propertyType);
    }

    private LegacyCompareOp mapRow(Map<String, Object> row, PropertyType propertyType) {
        LegacyCompareOp operation = new LegacyCompareOp();
        operation.setId(((Number) value(row, "SysID", "sysid")).longValue());
        operation.setShowName(Objects.toString(value(row, "SE_COMPARESHOW", "se_compareshow"), ""));
        operation.setDbName(Objects.toString(value(row, "SE_COMPAREEXP", "se_compareexp"), ""));
        operation.setPropertyType(propertyType);
        return operation;
    }

    private Object value(Map<String, Object> row, String... names) {
        for (String name : names) {
            if (row.containsKey(name)) {
                return row.get(name);
            }
        }
        return null;
    }
}
