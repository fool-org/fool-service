package org.fool.framework.dbmanage;

import java.util.Arrays;
import java.util.List;

public class SqlCon {
    private final SqlExecutionGateway gateway;

    public SqlCon(SqlExecutionGateway gateway) {
        this.gateway = gateway;
    }

    public SqlResultTable getTable(String sql) {
        return new SqlResultTable(gateway.queryForTable(sql));
    }

    public boolean exuteSqls(String[] sqls) {
        try {
            List<String> commands = sqls == null ? List.of() : Arrays.asList(sqls);
            gateway.executeInTransaction(commands);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public int excuteSql(String sql) {
        return gateway.execute(sql);
    }
}
