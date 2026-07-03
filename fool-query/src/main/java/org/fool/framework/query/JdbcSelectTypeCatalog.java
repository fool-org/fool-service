package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Repository
public class JdbcSelectTypeCatalog {
    public static final String SELECT_SQL = """
            SELECT
              `SE_SELECTEDTYPE`.`SE_SELECTEDSHOW`,
              `SE_SELECTEDTYPE`.`SE_SELECTEDEXP`,
              `SE_SELECTEDTYPE`.`SE_REQUIREGROUP`,
              `SE_SELECTEDTYPE`.`SysID` AS `SysID`
            FROM `SE_SELECTEDTYPE`
            JOIN `SE_SELECTEDTYPE_PROPERTYINDEX`
              ON `SE_SELECTEDTYPE`.`SysID` = `SE_SELECTEDTYPE_PROPERTYINDEX`.`SELECTEDTYPE_ID`
            WHERE `SE_SELECTEDTYPE_PROPERTYINDEX`.`PROPERTYTYPE_VALUE` = ?
            ORDER BY `SE_SELECTEDTYPE`.`SysID`
            """;

    public static final String SELECT_ALL_SQL = """
            SELECT
              `SE_SELECTEDSHOW`,
              `SE_SELECTEDEXP`,
              `SE_REQUIREGROUP`,
              `SysID`
            FROM `SE_SELECTEDTYPE`
            ORDER BY `SysID`
            """;

    private final Function<PropertyType, List<Map<String, Object>>> rowsByPropertyType;
    private final Supplier<List<Map<String, Object>>> allRows;

    @Autowired
    public JdbcSelectTypeCatalog(JdbcTemplate jdbcTemplate) {
        this(
                propertyType -> jdbcTemplate.queryForList(SELECT_SQL, propertyType.ordinal()),
                () -> jdbcTemplate.queryForList(SELECT_ALL_SQL));
    }

    JdbcSelectTypeCatalog(
            Function<PropertyType, List<Map<String, Object>>> rowsByPropertyType,
            Supplier<List<Map<String, Object>>> allRows) {
        this.rowsByPropertyType = rowsByPropertyType;
        this.allRows = allRows;
    }

    public List<SelectType> listFor(PropertyType propertyType) {
        return rowsByPropertyType.apply(propertyType).stream()
                .map(this::mapRow)
                .collect(Collectors.toList());
    }

    public List<SelectType> listFor(PropertyType propertyType, long propertyId) {
        return listFor(propertyType);
    }

    public List<SelectType> listAll() {
        return allRows.get().stream()
                .map(this::mapRow)
                .collect(Collectors.toList());
    }

    private SelectType mapRow(Map<String, Object> row) {
        SelectType selectType = new SelectType();
        selectType.setId(((Number) value(row, "SysID", "sysid")).longValue());
        selectType.setShow(Objects.toString(value(row, "SE_SELECTEDSHOW", "se_selectedshow"), ""));
        selectType.setDbExp(Objects.toString(value(row, "SE_SELECTEDEXP", "se_selectedexp"), ""));
        selectType.setRequireGroupCol(toBoolean(value(row, "SE_REQUIREGROUP", "se_requiregroup")));
        return selectType;
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return Boolean.parseBoolean(Objects.toString(value, "false"));
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
