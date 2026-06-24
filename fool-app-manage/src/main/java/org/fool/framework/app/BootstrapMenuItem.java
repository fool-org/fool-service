package org.fool.framework.app;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BootstrapMenuItem {
    private final String text;
    private final String viewName;
    private final List<BootstrapMenuItem> subItems = new ArrayList<>();
    private Long viewId;
    private Long persistedId;

    public BootstrapMenuItem(String text) {
        this(text, null);
    }

    public BootstrapMenuItem(String text, String viewName) {
        this.text = text;
        this.viewName = viewName;
    }

    public BootstrapMenuItem addSubItem(String text, String viewName) {
        subItems.add(new BootstrapMenuItem(text, viewName));
        return this;
    }
}
