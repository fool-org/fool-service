package org.fool.framework.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class JdbcEventRecordRepository implements EventRecordRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcEventRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean exists(UUID definitionId, String objectId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM `SW_EVT_EVENT`
                        WHERE `EVT_Defination` = ? AND `EVT_DEF` = ?
                        """,
                Integer.class,
                valueOf(definitionId),
                objectId);
        return count != null && count > 0;
    }

    @Override
    public void save(EventRecord event) {
        jdbcTemplate.update(
                """
                        INSERT INTO `SW_EVT_EVENT`
                            (`EVT_ID`, `EVT_CREATETIME`, `EVT_MSG`, `EVT_DEALMSG`,
                             `EVT_DEALTIME`, `EVT_DEALUSER`, `EVT_VIEW`, `EVT_DEF`,
                             `EVT_Defination`)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                valueOf(event.getEventId()),
                event.getGenerationTime(),
                event.getEventMessage(),
                event.getDealOperationText(),
                event.getLastDealTime(),
                event.getLastDealUser(),
                event.getViewId(),
                event.getObjectId(),
                valueOf(event.getDefinitionId()));
    }

    private static String valueOf(Object value) {
        return value == null ? null : value.toString();
    }
}
