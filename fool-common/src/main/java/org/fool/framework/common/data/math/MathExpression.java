package org.fool.framework.common.data.math;

import java.math.BigDecimal;
import java.util.function.Function;

public class MathExpression {
    private static final String OPERATORS = "+-()*/";

    public static boolean isMathExpression(String expression) {
        if (expression == null) {
            return false;
        }
        for (int i = 0; i < expression.length(); i++) {
            if (OPERATORS.indexOf(expression.charAt(i)) >= 0) {
                return true;
            }
        }
        return false;
    }

    public String calculateParenthesesExpression(String expression, Function<String, String> getOperator) {
        try {
            Function<String, String> resolver = getOperator == null ? Function.identity() : getOperator;
            return format(new Parser(expression, resolver).parse());
        } catch (RuntimeException ex) {
            return "0";
        }
    }

    private static String format(double value) {
        if (!Double.isFinite(value)) {
            return "0";
        }
        if (value == java.lang.Math.rint(value)) {
            return Long.toString((long) value);
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static final class Parser {
        private final String expression;
        private final Function<String, String> resolver;
        private int index;

        private Parser(String expression, Function<String, String> resolver) {
            this.expression = expression == null ? "" : expression;
            this.resolver = resolver;
        }

        private double parse() {
            double value = parseExpression();
            skipSpaces();
            if (index != expression.length()) {
                throw new IllegalArgumentException("Unexpected expression tail");
            }
            return value;
        }

        private double parseExpression() {
            double value = parseTerm();
            while (true) {
                skipSpaces();
                if (match('+')) {
                    value += parseTerm();
                } else if (match('-')) {
                    value -= parseTerm();
                } else {
                    return value;
                }
            }
        }

        private double parseTerm() {
            double value = parseFactor();
            while (true) {
                skipSpaces();
                if (match('*')) {
                    value *= parseFactor();
                } else if (match('/')) {
                    double divisor = parseFactor();
                    if (divisor == 0) {
                        throw new ArithmeticException("Division by zero");
                    }
                    value /= divisor;
                } else {
                    return value;
                }
            }
        }

        private double parseFactor() {
            skipSpaces();
            if (match('(')) {
                double value = parseExpression();
                if (!match(')')) {
                    throw new IllegalArgumentException("Missing closing parenthesis");
                }
                return value;
            }
            String token = readToken();
            if (token.isEmpty()) {
                throw new IllegalArgumentException("Missing operand");
            }
            return Double.parseDouble(resolver.apply(token));
        }

        private String readToken() {
            int start = index;
            while (index < expression.length() && OPERATORS.indexOf(expression.charAt(index)) < 0) {
                index++;
            }
            return expression.substring(start, index).trim();
        }

        private boolean match(char expected) {
            if (index < expression.length() && expression.charAt(index) == expected) {
                index++;
                return true;
            }
            return false;
        }

        private void skipSpaces() {
            while (index < expression.length() && Character.isWhitespace(expression.charAt(index))) {
                index++;
            }
        }
    }
}
