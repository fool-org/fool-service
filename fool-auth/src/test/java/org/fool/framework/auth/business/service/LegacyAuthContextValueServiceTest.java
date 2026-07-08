package org.fool.framework.auth.business.service;

import org.fool.framework.auth.dto.UserDTO;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LegacyAuthContextValueServiceTest {
    @Test
    public void resolvesLegacyUserContextValuesFromToken() {
        AuthService authService = mock(AuthService.class);
        LegacyAuthContextValueService service = new LegacyAuthContextValueService();
        ReflectionTestUtils.setField(service, "authService", authService);
        UserDTO user = new UserDTO();
        user.setId("admin");
        user.setName("Admin User");
        when(authService.getInfoByToken("token-1")).thenReturn(user);

        assertEquals("admin", service.getValue("token-1", "userid"));
        assertEquals("Admin User", service.getValue("token-1", "username"));
    }
}
