package org.fool.framework.agent.service;

public class AgentDataSourceRouteMetadata {
    private final String dataSourceKey;
    private final String dbNo;

    public AgentDataSourceRouteMetadata(String dataSourceKey, String dbNo) {
        this.dataSourceKey = dataSourceKey;
        this.dbNo = dbNo;
    }

    public String getDataSourceKey() {
        return dataSourceKey;
    }

    public String getDbNo() {
        return dbNo;
    }
}
