package org.fool.framework.query;

import org.fool.framework.dao.QueryAndArgs;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class InFilter extends SimpleFilter {


    private CompareColumn compareColumn;
    private List<CompareValue> compareValueList;


    public InFilter(String column, String... values) {
        this.compareColumn = new CompareColumn(column, column);
        this.compareValueList = new LinkedList<>();
        for (var value : values) {
            this.compareValueList.add(new CompareValue(value, value));
        }

    }


    @Override
    public QueryAndArgs generateSql() {
        QueryAndArgs andArgs = new QueryAndArgs();
        andArgs.setSql("`" + this.compareColumn.getDbValue() + "` IN  (" + this.compareValueList.stream().map(p -> "?").collect(Collectors.joining(",")));
        andArgs.setArgs(this.compareValueList.stream().map(CompareValue::getDbValue).toArray());
        return andArgs;
    }
}
