package org.fool.framework.query;

import org.fool.framework.dao.QueryAndArgs;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class SimpleFilter implements IQueryFilter {
    private CompareColumn compareColumn;
    private List<CompareValue> compareValue;
    private CompareOp compareOp;


    public SimpleFilter(String column, CompareOp op, String... values) {

        this.compareColumn = new CompareColumn(column, column);
        this.compareOp = op;
        this.compareValue = new LinkedList<>();
        for (String value : values
        ) {
            this.compareValue.add(new CompareValue(value, value));

        }
    }

    @Override
    public QueryAndArgs generateSql() {
        return null;
    }

    @Override
    public IQueryFilter and(IQueryFilter filter) {
        return new CompositeFilter(this).and(filter);
    }

    @Override
    public IQueryFilter or(IQueryFilter filter) {
        return new CompositeFilter(this).or(filter);
    }
}
