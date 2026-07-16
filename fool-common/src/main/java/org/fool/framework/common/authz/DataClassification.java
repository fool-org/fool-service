package org.fool.framework.common.authz;

public enum DataClassification {
    PUBLIC,
    INTERNAL,
    CONFIDENTIAL,
    RESTRICTED;

    public static DataClassification max(DataClassification left, DataClassification right) {
        DataClassification safeLeft = left == null ? INTERNAL : left;
        DataClassification safeRight = right == null ? INTERNAL : right;
        return safeLeft.ordinal() >= safeRight.ordinal() ? safeLeft : safeRight;
    }
}
