package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.fool.framework.dao.QueryAndArgs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QuerySqlBuilder {
    private static final String DEFAULT_ROW_INDEX = "RowIndex";

    private QuerySqlBuilder() {
    }

    public static String tableSql(SelectedTables tables) {
        List<SelectedTable> selectedTables = tables.getTables();
        StringBuilder sql = new StringBuilder(selectedTableSql(selectedTables.get(0)));

        for (JoinTable joinTable : tables.getJoins()) {
            sql.append(" JOIN ")
                    .append(selectedTableSql(joinTable.getRightTable()))
                    .append(" ON 1=1");
            for (JoinCondition condition : joinTable.getConditions()) {
                sql.append(" AND [")
                        .append(identifier(joinTable.getLeftTable().getSelectedTableName()))
                        .append("].[")
                        .append(identifier(condition.getLeftCol()))
                        .append("]=[")
                        .append(identifier(joinTable.getRightTable().getSelectedTableName()))
                        .append("].[")
                        .append(identifier(condition.getRightCol()))
                        .append("]");
            }
        }

        return sql.toString();
    }

    public static String selectedColumnSql(SelectedColumn column) {
        String expression = columnExpression(column);
        if (column.getDataColumn().getDataType() != PropertyType.Enum) {
            return String.format(" %s  AS [%s]", expression, identifier(column.getSelectedName()));
        }

        StringBuilder stateSql = new StringBuilder("(CASE");
        if (column.getValues() != null) {
            for (ColStateValue value : column.getValues()) {
                stateSql.append(" WHEN ")
                        .append(expression)
                        .append("=")
                        .append(value.getDbName())
                        .append(" THEN '")
                        .append(value.getShowName())
                        .append("' ");
            }
        }
        stateSql.append(" ELSE '' END) AS [")
                .append(identifier(column.getSelectedName()))
                .append("]");
        return stateSql.toString();
    }

    public static String selectSql(QueryInstance instance) {
        return selectSql(instance, DEFAULT_ROW_INDEX);
    }

    public static String selectSql(QueryInstance instance, String rowIndex) {
        List<SelectedColumn> selectedColumns = instance.getSelectedColumns().asList().stream()
                .sorted(Comparator.comparingInt(SelectedColumn::getSelectedIndex))
                .collect(Collectors.toList());

        StringBuilder sql = new StringBuilder("SELECT distinct ");
        for (SelectedColumn column : selectedColumns) {
            sql.append(selectedColumnSql(column)).append(",");
        }

        sql.append(rowNumberSql(selectedColumns, rowIndex));
        sql.append(" FROM ").append(tableSql(instance.getSelectedTables()));
        appendWhereSql(sql, instance);
        appendGroupBySql(sql, selectedColumns);
        return sql.toString();
    }

    public static QueryAndArgs pagedSql(QueryInstance instance, int pageSize, int startPage) {
        return pagedSql(instance, pageSize, startPage, DEFAULT_ROW_INDEX, false);
    }

    public static QueryAndArgs pagedSql(
            QueryInstance instance,
            int pageSize,
            int startPage,
            String rowIndex,
            boolean includeRowIndex) {
        PagedQuerySql pagedQuerySql = pagedQuerySql(instance, pageSize, startPage, rowIndex, includeRowIndex);

        QueryAndArgs query = new QueryAndArgs();
        query.setSql(pagedQuerySql.combinedSql());
        query.setArgs(pagedQuerySql.combinedArgs());
        return query;
    }

    public static PagedQuerySql pagedQuerySql(QueryInstance instance, int pageSize, int startPage) {
        return pagedQuerySql(instance, pageSize, startPage, DEFAULT_ROW_INDEX, false);
    }

    public static PagedQuerySql pagedQuerySql(
            QueryInstance instance,
            int pageSize,
            int startPage,
            String rowIndex,
            boolean includeRowIndex) {
        String baseSql = selectSql(instance, rowIndex);

        List<Object> countArgs = whereArgs(instance);
        List<Object> pageArgs = new ArrayList<>(countArgs);
        pageArgs.add(startPage);
        pageArgs.add(pageSize);
        pageArgs.add(startPage);
        pageArgs.add(pageSize);

        String countSql = "SELECT COUNT(*) FROM (" + baseSql + ")A";
        String pageSql = "SELECT " + pageProjection(instance, rowIndex, includeRowIndex)
                + "  FROM (" + baseSql + ")A WHERE " + rowIndex
                + ">(? -1) * ? AND " + rowIndex + "<=?*? ORDER BY " + rowIndex;

        return new PagedQuerySql(countSql, pageSql, countArgs.toArray(), pageArgs.toArray());
    }

    private static String selectedTableSql(SelectedTable table) {
        return String.format("[%s] as [%s]",
                identifier(table.getTable().getDbName()),
                identifier(table.getSelectedTableName()));
    }

    private static String rowNumberSql(List<SelectedColumn> selectedColumns, String rowIndex) {
        List<SelectedColumn> orderedColumns = selectedColumns.stream()
                .filter(column -> column.getOrderType() != OrderType.NULL)
                .collect(Collectors.toList());

        StringBuilder rowSql = new StringBuilder("ROW_NUMBER() OVER  (ORDER BY  ");
        if (orderedColumns.isEmpty()) {
            SelectedColumn firstColumn = selectedColumns.get(0);
            rowSql.append(columnExpression(firstColumn)).append(" ").append(OrderType.ASC);
        } else {
            for (SelectedColumn column : orderedColumns) {
                rowSql.append(columnExpression(column))
                        .append(" ")
                        .append(column.getOrderType())
                        .append(",");
            }
            rowSql.setLength(rowSql.length() - 1);
        }

        rowSql.append(") AS [").append(identifier(rowIndex)).append("]");
        return rowSql.toString();
    }

    private static void appendWhereSql(StringBuilder sql, QueryInstance instance) {
        QueryAndArgs where = whereSqlAndArgs(instance);
        if (where != null && where.getSql() != null && !where.getSql().isBlank()) {
            sql.append(" WHERE ").append(where.getSql());
        }
    }

    private static List<Object> whereArgs(QueryInstance instance) {
        List<Object> args = new ArrayList<>();
        QueryAndArgs where = whereSqlAndArgs(instance);
        if (where != null && where.getSql() != null && !where.getSql().isBlank() && where.getArgs() != null) {
            for (Object arg : where.getArgs()) {
                args.add(arg);
            }
        }
        return args;
    }

    private static QueryAndArgs whereSqlAndArgs(QueryInstance instance) {
        if (instance.getBoolExp() == null) {
            return null;
        }

        QueryAndArgs where = instance.getBoolExp().generateSql();
        if (where == null || where.getSql() == null || where.getSql().isBlank()) {
            return where;
        }
        return bindReportParameters(instance, where);
    }

    private static QueryAndArgs bindReportParameters(QueryInstance instance, QueryAndArgs where) {
        QueryAndArgs result = new QueryAndArgs();
        String sql = where.getSql();
        List<Object> args = new ArrayList<>();
        if (where.getArgs() != null) {
            for (Object arg : where.getArgs()) {
                args.add(arg);
            }
        }

        for (int i = 0; i < instance.getReportParams().size(); i++) {
            ReportParameter parameter = instance.getReportParams().get(i);
            String parameterName = reportParameterName(parameter, i);
            if (parameterName == null) {
                continue;
            }
            Pattern pattern = Pattern.compile(Pattern.quote(parameterName) + "(?![A-Za-z0-9_])");
            Matcher matcher = pattern.matcher(sql);
            StringBuffer replaced = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(replaced, "?");
                args.add(parameter.getValue());
            }
            matcher.appendTail(replaced);
            sql = replaced.toString();
        }

        result.setSql(sql);
        result.setArgs(args.toArray());
        return result;
    }

    private static String reportParameterName(ReportParameter parameter, int index) {
        String name = parameter.getExp();
        if (name == null || name.isBlank()) {
            name = "p" + index;
        }
        if (!name.startsWith("@")) {
            name = "@" + name;
        }
        return name;
    }

    private static String pageProjection(QueryInstance instance, String rowIndex, boolean includeRowIndex) {
        StringBuilder sql = new StringBuilder(" ");
        for (SelectedColumn column : instance.getSelectedColumns().asList().stream()
                .sorted(Comparator.comparingInt(SelectedColumn::getSelectedIndex))
                .collect(Collectors.toList())) {
            sql.append("[").append(identifier(column.getSelectedName())).append("],");
        }
        if (includeRowIndex) {
            sql.append("[").append(identifier(rowIndex)).append("],");
        }
        sql.setLength(sql.length() - 1);
        return sql.toString();
    }

    private static void appendGroupBySql(StringBuilder sql, List<SelectedColumn> selectedColumns) {
        boolean hasGroupColumn = selectedColumns.stream()
                .anyMatch(column -> column.getSelectType().isRequireGroupCol());
        boolean hasNonGroupColumn = selectedColumns.stream()
                .anyMatch(column -> !column.getSelectType().isRequireGroupCol());
        if (!hasGroupColumn || !hasNonGroupColumn) {
            return;
        }

        String groupBy = selectedColumns.stream()
                .filter(column -> !column.getSelectType().isRequireGroupCol())
                .map(QuerySqlBuilder::columnExpression)
                .collect(Collectors.joining(","));
        sql.append(" GROUP BY ").append(groupBy);
    }

    private static String columnExpression(SelectedColumn column) {
        return formatCSharp(
                column.getSelectType().getDbExp(),
                identifier(column.getSelectedTable().getSelectedTableName()),
                identifier(column.getDataColumn().getDbName()));
    }

    private static String identifier(String value) {
        if (value != null && value.startsWith("[") && value.endsWith("]") && value.length() > 1) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static String formatCSharp(String template, Object... values) {
        String result = template;
        for (int i = 0; i < values.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(values[i]));
        }
        return result.replace("{{", "{").replace("}}", "}");
    }
}
