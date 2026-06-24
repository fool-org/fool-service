package org.fool.framework.event;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventDepartment {
    private final String departmentId;
    private List<EventRecipient> users = new ArrayList<>();
    private List<EventDepartment> subDepartments = new ArrayList<>();
}
