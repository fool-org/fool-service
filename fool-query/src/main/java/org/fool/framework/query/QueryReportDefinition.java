package org.fool.framework.query;

import java.util.ArrayList;
import java.util.List;

public class QueryReportDefinition implements QueryReport {
    private final String sqlStript;
    private final List<QueryColumn> columns = new ArrayList<>();
    private final List<QueryParameter> parameters = new ArrayList<>();
    private String reportName;
    private String reportNo;

    public QueryReportDefinition(String sqlStript) {
        this.sqlStript = sqlStript;
    }

    @Override
    public String getSqlStript() {
        return sqlStript;
    }

    @Override
    public List<QueryColumn> getColumns() {
        return columns;
    }

    @Override
    public List<QueryParameter> getParameters() {
        return parameters;
    }

    @Override
    public String getReportName() {
        return reportName;
    }

    @Override
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    @Override
    public String getReportNo() {
        return reportNo;
    }

    @Override
    public void setReportNo(String reportNo) {
        this.reportNo = reportNo;
    }
}
