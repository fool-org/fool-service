package org.fool.framework.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReportCell {
    private int col;
    private int row;
    private int colSpan;
    private int rowSpan;
    private String fmtValue;

    @JsonProperty("Col")
    public int getLegacyCol() {
        return col;
    }

    @JsonProperty("Row")
    public int getLegacyRow() {
        return row;
    }

    @JsonProperty("ColSpan")
    public int getLegacyColSpan() {
        return colSpan;
    }

    @JsonProperty("RowSpan")
    public int getLegacyRowSpan() {
        return rowSpan;
    }

    @JsonProperty("FmtValue")
    public String getLegacyFmtValue() {
        return fmtValue;
    }
}
