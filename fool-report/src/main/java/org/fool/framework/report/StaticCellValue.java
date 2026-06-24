package org.fool.framework.report;

import lombok.Data;

@Data
class StaticCellValue {
    private StaticFormat cellFormat;
    private Object[] staticValue;
    private int staticIndex;
    private String staticFilter;
}
