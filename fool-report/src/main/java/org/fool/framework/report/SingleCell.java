package org.fool.framework.report;

import lombok.Data;

@Data
public class SingleCell {
    private String value;
    private String expression;
    private int span = 1;
    private int otherSpan = 1;
    private boolean megerToParent;

    @Override
    public String toString() {
        return String.format("%s %d", value, span);
    }
}
