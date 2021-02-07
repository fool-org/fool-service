package org.fool.framework.query;


import lombok.Getter;

@Getter
public class SeqFilter {
    private BoolOp boolOp;
    private IQueryFilter seqExp;

    public SeqFilter(BoolOp boolOp, IQueryFilter filter) {
        this.boolOp = boolOp;
        this.seqExp = filter;
    }
}
