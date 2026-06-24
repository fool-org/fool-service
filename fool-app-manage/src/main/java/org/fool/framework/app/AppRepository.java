package org.fool.framework.app;

import java.util.List;

public interface AppRepository {
    ApplicationDefinition findById(String appId);

    List<ApplicationDefinition> findAll();
}
