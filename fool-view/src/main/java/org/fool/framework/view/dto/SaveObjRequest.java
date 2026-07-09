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
    @JsonAlias({"SaveObj", "obj"})
    private SaveObject saveObj;

    @Data
    public static class SaveObject {
        @JsonAlias("Id")
        private String id;
        @JsonAlias("Propertyies")
        private List<SaveKeypair> propertyies = new ArrayList<>();
        @JsonAlias("Itemproperties")
        private List<ItemProperty> itemproperties = new ArrayList<>();
        @JsonAlias("ViewID")
        private String viewID;
        @JsonAlias("ParentId")
        private String parentId;
        @JsonAlias("Model")
        private String model;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveKeypair {
        @JsonAlias("Key")
        private String key;
        @JsonAlias("Value")
        private Object value;
    }

    @Data
    public static class ItemProperty {
        @JsonAlias("Key")
        private String key;
        @JsonAlias("Items")
        private List<Item> items = new ArrayList<>();
        @JsonAlias("DelteItems")
        private List<Item> delteItems = new ArrayList<>();
        @JsonAlias("AddedItems")
        private List<Item> addedItems = new ArrayList<>();
    }

    @Data
    public static class Item {
        @JsonAlias({"isExist", "IsExist"})
        private boolean exist;
        @JsonAlias("ItemId")
        private String itemId;
        @JsonAlias("Propertyies")
        private List<SaveKeypair> propertyies = new ArrayList<>();
    }
}
