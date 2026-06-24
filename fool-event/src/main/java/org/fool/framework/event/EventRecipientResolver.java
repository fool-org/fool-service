package org.fool.framework.event;

import java.util.List;

public interface EventRecipientResolver {
    List<EventNotificationPlan> resolve(EventDefinition definition, EventMatchedObject object);
}
