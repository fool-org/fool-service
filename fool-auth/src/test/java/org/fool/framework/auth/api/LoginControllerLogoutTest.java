package org.fool.framework.auth.api;

import org.fool.framework.auth.business.service.AuthService;
import org.fool.framework.auth.dto.UserDTO;
import org.fool.framework.dto.CommonRequest;
import org.fool.framework.dto.CommonResponse;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    public void getUserInfoReturnsLegacyUserShape() throws Exception {
        AuthService authService = mock(AuthService.class);
        LoginController controller = new LoginController();
        setField(controller, "authService", authService);
        CommonRequest request = new CommonRequest();
        request.setToken("token-1");
        UserDTO user = new UserDTO();
        user.setId("42");
        user.setName("Admin");
        when(authService.getInfoByToken("token-1")).thenReturn(user);

        CommonResponse<LoginController.LegacyUserInfoResult> response = controller.getUserInfo(request);

        assertEquals(0, response.getCode());
        assertEquals("token-1", response.getData().getToken());
        assertEquals(42L, response.getData().getUser().getUserId());
        assertEquals("42", response.getData().getUser().getLoginName());
        assertEquals("Admin", response.getData().getUser().getUserName());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
