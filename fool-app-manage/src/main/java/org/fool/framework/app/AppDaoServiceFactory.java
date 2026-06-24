package org.fool.framework.app;

import org.fool.framework.dao.DaoService;

import java.util.function.Function;

public interface AppDaoServiceFactory extends Function<String, DaoService> {
    DaoService create(String connectionString);

    @Override
    default DaoService apply(String connectionString) {
        return create(connectionString);
    }
}
