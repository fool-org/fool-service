package org.fool.framework.report;

public final class ReportEmptyValue {
    private static final ReportEmptyValue VALUE = new ReportEmptyValue();

    private ReportEmptyValue() {
    }

    public static ReportEmptyValue getValue() {
        return VALUE;
    }
}
