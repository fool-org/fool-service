package org.fool.framework.common.authz;

@FunctionalInterface
public interface AuthorizationService {
    AuthorizationDecision decide(AuthorizationRequest request);

    static AuthorizationService denyByDefault() {
        return request -> AuthorizationDecision.deny("NO_MATCHING_ALLOW", request.subject().policyVersion());
    }
}
