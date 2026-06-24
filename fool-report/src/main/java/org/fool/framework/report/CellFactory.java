package org.fool.framework.report;

import java.util.Comparator;
import java.util.List;

public class CellFactory {
    public void ordCells(List<Cell> items) {
        items.sort(Comparator
                .comparingInt(Cell::getColumn)
                .thenComparingInt(Cell::getRow));
    }
}
