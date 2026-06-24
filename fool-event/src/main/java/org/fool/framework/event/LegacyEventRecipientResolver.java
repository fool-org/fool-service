package org.fool.framework.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Component
public class LegacyEventRecipientResolver implements EventRecipientResolver {
    private final Supplier<List<EventRecipient>> allRecipientsSupplier;

    @Autowired
    public LegacyEventRecipientResolver(JdbcAuthorizedUserRecipientSource allRecipientsSupplier) {
        this((Supplier<List<EventRecipient>>) allRecipientsSupplier);
    }

    LegacyEventRecipientResolver(Supplier<List<EventRecipient>> allRecipientsSupplier) {
        this.allRecipientsSupplier = allRecipientsSupplier;
    }

    @Override
    public List<EventNotificationPlan> resolve(EventDefinition definition, EventMatchedObject object) {
        List<EventNotificationPlan> plans = new ArrayList<>();
        for (EventDepartment department : safe(definition.getNotifyDepartments())) {
            plans.add(new EventNotificationPlan(MsgNotifyType.Dep, departmentUsers(department)));
        }
        for (EventRole role : safe(definition.getNotifyRoles())) {
            plans.add(new EventNotificationPlan(MsgNotifyType.Role, safe(role.getAuthUsers())));
        }
        if (!safe(definition.getNotifyUsers()).isEmpty()) {
            plans.add(new EventNotificationPlan(MsgNotifyType.User, safe(definition.getNotifyUsers())));
        }
        for (EventCompany company : safe(definition.getNotifyCompanies())) {
            plans.add(new EventNotificationPlan(MsgNotifyType.Company, companyUsers(company)));
        }
        if (plans.isEmpty()) {
            plans.add(new EventNotificationPlan(MsgNotifyType.All, safe(allRecipientsSupplier.get())));
        }
        return plans;
    }

    private static List<EventRecipient> companyUsers(EventCompany company) {
        List<EventRecipient> users = new ArrayList<>();
        for (EventDepartment department : safe(company.getDepartments())) {
            users.addAll(departmentUsers(department));
        }
        return users;
    }

    private static List<EventRecipient> departmentUsers(EventDepartment department) {
        List<EventRecipient> users = new ArrayList<>(safe(department.getUsers()));
        for (EventDepartment subDepartment : safe(department.getSubDepartments())) {
            users.addAll(departmentUsers(subDepartment));
        }
        return users;
    }

    private static <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }
}
