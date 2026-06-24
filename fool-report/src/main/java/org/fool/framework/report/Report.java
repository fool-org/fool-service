package org.fool.framework.report;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class Report {
    private String name;
    private List<Param> params;
    private UUID id;
    private String no;
    private List<ReportResultTable> result;
    private IReportSource source;
    private LocalDateTime createTime;
    private String createPerson;
    private LocalDateTime modifyTime;
    private String moidiyPerson;
}
