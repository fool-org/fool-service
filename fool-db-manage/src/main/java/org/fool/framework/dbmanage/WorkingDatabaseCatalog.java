package org.fool.framework.dbmanage;

import java.util.List;

@FunctionalInterface
public interface WorkingDatabaseCatalog {
    List<WorkingDatabase> findAll();
}
