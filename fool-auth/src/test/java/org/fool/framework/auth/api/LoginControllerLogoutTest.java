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
import static org.junit.Assert.assertTrue;
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
        String json = new ObjectMapper().writeValueAsString(response.getData());
        assertTrue(json.contains("\"Key\":\"key-1\""));
        assertTrue(json.contains("\"Code\":\"A2BC\""));
        assertTrue(json.contains("\"ChkCodeImg\":\"image\""));
        verify(checkCodeService).create();
    }

    @Test
    public void getCheckCodeAlsoExposesLegacyWebGetChkRoute() throws Exception {
        var mapping = LoginController.class
                .getMethod("getCheckCode")
                .getAnnotation(org.springframework.web.bind.annotation.PostMapping.class);

        assertTrue(List.of(mapping.value()).contains("/getchk"));
    }

    @Test
    public void initAppReturnsLegacyAppDbsAndCheckCode() throws Exception {
        AuthService authService = mock(AuthService.class);
        CheckCodeService checkCodeService = mock(CheckCodeService.class);
        LoginController controller = new LoginController();
        setField(controller, "authService", authService);
        setField(controller, "checkCodeService", checkCodeService);
        LoginController.LegacyInitAppRequest request = new LoginController.LegacyInitAppRequest();
        request.setAppId("fool-service");
        request.setAppKey("fool-service");
        AuthService.LegacyInitAppInfo app = new AuthService.LegacyInitAppInfo();
        app.setAppTitle("Fool Service");
        app.setAppName("Fool Service");
        app.setAppVersion("1.0.0");
        AuthService.LegacyStoreBaseInfo db = new AuthService.LegacyStoreBaseInfo();
        db.setDbId("car_wash");
        db.setDbName("car_wash");
        app.setDbs(List.of(db));
        CheckCodeService.CheckCodeResult checkCode = new CheckCodeService.CheckCodeResult("key-1", "A2BC", "image");
        when(authService.getLegacyInitAppInfo("fool-service", "fool-service")).thenReturn(app);
        when(checkCodeService.create()).thenReturn(checkCode);

        CommonResponse<LoginController.LegacyInitAppResult> response = controller.initApp(request);

        assertEquals(0, response.getCode());
        assertEquals("Fool Service", response.getData().getAppTitle());
        assertEquals("1.0.0", response.getData().getAppVersion());
        assertEquals("key-1", response.getData().getCheckCode().getKey());
        assertEquals("car_wash", response.getData().getDbs().get(0).getDbId());
        String json = new ObjectMapper().writeValueAsString(response.getData());
        assertTrue(json.contains("\"AppTitle\":\"Fool Service\""));
        assertTrue(json.contains("\"CheckCode\""));
        assertTrue(json.contains("\"Key\":\"key-1\""));
        assertTrue(json.contains("\"Dbs\""));
        assertTrue(json.contains("\"DbId\":\"car_wash\""));
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
    public void loginV2ReturnsLegacyTokenUserAndApp() throws Exception {
        AuthService authService = mock(AuthService.class);
        CheckCodeService checkCodeService = mock(CheckCodeService.class);
        LoginController controller = new LoginController();
        setField(controller, "authService", authService);
        setField(controller, "checkCodeService", checkCodeService);
        LoginController.LegacyLoginRequest request = new LoginController.LegacyLoginRequest();
        request.setUserId("42");
        request.setPassWord("pwd");
        request.setAppId("fool-service");
        request.setAppKey("fool-service");
        request.setDbId("car_wash");
        request.setCheckCodeKey("key-1");
        request.setCheckCode("A2BC");
        AuthService.LegacyAppInfo app = new AuthService.LegacyAppInfo();
        app.setAppName("Fool Service");
        UserDTO user = new UserDTO();
        user.setId("42");
        user.setName("Admin");
        org.fool.framework.auth.dto.LoginVo login = new org.fool.framework.auth.dto.LoginVo();
        login.setToken("token-1");
        login.setUser(user);
        when(checkCodeService.validate(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(authService.getLegacyAppInfo("fool-service", "fool-service")).thenReturn(app);
        when(authService.hasLegacyStoreDatabase("fool-service", "car_wash")).thenReturn(true);
        when(authService.login("42", "pwd")).thenReturn(login);

        CommonResponse<LoginController.LegacyLoginResult> response = controller.loginV2(request);

        assertEquals(0, response.getCode());
        assertEquals("token-1", response.getData().getToken());
        assertEquals(true, response.getData().isLoginSucess());
        assertEquals("Fool Service", response.getData().getApp().getAppName());
        assertEquals("Admin", response.getData().getUser().getUserName());
        String json = new ObjectMapper().writeValueAsString(response.getData());
        assertTrue(json.contains("\"LoginSucess\":true"));
        assertTrue(json.contains("\"Token\":\"token-1\""));
        assertTrue(json.contains("\"AppName\":\"Fool Service\""));
        verify(authService).rememberLegacyApp("token-1", "fool-service", "car_wash");
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
        String json = new ObjectMapper().writeValueAsString(response.getData());
        assertTrue(json.contains("\"Items\""));
        assertTrue(json.contains("\"AuthNo\":\"2\""));
        verify(authService).getLegacySubMenus("token-1", "1");
    }

    @Test
    public void getSubMenuAlsoExposesLegacyGetMenuRoute() throws Exception {
        var mapping = LoginController.class
                .getMethod("getSubMenu", LoginController.LegacySubMenuRequest.class)
                .getAnnotation(org.springframework.web.bind.annotation.PostMapping.class);

        assertTrue(List.of(mapping.value()).contains("/getmenu"));
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
        AuthService.LegacyAppInfo app = new AuthService.LegacyAppInfo();
        app.setAppName("Fool Service");
        app.setDefaultViewId(100L);
        when(authService.getInfoByToken("token-1")).thenReturn(user);
        when(authService.getLegacyAppInfo("token-1")).thenReturn(app);
        when(authService.getLegacySubMenus("token-1", "")).thenReturn(List.of(menu));

        CommonResponse<LoginController.LegacyMainResult> response = controller.getMain("token-1");

        assertEquals(0, response.getCode());
        assertEquals("token-1", response.getData().getToken());
        assertEquals(42L, response.getData().getUser().getUserId());
        assertEquals("Views", response.getData().getTopMenu().get(0).getText());
        assertEquals("Fool Service", response.getData().getApp().getAppName());
        assertEquals(100L, response.getData().getApp().getDefaultViewId());
        String json = new ObjectMapper().writeValueAsString(response.getData());
        assertTrue(json.contains("\"App\""));
        assertTrue(json.contains("\"TopMenu\""));
        assertTrue(json.contains("\"DefaultViewId\":100"));
    }

    @Test
    public void getAppReturnsLegacyAppInfo() throws Exception {
        AuthService authService = mock(AuthService.class);
        LoginController controller = new LoginController();
        setField(controller, "authService", authService);
        CommonRequest request = new CommonRequest();
        request.setToken("token-1");
        AuthService.LegacyAppInfo app = new AuthService.LegacyAppInfo();
        app.setAppName("Fool Service");
        app.setAppId("fool-service");
        when(authService.getLegacyAppInfo("token-1")).thenReturn(app);

        CommonResponse<LoginController.LegacyAppResult> response = controller.getApp(request);

        assertEquals(0, response.getCode());
        assertEquals("token-1", response.getData().getToken());
        assertEquals("Fool Service", response.getData().getApp().getAppName());
        String json = new ObjectMapper().writeValueAsString(response.getData());
        assertTrue(json.contains("\"App\""));
        assertTrue(json.contains("\"AppId\":\"fool-service\""));
    }

    @Test
    public void subMenuRequestAcceptsLegacyParentAuthCode() throws Exception {
        LoginController.LegacySubMenuRequest request = new ObjectMapper().readValue(
                "{\"Token\":\"token-1\",\"ParentAuthCode\":\"1\"}",
                LoginController.LegacySubMenuRequest.class);

        assertEquals("token-1", request.getToken());
        assertEquals("1", request.getParentAuthCode());
    }

    @Test
    public void subMenuRequestAcceptsLegacyWebAuthCode() throws Exception {
        LoginController.LegacySubMenuRequest request = new ObjectMapper().readValue(
                "{\"Token\":\"token-1\",\"authcode\":\"1\"}",
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
