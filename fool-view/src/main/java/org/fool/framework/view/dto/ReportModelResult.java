package org.fool.framework.view.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReportModelResult {
    private List<QueryCol> cols = new ArrayList<>();

    @Data
    public static class QueryCol {
        private String id;
        private String name;
        private Integer prpType;
        private Long modelId;
        private List<StateValue> states = new ArrayList<>();
        private List<Option> compareTypes = new ArrayList<>();
        private List<Option> queryTypes = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        private String id;
        private String name;
    }

    @Data
    public static class StateValue {
        private String showName;
        private String dbName;
    }
}
