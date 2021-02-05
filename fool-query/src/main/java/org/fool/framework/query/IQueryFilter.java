package org.fool.framework.query;

import org.fool.framework.dao.QueryAndArgs;

public interface IQueryFilter {
    QueryAndArgs generateSql();

    IQueryFilter and(IQueryFilter filter);

    IQueryFilter or(IQueryFilter filter);


}
