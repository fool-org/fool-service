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
        QueryAndArgs queryAndArgs = new QueryAndArgs();
        StringBuilder builder = new StringBuilder();
        var first = this.first.generateSql();
        builder = builder.append(first.getSql());
        var params = first.getArgs();
        for (var seq : this.seqFilterList
        ) {
            var seqSql = seq.getSeqExp().generateSql();
            builder = builder.append(seq.getBoolOp().toString());
            builder = builder.append(" ( " + seqSql.getSql() + " ) ");
            params = merge(params, seqSql.getArgs());
        }
        queryAndArgs.setSql(builder.toString());
        queryAndArgs.setArgs(params);
        return queryAndArgs;
    }

    private Object[] merge(Object[] params, Object[] args) {
        Object[] result = new Object[params.length + args.length];
        System.arraycopy(params, 0, result, 0, params.length);
        System.arraycopy(args, 0, result, params.length, args.length);
        return result;
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
