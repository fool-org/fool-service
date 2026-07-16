package org.fool.framework.common.authz;

import java.util.Map;

public record ControlledActionContext(String actionRequestId,
                                      EffectiveSubject subject,
                                      String action,
                                      String resourceType,
                                      String resourceId,
                                      String resourceKey,
                                      Map<String, Object> arguments,
                                      DataPolicy dataPolicy) {
    public ControlledActionContext {
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
        dataPolicy = dataPolicy == null ? DataPolicy.unrestricted() : dataPolicy;
    }
}
