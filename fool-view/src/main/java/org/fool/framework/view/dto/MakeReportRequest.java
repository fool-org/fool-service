package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.fool.framework.dto.CommonRequest;

import java.util.List;

@Data
public class MakeReportRequest extends CommonRequest {
    @JsonAlias({"ViewId", "viewid"})
    private Long viewId;
    @JsonAlias({"ReportCols", "cols"})
    private List<ReportCol> reportCols;
    @JsonAlias({"CurrentPage", "pageindex"})
    private Integer currentPage;
    @JsonAlias({"PageSize", "pagesize"})
    private Integer pageSize;
    @JsonAlias({"QueryFilter", "queryfilter"})
    private String queryFilter;
    @JsonAlias({"FilterExp", "exp"})
    private BoolExp filterExp;
    @JsonAlias({"ReportName", "reportname"})
    private String reportName;

    @Data
    public static class ReportCol {
        @JsonAlias("ColName")
        private String colName;
        @JsonAlias("ColId")
        private String colId;
        @JsonAlias("SelectedTypeId")
        private String selectedTypeId;
        @JsonAlias("Index")
        private Integer index;
        @JsonAlias("OrderType")
        private String orderType;
    }

    @Data
    public static class BoolExp {
        @JsonAlias("Col")
        private QueryCol col;
        @JsonAlias("CompareOp")
        private CompareOpItem compareOp;
        @JsonAlias("ValueExp")
        private String valueExp;
        @JsonAlias("ValueFmt")
        private String valueFmt;
        @JsonAlias("FirstExp")
        private BoolExp firstExp;
        @JsonAlias("Sequences")
        private List<AddBoolExp> sequences;
        @JsonAlias("ParamName")
        private String paramName;
        @JsonAlias("IsFixed")
        private Boolean fixed;
        @JsonAlias("IsMerged")
        private Boolean merged;
    }

    @Data
    public static class AddBoolExp {
        @JsonAlias("BoolOp")
        private BoolOp boolOp;
        @JsonAlias("AddedExp")
        private BoolExp addedExp;
    }

    @Data
    public static class BoolOp {
        @JsonAlias("DBName")
        private String dbName;
        @JsonAlias("ShowName")
        private String showName;
    }

    @Data
    public static class QueryCol {
        @JsonAlias("ID")
        private String id;
        @JsonAlias("Name")
        private String name;
    }

    @Data
    public static class CompareOpItem {
        @JsonAlias("ID")
        private String id;
        @JsonAlias("Name")
        private String name;
    }
}
