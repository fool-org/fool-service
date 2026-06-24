package org.fool.framework.dbmanage;

public class DataBaseSourceResolver {
    private final DataBaseSourceRepository dataBaseSourceRepository;
    private final WorkingDatabaseCatalog workingDatabaseCatalog;
    private final String masterConnectionString;
    private final String applicationName;

    public DataBaseSourceResolver(
            DataBaseSourceRepository dataBaseSourceRepository,
            WorkingDatabaseCatalog workingDatabaseCatalog,
            String masterConnectionString,
            String applicationName) {
        this.dataBaseSourceRepository = dataBaseSourceRepository;
        this.workingDatabaseCatalog = workingDatabaseCatalog;
        this.masterConnectionString = masterConnectionString;
        this.applicationName = applicationName;
    }

    public String getConnectionString(String key) {
        DataBaseSource dataBaseSource = dataBaseSourceRepository.findByKey(key);
        if (dataBaseSource == null) {
            return null;
        }
        return workingDatabaseCatalog.findAll().stream()
                .filter(db -> dataBaseSource.getDbNo().equals(db.getCode()))
                .findFirst()
                .map(db -> db.buildConnectionString(masterConnectionString, applicationName))
                .orElse(null);
    }
}
