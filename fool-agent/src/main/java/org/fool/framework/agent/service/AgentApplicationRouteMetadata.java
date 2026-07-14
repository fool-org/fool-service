package org.fool.framework.agent.service;

public class AgentApplicationRouteMetadata {
    private final Long appId;
    private final String appName;
    private final String dbNo;

    public AgentApplicationRouteMetadata(Long appId, String appName, String dbNo) {
        this.appId = appId;
        this.appName = appName;
        this.dbNo = dbNo;
    }

    public Long getAppId() {
        return appId;
    }

    public String getAppName() {
        return appName;
    }

    public String getDbNo() {
        return dbNo;
    }
}
