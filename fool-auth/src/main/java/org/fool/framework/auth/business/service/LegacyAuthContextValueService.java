package org.fool.framework.auth.business.service;

import org.fool.framework.auth.dto.UserDTO;
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
        if (!StringUtils.hasText(token) || !StringUtils.hasText(key)) {
            return "";
        }
        UserDTO user = authService.getInfoByToken(token);
        if (user == null) {
            return "";
        }
        return switch (key.trim().toLowerCase(Locale.ROOT)) {
            case "userid" -> empty(user.getId());
            case "username" -> empty(user.getName());
            default -> "";
        };
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }
}
