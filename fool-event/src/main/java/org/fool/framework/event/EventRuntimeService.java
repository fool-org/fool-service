package org.fool.framework.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class EventRuntimeService {
    private final EventDefinitionRepository definitionRepository;
    private final EventObjectQuery objectQuery;
    private final EventRecordRepository eventRecordRepository;
    private final EventRecipientResolver recipientResolver;
    private final MessageFactory messageFactory;
    private final Supplier<UUID> uuidSupplier;
    private final Supplier<LocalDateTime> clock;

    @Autowired
    public EventRuntimeService(
            EventDefinitionRepository definitionRepository,
            EventObjectQuery objectQuery,
            EventRecordRepository eventRecordRepository,
            EventRecipientResolver recipientResolver,
            MessageFactory messageFactory) {
        this(
                definitionRepository,
                objectQuery,
                eventRecordRepository,
                recipientResolver,
                messageFactory,
                UUID::randomUUID,
                LocalDateTime::now);
    }

    public EventRuntimeService(
            EventDefinitionRepository definitionRepository,
            EventObjectQuery objectQuery,
            EventRecordRepository eventRecordRepository,
            EventRecipientResolver recipientResolver,
            MessageFactory messageFactory,
            Supplier<UUID> uuidSupplier,
            Supplier<LocalDateTime> clock) {
        this.definitionRepository = definitionRepository;
        this.objectQuery = objectQuery;
        this.eventRecordRepository = eventRecordRepository;
        this.recipientResolver = recipientResolver;
        this.messageFactory = messageFactory;
        this.uuidSupplier = uuidSupplier;
        this.clock = clock;
    }

    public EventRuntimeResult processRunningDefinitions() {
        EventRuntimeResult result = new EventRuntimeResult();
        for (EventDefinition definition : definitionRepository.findRunningDefinitions()) {
            result.definitionChecked();
            processDefinition(definition, result);
        }
        return result;
    }

    private void processDefinition(EventDefinition definition, EventRuntimeResult result) {
        for (EventMatchedObject object : objectQuery.findMatchedObjects(definition)) {
            result.objectMatched();
            if (eventRecordRepository.exists(definition.getDefId(), object.objectId())) {
                result.eventSkipped();
                continue;
            }

            EventRecord event = createEvent(definition, object);
            eventRecordRepository.save(event);
            createMessages(definition, object, event);
            result.eventCreated();
        }
    }

    private EventRecord createEvent(EventDefinition definition, EventMatchedObject object) {
        EventRecord event = new EventRecord();
        event.setEventId(uuidSupplier.get());
        event.setGenerationTime(clock.get());
        event.setDefinition(definition);
        event.setDefinitionId(definition.getDefId());
        event.setObjectId(object.objectId());
        event.setViewId(definition.getViewId());
        event.setEventMessage(definition.getMessageFormat());
        event.setDealOperationText(definition.getOperationId());
        return event;
    }

    private void createMessages(EventDefinition definition, EventMatchedObject object, EventRecord event) {
        List<EventNotificationPlan> plans = recipientResolver.resolve(definition, object);
        for (EventNotificationPlan plan : plans) {
            if (!plan.recipients().isEmpty()) {
                messageFactory.createAndSaveMessages(plan.notifyType(), plan.recipients(), event);
            }
        }
    }
}
