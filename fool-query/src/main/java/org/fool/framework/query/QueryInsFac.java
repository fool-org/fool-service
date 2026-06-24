package org.fool.framework.query;

public class QueryInsFac {

    public void refreshQueryInsReportParam(QueryInstance instance) {
        if (instance.getBoolExp() != null) {
            instance.getBoolExp().generateSql();
        }
    }
}
