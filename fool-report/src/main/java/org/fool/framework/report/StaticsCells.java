package org.fool.framework.report;

import lombok.Data;

import java.util.List;

@Data
public class StaticsCells {
    private List<StaticCellFormate> staticFormat;
    private ValueCell valueCell;
}
