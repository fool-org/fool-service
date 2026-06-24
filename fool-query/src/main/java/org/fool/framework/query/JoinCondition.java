package org.fool.framework.query;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JoinCondition {
    private String leftCol;
    private String rightCol;

    public JoinCondition(String leftCol, String rightCol) {
        this.leftCol = leftCol;
        this.rightCol = rightCol;
    }
}
