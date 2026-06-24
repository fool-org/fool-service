package org.fool.framework.report;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MatrixTable {
    private List<List<SingleCell>> colHeaders = new ArrayList<>();
    private List<List<SingleCell>> rowHeaders = new ArrayList<>();
    private List<DataRect> cells = new ArrayList<>();
}
