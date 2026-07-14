package org.fool.framework.agent.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class JdbcAgentSessionStore implements AgentSessionStore {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<AgentSession> sessionRowMapper = new AgentSessionRowMapper();
    private final RowMapper<AgentMessage> messageRowMapper = new AgentMessageRowMapper();

    public JdbcAgentSessionStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(AgentSession session) {
        int updated = jdbcTemplate.update("""
                        UPDATE `FOOL_AGENT_SESSION`
                           SET `SESSION_TOKEN` = ?,
                               `SESSION_TITLE` = ?,
                               `CURRENT_CAPABILITY` = ?,
                               `SESSION_STATUS` = ?,
                               `UPDATED_AT` = ?
                         WHERE `SESSION_ID` = ?
                        """,
                session.getToken(),
                session.getTitle(),
                idOf(session.getCurrentCapability()),
                nameOf(session.getStatus()),
                timestampOf(session.getUpdatedAt()),
                session.getId());
        if (updated == 0) {
            jdbcTemplate.update("""
                            INSERT INTO `FOOL_AGENT_SESSION`
                                (`SESSION_ID`, `SESSION_TOKEN`, `SESSION_TITLE`, `CURRENT_CAPABILITY`,
                                 `SESSION_STATUS`, `CREATED_AT`, `UPDATED_AT`)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            """,
                    session.getId(),
                    session.getToken(),
                    session.getTitle(),
                    idOf(session.getCurrentCapability()),
                    nameOf(session.getStatus()),
                    timestampOf(session.getCreatedAt()),
                    timestampOf(session.getUpdatedAt()));
        }
        jdbcTemplate.update("DELETE FROM `FOOL_AGENT_MESSAGE` WHERE `SESSION_ID` = ?", session.getId());
        int index = 0;
        for (AgentMessage message : session.getMessages()) {
            jdbcTemplate.update("""
                            INSERT INTO `FOOL_AGENT_MESSAGE`
                                (`MESSAGE_ID`, `SESSION_ID`, `MESSAGE_INDEX`, `MSG_ROLE`, `CAPABILITY`,
                                 `CONTENT`, `CREATED_AT`)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            """,
                    message.getId(),
                    session.getId(),
                    index++,
                    nameOf(message.getRole()),
                    idOf(message.getCapability()),
                    message.getContent(),
                    timestampOf(message.getCreatedAt()));
        }
    }

    @Override
    public Optional<AgentSession> findById(String sessionId) {
        List<AgentSession> sessions = jdbcTemplate.query("""
                        SELECT `SESSION_ID`, `SESSION_TOKEN`, `SESSION_TITLE`, `CURRENT_CAPABILITY`,
                               `SESSION_STATUS`, `CREATED_AT`, `UPDATED_AT`
                          FROM `FOOL_AGENT_SESSION`
                         WHERE `SESSION_ID` = ?
                         LIMIT 1
                        """,
                sessionRowMapper,
                sessionId);
        if (sessions.isEmpty()) {
            return Optional.empty();
        }
        AgentSession session = sessions.get(0);
        List<AgentMessage> messages = jdbcTemplate.query("""
                        SELECT `MESSAGE_ID`, `MSG_ROLE`, `CAPABILITY`, `CONTENT`, `CREATED_AT`
                          FROM `FOOL_AGENT_MESSAGE`
                         WHERE `SESSION_ID` = ?
                         ORDER BY `MESSAGE_INDEX`, `CREATED_AT`, `MESSAGE_ID`
                        """,
                messageRowMapper,
                sessionId);
        for (AgentMessage message : messages) {
            session.addMessage(message);
        }
        return Optional.of(session);
    }

    private static String idOf(AgentCapabilityType capability) {
        return capability == null ? null : capability.getId();
    }

    private static String nameOf(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static Timestamp timestampOf(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant instantOf(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        return null;
    }

    private static class AgentSessionRowMapper implements RowMapper<AgentSession> {
        @Override
        public AgentSession mapRow(ResultSet rs, int rowNum) throws SQLException {
            AgentSession session = new AgentSession(
                    rs.getString("SESSION_ID"),
                    rs.getString("SESSION_TOKEN"),
                    rs.getString("SESSION_TITLE"),
                    AgentCapabilityType.fromJson(rs.getString("CURRENT_CAPABILITY")),
                    instantOf(rs.getTimestamp("CREATED_AT")));
            session.setStatus(AgentSessionStatus.valueOf(rs.getString("SESSION_STATUS")));
            session.setUpdatedAt(instantOf(rs.getTimestamp("UPDATED_AT")));
            return session;
        }
    }

    private static class AgentMessageRowMapper implements RowMapper<AgentMessage> {
        @Override
        public AgentMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AgentMessage(
                    rs.getString("MESSAGE_ID"),
                    AgentMessageRole.valueOf(rs.getString("MSG_ROLE")),
                    AgentCapabilityType.fromJson(rs.getString("CAPABILITY")),
                    rs.getString("CONTENT"),
                    instantOf(rs.getTimestamp("CREATED_AT")));
        }
    }
}
