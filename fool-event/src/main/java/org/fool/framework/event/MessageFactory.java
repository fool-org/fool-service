package org.fool.framework.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class MessageFactory {
    private final Supplier<UUID> uuidSupplier;
    private final Supplier<LocalDateTime> clock;
    private final EventMessageRepository eventMessageRepository;

    @Autowired
    public MessageFactory(EventMessageRepository eventMessageRepository) {
        this(UUID::randomUUID, LocalDateTime::now, eventMessageRepository);
    }

    public MessageFactory(Supplier<UUID> uuidSupplier, Supplier<LocalDateTime> clock) {
        this(uuidSupplier, clock, null);
    }

    public MessageFactory(
            Supplier<UUID> uuidSupplier,
            Supplier<LocalDateTime> clock,
            EventMessageRepository eventMessageRepository) {
        this.uuidSupplier = uuidSupplier;
        this.clock = clock;
        this.eventMessageRepository = eventMessageRepository;
    }

    public List<EventMessage> createMessages(MsgNotifyType notifyType, List<EventRecipient> users, EventRecord event) {
        return users.stream()
                .map(user -> createMessage(notifyType, user, event))
                .toList();
    }

    public List<EventMessage> createAndSaveMessages(
            MsgNotifyType notifyType,
            List<EventRecipient> users,
            EventRecord event) {
        List<EventMessage> messages = createMessages(notifyType, users, event);
        if (eventMessageRepository == null) {
            throw new IllegalStateException("EventMessageRepository is required to save event messages.");
        }
        eventMessageRepository.saveAll(messages);
        return messages;
    }

    private EventMessage createMessage(MsgNotifyType notifyType, EventRecipient user, EventRecord event) {
        EventDefinition definition = event.getDefinition();
        EventMessage message = new EventMessage();
        message.setMessageId(uuidSupplier.get());
        message.setNotifyType(notifyType);
        message.setNotifyUserId(user.getUserId());
        message.setObjectId(event.getObjectId());
        message.setViewId(event.getViewId());
        message.setGenerateTime(clock.get());
        message.setEventId(event.getEventId());
        message.setMessageFormat(definition.getMessageFormat());
        message.setState(MsgState.Generate);
        message.setReadOperationId(definition.getOperationId());
        return message;
    }
}
