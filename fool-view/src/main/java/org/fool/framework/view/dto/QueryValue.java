package org.fool.framework.view.dto;

import lombok.Data;

import java.util.List;


@Data
public class QueryValue {
    private String property;
    private String value;
    private List<String> values;
}
