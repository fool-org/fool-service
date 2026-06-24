package org.fool.framework.report;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DataRect {
    private int colHeaderIndex;
    private int rowHeaderIndex;
    private List<Cell> cells = new ArrayList<>();
}
