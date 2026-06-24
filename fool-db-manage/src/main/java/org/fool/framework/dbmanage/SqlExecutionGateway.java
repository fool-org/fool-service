package org.fool.framework.dbmanage;

import java.util.List;
import java.util.Map;

public interface SqlExecutionGateway {
    List<Map<String, Object>> queryForTable(String sql);

    int execute(String sql);

    void executeInTransaction(List<String> sqls);
}
