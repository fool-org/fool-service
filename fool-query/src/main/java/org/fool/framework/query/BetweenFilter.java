package org.fool.framework.query;

import org.fool.framework.dao.QueryAndArgs;

/**
 * between 的表达式
 */
public class BetweenFilter extends SimpleFilter {


    private CompareColumn compareColumn;
    private CompareValue compareBottom;
    private CompareValue compareTop;


    public BetweenFilter(String column, String bottom, String top) {
        this.compareColumn = new CompareColumn(column, column);
        this.compareTop = new CompareValue(top, top);
        this.compareBottom = new CompareValue(bottom, bottom);
    }


    @Override
    public QueryAndArgs generateSql() {
        QueryAndArgs andArgs = new QueryAndArgs();
        andArgs.setSql("`" + this.compareColumn.getDbValue() + "` ? AND ? ");
        andArgs.setArgs(new Object[]{this.compareBottom.getDbValue(), this.compareTop.getDbValue()});
        return andArgs;
    }
}
