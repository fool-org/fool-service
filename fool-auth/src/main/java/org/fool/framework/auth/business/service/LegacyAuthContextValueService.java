package org.fool.framework.auth.business.service;

import org.fool.framework.auth.dto.UserDTO;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.EffectiveSubjectContext;
import org.fool.framework.common.context.LegacyContextValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
public class LegacyAuthContextValueService implements LegacyContextValueService {
    @Autowired
    private AuthService authService;

    @Override
    public Object getValue(String token, String key) {
        if (!StringUtils.hasText(key)) {
            return "";
        }
        EffectiveSubject subject = EffectiveSubjectContext.get();
        return subject == null ? "" : subjectValue(subject, key);
    }

    private Object subjectValue(EffectiveSubject subject, String key) {
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        if ("appcon".equals(normalized)) {
            return authService.getLegacyAppConnectionForScope(subject.appId());
        }
        if ("datacon".equals(normalized)) {
            return authService.getLegacyDataConnectionForScope(subject.appId(), subject.databaseId());
        }
        UserDTO user = authService.getInfoForUser(subject.userId());
        if (user == null) {
            return "";
        }
        return switch (normalized) {
            case "userid" -> empty(user.getId());
            case "username" -> empty(user.getName());
            default -> "";
        };
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }
}
