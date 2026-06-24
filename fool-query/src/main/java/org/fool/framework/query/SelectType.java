package org.fool.framework.query;

import lombok.Data;

@Data
public class SelectType {
    private long id;
    private String show;
    private String dbExp;
    private boolean requireGroupCol;
}
