package org.fool.framework.report;

import lombok.Data;

import java.util.List;

@Data
public class ReportResult {
    private Report report;
    private List<?> result;
    private String reportTime;
    private String reportPerson;
    private List<ParamInput> inputs;
    private String title;
}
