package org.fool.framework.agent.service;

public class AgentWorkingDatabaseMetadata {
    private final Long dbId;
    private final String dbName;
    private final String dbYear;
    private final String dbSysName;
    private final Boolean active;
    private final String dbNo;
    private final String userName;
    private final String companyName;
    private final String serverIp;
    private final Boolean local;
    private final Boolean credentialConfigured;

    public AgentWorkingDatabaseMetadata(Long dbId,
                                        String dbName,
                                        String dbYear,
                                        String dbSysName,
                                        Boolean active,
                                        String dbNo,
                                        String userName,
                                        String companyName,
                                        String serverIp,
                                        Boolean local,
                                        Boolean credentialConfigured) {
        this.dbId = dbId;
        this.dbName = dbName;
        this.dbYear = dbYear;
        this.dbSysName = dbSysName;
        this.active = active;
        this.dbNo = dbNo;
        this.userName = userName;
        this.companyName = companyName;
        this.serverIp = serverIp;
        this.local = local;
        this.credentialConfigured = credentialConfigured;
    }

    public Long getDbId() {
        return dbId;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbYear() {
        return dbYear;
    }

    public String getDbSysName() {
        return dbSysName;
    }

    public Boolean getActive() {
        return active;
    }

    public String getDbNo() {
        return dbNo;
    }

    public String getUserName() {
        return userName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getServerIp() {
        return serverIp;
    }

    public Boolean getLocal() {
        return local;
    }

    public Boolean getCredentialConfigured() {
        return credentialConfigured;
    }
}
