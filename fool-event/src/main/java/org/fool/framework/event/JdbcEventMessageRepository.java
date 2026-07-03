package org.fool.framework.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcEventMessageRepository implements EventMessageRepository {
    public static final String SELECT_GENERATED_MESSAGES_SQL = """
            SELECT * FROM `SW_SYS_MSG`
            WHERE `MSG_STATE` = ? AND `MSG_USERID` = ?
            ORDER BY `MSG_CREATETIME` DESC
            LIMIT ?
            """;
    public static final String MARK_PUSHED_SQL = """
            UPDATE `SW_SYS_MSG`
            SET `MSG_STATE` = ?, `MSG_PUSHTIME` = ?
            WHERE `MSG_ID` = ?
            """;

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

    @Override
    public List<EventMessage> findGeneratedForUser(String userId, int limit) {
        return jdbcTemplate.query(
                SELECT_GENERATED_MESSAGES_SQL,
                (rs, rowNum) -> mapMessage(rs),
                MsgState.Generate.code(),
                userId,
                limit);
    }

    @Override
    public void markPushed(UUID messageId, LocalDateTime pushTime) {
        jdbcTemplate.update(
                MARK_PUSHED_SQL,
                MsgState.Push.code(),
                pushTime,
                valueOf(messageId));
    }

    private static EventMessage mapMessage(java.sql.ResultSet rs) throws SQLException {
        EventMessage message = new EventMessage();
        message.setMessageId(uuidOf(rs.getObject("MSG_ID")));
        message.setEventId(uuidOf(rs.getObject("MSG_EVT")));
        message.setViewId(valueOf(rs.getObject("MSG_VIEW")));
        message.setObjectId(valueOf(rs.getObject("MSG_OBJ")));
        message.setMessageFormat(valueOf(rs.getObject("MSG_MSG")));
        message.setGenerateTime(localDateTimeOf(rs.getObject("MSG_CREATETIME")));
        message.setReadTime(localDateTimeOf(rs.getObject("MSG_READTIME")));
        message.setPushTime(localDateTimeOf(rs.getObject("MSG_PUSHTIME")));
        message.setReadTimeoutTime(localDateTimeOf(rs.getObject("MSG_ENDLINETIME")));
        message.setState(msgStateOf(rs.getObject("MSG_STATE")));
        message.setReadOperationId(valueOf(rs.getObject("MSG_READOPERATION")));
        message.setNotifyUserId(valueOf(rs.getObject("MSG_USERID")));
        message.setNotifyType(msgNotifyTypeOf(rs.getObject("MSG_MSGTYPE")));
        return message;
    }

    private static String valueOf(Object value) {
        return value == null ? null : value.toString();
    }

    private static Integer ordinalOf(Enum<?> value) {
        return value == null ? null : value.ordinal();
    }

    private static UUID uuidOf(Object value) {
        return value == null ? null : UUID.fromString(value.toString());
    }

    private static LocalDateTime localDateTimeOf(Object value) {
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }

    private static MsgState msgStateOf(Object value) {
        Integer code = integerOf(value);
        if (code == null) {
            return null;
        }
        for (MsgState state : MsgState.values()) {
            if (state.code() == code) {
                return state;
            }
        }
        return null;
    }

    private static MsgNotifyType msgNotifyTypeOf(Object value) {
        Integer code = integerOf(value);
        if (code == null) {
            return null;
        }
        for (MsgNotifyType type : MsgNotifyType.values()) {
            if (type.code() == code) {
                return type;
            }
        }
        return null;
    }

    private static Integer integerOf(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return value == null ? null : Integer.parseInt(value.toString());
    }
}
