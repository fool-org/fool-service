package org.fool.framework.query;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class SelectedColumn {
    private int selectedIndex;
    private QueryColumn dataColumn;
    private SelectType selectType;
    private OrderType orderType = OrderType.NULL;
    private String selectedName;
    private SelectedTable selectedTable;
    private List<ColStateValue> values = new ArrayList<>();

    public SelectedColumn(String selectedName, QueryColumn dataColumn) {
        this.selectedName = selectedName;
        this.dataColumn = dataColumn;
    }
}
