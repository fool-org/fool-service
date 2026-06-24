package org.fool.framework.query;

import org.fool.framework.dao.QueryAndArgs;

import java.util.Objects;

public class SimpleBoolExpression extends SimpleFilter {
    private final CompareCol col;
    private final CompareOp op;
    private Object value;
    private String valueStr;
    private final QueryInstance owner;
    private final String paramName;

    public SimpleBoolExpression(
            CompareCol col,
            CompareOp op,
            Object value,
            String valueStr,
            QueryInstance owner,
            String paramName) {
        this.col = col;
        this.op = op;
        this.value = value;
        this.valueStr = valueStr;
        this.owner = owner;
        this.paramName = paramName;
    }

    @Override
    public QueryAndArgs generateSql() {
        return generateSql(0);
    }

    @Override
    public QueryAndArgs generateSql(int parameterStartIndex) {
        Object parameterValue = value;
        if (hasParamName()) {
            ReportParameter parameter = findReportParameter();
            if (parameter == null) {
                parameter = new ReportParameter(
                        paramName,
                        "@p" + parameterStartIndex,
                        value == null ? "" : value.toString(),
                        valueStr);
                owner.getReportParams().add(parameter);
            } else {
                valueStr = parameter.getFmtValue();
                value = parameter.getValue();
                parameterValue = parameter.getValue();
            }
        }

        QueryAndArgs queryAndArgs = new QueryAndArgs();
        queryAndArgs.setSql(columnSql() + op.getDbValue() + " ?");
        queryAndArgs.setArgs(new Object[]{parameterValue});
        return queryAndArgs;
    }

    private boolean hasParamName() {
        return paramName != null && !paramName.isBlank();
    }

    private ReportParameter findReportParameter() {
        return owner.getReportParams().stream()
                .filter(parameter -> Objects.equals(parameter.getName(), paramName))
                .findFirst()
                .orElse(null);
    }

    private String columnSql() {
        return "[" + col.getSelectedTableName() + "].[" + col.getCol().getDbName() + "]";
    }

    @Override
    public String toString() {
        return valueOrEmpty(col.getSelectedTableName())
                + "."
                + valueOrEmpty(col.getCol().getShowName())
                + " "
                + valueOrEmpty(op.getDisPlayValue())
                + " "
                + valueOrEmpty(valueStr);
    }

    private String valueOrEmpty(Object value) {
        return value == null ? "" : value.toString();
    }
}
