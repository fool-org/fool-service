package org.fool.framework.common.data;

import java.util.Map;

public interface IObjectFromDataBase {
    Map<String, Object> getRow();

    void setRow(Map<String, Object> row);
}
