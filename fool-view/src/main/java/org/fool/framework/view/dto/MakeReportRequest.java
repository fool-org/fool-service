package org.fool.framework.view.dto;

import lombok.Data;
import org.fool.framework.dto.CommonRequest;

import java.util.List;

@Data
public class MakeReportRequest extends CommonRequest {
    private Long viewId;
    private List<ReportCol> reportCols;
    private Integer currentPage;
    private Integer pageSize;
    private String queryFilter;

    @Data
    public static class ReportCol {
        private String colName;
        private String colId;
        private String selectedTypeId;
        private Integer index;
        private String orderType;
    }
}
