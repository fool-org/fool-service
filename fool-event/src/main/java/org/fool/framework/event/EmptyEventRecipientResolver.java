package org.fool.framework.event;

import java.util.List;

public class EmptyEventRecipientResolver implements EventRecipientResolver {
    @Override
    public List<EventNotificationPlan> resolve(EventDefinition definition, EventMatchedObject object) {
        return List.of();
    }
}
