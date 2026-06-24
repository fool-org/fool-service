package org.fool.framework.report;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class MatrixHeader implements Comparable<Object> {
    private CellFormat formatCell;
    private Object value;
    private boolean compute;
    private StaticFormat staticCell;
    private String computeExp;

    @Override
    public int compareTo(Object obj) {
        if (!(obj instanceof MatrixHeader matrixHeader)) {
            return -1;
        }
        if (matrixHeader == this
                || (matrixHeader.formatCell == this.formatCell
                && Objects.equals(matrixHeader.value, this.value)
                && matrixHeader.staticCell == this.staticCell)) {
            return 0;
        }
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        return compareTo(obj) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                System.identityHashCode(formatCell),
                value,
                System.identityHashCode(staticCell));
    }
}
