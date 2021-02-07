package org.fool.framework.query;

import org.fool.framework.dao.QueryAndArgs;

public interface IQueryFilter {
    static IQueryFilter init() {

        return new IQueryFilter() {
            @Override
            public QueryAndArgs generateSql() {
                QueryAndArgs andArgs = new QueryAndArgs();
                andArgs.setSql(" 1=1 ");
                andArgs.setArgs(new Object[]{});
                return andArgs;
            }

            @Override
            public IQueryFilter and(IQueryFilter filter) {
                return new CompositeFilter(this).and(filter);
            }

            @Override
            public IQueryFilter or(IQueryFilter filter) {
                return new CompositeFilter(this).or(filter);
            }
        };
    }

    QueryAndArgs generateSql();

    IQueryFilter and(IQueryFilter filter);

    IQueryFilter or(IQueryFilter filter);


}
