package org.fool.framework.auth.business.service;

import org.fool.framework.auth.dto.UserDTO;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.EffectiveSubjectContext;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LegacyAuthContextValueServiceTest {
    @Test
    public void resolvesLegacyContextValuesFromEffectiveSubject() {
        AuthService authService = mock(AuthService.class);
        LegacyAuthContextValueService service = new LegacyAuthContextValueService();
        ReflectionTestUtils.setField(service, "authService", authService);
        UserDTO user = new UserDTO();
        user.setId("admin");
        user.setName("Admin User");
        when(authService.getInfoForUser("admin")).thenReturn(user);
        when(authService.getLegacyAppConnectionForScope("fool-service")).thenReturn("app-con");
        when(authService.getLegacyDataConnectionForScope("fool-service", "car_wash")).thenReturn("data-con");
        EffectiveSubjectContext.set(new EffectiveSubject(
                "admin", List.of(), "company", List.of(), "fool-service", "car_wash",
                "session", Instant.EPOCH, null, 1));
        try {
            assertEquals("admin", service.getValue("ignored-token", "userid"));
            assertEquals("Admin User", service.getValue("ignored-token", "username"));
            assertEquals("app-con", service.getValue("ignored-token", "appcon"));
            assertEquals("data-con", service.getValue("ignored-token", "datacon"));
        } finally {
            EffectiveSubjectContext.clear();
        }
    }

    @Test
    public void rawTokenCannotRestoreLegacyContextWithoutSubject() {
        AuthService authService = mock(AuthService.class);
        LegacyAuthContextValueService service = new LegacyAuthContextValueService();
        ReflectionTestUtils.setField(service, "authService", authService);

        assertEquals("", service.getValue("raw-token", "userid"));
        assertEquals("", service.getValue("raw-token", "appcon"));
    }
}
