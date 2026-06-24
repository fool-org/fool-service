package org.fool.framework.query;

public class BoolExpressionFactory {
    private final QueryInstance ins;

    public BoolExpressionFactory(QueryInstance ins) {
        this.ins = ins;
    }

    public QueryInstance getIns() {
        return ins;
    }

    public IQueryFilter createBoolExpression(
            CompareCol col,
            CompareOp op,
            Object value,
            String showValue,
            String paramName) {
        return new SimpleBoolExpression(col, op, value, showValue, ins, paramName);
    }

    public void addBoolExpression(BoolExpression exp1, BoolOp op, BoolExpression exp2) {
        requireExpression(exp1);
        requireExpression(exp2);
        exp1.setExp(add(exp1.getExp(), op, exp2.getExp()));
    }

    public void addBoolExpression(
            BoolExpression exp1,
            BoolOp op,
            CompareCol col,
            CompareOp comOp,
            Object value,
            String showValue,
            String paramName) {
        requireExpression(exp1);
        QueryInstance owner = exp1.getQueryIns() == null ? ins : exp1.getQueryIns();
        BoolExpression exp2 = new BoolExpression(
                owner,
                new SimpleBoolExpression(col, comOp, value, showValue, owner, paramName));
        addBoolExpression(exp1, op, exp2);
    }

    private IQueryFilter add(IQueryFilter left, BoolOp op, IQueryFilter right) {
        if (op == BoolOp.OR) {
            return left.or(right);
        }
        if (op == BoolOp.AND) {
            return left.and(right);
        }
        throw new IllegalArgumentException("unsupported bool operator: " + op);
    }

    private void requireExpression(BoolExpression expression) {
        if (expression == null || expression.getExp() == null) {
            throw new IllegalArgumentException("bool expression is required");
        }
    }
}
