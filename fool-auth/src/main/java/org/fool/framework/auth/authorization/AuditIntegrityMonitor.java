package org.fool.framework.auth.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuditIntegrityMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(AuditIntegrityMonitor.class);
    private final JdbcSecurityAuditService auditService;

    public AuditIntegrityMonitor(JdbcSecurityAuditService auditService) {
        this.auditService = auditService;
    }

    @Scheduled(initialDelayString = "${fool.auth.audit-integrity.initial-delay-ms:60000}",
            fixedDelayString = "${fool.auth.audit-integrity.interval-ms:300000}")
    public void verify() {
        JdbcSecurityAuditService.IntegrityReport report = auditService.verifyAndAlert();
        if (!report.valid()) {
            LOG.error("security_audit_integrity_failed reason={} eventCount={} lastEventId={}",
                    report.reasonCode(), report.eventCount(), report.lastEventId());
        }
    }
}
