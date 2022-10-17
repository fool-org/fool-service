package org.fool.framework.query;

import lombok.Data;
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

    public static LinkedNode revert(LinkedNode head) {
        LinkedNode tmp = head;
        while (head.next != null) {
            tmp.next = tmp;
            tmp = head;
            head = head.next;
        }
        return tmp;
    }

    @Override
    public QueryAndArgs generateSql() {
        QueryAndArgs queryAndArgs = new QueryAndArgs();
        queryAndArgs.setSql("`" + this.compareColumn.getDbValue() + "`" + this.compareOp.getDbValue() + " ?");
        queryAndArgs.setArgs(new Object[]{compareValue.getDbValue()});
        return queryAndArgs;
    }

    @Data
    static class LinkedNode<T> {
        private T data;
        private LinkedNode next;
    }

}
