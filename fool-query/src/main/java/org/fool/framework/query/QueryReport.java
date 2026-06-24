package org.fool.framework.query;

import java.util.List;

public interface QueryReport {
    String getSqlStript();

    List<QueryColumn> getColumns();

    List<QueryParameter> getParameters();

    String getReportName();

    void setReportName(String reportName);

    String getReportNo();

    void setReportNo(String reportNo);
}
