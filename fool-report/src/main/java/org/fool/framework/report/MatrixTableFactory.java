package org.fool.framework.report;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MatrixTableFactory {
    public MatrixTable createMatrixTable(TableFormat format, List<Map<String, Object>> source) {
        MatrixTable table = new MatrixTable();
        HeaderBuild colHeaders = buildHeaders(format.getColums(), source);
        HeaderBuild rowHeaders = buildHeaders(format.getRows(), source);
        table.getColHeaders().addAll(colHeaders.cells());
        table.getRowHeaders().addAll(rowHeaders.cells());

        for (Map<String, Object> row : source) {
            DataRect rect = new DataRect();
            rect.setColHeaderIndex(colHeaders.indexOf(pathValues(format.getColums(), row)));
            rect.setRowHeaderIndex(rowHeaders.indexOf(pathValues(format.getRows(), row)));
            for (ValueCell valueCell : format.getValueCell()) {
                Cell cell = new Cell();
                cell.setValue(row.get(valueCell.getSourceColumn()));
                rect.getCells().add(cell);
            }
            table.getCells().add(rect);
        }

        addStaticDataRects(table, rowHeaders, colHeaders, CalDirection.Column);
        addStaticDataRects(table, colHeaders, rowHeaders, CalDirection.Row);

        return table;
    }

    public List<Cell> getCells(MatrixTable table) {
        return getCells(table, 0, 0);
    }

    public List<Cell> getCells(MatrixTable table, int colStart, int rowStart) {
        List<Cell> result = new ArrayList<>();
        int currentCol;
        int currentRow = rowStart;

        for (List<SingleCell> cols : table.getColHeaders()) {
            currentCol = colStart + table.getRowHeaders().size();
            for (SingleCell col : cols) {
                Cell cell = new Cell();
                cell.setColSpan(col.getSpan());
                cell.setColumn(currentCol);
                cell.setRowSpan(1);
                cell.setRow(currentRow);
                cell.setValue(col.getValue());
                if (!col.isMegerToParent()) {
                    result.add(cell);
                } else {
                    Cell parent = findColumnParent(result, currentCol, col.getSpan(), currentRow);
                    if (parent != null) {
                        parent.setRowSpan(parent.getRowSpan() + 1);
                    }
                }
                currentCol += col.getSpan();
            }
            currentRow++;
        }

        currentCol = colStart;
        for (List<SingleCell> rows : table.getRowHeaders()) {
            currentRow = rowStart + table.getColHeaders().size();
            for (SingleCell row : rows) {
                Cell cell = new Cell();
                cell.setColSpan(1);
                cell.setColumn(currentCol);
                cell.setRow(currentRow);
                cell.setRowSpan(row.getSpan());
                cell.setValue(row.getValue());
                if (!row.isMegerToParent()) {
                    result.add(cell);
                } else {
                    Cell parent = findRowParent(result, currentRow, row.getSpan(), currentCol);
                    if (parent != null) {
                        parent.setColSpan(parent.getColSpan() + 1);
                    }
                }
                currentRow += row.getSpan();
            }
            currentCol++;
        }

        currentRow = rowStart + table.getColHeaders().size();
        currentCol = colStart + table.getRowHeaders().size();
        for (DataRect cells : table.getCells()) {
            for (Cell sourceCell : cells.getCells()) {
                Cell cell = new Cell();
                cell.setColSpan(1);
                cell.setRowSpan(1);
                cell.setColumn(sourceCell.getColumn() + currentCol + cells.getColHeaderIndex());
                cell.setRow(sourceCell.getRow() + currentRow + cells.getRowHeaderIndex());
                cell.setCalculate(sourceCell.isCalculate());
                cell.setCalDirection(sourceCell.getCalDirection());
                cell.setCalScope(shiftScope(sourceCell, currentCol, currentRow));
                cell.setExpression(sourceCell.getExpression());
                cell.setValue(sourceCell.getValue());
                result.add(cell);
            }
        }
        return result;
    }

    private static Cell findColumnParent(List<Cell> cells, int currentCol, int span, int currentRow) {
        return cells.stream()
                .filter(cell -> cell.getColumn() == currentCol
                        && cell.getColSpan() == span
                        && cell.getRow() < currentRow)
                .findFirst()
                .orElse(null);
    }

    private static Cell findRowParent(List<Cell> cells, int currentRow, int span, int currentCol) {
        return cells.stream()
                .filter(cell -> cell.getRow() == currentRow
                        && cell.getRowSpan() == span
                        && cell.getColumn() < currentCol)
                .findFirst()
                .orElse(null);
    }

    private static String shiftScope(Cell cell, int currentCol, int currentRow) {
        if (cell.getCalDirection() == CalDirection.Column) {
            return cell.getScopeFromOffset(currentRow);
        }
        if (cell.getCalDirection() == CalDirection.Row) {
            return cell.getScopeFromOffset(currentCol);
        }
        return "";
    }

    private static HeaderBuild buildHeaders(List<CellFormat> formats, List<Map<String, Object>> source) {
        if (formats.isEmpty()) {
            return new HeaderBuild(List.of(), Map.of(List.of(), 0), List.of(), List.of(), Map.of());
        }

        HeaderNode root = new HeaderNode(null);
        List<List<Object>> realPaths = new ArrayList<>();
        for (Map<String, Object> row : source) {
            List<Object> path = pathValues(formats, row);
            if (!realPaths.contains(path)) {
                realPaths.add(path);
            }
            root.add(path, 0);
        }
        addStaticHeaderPaths(root, formats, realPaths);

        List<List<SingleCell>> cells = new ArrayList<>();
        collectCells(root.children(), 0, cells);

        Map<List<Object>, Integer> indexes = new LinkedHashMap<>();
        List<HeaderNode> leaves = new ArrayList<>();
        collectLeafIndexes(root.children(), new ArrayList<>(), indexes, leaves);
        List<HeaderNode> staticNodes = new ArrayList<>();
        collectStaticNodes(root.children(), staticNodes);
        Map<HeaderNode, Integer> leafIndexes = new IdentityHashMap<>();
        for (int i = 0; i < leaves.size(); i++) {
            leafIndexes.put(leaves.get(i), i);
        }
        return new HeaderBuild(cells, indexes, leaves, staticNodes, leafIndexes);
    }

    private static List<Object> pathValues(List<CellFormat> formats, Map<String, Object> row) {
        return formats.stream()
                .map(format -> row.get(format.getSourceColumn()))
                .toList();
    }

    private static void collectCells(HeaderNode node, int level, List<List<SingleCell>> result) {
        if (result.size() <= level) {
            result.add(new ArrayList<>());
        }
        result.get(level).add(toSingleCell(node));
        for (HeaderNode child : node.children()) {
            collectCells(child, level + 1, result);
        }
    }

    private static void collectCells(List<HeaderNode> nodes, int level, List<List<SingleCell>> result) {
        for (HeaderNode node : nodes) {
            collectCells(node, level, result);
        }
    }

    private static SingleCell toSingleCell(HeaderNode node) {
        SingleCell cell = new SingleCell();
        cell.setValue(node.value() == ReportEmptyValue.getValue() ? "" : Objects.toString(node.value(), ""));
        cell.setSpan(node.width());
        cell.setMegerToParent(node.value() == ReportEmptyValue.getValue());
        return cell;
    }

    private static void collectLeafIndexes(
            List<HeaderNode> nodes,
            List<Object> path,
            Map<List<Object>, Integer> result,
            List<HeaderNode> leaves) {
        for (HeaderNode node : nodes) {
            List<Object> currentPath = new ArrayList<>(path);
            currentPath.add(node.value());
            if (node.children().isEmpty()) {
                leaves.add(node);
                result.put(currentPath, result.size());
            } else {
                collectLeafIndexes(node.children(), currentPath, result, leaves);
            }
        }
    }

    private static void collectStaticNodes(List<HeaderNode> nodes, List<HeaderNode> result) {
        for (HeaderNode node : nodes) {
            if (node.staticFormat() != null) {
                result.add(node);
            }
            collectStaticNodes(node.children(), result);
        }
    }

    private static void addStaticHeaderPaths(
            HeaderNode root,
            List<CellFormat> formats,
            List<List<Object>> realPaths) {
        for (List<Object> realPath : realPaths) {
            for (int level = 0; level < formats.size(); level++) {
                for (StaticFormat staticFormat : formats.get(level).getStaticFormats()) {
                    List<Object> staticPath = new ArrayList<>(realPath);
                    staticPath.set(level, staticFormat.getName());
                    for (int next = level + 1; next < staticPath.size(); next++) {
                        staticPath.set(next, ReportEmptyValue.getValue());
                    }
                    root.add(staticPath, 0, staticFormat, level);
                }
            }
        }
    }

    private static void addStaticDataRects(
            MatrixTable table,
            HeaderBuild staticAxis,
            HeaderBuild otherAxis,
            CalDirection direction) {
        for (HeaderNode staticNode : staticAxis.staticNodes()) {
            int staticIndex = staticAxis.firstLeafIndex(staticNode);
            String calScope = staticAxis.calScopeBefore(staticNode);
            int otherCount = otherAxis.leafCount();
            for (int otherIndex = 0; otherIndex < otherCount; otherIndex++) {
                DataRect rect = new DataRect();
                if (direction == CalDirection.Column) {
                    rect.setColHeaderIndex(otherIndex);
                    rect.setRowHeaderIndex(staticIndex);
                } else {
                    rect.setColHeaderIndex(staticIndex);
                    rect.setRowHeaderIndex(otherIndex);
                }

                for (StaticCellFormate staticCell : staticNode.staticFormat().getStaticsCells()) {
                    Cell cell = new Cell();
                    cell.setValue(staticNode.staticFormat().getName() + otherIndex);
                    cell.setCalculate(true);
                    cell.setCalDirection(direction);
                    cell.setCalScope(calScope);
                    cell.setExpression(staticCell.getStaticType());
                    rect.getCells().add(cell);
                }
                table.getCells().add(rect);
            }
        }
    }

    private record HeaderBuild(
            List<List<SingleCell>> cells,
            Map<List<Object>, Integer> indexes,
            List<HeaderNode> leaves,
            List<HeaderNode> staticNodes,
            Map<HeaderNode, Integer> leafIndexes) {
        private int indexOf(List<Object> path) {
            return indexes.getOrDefault(path, 0);
        }

        private int leafCount() {
            return leaves.size();
        }

        private int firstLeafIndex(HeaderNode node) {
            HeaderNode leaf = firstLeaf(node);
            return leafIndexes.getOrDefault(leaf, 0);
        }

        private String calScopeBefore(HeaderNode staticNode) {
            int staticIndex = firstLeafIndex(staticNode);
            List<String> ranges = new ArrayList<>();
            int start = -1;
            int last = -1;
            for (HeaderNode leaf : leavesUnder(staticNode.parent())) {
                int index = leafIndexes.getOrDefault(leaf, -1);
                if (index < 0 || index >= staticIndex) {
                    break;
                }
                if (leaf.hasStaticAncestor()) {
                    if (start >= 0) {
                        ranges.add(start + "-" + last);
                        start = -1;
                    }
                    last = -1;
                    continue;
                }
                if (start < 0) {
                    start = index;
                } else if (index != last + 1) {
                    ranges.add(start + "-" + last);
                    start = index;
                }
                last = index;
            }
            if (start >= 0) {
                ranges.add(start + "-" + last);
            }
            return String.join(",", ranges);
        }

        private static HeaderNode firstLeaf(HeaderNode node) {
            HeaderNode current = node;
            while (!current.children().isEmpty()) {
                current = current.children().get(0);
            }
            return current;
        }

        private static List<HeaderNode> leavesUnder(HeaderNode node) {
            List<HeaderNode> result = new ArrayList<>();
            collectLeaves(node.children(), result);
            return result;
        }

        private static void collectLeaves(List<HeaderNode> nodes, List<HeaderNode> result) {
            for (HeaderNode node : nodes) {
                if (node.children().isEmpty()) {
                    result.add(node);
                } else {
                    collectLeaves(node.children(), result);
                }
            }
        }
    }

    private static final class HeaderNode {
        private final Object value;
        private final HeaderNode parent;
        private final List<HeaderNode> children = new ArrayList<>();
        private StaticFormat staticFormat;

        private HeaderNode(Object value) {
            this(value, null);
        }

        private HeaderNode(Object value, HeaderNode parent) {
            this.value = value;
            this.parent = parent;
        }

        private void add(List<Object> path, int index) {
            add(path, index, null, -1);
        }

        private void add(List<Object> path, int index, StaticFormat staticFormat, int staticLevel) {
            if (index >= path.size()) {
                return;
            }

            Object item = path.get(index);
            HeaderNode child = children.stream()
                    .filter(node -> Objects.equals(node.value, item))
                    .findFirst()
                    .orElseGet(() -> {
                        HeaderNode created = new HeaderNode(item, this);
                        children.add(created);
                        return created;
                    });
            if (index == staticLevel) {
                child.staticFormat = staticFormat;
            }
            child.add(path, index + 1, staticFormat, staticLevel);
        }

        private Object value() {
            return value;
        }

        private HeaderNode parent() {
            return parent;
        }

        private List<HeaderNode> children() {
            return children;
        }

        private StaticFormat staticFormat() {
            return staticFormat;
        }

        private boolean hasStaticAncestor() {
            HeaderNode node = this;
            while (node != null) {
                if (node.staticFormat != null) {
                    return true;
                }
                node = node.parent;
            }
            return false;
        }

        private int width() {
            if (children.isEmpty()) {
                return 1;
            }
            return children.stream().mapToInt(HeaderNode::width).sum();
        }
    }
}
