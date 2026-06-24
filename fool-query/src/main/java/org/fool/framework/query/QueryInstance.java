package org.fool.framework.query;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QueryInstance {
    private SelectedTables selectedTables;
    private SelectedColumnCollection selectedColumns;
    private IQueryFilter boolExp;
    private List<QueryParameter> params;
    private List<ReportParameter> reportParams;

    public QueryInstance() {
        this.selectedColumns = new SelectedColumnCollection();
        this.params = new ArrayList<>();
        this.reportParams = new ArrayList<>();
    }
}
