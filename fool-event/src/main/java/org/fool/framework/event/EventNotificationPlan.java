package org.fool.framework.event;

import java.util.List;

public record EventNotificationPlan(MsgNotifyType notifyType, List<EventRecipient> recipients) {
}
