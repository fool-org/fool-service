package com.github.yfge.fool.view.model;

import lombok.Data;

@Data
public class View {
    private String viewName;
    private String viewText;
    private int viewType;
    private List<ViewItem> items;
    private String modelClass;
}
