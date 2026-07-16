package org.fool.framework.common.authz;

public record AuthorizationDecision(boolean allowed,
                                    String reasonCode,
                                    long policyVersion,
                                    String dataPolicyId,
                                    DataPolicy dataPolicy,
                                    RiskLevel minimumRisk) {
    public AuthorizationDecision {
        minimumRisk = minimumRisk == null ? RiskLevel.LOW : minimumRisk;
    }

    public static AuthorizationDecision allow(long policyVersion, String dataPolicyId) {
        return new AuthorizationDecision(
                true, "ALLOW", policyVersion, dataPolicyId, DataPolicy.unrestricted(), RiskLevel.LOW);
    }

    public static AuthorizationDecision allow(long policyVersion,
                                              String dataPolicyId,
                                              DataPolicy dataPolicy) {
        return new AuthorizationDecision(
                true,
                "ALLOW",
                policyVersion,
                dataPolicyId,
                dataPolicy == null ? DataPolicy.unrestricted() : dataPolicy,
                RiskLevel.LOW);
    }

    public static AuthorizationDecision allow(long policyVersion,
                                              String dataPolicyId,
                                              DataPolicy dataPolicy,
                                              RiskLevel minimumRisk) {
        return new AuthorizationDecision(
                true,
                "ALLOW",
                policyVersion,
                dataPolicyId,
                dataPolicy == null ? DataPolicy.unrestricted() : dataPolicy,
                minimumRisk);
    }

    public static AuthorizationDecision deny(String reasonCode, long policyVersion) {
        return new AuthorizationDecision(false, reasonCode, policyVersion, null, null, RiskLevel.LOW);
    }
}
