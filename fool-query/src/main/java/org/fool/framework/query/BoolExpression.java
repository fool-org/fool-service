package org.fool.framework.query;

public class BoolExpression {
    private QueryInstance queryIns;
    private IQueryFilter exp;

    public BoolExpression() {
    }

    public BoolExpression(QueryInstance queryIns, IQueryFilter exp) {
        this.queryIns = queryIns;
        this.exp = exp;
    }

    public QueryInstance getQueryIns() {
        return queryIns;
    }

    public void setQueryIns(QueryInstance queryIns) {
        this.queryIns = queryIns;
    }

    public IQueryFilter getExp() {
        return exp;
    }

    public void setExp(IQueryFilter exp) {
        this.exp = exp;
    }
}
