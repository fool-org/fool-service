package org.fool.framework.common.authz;

@FunctionalInterface
public interface SecurityAuditService {
    void record(SecurityAuditEvent event);
}
