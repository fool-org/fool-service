package org.fool.framework.query;

import org.fool.framework.dao.QueryAndArgs;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public abstract class SimpleFilter implements IQueryFilter {

    @Override
    public IQueryFilter and(IQueryFilter filter) {
        return new CompositeFilter(this).and(filter);
    }

    @Override
    public IQueryFilter or(IQueryFilter filter) {
        return new CompositeFilter(this).or(filter);
    }
}
