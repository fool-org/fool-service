package org.fool.framework.dbmanage;

import java.util.List;

public interface WorkingDatabaseRepository extends WorkingDatabaseCatalog {
    List<WorkingDatabase> findByAppName(String appName);

    WorkingDatabase findByNameAndYear(String name, String year);

    WorkingDatabase findByNameAndYearExceptCode(String name, String year, String code);

    String findMaxCode();

    void insert(WorkingDatabase database);

    void update(WorkingDatabase database);

    void deleteByCode(String code);
}
