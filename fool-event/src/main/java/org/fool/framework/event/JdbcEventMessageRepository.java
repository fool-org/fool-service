package org.fool.framework.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcEventMessageRepository implements EventMessageRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcEventMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAll(List<EventMessage> messages) {
        for (EventMessage message : messages) {
            jdbcTemplate.update("""
                            INSERT INTO `SW_SYS_MSG`
                                (`MSG_ID`, `MSG_EVT`, `MSG_VIEW`, `MSG_OBJ`, `MSG_MSG`,
                                 `MSG_CREATETIME`, `MSG_READTIME`, `MSG_PUSHTIME`,
                                 `MSG_ENDLINETIME`, `MSG_STATE`, `MSG_READOPERATION`,
                                 `MSG_USERID`, `MSG_MSGTYPE`)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    valueOf(message.getMessageId()),
                    valueOf(message.getEventId()),
                    message.getViewId(),
                    message.getObjectId(),
                    message.getMessageFormat(),
                    message.getGenerateTime(),
                    message.getReadTime(),
                    message.getPushTime(),
                    message.getReadTimeoutTime(),
                    ordinalOf(message.getState()),
                    message.getReadOperationId(),
                    message.getNotifyUserId(),
                    ordinalOf(message.getNotifyType()));
        }
    }

    private static String valueOf(Object value) {
        return value == null ? null : value.toString();
    }

    private static Integer ordinalOf(Enum<?> value) {
        return value == null ? null : value.ordinal();
    }
}
