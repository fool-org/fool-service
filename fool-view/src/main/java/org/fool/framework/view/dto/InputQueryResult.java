package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class InputQueryResult {
    private List<QueryItem> items = new ArrayList<>();

    @JsonProperty("Items")
    public List<QueryItem> getLegacyItems() {
        return items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryItem {
        private String id;
        private String text;

        @JsonProperty("Id")
        public String getLegacyId() {
            return id;
        }

        @JsonProperty("Text")
        public String getLegacyText() {
            return text;
        }
    }
}
