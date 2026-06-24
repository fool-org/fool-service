package org.fool.framework.query;

import org.fool.framework.dao.QueryAndArgs;

import java.util.Objects;

public class ReportCompareFilter extends SimpleFilter {
    private final QueryInstance owner;
    private final String column;
    private final CompareOp compareOp;
    private final Object value;
    private final String fmtValue;
    private final String paramName;

    public ReportCompareFilter(
            QueryInstance owner,
            String column,
            CompareOp compareOp,
            Object value,
            String fmtValue,
            String paramName) {
        this.owner = owner;
        this.column = column;
        this.compareOp = compareOp;
        this.value = value;
        this.fmtValue = fmtValue;
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
                        fmtValue);
                owner.getReportParams().add(parameter);
            } else {
                parameterValue = parameter.getValue();
            }
        }

        QueryAndArgs queryAndArgs = new QueryAndArgs();
        queryAndArgs.setSql("`" + column + "`" + compareOp.getDbValue() + " ?");
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
}
