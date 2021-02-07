package org.fool.framework.query;
import lombok.Getter;
/**
 *
 */
@Getter
public enum CompareOp {
    LESS("<", "小于"),
    LESS_OR_EQUAL("<=", "小于等于"),
    EQUAL("=", "等于"),
    MORE_OR_EQUAL(">=", "大于等于"),
    MORE(">", "大于");

    String dbValue;
    String disPlayValue;

    private CompareOp(String dbValue, String disPlayValue) {
        this.dbValue = dbValue;
        this.disPlayValue = disPlayValue;
    }
}
