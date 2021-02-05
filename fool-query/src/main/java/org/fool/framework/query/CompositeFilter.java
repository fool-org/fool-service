package org.fool.framework.query;

import org.fool.framework.dao.QueryAndArgs;

import java.util.LinkedList;
import java.util.List;

/**
 * 复杂条件表达式
 */
public class CompositeFilter implements IQueryFilter {

    private IQueryFilter first;
    private List<SeqFilter> seqFilterList;

    CompositeFilter(IQueryFilter filter) {
        this.first = filter;
        this.seqFilterList = new LinkedList<>();
    }

    @Override
    public QueryAndArgs generateSql() {
        return null;
    }

    @Override
    public IQueryFilter and(IQueryFilter filter) {
        return addSeq(BoolOp.AND, filter);
    }

    @Override
    public IQueryFilter or(IQueryFilter filter) {
        return addSeq(BoolOp.OR, filter);
    }

    private IQueryFilter addSeq(BoolOp boolOp, IQueryFilter filter) {
        if (seqFilterList == null) {
            seqFilterList = new LinkedList<>();
        }
        seqFilterList.add(new SeqFilter(boolOp, filter));
        return this;
    }
}
