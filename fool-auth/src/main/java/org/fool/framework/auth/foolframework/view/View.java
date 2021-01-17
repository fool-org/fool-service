package org.fool.framework.auth.foolframework.view;


import lombok.Data;

import java.util.List;

@Data
public class View {

    private String viewName;
    private long id;
    private List<ViewItem> items;
}
