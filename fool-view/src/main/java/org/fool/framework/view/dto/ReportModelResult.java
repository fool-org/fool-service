package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReportModelResult {
    private List<QueryCol> cols = new ArrayList<>();

    @JsonProperty("Cols")
    public List<QueryCol> getLegacyCols() {
        return cols;
    }

    @Data
    public static class QueryCol {
        private String id;
        private String name;
        private Integer prpType;
        private Long modelId;
        private List<StateValue> states = new ArrayList<>();
        private List<Option> compareTypes = new ArrayList<>();
        private List<Option> queryTypes = new ArrayList<>();

        @JsonProperty("ID")
        public String getLegacyId() {
            return id;
        }

        @JsonProperty("Name")
        public String getLegacyName() {
            return name;
        }

        @JsonProperty("PrpType")
        public Integer getLegacyPrpType() {
            return prpType;
        }

        @JsonProperty("ModelId")
        public Long getLegacyModelId() {
            return modelId;
        }

        @JsonProperty("States")
        public List<StateValue> getLegacyStates() {
            return states;
        }

        @JsonProperty("CompareTypes")
        public List<Option> getLegacyCompareTypes() {
            return compareTypes;
        }

        @JsonProperty("QueryTypes")
        public List<Option> getLegacyQueryTypes() {
            return queryTypes;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        private String id;
        private String name;

        @JsonProperty("ID")
        public String getLegacyId() {
            return id;
        }

        @JsonProperty("Name")
        public String getLegacyName() {
            return name;
        }
    }

    @Data
    public static class StateValue {
        private String showName;
        private String dbName;

        @JsonProperty("ShowName")
        public String getLegacyShowName() {
            return showName;
        }

        @JsonProperty("DBName")
        public String getLegacyDbName() {
            return dbName;
        }
    }
}
