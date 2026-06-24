package org.fool.framework.dbmanage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

@Repository
public class JdbcSqlExecutionGateway implements SqlExecutionGateway {
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public JdbcSqlExecutionGateway(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public List<Map<String, Object>> queryForTable(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public int execute(String sql) {
        return jdbcTemplate.update(sql);
    }

    @Override
    public void executeInTransaction(List<String> sqls) {
        transactionTemplate.executeWithoutResult(status -> {
            for (String sql : sqls) {
                jdbcTemplate.update(sql);
            }
        });
    }
}
