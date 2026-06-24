package org.fool.framework.query;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SelectedTablesTest {

    @Test
    public void addUsesLegacyJoinConditionAndFlipsColumnsWhenFactoryDirectionIsReversed() {
        SelectedTable orders = selectedTable("Orders", "orders", "o");
        SelectedTable items = selectedTable("Items", "order_items", "i");
        JoinTable factoryJoin = join(items, orders, "ORDER_ID", "ID");

        SelectedTables selectedTables = new SelectedTables(
                orders,
                (table, joinType) -> List.of(factoryJoin));

        selectedTables.add(items, orders);

        assertEquals(2, selectedTables.getTables().size());
        assertEquals(1, selectedTables.getJoins().size());

        JoinTable join = selectedTables.getJoins().get(0);
        assertEquals(orders, join.getLeftTable());
        assertEquals(items, join.getRightTable());
        assertEquals("ID", join.getConditions().get(0).getLeftCol());
        assertEquals("ORDER_ID", join.getConditions().get(0).getRightCol());
    }

    @Test
    public void addRejectsFromTableThatIsNotSelected() {
        SelectedTable orders = selectedTable("Orders", "orders", "o");
        SelectedTable items = selectedTable("Items", "order_items", "i");

        SelectedTables selectedTables = new SelectedTables(
                orders,
                (table, joinType) -> List.of());

        try {
            selectedTables.add(items, selectedTable("Unknown", "unknown", "u"));
            fail("expected unselected source table to be rejected");
        } catch (IllegalArgumentException ex) {
            assertEquals("要加的表没有选择！", ex.getMessage());
        }
    }

    @Test
    public void addRejectsTablesWithoutJoinCondition() {
        SelectedTable orders = selectedTable("Orders", "orders", "o");
        SelectedTable items = selectedTable("Items", "order_items", "i");

        SelectedTables selectedTables = new SelectedTables(
                orders,
                (table, joinType) -> List.of());

        try {
            selectedTables.add(items, orders);
            fail("expected missing join condition to be rejected");
        } catch (IllegalArgumentException ex) {
            assertEquals("未找到连接条件", ex.getMessage());
        }
    }

    @Test
    public void tryAddReturnsLegacyResultContractWithoutThrowing() {
        SelectedTable orders = selectedTable("Orders", "orders", "o");
        SelectedTable items = selectedTable("Items", "order_items", "i");
        JoinTable factoryJoin = join(orders, items, "ID", "ORDER_ID");
        SelectedTables selectedTables = new SelectedTables(
                orders,
                (table, joinType) -> List.of(factoryJoin));

        assertEquals(AddQueryTable.Success, selectedTables.tryAdd(items, orders));
        assertEquals(AddQueryTable.Exists, selectedTables.tryAdd(items, orders));
        assertEquals(AddQueryTable.NoRelation,
                selectedTables.tryAdd(selectedTable("Payment", "payments", "p"), orders));
        assertEquals(2, selectedTables.getTables().size());
        assertEquals(1, selectedTables.getJoins().size());
    }

    private SelectedTable selectedTable(String showName, String dbName, String selectedName) {
        return new SelectedTable(new QueryTable(showName, dbName), selectedName);
    }

    private JoinTable join(SelectedTable left, SelectedTable right, String leftCol, String rightCol) {
        JoinTable join = new JoinTable();
        join.setLeftTable(left);
        join.setRightTable(right);
        join.getConditions().add(new JoinCondition(leftCol, rightCol));
        return join;
    }
}
