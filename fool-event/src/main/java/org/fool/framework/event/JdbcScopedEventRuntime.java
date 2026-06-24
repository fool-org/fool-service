package org.fool.framework.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;
import java.util.function.Function;

@Component
public class JdbcScopedEventRuntime implements ScopedEventRuntime {
    private final Function<String, JdbcTemplate> jdbcTemplates;
    private final BiFunction<JdbcTemplate, JdbcTemplate, EventRuntimeResult> runtimeProcessor;

    @Autowired
    public JdbcScopedEventRuntime(EventJdbcTemplateFactory jdbcTemplateFactory) {
        this(jdbcTemplateFactory::create, JdbcScopedEventRuntime::processWithTemplates);
    }

    JdbcScopedEventRuntime(
            Function<String, JdbcTemplate> jdbcTemplates,
            BiFunction<JdbcTemplate, JdbcTemplate, EventRuntimeResult> runtimeProcessor) {
        this.jdbcTemplates = jdbcTemplates;
        this.runtimeProcessor = runtimeProcessor;
    }

    @Override
    public EventRuntimeResult process(EventApplicationScope application, String databaseConnection) {
        JdbcTemplate systemJdbcTemplate = jdbcTemplates.apply(application.systemConnection());
        JdbcTemplate databaseJdbcTemplate = jdbcTemplates.apply(databaseConnection);
        return runtimeProcessor.apply(systemJdbcTemplate, databaseJdbcTemplate);
    }

    private static EventRuntimeResult processWithTemplates(
            JdbcTemplate systemJdbcTemplate,
            JdbcTemplate databaseJdbcTemplate) {
        EventDefinitionRelationLoader relationLoader =
                new JdbcEventDefinitionRecipientRelationLoader(systemJdbcTemplate);
        EventDefinitionRepository definitionRepository =
                new JdbcEventDefinitionRepository(systemJdbcTemplate, relationLoader);
        EventModelTableResolver tableResolver = new JdbcEventModelTableResolver(systemJdbcTemplate);
        EventObjectQuery objectQuery = new JdbcEventObjectQuery(databaseJdbcTemplate, tableResolver);
        EventRecordRepository eventRecordRepository = new JdbcEventRecordRepository(databaseJdbcTemplate);
        EventRecipientResolver recipientResolver = new LegacyEventRecipientResolver(
                new JdbcAuthorizedUserRecipientSource(systemJdbcTemplate));
        EventMessageRepository eventMessageRepository = new JdbcEventMessageRepository(databaseJdbcTemplate);

        return new EventRuntimeService(
                definitionRepository,
                objectQuery,
                eventRecordRepository,
                recipientResolver,
                new MessageFactory(eventMessageRepository))
                .processRunningDefinitions();
    }
}
