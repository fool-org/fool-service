package org.fool.framework.query;

import org.fool.framework.dao.QueryAndArgs;

/**
 * 值比较
 */
public class CompareFilter extends SimpleFilter {
    private CompareColumn compareColumn;
    private CompareValue compareValue;
    private CompareOp compareOp;


    public CompareFilter(String column, CompareOp op, String value) {

        this.compareColumn = new CompareColumn(column, column);
        this.compareOp = op;
        this.compareValue = new CompareValue(value, value);

    }

    @Override
    public QueryAndArgs generateSql() {
        QueryAndArgs queryAndArgs = new QueryAndArgs();
        queryAndArgs.setSql("`" + this.compareColumn.getDbValue() + "`" + this.compareOp.getDbValue()+ " ?");
        queryAndArgs.setArgs(new Object[]{compareValue.getDbValue()});
        return queryAndArgs;
    }

}
