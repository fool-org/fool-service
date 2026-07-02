package org.fool.framework.event;

import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class EventMigrationTest {
    @Test
    public void mapsEventEntitiesToLegacyTables() throws Exception {
        assertEquals("SW_EVT_DEF", tableName(EventDefinition.class));
        assertColumn(EventDefinition.class, "defId", "EVTDEF_ID", true);
        assertColumn(EventDefinition.class, "filter", "EVTDEF_FILTER", false);
        assertColumn(EventDefinition.class, "viewId", "EVTDEF_VIEW", false);
        assertColumn(EventDefinition.class, "operationId", "EVTDEF_OPERATION", false);
        assertColumn(EventDefinition.class, "messageFormat", "EVTDEF_MSGFMT", false);
        assertColumn(EventDefinition.class, "timeoutSeconds", "EVTDEF_TIMEOUTSECS", false);
        assertColumn(EventDefinition.class, "modelId", "EVTDEF_MODEL", false);
        assertColumn(EventDefinition.class, "modelRefType", "EVTDEF_MODELREF", false);
        assertColumn(EventDefinition.class, "state", "EVTDEF_STATE", false);

        assertEquals("SW_EVT_EVENT", tableName(EventRecord.class));
        assertColumn(EventRecord.class, "eventId", "EVT_ID", true);
        assertColumn(EventRecord.class, "generationTime", "EVT_CREATETIME", false);
        assertColumn(EventRecord.class, "eventMessage", "EVT_MSG", false);
        assertColumn(EventRecord.class, "dealOperationText", "EVT_DEALMSG", false);
        assertColumn(EventRecord.class, "lastDealTime", "EVT_DEALTIME", false);
        assertColumn(EventRecord.class, "lastDealUser", "EVT_DEALUSER", false);
        assertColumn(EventRecord.class, "viewId", "EVT_VIEW", false);
        assertColumn(EventRecord.class, "objectId", "EVT_DEF", false);
        assertColumn(EventRecord.class, "definitionId", "EVT_Defination", false);

        assertEquals("SW_SYS_MSG", tableName(EventMessage.class));
        assertColumn(EventMessage.class, "messageId", "MSG_ID", true);
        assertColumn(EventMessage.class, "eventId", "MSG_EVT", false);
        assertColumn(EventMessage.class, "viewId", "MSG_VIEW", false);
        assertColumn(EventMessage.class, "objectId", "MSG_OBJ", false);
        assertColumn(EventMessage.class, "messageFormat", "MSG_MSG", false);
        assertColumn(EventMessage.class, "generateTime", "MSG_CREATETIME", false);
        assertColumn(EventMessage.class, "readTime", "MSG_READTIME", false);
        assertColumn(EventMessage.class, "pushTime", "MSG_PUSHTIME", false);
        assertColumn(EventMessage.class, "readTimeoutTime", "MSG_ENDLINETIME", false);
        assertColumn(EventMessage.class, "state", "MSG_STATE", false);
        assertColumn(EventMessage.class, "readOperationId", "MSG_READOPERATION", false);
        assertColumn(EventMessage.class, "notifyUserId", "MSG_USERID", false);
        assertColumn(EventMessage.class, "notifyType", "MSG_MSGTYPE", false);
    }

    @Test
    public void preservesLegacyEnumOrdinals() {
        assertEquals(List.of("IsRunning", "Stopped"),
                Arrays.stream(EventState.values()).map(Enum::name).toList());
        assertEquals(0, EventState.IsRunning.code());
        assertEquals(1, EventState.Stopped.code());
        assertEquals(List.of("Generate", "Push", "Readed", "Dealed", "TimeOut"),
                Arrays.stream(MsgState.values()).map(Enum::name).toList());
        assertEquals(0, MsgState.Generate.code());
        assertEquals(1, MsgState.Push.code());
        assertEquals(2, MsgState.Readed.code());
        assertEquals(3, MsgState.Dealed.code());
        assertEquals(4, MsgState.TimeOut.code());
        assertEquals(List.of("User", "Role", "Dep", "Company", "Auth", "All"),
                Arrays.stream(MsgNotifyType.values()).map(Enum::name).toList());
        assertEquals(0, MsgNotifyType.User.code());
        assertEquals(1, MsgNotifyType.Role.code());
        assertEquals(2, MsgNotifyType.Dep.code());
        assertEquals(3, MsgNotifyType.Company.code());
        assertEquals(4, MsgNotifyType.Auth.code());
        assertEquals(5, MsgNotifyType.All.code());
        assertEquals(List.of("SysModel", "AppModel", "DbModel"),
                Arrays.stream(EventModelRefType.values()).map(Enum::name).toList());
        assertEquals(0, EventModelRefType.SysModel.code());
        assertEquals(1, EventModelRefType.AppModel.code());
        assertEquals(2, EventModelRefType.DbModel.code());
    }

    @Test
    public void eventSqlHelperBuildsLegacyQueryCommand() {
        EventDefinition definition = new EventDefinition();
        definition.setFilter("[State] = 0 AND [Amount] > 10");

        assertEquals(
                "SELECT * FROM [OrderTable] WHERE [State] = 0 AND [Amount] > 10",
                EventSqlHelper.buildQuerySql("[OrderTable]", definition));
    }

    @Test
    public void eventSqlHelperRendersLegacyNullFilterAsEmptyText() {
        EventDefinition definition = new EventDefinition();

        assertEquals(
                "SELECT * FROM [OrderTable] WHERE ",
                EventSqlHelper.buildQuerySql("[OrderTable]", definition));
    }

    @Test
    public void jdbcEventModelTableResolverLoadsMigratedModelQueryMetadata() {
        JdbcEventModelTableResolver resolver = new JdbcEventModelTableResolver(modelId -> {
            assertEquals("order-model", modelId);
            return List.of(Map.of(
                    "id", 9L,
                    "name", "order-model",
                    "table_name", "market_order",
                    "object_id_column", "order_id"));
        });

        EventModelQueryMetadata metadata = resolver.resolve("order-model");

        assertEquals("market_order", metadata.tableName());
        assertEquals("order_id", metadata.objectIdColumn());
        assertTrue(JdbcEventModelTableResolver.SELECT_MODEL_TABLE_SQL.contains("fool_sys_model"));
        assertTrue(JdbcEventModelTableResolver.SELECT_MODEL_TABLE_SQL.contains("fool_sys_model_property"));
        assertTrue(JdbcEventModelTableResolver.SELECT_MODEL_TABLE_SQL.contains("table_name"));
        assertTrue(JdbcEventModelTableResolver.SELECT_MODEL_TABLE_SQL.contains("id_property"));
        assertTrue(JdbcEventModelTableResolver.SELECT_MODEL_TABLE_SQL.contains("object_id_column"));
        assertTrue(JdbcEventModelTableResolver.SELECT_MODEL_TABLE_SQL.contains("name"));
        assertTrue(JdbcEventModelTableResolver.SELECT_MODEL_TABLE_SQL.contains("id"));
        assertNotNull(JdbcEventModelTableResolver.class.getDeclaredAnnotation(org.springframework.stereotype.Repository.class));
    }

    @Test
    public void jdbcEventModelTableResolverFallsBackToModelIdWhenMetadataIsMissing() {
        JdbcEventModelTableResolver resolver = new JdbcEventModelTableResolver(modelId -> List.of());

        assertEquals("market_order", resolver.resolveTableName("market_order"));
        assertEquals("ID", resolver.resolve("market_order").objectIdColumn());
    }

    @Test
    public void jdbcEventModelTableResolverUsesLegacySysIdForAutoSysIdModels() {
        JdbcEventModelTableResolver resolver = new JdbcEventModelTableResolver(modelId -> {
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("table_name", "market_order");
            row.put("object_id_column", null);
            row.put("auto_sys_id", true);
            return List.of(row);
        });

        EventModelQueryMetadata metadata = resolver.resolve("order-model");

        assertEquals("market_order", metadata.tableName());
        assertEquals("SYSID", metadata.objectIdColumn());
        assertTrue(JdbcEventModelTableResolver.SELECT_MODEL_TABLE_SQL.contains("auto_sys_id"));
    }

    @Test
    public void jdbcEventObjectQueryUsesResolvedModelTableName() {
        EventDefinition definition = new EventDefinition();
        definition.setModelId("order-model");
        definition.setFilter("`status` = 0");
        RecordingJdbcTemplate jdbcTemplate =
                new RecordingJdbcTemplate(List.of(Map.of(
                        "order_id", "order-1",
                        "status", 0,
                        "amount", 125)));
        EventModelTableResolver tableResolver = modelId -> {
            assertEquals("order-model", modelId);
            return new EventModelQueryMetadata("market_order", "order_id");
        };

        List<EventMatchedObject> matchedObjects =
                new JdbcEventObjectQuery(jdbcTemplate, tableResolver).findMatchedObjects(definition);

        assertEquals("SELECT * FROM market_order WHERE `status` = 0", jdbcTemplate.queriedSql);
        assertEquals("order-1", matchedObjects.get(0).objectId());
        assertEquals("order-1", matchedObjects.get(0).values().get("order_id"));
        assertEquals(0, matchedObjects.get(0).values().get("status"));
        assertEquals(125, matchedObjects.get(0).values().get("amount"));
    }

    @Test
    public void jdbcEventObjectQueryMatchesLegacyKeyColumnCaseInsensitively() {
        EventDefinition definition = new EventDefinition();
        definition.setModelId("order-model");
        definition.setFilter("`status` = 0");
        RecordingJdbcTemplate jdbcTemplate =
                new RecordingJdbcTemplate(List.of(Map.of(
                        "sysid", "order-1",
                        "status", 0)));
        EventModelTableResolver tableResolver =
                modelId -> new EventModelQueryMetadata("market_order", "SYSID");

        List<EventMatchedObject> matchedObjects =
                new JdbcEventObjectQuery(jdbcTemplate, tableResolver).findMatchedObjects(definition);

        assertEquals("SELECT * FROM market_order WHERE `status` = 0", jdbcTemplate.queriedSql);
        assertEquals("order-1", matchedObjects.get(0).objectId());
        assertEquals("order-1", matchedObjects.get(0).values().get("sysid"));
        assertEquals(0, matchedObjects.get(0).values().get("status"));
    }

    @Test
    public void jdbcEventObjectQueryRejectsResultsWithoutLegacyKeyColumn() {
        EventDefinition definition = new EventDefinition();
        definition.setModelId("order-model");
        definition.setFilter("`status` = 0");
        RecordingJdbcTemplate jdbcTemplate =
                new RecordingJdbcTemplate(List.of(Map.of("status", 0, "amount", 125)));
        EventModelTableResolver tableResolver =
                modelId -> new EventModelQueryMetadata("market_order", "SYSID");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new JdbcEventObjectQuery(jdbcTemplate, tableResolver).findMatchedObjects(definition));

        assertEquals(
                "Can't Gerneration Query Because The Id Column Isn't Included!",
                exception.getMessage());
        assertEquals("SELECT * FROM market_order WHERE `status` = 0", jdbcTemplate.queriedSql);
    }

    @Test
    public void jdbcEventObjectQueryReturnsNoMatchesWhenDefinitionHasNoModel() {
        EventDefinition definition = new EventDefinition();
        definition.setFilter("`status` = 0");
        RecordingJdbcTemplate jdbcTemplate = new RecordingJdbcTemplate(List.of());
        EventModelTableResolver tableResolver = modelId -> {
            throw new AssertionError("legacy DefModel null branch must not resolve a table");
        };

        List<EventMatchedObject> matchedObjects =
                new JdbcEventObjectQuery(jdbcTemplate, tableResolver).findMatchedObjects(definition);

        assertTrue(matchedObjects.isEmpty());
        assertNull(jdbcTemplate.queriedSql);
    }

    @Test
    public void jdbcEventObjectQueryMarksResolverConstructorForSpringInjection() throws Exception {
        Constructor<JdbcEventObjectQuery> constructor = JdbcEventObjectQuery.class.getConstructor(
                JdbcTemplate.class,
                EventModelTableResolver.class);

        assertNotNull(constructor.getDeclaredAnnotation(Autowired.class));
    }

    @Test
    public void messageFactoryCreatesLegacyMessagesForRecipients() {
        UUID eventId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 6, 23, 9, 40);

        EventDefinition definition = new EventDefinition();
        definition.setMessageFormat("Order timeout");
        definition.setOperationId("op-read");

        EventRecord event = new EventRecord();
        event.setEventId(eventId);
        event.setObjectId("order-1");
        event.setViewId("view-order");
        event.setDefinition(definition);

        MessageFactory factory = new MessageFactory(() -> messageId, () -> now);

        List<EventMessage> messages = factory.createMessages(
                MsgNotifyType.Role,
                List.of(new EventRecipient("user-1"), new EventRecipient("user-2")),
                event);

        assertEquals(2, messages.size());
        EventMessage first = messages.get(0);
        assertEquals(messageId, first.getMessageId());
        assertEquals(eventId, first.getEventId());
        assertEquals("view-order", first.getViewId());
        assertEquals("order-1", first.getObjectId());
        assertEquals("Order timeout", first.getMessageFormat());
        assertEquals(now, first.getGenerateTime());
        assertEquals(MsgState.Generate, first.getState());
        assertEquals("op-read", first.getReadOperationId());
        assertEquals("user-1", first.getNotifyUserId());
        assertSame(MsgNotifyType.Role, first.getNotifyType());
    }

    @Test
    public void messageFactoryCanPersistCreatedMessages() {
        UUID eventId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 6, 23, 9, 45);
        CapturingEventMessageRepository repository = new CapturingEventMessageRepository();

        EventDefinition definition = new EventDefinition();
        definition.setMessageFormat("Order timeout");
        definition.setOperationId("op-read");

        EventRecord event = new EventRecord();
        event.setEventId(eventId);
        event.setObjectId("order-1");
        event.setViewId("view-order");
        event.setDefinition(definition);

        MessageFactory factory = new MessageFactory(() -> messageId, () -> now, repository);

        List<EventMessage> messages = factory.createAndSaveMessages(
                MsgNotifyType.User,
                List.of(new EventRecipient("user-1")),
                event);

        assertEquals(messages, repository.saved);
    }

    @Test
    public void jdbcEventMessageRepositoryIsASpringRepository() {
        assertNotNull(JdbcEventMessageRepository.class.getDeclaredAnnotation(org.springframework.stereotype.Repository.class));
    }

    @Test
    public void messageFactoryIsASpringService() {
        assertNotNull(MessageFactory.class.getDeclaredAnnotation(Service.class));
    }

    @Test
    public void messageFactoryMarksRepositoryConstructorForSpringInjection() throws Exception {
        Constructor<MessageFactory> constructor = MessageFactory.class.getConstructor(EventMessageRepository.class);

        assertNotNull(constructor.getDeclaredAnnotation(Autowired.class));
    }

    @Test
    public void eventRuntimeServiceMarksRepositoryConstructorForSpringInjection() throws Exception {
        Constructor<EventRuntimeService> constructor = EventRuntimeService.class.getConstructor(
                EventDefinitionRepository.class,
                EventObjectQuery.class,
                EventRecordRepository.class,
                EventRecipientResolver.class,
                MessageFactory.class);

        assertNotNull(constructor.getDeclaredAnnotation(Autowired.class));
    }

    @Test
    public void eventMakeServiceIsSpringServiceAndMarksConstructorForInjection() throws Exception {
        assertNotNull(EventMakeService.class.getDeclaredAnnotation(Service.class));

        Constructor<EventMakeService> constructor = EventMakeService.class.getConstructor(
                EventApplicationCatalog.class,
                ScopedEventRuntime.class);

        assertNotNull(constructor.getDeclaredAnnotation(Autowired.class));
    }

    @Test
    public void eventMakeServiceTraversesApplicationDatabasesInLegacyOrder() {
        EventApplicationScope appOne = new EventApplicationScope("app-1", "sys-1", List.of("db-1", "db-2"));
        EventApplicationScope appTwo = new EventApplicationScope("app-2", "sys-2", List.of("db-3"));
        RecordingScopedEventRuntime runtime = new RecordingScopedEventRuntime();
        runtime.results.add(runtimeResult(1, 2, 1, 0));
        runtime.results.add(runtimeResult(2, 3, 2, 1));
        runtime.results.add(runtimeResult(3, 4, 3, 2));

        EventRuntimeResult result = new EventMakeService(
                new RecordingEventApplicationCatalog(List.of(appOne, appTwo)),
                runtime)
                .workOnce();

        assertEquals(List.of(
                "app-1:sys-1:db-1",
                "app-1:sys-1:db-2",
                "app-2:sys-2:db-3"), runtime.calls);
        assertEquals(6, result.getDefinitionsChecked());
        assertEquals(9, result.getObjectsMatched());
        assertEquals(6, result.getEventsCreated());
        assertEquals(3, result.getEventsSkipped());
    }

    @Test
    public void eventMakeServiceKeepsLegacyMinutePollingInterval() {
        EventMakeService service = new EventMakeService(
                new RecordingEventApplicationCatalog(List.of()),
                new RecordingScopedEventRuntime());

        assertEquals(60_000L, service.getSleepMillis());
    }

    @Test
    public void eventSchedulerLifecycleStartsAndStopsEventMakeServiceWhenEnabled() {
        RecordingEventMakeService service = new RecordingEventMakeService();
        EventSchedulerProperties properties = new EventSchedulerProperties();
        properties.setEnabled(true);
        EventSchedulerLifecycle lifecycle = new EventSchedulerLifecycle(service, properties);

        assertTrue(lifecycle.isAutoStartup());
        assertFalse(lifecycle.isRunning());

        lifecycle.start();
        assertTrue(service.started);
        assertTrue(lifecycle.isRunning());

        lifecycle.stop();
        assertTrue(service.stopped);
        assertFalse(lifecycle.isRunning());
        assertNotNull(EventSchedulerLifecycle.class.getDeclaredAnnotation(org.springframework.stereotype.Component.class));
        assertNotNull(EventSchedulerProperties.class.getDeclaredAnnotation(
                org.springframework.boot.context.properties.ConfigurationProperties.class));
    }

    @Test
    public void eventSchedulerLifecycleDoesNotStartWhenDisabled() {
        RecordingEventMakeService service = new RecordingEventMakeService();
        EventSchedulerProperties properties = new EventSchedulerProperties();
        properties.setEnabled(false);
        EventSchedulerLifecycle lifecycle = new EventSchedulerLifecycle(service, properties);

        lifecycle.start();

        assertFalse(service.started);
        assertFalse(lifecycle.isRunning());
    }

    @Test
    public void jdbcEventApplicationCatalogLoadsLegacyApplicationsAndStoreDatabases() {
        JdbcEventApplicationCatalog catalog = new JdbcEventApplicationCatalog(
                () -> List.of(
                        Map.of("SW_APP_APPLICATIONID", "app-1", "SW_APP_CON", "sys-con-1"),
                        Map.of("SW_APP_APPLICATIONID", "app-2", "SW_APP_CON", "sys-con-2")),
                appIds -> {
                    assertEquals(List.of("app-1", "app-2"), appIds);
                    return List.of(
                            Map.of("SW_APPLICATION_ID", "app-1", "SW_STORE_CON", "db-con-1"),
                            Map.of("SW_APPLICATION_ID", "app-1", "SW_STORE_CON", "db-con-2"),
                            Map.of("SW_APPLICATION_ID", "app-2", "SW_STORE_CON", "db-con-3"));
                });

        List<EventApplicationScope> applications = catalog.findApplications();

        assertEquals(2, applications.size());
        assertEquals("app-1", applications.get(0).applicationId());
        assertEquals("sys-con-1", applications.get(0).systemConnection());
        assertEquals(List.of("db-con-1", "db-con-2"), applications.get(0).databaseConnections());
        assertEquals("app-2", applications.get(1).applicationId());
        assertEquals(List.of("db-con-3"), applications.get(1).databaseConnections());
        assertTrue(JdbcEventApplicationCatalog.SELECT_APPLICATIONS_SQL.contains("SW_APPLICATION"));
        assertTrue(JdbcEventApplicationCatalog.SELECT_DATABASE_CONNECTIONS_SQL_TEMPLATE
                .contains("SW_APPLICATION_SW_STOREDB"));
        assertTrue(JdbcEventApplicationCatalog.SELECT_DATABASE_CONNECTIONS_SQL_TEMPLATE
                .contains("SW_APPLICATION_ID"));
        assertTrue(JdbcEventApplicationCatalog.SELECT_DATABASE_CONNECTIONS_SQL_TEMPLATE
                .contains("SW_STOREDB_ID"));
        assertNotNull(JdbcEventApplicationCatalog.class.getDeclaredAnnotation(org.springframework.stereotype.Repository.class));
    }

    @Test
    public void jdbcScopedEventRuntimeUsesSystemAndDatabaseConnections() {
        JdbcTemplate systemTemplate = new JdbcTemplate();
        JdbcTemplate databaseTemplate = new JdbcTemplate();
        List<String> requestedConnections = new ArrayList<>();
        EventRuntimeResult expected = runtimeResult(2, 3, 1, 1);
        JdbcScopedEventRuntime runtime = new JdbcScopedEventRuntime(
                connection -> {
                    requestedConnections.add(connection);
                    if ("sys-con".equals(connection)) {
                        return systemTemplate;
                    }
                    if ("db-con".equals(connection)) {
                        return databaseTemplate;
                    }
                    throw new IllegalArgumentException(connection);
                },
                (systemJdbcTemplate, databaseJdbcTemplate) -> {
                    assertSame(systemTemplate, systemJdbcTemplate);
                    assertSame(databaseTemplate, databaseJdbcTemplate);
                    return expected;
                });

        EventRuntimeResult result = runtime.process(
                new EventApplicationScope("app-1", "sys-con", List.of("db-con")),
                "db-con");

        assertEquals(List.of("sys-con", "db-con"), requestedConnections);
        assertEquals(2, result.getDefinitionsChecked());
        assertEquals(3, result.getObjectsMatched());
        assertEquals(1, result.getEventsCreated());
        assertEquals(1, result.getEventsSkipped());
        assertNotNull(JdbcScopedEventRuntime.class.getDeclaredAnnotation(org.springframework.stereotype.Component.class));
    }

    @Test
    public void eventRuntimeCreatesEventsAndMessagesForMatchedObjects() {
        UUID definitionId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 6, 23, 10, 5);

        EventDefinition definition = eventDefinition(definitionId);
        CapturingEventDefinitionRepository definitionRepository =
                new CapturingEventDefinitionRepository(List.of(definition));
        CapturingEventObjectQuery objectQuery =
                new CapturingEventObjectQuery(List.of(new EventMatchedObject("order-1")));
        CapturingEventRecordRepository eventRepository = new CapturingEventRecordRepository();
        CapturingEventMessageRepository messageRepository = new CapturingEventMessageRepository();
        EventRecipientResolver resolver =
                (def, object) -> List.of(new EventNotificationPlan(MsgNotifyType.User, List.of(new EventRecipient("user-1"))));

        EventRuntimeService service = new EventRuntimeService(
                definitionRepository,
                objectQuery,
                eventRepository,
                resolver,
                new MessageFactory(() -> messageId, () -> now, messageRepository),
                () -> eventId,
                () -> now);

        EventRuntimeResult result = service.processRunningDefinitions();

        assertEquals(1, result.getDefinitionsChecked());
        assertEquals(1, result.getObjectsMatched());
        assertEquals(1, result.getEventsCreated());
        assertEquals(0, result.getEventsSkipped());
        assertEquals(definition, objectQuery.queriedDefinitions.get(0));
        assertEquals(1, eventRepository.saved.size());
        EventRecord saved = eventRepository.saved.get(0);
        assertEquals(eventId, saved.getEventId());
        assertEquals(now, saved.getGenerationTime());
        assertEquals("Order timeout", saved.getEventMessage());
        assertEquals("op-read", saved.getDealOperationText());
        assertEquals("view-order", saved.getViewId());
        assertEquals("order-1", saved.getObjectId());
        assertEquals(definitionId, saved.getDefinitionId());
        assertSame(definition, saved.getDefinition());
        assertEquals(1, messageRepository.saved.size());
        assertEquals(eventId, messageRepository.saved.get(0).getEventId());
        assertEquals("user-1", messageRepository.saved.get(0).getNotifyUserId());
    }

    @Test
    public void eventRuntimeUsesLegacyOperationNameForDealText() {
        UUID definitionId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 6, 24, 8, 55);

        EventDefinition definition = eventDefinition(definitionId);
        definition.setOperationName("Read order");
        CapturingEventRecordRepository eventRepository = new CapturingEventRecordRepository();

        EventRuntimeService service = new EventRuntimeService(
                new CapturingEventDefinitionRepository(List.of(definition)),
                new CapturingEventObjectQuery(List.of(new EventMatchedObject("order-1"))),
                eventRepository,
                (def, object) -> List.of(),
                new MessageFactory(UUID::randomUUID, () -> now, new CapturingEventMessageRepository()),
                () -> eventId,
                () -> now);

        service.processRunningDefinitions();

        assertEquals("Read order", eventRepository.saved.get(0).getDealOperationText());
    }

    @Test
    public void eventRuntimeSkipsExistingEventsForIdempotency() {
        UUID definitionId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 6, 23, 10, 10);
        CapturingEventRecordRepository eventRepository = new CapturingEventRecordRepository();
        eventRepository.existingDefinitionId = definitionId;
        eventRepository.existingObjectId = "order-1";
        CapturingEventMessageRepository messageRepository = new CapturingEventMessageRepository();

        EventRuntimeService service = new EventRuntimeService(
                new CapturingEventDefinitionRepository(List.of(eventDefinition(definitionId))),
                new CapturingEventObjectQuery(List.of(new EventMatchedObject("order-1"))),
                eventRepository,
                (def, object) -> List.of(new EventNotificationPlan(MsgNotifyType.User, List.of(new EventRecipient("user-1")))),
                new MessageFactory(UUID::randomUUID, () -> now, messageRepository),
                UUID::randomUUID,
                () -> now);

        EventRuntimeResult result = service.processRunningDefinitions();

        assertEquals(0, result.getEventsCreated());
        assertEquals(1, result.getEventsSkipped());
        assertTrue(eventRepository.saved.isEmpty());
        assertTrue(messageRepository.saved.isEmpty());
    }

    @Test
    public void eventRuntimeLoadsOnlyRunningDefinitionsFromRepository() {
        assertNotNull(JdbcEventDefinitionRepository.class.getDeclaredAnnotation(org.springframework.stereotype.Repository.class));
        assertTrue(JdbcEventDefinitionRepository.SELECT_RUNNING_DEFINITIONS_SQL.contains("FROM `SW_EVT_DEF`"));
        assertTrue(JdbcEventDefinitionRepository.SELECT_RUNNING_DEFINITIONS_SQL.contains("`EVTDEF_STATE` = 0"));
        assertEquals(0, EventState.IsRunning.ordinal());
    }

    @Test
    public void jdbcEventDefinitionRepositoryAttachesPersistedNotifyUsers() {
        UUID definitionId = UUID.randomUUID();
        EventDefinition definition = eventDefinition(definitionId);
        RecordingEventDefinitionRelationLoader relationLoader = new RecordingEventDefinitionRelationLoader();
        JdbcEventDefinitionRepository repository = new JdbcEventDefinitionRepository(() -> List.of(definition), relationLoader);

        List<EventDefinition> definitions = repository.findRunningDefinitions();

        assertEquals(List.of(definition), definitions);
        assertEquals(List.of(definitionId), relationLoader.loadedDefinitionIds);
        assertEquals(List.of("loaded-user"), definitions.get(0).getNotifyUsers().stream()
                .map(EventRecipient::getUserId)
                .toList());
    }

    @Test
    public void jdbcEventDefinitionRecipientRelationLoaderLoadsLegacyNotifyUsers() {
        UUID definitionId = UUID.randomUUID();
        EventDefinition definition = eventDefinition(definitionId);
        JdbcEventDefinitionRecipientRelationLoader loader = new JdbcEventDefinitionRecipientRelationLoader(
                ids -> {
                    assertEquals(List.of(definitionId), ids);
                    return List.of(
                            Map.of("SW_EVT_DEF_ID", definitionId.toString(), "APP_AUTH_USERID", "user-1"),
                            Map.of("SW_EVT_DEF_ID", definitionId.toString(), "APP_AUTH_USERID", "user-2"));
                });

        loader.loadRelations(List.of(definition));

        assertEquals(List.of("user-1", "user-2"), definition.getNotifyUsers().stream()
                .map(EventRecipient::getUserId)
                .toList());
        assertTrue(JdbcEventDefinitionRecipientRelationLoader.SELECT_NOTIFY_USERS_SQL_TEMPLATE
                .contains("SW_APP_AUTH_USER_SW_EVT_DEF"));
        assertTrue(JdbcEventDefinitionRecipientRelationLoader.SELECT_NOTIFY_USERS_SQL_TEMPLATE
                .contains("SW_APP_AUTH_USER_ID"));
        assertTrue(JdbcEventDefinitionRecipientRelationLoader.SELECT_NOTIFY_USERS_SQL_TEMPLATE
                .contains("APP_AUTH_USERID"));
    }

    @Test
    public void jdbcEventDefinitionRecipientRelationLoaderLoadsLegacyNotifyRolesWithAuthUsers() {
        UUID definitionId = UUID.randomUUID();
        EventDefinition definition = eventDefinition(definitionId);
        JdbcEventDefinitionRecipientRelationLoader loader = new JdbcEventDefinitionRecipientRelationLoader(
                ids -> List.of(),
                ids -> {
                    assertEquals(List.of(definitionId), ids);
                    return List.of(
                            Map.of("SW_EVT_DEF_ID", definitionId.toString(), "AUTH_ROLE_ID", 10L),
                            Map.of("SW_EVT_DEF_ID", definitionId.toString(), "AUTH_ROLE_ID", 20L));
                },
                roleIds -> {
                    assertEquals(List.of("10", "20"), roleIds);
                    return List.of(
                            Map.of("SW_APP_AUTH_ROLE_ID", "10", "APP_AUTH_USERID", "role-user-1"),
                            Map.of("SW_APP_AUTH_ROLE_ID", "10", "APP_AUTH_USERID", "role-user-2"),
                            Map.of("SW_APP_AUTH_ROLE_ID", "20", "APP_AUTH_USERID", "role-user-3"));
                });

        loader.loadRelations(List.of(definition));

        assertEquals(List.of("10", "20"), definition.getNotifyRoles().stream()
                .map(EventRole::getRoleId)
                .toList());
        assertEquals(List.of("role-user-1", "role-user-2"), definition.getNotifyRoles().get(0).getAuthUsers().stream()
                .map(EventRecipient::getUserId)
                .toList());
        assertEquals(List.of("role-user-3"), definition.getNotifyRoles().get(1).getAuthUsers().stream()
                .map(EventRecipient::getUserId)
                .toList());
        assertTrue(JdbcEventDefinitionRecipientRelationLoader.SELECT_NOTIFY_ROLES_SQL_TEMPLATE
                .contains("SW_APP_AUTH_ROLE_SW_EVT_DEF"));
        assertTrue(JdbcEventDefinitionRecipientRelationLoader.SELECT_ROLE_AUTH_USERS_SQL_TEMPLATE
                .contains("SW_APP_AUTH_ROLE_SW_APP_AUTH_USER"));
        assertTrue(JdbcEventDefinitionRecipientRelationLoader.SELECT_ROLE_AUTH_USERS_SQL_TEMPLATE
                .contains("APP_AUTH_USERID"));
    }

    @Test
    public void jdbcEventDefinitionRecipientRelationLoaderExpandsSharedRolesForEachDefinition() {
        UUID firstDefinitionId = UUID.randomUUID();
        UUID secondDefinitionId = UUID.randomUUID();
        EventDefinition firstDefinition = eventDefinition(firstDefinitionId);
        EventDefinition secondDefinition = eventDefinition(secondDefinitionId);
        JdbcEventDefinitionRecipientRelationLoader loader = new JdbcEventDefinitionRecipientRelationLoader(
                ids -> List.of(),
                ids -> List.of(
                        Map.of("SW_EVT_DEF_ID", firstDefinitionId.toString(), "AUTH_ROLE_ID", 10L),
                        Map.of("SW_EVT_DEF_ID", secondDefinitionId.toString(), "AUTH_ROLE_ID", 10L)),
                roleIds -> List.of(
                        Map.of("SW_APP_AUTH_ROLE_ID", "10", "APP_AUTH_USERID", "role-user-1")));

        loader.loadRelations(List.of(firstDefinition, secondDefinition));

        assertEquals(List.of("role-user-1"), firstDefinition.getNotifyRoles().get(0).getAuthUsers().stream()
                .map(EventRecipient::getUserId)
                .toList());
        assertEquals(List.of("role-user-1"), secondDefinition.getNotifyRoles().get(0).getAuthUsers().stream()
                .map(EventRecipient::getUserId)
                .toList());
    }

    @Test
    public void jdbcEventDefinitionRecipientRelationLoaderLoadsLegacyNotifyDepartmentsWithRecursiveUsers() {
        UUID definitionId = UUID.randomUUID();
        EventDefinition definition = eventDefinition(definitionId);
        JdbcEventDefinitionRecipientRelationLoader loader = new JdbcEventDefinitionRecipientRelationLoader(
                ids -> List.of(),
                ids -> List.of(),
                roleIds -> List.of(),
                ids -> {
                    assertEquals(List.of(definitionId), ids);
                    return List.of(Map.of("SW_EVT_DEF_ID", definitionId.toString(), "APP_DEP_ID", 10L));
                },
                departmentIds -> {
                    assertEquals(List.of("10", "11", "12"), departmentIds);
                    return List.of(
                            Map.of("APP_AUTH_DEP", "10", "APP_AUTH_USERID", "dep-user-1"),
                            Map.of("APP_AUTH_DEP", "11", "APP_AUTH_USERID", "dep-user-2"),
                            Map.of("APP_AUTH_DEP", "12", "APP_AUTH_USERID", "dep-user-3"));
                },
                departmentIds -> switch (String.join(",", departmentIds)) {
                    case "10" -> List.of(Map.of(
                            "SW_APP_AUTH_DEPARTMENT_SubDepartmentsAPP_DEP_ID", "10",
                            "APP_DEP_ID", 11L));
                    case "11" -> List.of(Map.of(
                            "SW_APP_AUTH_DEPARTMENT_SubDepartmentsAPP_DEP_ID", "11",
                            "APP_DEP_ID", 12L));
                    default -> List.of();
                },
                ids -> List.of(),
                companyIds -> List.of());

        loader.loadRelations(List.of(definition));

        EventDepartment department = definition.getNotifyDepartments().get(0);
        assertEquals("10", department.getDepartmentId());
        assertEquals(List.of("dep-user-1"), department.getUsers().stream()
                .map(EventRecipient::getUserId)
                .toList());
        EventDepartment childDepartment = department.getSubDepartments().get(0);
        assertEquals("11", childDepartment.getDepartmentId());
        assertEquals(List.of("dep-user-2"), childDepartment.getUsers().stream()
                .map(EventRecipient::getUserId)
                .toList());
        assertEquals(List.of("dep-user-3"), childDepartment.getSubDepartments().get(0).getUsers().stream()
                .map(EventRecipient::getUserId)
                .toList());
        assertTrue(JdbcEventDefinitionRecipientRelationLoader.SELECT_NOTIFY_DEPARTMENTS_SQL_TEMPLATE
                .contains("SW_APP_AUTH_DEPARTMENT_SW_EVT_DEF"));
        assertTrue(JdbcEventDefinitionRecipientRelationLoader.SELECT_DEPARTMENT_AUTH_USERS_SQL_TEMPLATE
                .contains("APP_AUTH_DEP"));
        assertTrue(JdbcEventDefinitionRecipientRelationLoader.SELECT_SUB_DEPARTMENTS_SQL_TEMPLATE
                .contains("SW_APP_AUTH_DEPARTMENT_SubDepartmentsAPP_DEP_ID"));
    }

    @Test
    public void jdbcEventDefinitionRecipientRelationLoaderLoadsLegacyNotifyCompaniesWithDepartmentUsers() {
        UUID definitionId = UUID.randomUUID();
        EventDefinition definition = eventDefinition(definitionId);
        JdbcEventDefinitionRecipientRelationLoader loader = new JdbcEventDefinitionRecipientRelationLoader(
                ids -> List.of(),
                ids -> List.of(),
                roleIds -> List.of(),
                ids -> List.of(),
                departmentIds -> {
                    assertEquals(List.of("30"), departmentIds);
                    return List.of(Map.of("APP_AUTH_DEP", "30", "APP_AUTH_USERID", "company-user-1"));
                },
                departmentIds -> List.of(),
                ids -> {
                    assertEquals(List.of(definitionId), ids);
                    return List.of(Map.of("SW_EVT_DEF_ID", definitionId.toString(), "APP_COR_ID", 7L));
                },
                companyIds -> {
                    assertEquals(List.of("7"), companyIds);
                    return List.of(Map.of("SW_APP_AUTH_COMPANY_DepsAPP_COR_ID", "7", "APP_DEP_ID", 30L));
                });

        loader.loadRelations(List.of(definition));

        EventCompany company = definition.getNotifyCompanies().get(0);
        assertEquals("7", company.getCompanyId());
        assertEquals("30", company.getDepartments().get(0).getDepartmentId());
        assertEquals(List.of("company-user-1"), company.getDepartments().get(0).getUsers().stream()
                .map(EventRecipient::getUserId)
                .toList());
        assertTrue(JdbcEventDefinitionRecipientRelationLoader.SELECT_NOTIFY_COMPANIES_SQL_TEMPLATE
                .contains("SW_APP_AUTH_COMPANY_SW_EVT_DEF"));
        assertTrue(JdbcEventDefinitionRecipientRelationLoader.SELECT_COMPANY_DEPARTMENTS_SQL_TEMPLATE
                .contains("SW_APP_AUTH_COMPANY_DepsAPP_COR_ID"));
    }

    @Test
    public void jdbcAuthorizedUserRecipientSourceLoadsLegacyAllUsers() {
        JdbcAuthorizedUserRecipientSource source = new JdbcAuthorizedUserRecipientSource(
                () -> List.of(
                        Map.of("APP_AUTH_USERID", "user-1"),
                        Map.of("APP_AUTH_USERID", 2002L)));

        List<EventRecipient> recipients = source.get();

        assertEquals(List.of("user-1", "2002"), recipients.stream().map(EventRecipient::getUserId).toList());
        assertTrue(JdbcAuthorizedUserRecipientSource.SELECT_AUTHORIZED_USERS_SQL.contains("SW_APP_AUTH_USER"));
        assertTrue(JdbcAuthorizedUserRecipientSource.SELECT_AUTHORIZED_USERS_SQL.contains("APP_AUTH_USERID"));
        assertNotNull(JdbcAuthorizedUserRecipientSource.class.getDeclaredAnnotation(org.springframework.stereotype.Repository.class));
    }

    @Test
    public void jdbcAuthorizedUserRecipientSourceMarksJdbcConstructorForSpringInjection() throws Exception {
        Constructor<JdbcAuthorizedUserRecipientSource> constructor =
                JdbcAuthorizedUserRecipientSource.class.getConstructor(org.springframework.jdbc.core.JdbcTemplate.class);

        assertNotNull(constructor.getDeclaredAnnotation(Autowired.class));
    }

    @Test
    public void legacyRecipientResolverExpandsExplicitRecipientTypesInLegacyOrder() {
        EventDefinition definition = new EventDefinition();
        EventDepartment department = new EventDepartment("dep-1");
        department.getUsers().add(new EventRecipient("dep-user-1"));
        EventDepartment childDepartment = new EventDepartment("dep-1-1");
        childDepartment.getUsers().add(new EventRecipient("dep-user-2"));
        department.getSubDepartments().add(childDepartment);
        definition.getNotifyDepartments().add(department);

        EventRole role = new EventRole("role-1");
        role.getAuthUsers().add(new EventRecipient("role-user-1"));
        definition.getNotifyRoles().add(role);

        definition.getNotifyUsers().add(new EventRecipient("direct-user-1"));

        EventCompany company = new EventCompany("company-1");
        EventDepartment companyDepartment = new EventDepartment("company-dep-1");
        companyDepartment.getUsers().add(new EventRecipient("company-user-1"));
        company.getDepartments().add(companyDepartment);
        definition.getNotifyCompanies().add(company);

        List<EventNotificationPlan> plans = new LegacyEventRecipientResolver(
                () -> List.of(new EventRecipient("all-user-1")))
                .resolve(definition, new EventMatchedObject("order-1"));

        assertEquals(4, plans.size());
        assertEquals(MsgNotifyType.Dep, plans.get(0).notifyType());
        assertEquals(List.of("dep-user-1", "dep-user-2"), userIds(plans.get(0)));
        assertEquals(MsgNotifyType.Role, plans.get(1).notifyType());
        assertEquals(List.of("role-user-1"), userIds(plans.get(1)));
        assertEquals(MsgNotifyType.User, plans.get(2).notifyType());
        assertEquals(List.of("direct-user-1"), userIds(plans.get(2)));
        assertEquals(MsgNotifyType.Company, plans.get(3).notifyType());
        assertEquals(List.of("company-user-1"), userIds(plans.get(3)));
    }

    @Test
    public void legacyRecipientResolverFallsBackToAllAuthorizedUsers() {
        EventDefinition definition = new EventDefinition();

        List<EventNotificationPlan> plans = new LegacyEventRecipientResolver(
                () -> List.of(new EventRecipient("all-user-1"), new EventRecipient("all-user-2")))
                .resolve(definition, new EventMatchedObject("order-1"));

        assertEquals(1, plans.size());
        assertEquals(MsgNotifyType.All, plans.get(0).notifyType());
        assertEquals(List.of("all-user-1", "all-user-2"), userIds(plans.get(0)));
    }

    @Test
    public void legacyRecipientResolverIsTheSpringRuntimeResolver() {
        assertNotNull(LegacyEventRecipientResolver.class.getDeclaredAnnotation(org.springframework.stereotype.Component.class));
        assertNull(EmptyEventRecipientResolver.class.getDeclaredAnnotation(org.springframework.stereotype.Component.class));
    }

    private static String tableName(Class<?> type) {
        Table table = type.getDeclaredAnnotation(Table.class);
        assertNotNull("missing @Table on " + type.getName(), table);
        return table.value();
    }

    private static void assertColumn(Class<?> type, String fieldName, String columnName, boolean key) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        Column column = field.getDeclaredAnnotation(Column.class);
        assertNotNull("missing @Column on " + fieldName, column);
        assertEquals(columnName, column.value());
        assertEquals(key, field.getDeclaredAnnotation(Id.class) != null);
    }

    private static EventDefinition eventDefinition(UUID definitionId) {
        EventDefinition definition = new EventDefinition();
        definition.setDefId(definitionId);
        definition.setMessageFormat("Order timeout");
        definition.setOperationId("op-read");
        definition.setViewId("view-order");
        return definition;
    }

    private static List<String> userIds(EventNotificationPlan plan) {
        return plan.recipients().stream().map(EventRecipient::getUserId).toList();
    }

    private static EventRuntimeResult runtimeResult(
            int definitionsChecked,
            int objectsMatched,
            int eventsCreated,
            int eventsSkipped) {
        EventRuntimeResult result = new EventRuntimeResult();
        for (int i = 0; i < definitionsChecked; i++) {
            result.definitionChecked();
        }
        for (int i = 0; i < objectsMatched; i++) {
            result.objectMatched();
        }
        for (int i = 0; i < eventsCreated; i++) {
            result.eventCreated();
        }
        for (int i = 0; i < eventsSkipped; i++) {
            result.eventSkipped();
        }
        return result;
    }

    private static final class CapturingEventDefinitionRepository implements EventDefinitionRepository {
        private final List<EventDefinition> definitions;

        private CapturingEventDefinitionRepository(List<EventDefinition> definitions) {
            this.definitions = definitions;
        }

        @Override
        public List<EventDefinition> findRunningDefinitions() {
            return definitions;
        }
    }

    private static final class CapturingEventObjectQuery implements EventObjectQuery {
        private final List<EventMatchedObject> matchedObjects;
        private final List<EventDefinition> queriedDefinitions = new ArrayList<>();

        private CapturingEventObjectQuery(List<EventMatchedObject> matchedObjects) {
            this.matchedObjects = matchedObjects;
        }

        @Override
        public List<EventMatchedObject> findMatchedObjects(EventDefinition definition) {
            queriedDefinitions.add(definition);
            return matchedObjects;
        }
    }

    private static final class RecordingJdbcTemplate extends JdbcTemplate {
        private final List<Map<String, Object>> rows;
        private String queriedSql;

        private RecordingJdbcTemplate(List<Map<String, Object>> rows) {
            this.rows = rows;
        }

        @Override
        public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
            this.queriedSql = sql;
            List<T> mappedRows = new ArrayList<>();
            for (int index = 0; index < rows.size(); index++) {
                mappedRows.add(mapRow(rowMapper, rows.get(index), index));
            }
            return mappedRows;
        }

        @Override
        public <T> T query(String sql, ResultSetExtractor<T> resultSetExtractor) {
            this.queriedSql = sql;
            try {
                return resultSetExtractor.extractData(resultSet(rows));
            } catch (java.sql.SQLException exception) {
                throw new IllegalStateException(exception);
            }
        }

        private static <T> T mapRow(RowMapper<T> rowMapper, Map<String, Object> row, int index) {
            List<String> columns = new ArrayList<>(row.keySet());
            java.sql.ResultSetMetaData metaData = (java.sql.ResultSetMetaData) java.lang.reflect.Proxy.newProxyInstance(
                    java.sql.ResultSetMetaData.class.getClassLoader(),
                    new Class<?>[]{java.sql.ResultSetMetaData.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getColumnCount" -> columns.size();
                        case "getColumnLabel", "getColumnName" -> columns.get(((Integer) args[0]) - 1);
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
            java.sql.ResultSet resultSet = (java.sql.ResultSet) java.lang.reflect.Proxy.newProxyInstance(
                    java.sql.ResultSet.class.getClassLoader(),
                    new Class<?>[]{java.sql.ResultSet.class},
                    (proxy, method, args) -> {
                        if ("getString".equals(method.getName()) && args != null && args.length == 1) {
                            Object value = row.get(args[0].toString());
                            return value == null ? null : value.toString();
                        }
                        if ("getMetaData".equals(method.getName())) {
                            return metaData;
                        }
                        if ("getObject".equals(method.getName()) && args != null && args.length == 1) {
                            return row.get(columns.get(((Integer) args[0]) - 1));
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
            try {
                return rowMapper.mapRow(resultSet, index);
            } catch (java.sql.SQLException exception) {
                throw new IllegalStateException(exception);
            }
        }

        private static java.sql.ResultSet resultSet(List<Map<String, Object>> rows) {
            List<String> columns = rows.isEmpty() ? List.of() : new ArrayList<>(rows.get(0).keySet());
            int[] cursor = {-1};
            java.sql.ResultSetMetaData metaData = (java.sql.ResultSetMetaData) java.lang.reflect.Proxy.newProxyInstance(
                    java.sql.ResultSetMetaData.class.getClassLoader(),
                    new Class<?>[]{java.sql.ResultSetMetaData.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getColumnCount" -> columns.size();
                        case "getColumnLabel", "getColumnName" -> columns.get(((Integer) args[0]) - 1);
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
            return (java.sql.ResultSet) java.lang.reflect.Proxy.newProxyInstance(
                    java.sql.ResultSet.class.getClassLoader(),
                    new Class<?>[]{java.sql.ResultSet.class},
                    (proxy, method, args) -> {
                        if ("next".equals(method.getName())) {
                            cursor[0]++;
                            return cursor[0] < rows.size();
                        }
                        if ("getMetaData".equals(method.getName())) {
                            return metaData;
                        }
                        if ("getObject".equals(method.getName()) && args != null && args.length == 1) {
                            return rows.get(cursor[0]).get(columns.get(((Integer) args[0]) - 1));
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
        }
    }

    private static final class CapturingEventRecordRepository implements EventRecordRepository {
        private final List<EventRecord> saved = new ArrayList<>();
        private UUID existingDefinitionId;
        private String existingObjectId;

        @Override
        public boolean exists(UUID definitionId, String objectId) {
            return definitionId.equals(existingDefinitionId) && objectId.equals(existingObjectId);
        }

        @Override
        public void save(EventRecord event) {
            saved.add(event);
        }
    }

    private static final class CapturingEventMessageRepository implements EventMessageRepository {
        private final List<EventMessage> saved = new ArrayList<>();

        @Override
        public void saveAll(List<EventMessage> messages) {
            saved.addAll(messages);
        }
    }

    private static final class RecordingEventApplicationCatalog implements EventApplicationCatalog {
        private final List<EventApplicationScope> applications;

        private RecordingEventApplicationCatalog(List<EventApplicationScope> applications) {
            this.applications = applications;
        }

        @Override
        public List<EventApplicationScope> findApplications() {
            return applications;
        }
    }

    private static final class RecordingScopedEventRuntime implements ScopedEventRuntime {
        private final List<String> calls = new ArrayList<>();
        private final List<EventRuntimeResult> results = new ArrayList<>();

        @Override
        public EventRuntimeResult process(EventApplicationScope application, String databaseConnection) {
            calls.add(application.applicationId() + ":" + application.systemConnection() + ":" + databaseConnection);
            if (results.isEmpty()) {
                return new EventRuntimeResult();
            }
            return results.remove(0);
        }
    }

    private static final class RecordingEventMakeService extends EventMakeService {
        private boolean started;
        private boolean stopped;

        private RecordingEventMakeService() {
            super(new RecordingEventApplicationCatalog(List.of()), new RecordingScopedEventRuntime());
        }

        @Override
        public void start() {
            started = true;
        }

        @Override
        public void stop() {
            stopped = true;
        }
    }

    private static final class RecordingEventDefinitionRelationLoader implements EventDefinitionRelationLoader {
        private final List<UUID> loadedDefinitionIds = new ArrayList<>();

        @Override
        public void loadRelations(List<EventDefinition> definitions) {
            loadedDefinitionIds.addAll(definitions.stream().map(EventDefinition::getDefId).toList());
            definitions.get(0).getNotifyUsers().add(new EventRecipient("loaded-user"));
        }
    }
}
