package org.fool.framework.report;

import lombok.Data;

@Data
public class ValueCell {
    private String sourceColumn;
    private String format;
    private String name;
}
