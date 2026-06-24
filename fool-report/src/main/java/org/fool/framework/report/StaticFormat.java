package org.fool.framework.report;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StaticFormat {
    private List<StaticCellFormate> staticsCells = new ArrayList<>();
    private String name;
    private String fileter;
}
