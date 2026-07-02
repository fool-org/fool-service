package org.fool.framework.common.data.math;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MathExpressionTest {
    @Test
    public void detectsLegacyMathExpressionOperators() {
        assertTrue(MathExpression.isMathExpression("A+B"));
        assertTrue(MathExpression.isMathExpression("(A)"));
        assertFalse(MathExpression.isMathExpression("A"));
        assertFalse(MathExpression.isMathExpression(null));
    }

    @Test
    public void evaluatesParenthesesAndOperatorPrecedence() {
        MathExpression expression = new MathExpression();

        assertEquals(
                "191",
                expression.calculateParenthesesExpression(
                        "23+56/(102-100)*((36-24)/(8-6))",
                        value -> value));
    }

    @Test
    public void resolvesLegacyVariablesBeforeEvaluation() {
        MathExpression expression = new MathExpression();
        Map<String, String> values = Map.of("A", "1", "B", "2", "C", "4");

        assertEquals("9", expression.calculateParenthesesExpression("A+B*C", values::get));
    }

    @Test
    public void returnsZeroForInvalidExpressionLikeLegacyEvaluator() {
        MathExpression expression = new MathExpression();

        assertEquals("0", expression.calculateParenthesesExpression("A/0", value -> "0"));
        assertEquals("0", expression.calculateParenthesesExpression("A+", value -> value));
    }
}
