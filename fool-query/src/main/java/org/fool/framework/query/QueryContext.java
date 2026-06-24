package org.fool.framework.query;

import org.fool.framework.common.PropertyType;

import java.util.ArrayList;
import java.util.List;

public class QueryContext {
    private final QueryFactory factory;
    private final JdbcQueryExecutor executor;
    private QueryInstance instance;

    public QueryContext(QueryFactory factory) {
        this(factory, null);
    }

    public QueryContext(QueryFactory factory, JdbcQueryExecutor executor) {
        this.factory = factory;
        this.executor = executor;
        this.instance = new QueryInstance();
    }

    public QueryInstance getInstance() {
        return instance;
    }

    public void add(QueryTable table) {
        add(table, null);
    }

    public void add(QueryTable table, SelectedTable fromTable) {
        if (fromTable == null) {
            instance.setSelectedTables(new SelectedTables(
                    new SelectedTable(table, table.getShowName()),
                    factory));
            return;
        }

        long count = instance.getSelectedTables().getTables().stream()
                .filter(selectedTable -> selectedTable.getTable().getDbName().equals(table.getDbName()))
                .count();
        String selectedName = table.getShowName() + (count == 0 ? "" : count);
        instance.getSelectedTables().add(new SelectedTable(table, selectedName), fromTable);
    }

    public void clear() {
        this.instance = new QueryInstance();
    }

    public String getSql() {
        return getSql("RowIndex");
    }

    public String getSql(String rowIndex) {
        populateEnumStateValues();
        return QuerySqlBuilder.selectSql(instance, rowIndex);
    }

    public QueryResult getResult(int pageSize) {
        return getResult(pageSize, 1);
    }

    public QueryResult getResult(int pageSize, int startPage) {
        if (executor == null) {
            throw new IllegalStateException("JdbcQueryExecutor is required to execute a QueryContext");
        }
        populateEnumStateValues();
        return executor.execute(instance, pageSize, startPage);
    }

    private void populateEnumStateValues() {
        for (SelectedColumn column : instance.getSelectedColumns()) {
            if (column.getDataColumn().getDataType() == PropertyType.Enum
                    && (column.getValues() == null || column.getValues().isEmpty())) {
                List<ColStateValue> values = factory.getStateValues(column.getDataColumn());
                column.setValues(values == null ? new ArrayList<>() : new ArrayList<>(values));
            }
        }
    }
}
