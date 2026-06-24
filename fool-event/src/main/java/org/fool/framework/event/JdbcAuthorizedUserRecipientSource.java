package org.fool.framework.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Repository
public class JdbcAuthorizedUserRecipientSource implements Supplier<List<EventRecipient>> {
    static final String USER_ID_COLUMN = "APP_AUTH_USERID";
    static final String SELECT_AUTHORIZED_USERS_SQL = """
            SELECT `APP_AUTH_USERID`
            FROM `SW_APP_AUTH_USER`
            WHERE `APP_AUTH_USERID` IS NOT NULL
            ORDER BY `APP_AUTH_USERID`
            """;

    private final Supplier<List<Map<String, Object>>> authorizedUserRows;

    @Autowired
    public JdbcAuthorizedUserRecipientSource(JdbcTemplate jdbcTemplate) {
        this(() -> jdbcTemplate.queryForList(SELECT_AUTHORIZED_USERS_SQL));
    }

    JdbcAuthorizedUserRecipientSource(Supplier<List<Map<String, Object>>> authorizedUserRows) {
        this.authorizedUserRows = authorizedUserRows;
    }

    @Override
    public List<EventRecipient> get() {
        return authorizedUserRows.get().stream()
                .map(JdbcAuthorizedUserRecipientSource::toRecipient)
                .toList();
    }

    private static EventRecipient toRecipient(Map<String, Object> row) {
        return new EventRecipient(valueOf(row.get(USER_ID_COLUMN)));
    }

    private static String valueOf(Object value) {
        return value == null ? null : value.toString();
    }
}
