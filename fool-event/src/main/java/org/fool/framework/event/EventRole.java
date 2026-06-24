package org.fool.framework.event;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventRole {
    private final String roleId;
    private List<EventRecipient> authUsers = new ArrayList<>();
}
