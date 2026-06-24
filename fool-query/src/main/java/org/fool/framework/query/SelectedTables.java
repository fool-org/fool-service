package org.fool.framework.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SelectedTables {
    private final List<SelectedTable> tables = new ArrayList<>();
    private final List<JoinTable> joins = new ArrayList<>();
    private final QueryFactory factory;

    public SelectedTables(SelectedTable first, QueryFactory factory) {
        tables.add(first);
        this.factory = factory;
    }

    public void add(SelectedTable table, SelectedTable from) {
        if (!tables.contains(from)) {
            throw new IllegalArgumentException("要加的表没有选择！");
        }

        JoinTable condition = factory.getCanJoinedTables(from.getTable(), JoinQueryType.All).stream()
                .filter(candidate -> matchesEitherDirection(candidate, table, from))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到连接条件"));

        JoinTable addJoin = new JoinTable();
        addJoin.setLeftTable(from);
        addJoin.setRightTable(table);
        if (sameDbName(condition.getLeftTable(), table)) {
            for (JoinCondition joinCondition : condition.getConditions()) {
                addJoin.getConditions().add(
                        new JoinCondition(joinCondition.getRightCol(), joinCondition.getLeftCol()));
            }
        } else {
            addJoin.getConditions().addAll(condition.getConditions());
        }

        joins.add(addJoin);
        tables.add(table);
    }

    public AddQueryTable tryAdd(SelectedTable table, SelectedTable from) {
        if (tables.contains(table)) {
            return AddQueryTable.Exists;
        }

        try {
            add(table, from);
            return AddQueryTable.Success;
        } catch (IllegalArgumentException ex) {
            return AddQueryTable.NoRelation;
        }
    }

    public List<SelectedTable> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public List<JoinTable> getJoins() {
        return Collections.unmodifiableList(joins);
    }

    private boolean matchesEitherDirection(JoinTable candidate, SelectedTable table, SelectedTable from) {
        return sameDbName(candidate.getLeftTable(), table) && sameDbName(candidate.getRightTable(), from)
                || sameDbName(candidate.getLeftTable(), from) && sameDbName(candidate.getRightTable(), table);
    }

    private boolean sameDbName(SelectedTable left, SelectedTable right) {
        return Objects.equals(left.getTable().getDbName(), right.getTable().getDbName());
    }
}
