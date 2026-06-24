package org.fool.framework.query;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportParameter {
    private String name;
    private String exp;
    private Object value;
    private String fmtValue;

    public ReportParameter(String name, String exp, Object value, String fmtValue) {
        this.name = name;
        this.exp = exp;
        this.value = value;
        this.fmtValue = fmtValue;
    }
}
