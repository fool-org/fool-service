package org.fool.framework.query;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JoinTable {
    private SelectedTable leftTable;
    private SelectedTable rightTable;
    private List<JoinCondition> conditions = new ArrayList<>();

    public JoinTable convert() {
        JoinTable result = new JoinTable();
        result.setLeftTable(rightTable);
        result.setRightTable(leftTable);
        for (JoinCondition condition : conditions) {
            result.getConditions().add(new JoinCondition(condition.getRightCol(), condition.getLeftCol()));
        }
        return result;
    }
}
