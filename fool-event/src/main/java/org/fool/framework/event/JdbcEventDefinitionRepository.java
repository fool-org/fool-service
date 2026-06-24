package org.fool.framework.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Repository
public class JdbcEventDefinitionRepository implements EventDefinitionRepository {
    static final String SELECT_RUNNING_DEFINITIONS_SQL = """
            SELECT `EVTDEF_ID`, `EVTDEF_FILTER`, `EVTDEF_VIEW`, `EVTDEF_OPERATION`,
                   `EVTDEF_MSGFMT`, `EVTDEF_TIMEOUTSECS`, `EVTDEF_MODEL`,
                   `EVTDEF_MODELREF`, `EVTDEF_STATE`
            FROM `SW_EVT_DEF`
            WHERE `EVTDEF_STATE` = 0
            """;

    private final Supplier<List<EventDefinition>> definitionRows;
    private final EventDefinitionRelationLoader relationLoader;

    @Autowired
    public JdbcEventDefinitionRepository(JdbcTemplate jdbcTemplate, EventDefinitionRelationLoader relationLoader) {
        this(() -> jdbcTemplate.query(
                SELECT_RUNNING_DEFINITIONS_SQL,
                (rs, rowNum) -> mapDefinition(rs)), relationLoader);
    }

    JdbcEventDefinitionRepository(
            Supplier<List<EventDefinition>> definitionRows,
            EventDefinitionRelationLoader relationLoader) {
        this.definitionRows = definitionRows;
        this.relationLoader = relationLoader;
    }

    @Override
    public List<EventDefinition> findRunningDefinitions() {
        List<EventDefinition> definitions = definitionRows.get();
        relationLoader.loadRelations(definitions);
        return definitions;
    }

    private static EventDefinition mapDefinition(ResultSet rs) throws SQLException {
        EventDefinition definition = new EventDefinition();
        definition.setDefId(UUID.fromString(rs.getString("EVTDEF_ID")));
        definition.setFilter(rs.getString("EVTDEF_FILTER"));
        definition.setViewId(rs.getString("EVTDEF_VIEW"));
        definition.setOperationId(rs.getString("EVTDEF_OPERATION"));
        definition.setMessageFormat(rs.getString("EVTDEF_MSGFMT"));
        definition.setTimeoutSeconds((Integer) rs.getObject("EVTDEF_TIMEOUTSECS"));
        definition.setModelId(rs.getString("EVTDEF_MODEL"));
        definition.setModelRefType(enumAt(EventModelRefType.values(), (Integer) rs.getObject("EVTDEF_MODELREF")));
        definition.setState(enumAt(EventState.values(), (Integer) rs.getObject("EVTDEF_STATE")));
        return definition;
    }

    private static <T> T enumAt(T[] values, Integer ordinal) {
        if (ordinal == null || ordinal < 0 || ordinal >= values.length) {
            return null;
        }
        return values[ordinal];
    }
}
