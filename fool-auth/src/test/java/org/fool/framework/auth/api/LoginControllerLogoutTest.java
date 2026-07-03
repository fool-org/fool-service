package org.fool.framework.auth.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.auth.business.service.AuthService;
import org.fool.framework.auth.business.service.CheckCodeService;
import org.fool.framework.auth.dto.UserDTO;
import org.fool.framework.dto.CommonRequest;
import org.fool.framework.dto.CommonResponse;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

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

    @Test
    public void getCheckCodeReturnsGeneratedLegacyPayload() throws Exception {
        CheckCodeService checkCodeService = mock(CheckCodeService.class);
        LoginController controller = new LoginController();
        setField(controller, "checkCodeService", checkCodeService);
        CheckCodeService.CheckCodeResult result = new CheckCodeService.CheckCodeResult("key-1", "A2BC", "image");
        when(checkCodeService.create()).thenReturn(result);

        CommonResponse<CheckCodeService.CheckCodeResult> response = controller.getCheckCode();

        assertEquals(0, response.getCode());
        assertEquals("key-1", response.getData().getKey());
        assertEquals("A2BC", response.getData().getCode());
        verify(checkCodeService).create();
    }

    @Test
    public void checkCodeReturnsLegacyValidationBoolean() throws Exception {
        CheckCodeService checkCodeService = mock(CheckCodeService.class);
        LoginController controller = new LoginController();
        setField(controller, "checkCodeService", checkCodeService);
        CheckCodeService.CheckCodeRequest request = new CheckCodeService.CheckCodeRequest();
        request.setKey("key-1");
        request.setCode("A2BC");
        when(checkCodeService.validate(request)).thenReturn(true);

        CommonResponse<Boolean> response = controller.checkCode(request);

        assertEquals(0, response.getCode());
        assertEquals(Boolean.TRUE, response.getData());
        verify(checkCodeService).validate(request);
    }

    @Test
    public void getSubMenuReturnsLegacyAuthItems() throws Exception {
        AuthService authService = mock(AuthService.class);
        LoginController controller = new LoginController();
        setField(controller, "authService", authService);
        LoginController.LegacySubMenuRequest request = new LoginController.LegacySubMenuRequest();
        request.setToken("token-1");
        request.setParentAuthCode("1");
        AuthService.LegacyAuthItem item = new AuthService.LegacyAuthItem();
        item.setAuthNo("2");
        item.setText("OrderList");
        when(authService.getLegacySubMenus("token-1", "1")).thenReturn(List.of(item));

        CommonResponse<LoginController.LegacySubMenuResult> response = controller.getSubMenu(request);

        assertEquals(0, response.getCode());
        assertEquals("token-1", response.getData().getToken());
        assertEquals("2", response.getData().getItems().get(0).getAuthNo());
        verify(authService).getLegacySubMenus("token-1", "1");
    }

    @Test
    public void getMainReturnsLegacyUserAndTopMenus() throws Exception {
        AuthService authService = mock(AuthService.class);
        LoginController controller = new LoginController();
        setField(controller, "authService", authService);
        UserDTO user = new UserDTO();
        user.setId("42");
        user.setName("Admin");
        AuthService.LegacyAuthItem menu = new AuthService.LegacyAuthItem();
        menu.setAuthNo("1");
        menu.setText("Views");
        when(authService.getInfoByToken("token-1")).thenReturn(user);
        when(authService.getLegacySubMenus("token-1", "")).thenReturn(List.of(menu));

        CommonResponse<LoginController.LegacyMainResult> response = controller.getMain("token-1");

        assertEquals(0, response.getCode());
        assertEquals("token-1", response.getData().getToken());
        assertEquals(42L, response.getData().getUser().getUserId());
        assertEquals("Views", response.getData().getTopMenu().get(0).getText());
        assertEquals(0L, response.getData().getApp().getDefaultViewId());
    }

    @Test
    public void subMenuRequestAcceptsLegacyParentAuthCode() throws Exception {
        LoginController.LegacySubMenuRequest request = new ObjectMapper().readValue(
                "{\"Token\":\"token-1\",\"ParentAuthCode\":\"1\"}",
                LoginController.LegacySubMenuRequest.class);

        assertEquals("token-1", request.getToken());
        assertEquals("1", request.getParentAuthCode());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
