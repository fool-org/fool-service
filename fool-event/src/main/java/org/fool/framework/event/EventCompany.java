package org.fool.framework.event;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventCompany {
    private final String companyId;
    private List<EventDepartment> departments = new ArrayList<>();
}
