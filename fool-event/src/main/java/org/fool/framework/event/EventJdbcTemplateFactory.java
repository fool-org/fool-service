package org.fool.framework.event;

import org.springframework.jdbc.core.JdbcTemplate;

public interface EventJdbcTemplateFactory {
    JdbcTemplate create(String connectionString);
}
