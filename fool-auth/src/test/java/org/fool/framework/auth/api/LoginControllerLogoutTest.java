package org.fool.framework.auth.api;

import org.fool.framework.auth.business.service.AuthService;
import org.fool.framework.dto.CommonRequest;
import org.fool.framework.dto.CommonResponse;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LoginControllerLogoutTest {
    @Test
    public void logoutUsesRequestToken() throws Exception {
        AuthService authService = mock(AuthService.class);
        LoginController controller = new LoginController();
        setField(controller, "authService", authService);
        CommonRequest request = new CommonRequest();
        request.setToken("token-1");

        CommonResponse<Void> response = controller.logout(request);

        verify(authService).logout("token-1");
        assertEquals(0, response.getCode());
        assertNull(response.getData());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
