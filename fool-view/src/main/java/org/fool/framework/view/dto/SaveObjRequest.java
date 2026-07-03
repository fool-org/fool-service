package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fool.framework.dto.CommonRequest;

import java.util.ArrayList;
import java.util.List;

@Data
public class SaveObjRequest extends CommonRequest {
    private SaveObject saveObj;

    @Data
    public static class SaveObject {
        private String id;
        private List<SaveKeypair> propertyies = new ArrayList<>();
        private List<ItemProperty> itemproperties = new ArrayList<>();
        private String viewID;
        private String parentId;
        private String model;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveKeypair {
        private String key;
        private Object value;
    }

    @Data
    public static class ItemProperty {
        private String key;
        private List<Item> items = new ArrayList<>();
        private List<Item> delteItems = new ArrayList<>();
        private List<Item> addedItems = new ArrayList<>();
    }

    @Data
    public static class Item {
        @JsonAlias("isExist")
        private boolean exist;
        private String itemId;
        private List<SaveKeypair> propertyies = new ArrayList<>();
    }
}
