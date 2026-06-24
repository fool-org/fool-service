package org.fool.framework.report;

import lombok.Data;

@Data
public class Cell {
    private int column;
    private int row;
    private int colSpan;
    private int rowSpan;
    private Object value;
    private boolean calculate;
    private CalDirection calDirection = CalDirection.Null;
    private String calScope;
    private StaticType expression;

    public String getScopeFromOffset(int offset) {
        if (calScope == null || calScope.isBlank()) {
            return "";
        }

        String[] parts = calScope.split("[,-]");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isBlank()) {
                continue;
            }
            result.append(i % 2 == 0 ? "," : "-");
            result.append(Integer.parseInt(parts[i]) + offset);
        }
        return result.length() == 0 ? "" : result.substring(1);
    }

    @Override
    public String toString() {
        return String.format(
                "Col:%d,Row%d,ColSpan:%d,RowSpan:%d,Value:%s",
                column,
                row,
                colSpan,
                rowSpan,
                value);
    }
}
