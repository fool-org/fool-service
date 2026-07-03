package org.fool.framework.query;

import org.fool.framework.common.PropertyType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class QueryContext {
    private final QueryFactory factory;
    private final Function<String, JdbcQueryExecutor> executorFactory;
    private final String queryConnectionString;
    private boolean canJoinSelected;
    private QueryInstance instance;

    public QueryContext(QueryFactory factory) {
        this(factory, (Function<String, JdbcQueryExecutor>) null, null);
    }

    public QueryContext(QueryFactory factory, JdbcQueryExecutor executor) {
        this(factory, ignored -> executor, null);
    }

    public QueryContext(QueryFactory factory, String queryConnectionString) {
        this(factory, (Function<String, JdbcQueryExecutor>) null, queryConnectionString);
    }

    public QueryContext(QueryFactory factory, JdbcQueryExecutor executor, String queryConnectionString) {
        this(factory, ignored -> executor, queryConnectionString);
    }

    QueryContext(QueryFactory factory, Function<String, JdbcQueryExecutor> executorFactory, String queryConnectionString) {
        this.factory = factory;
        this.executorFactory = executorFactory;
        this.queryConnectionString = queryConnectionString;
        this.instance = new QueryInstance();
    }

    public QueryInstance getInstance() {
        return instance;
    }

    public String getQueryConnectionString() {
        return queryConnectionString;
    }

    public boolean isCanJoinSelected() {
        return canJoinSelected;
    }

    public void setCanJoinSelected(boolean canJoinSelected) {
        this.canJoinSelected = canJoinSelected;
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

    public void save() {
        throw new UnsupportedOperationException("NotImplementedException");
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

    public QueryResult getResult(String connectionString, int pageSize) {
        return getResult(connectionString, pageSize, 1);
    }

    public QueryResult getResult(String connectionString, int pageSize, int startPage) {
        JdbcQueryExecutor runtimeExecutor = executorFor(connectionString);
        populateEnumStateValues();
        return runtimeExecutor.execute(instance, pageSize, startPage);
    }

    public QueryResult getResult(int pageSize, int startPage) {
        return getResult(queryConnectionString, pageSize, startPage);
    }

    private JdbcQueryExecutor executorFor(String connectionString) {
        if (executorFactory != null) {
            JdbcQueryExecutor executor = executorFactory.apply(connectionString);
            if (executor != null) {
                return executor;
            }
        }
        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalStateException("JdbcQueryExecutor is required to execute a QueryContext");
        }
        return new JdbcQueryExecutor(connectionString);
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
