package org.fool.framework.auth.business.service;

import org.fool.framework.auth.foolframework.auth.MenuItem;
import org.fool.framework.app.AppFacade;
import org.fool.framework.app.ApplicationDefinition;
import org.fool.framework.app.StoreDatabase;
import org.fool.framework.dao.DaoService;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthServiceLegacyMenuTest {
    @Test
    public void getLegacySubMenusMapsTopMenusToLegacyAuthItems() throws Exception {
        DaoService daoService = mock(DaoService.class);
        TokenService tokenService = mock(TokenService.class);
        AuthService service = new AuthService();
        setField(service, "daoService", daoService);
        setField(service, "tokenService", tokenService);
        when(tokenService.getUidByToken("token-1")).thenReturn("admin");
        when(daoService.selectList(eq(MenuItem.class), anyString(), eq("admin")))
                .thenReturn(List.of(menu(2L, "OrderList", 100L, 7)));

        List<AuthService.LegacyAuthItem> items = service.getLegacySubMenus("token-1", "");

        assertEquals(1, items.size());
        assertEquals("2", items.get(0).getAuthNo());
        assertEquals("OrderList", items.get(0).getText());
        assertEquals(100L, items.get(0).getViewId());
        assertEquals(7, items.get(0).getIndex());
    }

    @Test
    public void getLegacySubMenusUsesParentAuthCodeForChildren() throws Exception {
        DaoService daoService = mock(DaoService.class);
        TokenService tokenService = mock(TokenService.class);
        AuthService service = new AuthService();
        setField(service, "daoService", daoService);
        setField(service, "tokenService", tokenService);
        when(tokenService.getUidByToken("token-1")).thenReturn("admin");

        service.getLegacySubMenus("token-1", "1");

        verify(daoService).selectList(eq(MenuItem.class), anyString(), eq("admin"), eq(1L));
    }

    @Test
    public void getLegacyAppInfoMapsDefaultApplication() throws Exception {
        TokenService tokenService = mock(TokenService.class);
        AppFacade appFacade = mock(AppFacade.class);
        AuthService service = new AuthService();
        setField(service, "tokenService", tokenService);
        setField(service, "appFacade", appFacade);
        ApplicationDefinition app = new ApplicationDefinition();
        app.setAppId("fool-service");
        app.setName("Fool Service");
        app.setVersion("1.0.0");
        app.setCompany("YFGE");
        app.setUrl("https://example.com");
        app.setAvatar("/logo.png");
        app.setDefaultView(100L);
        when(tokenService.getUidByToken("token-1")).thenReturn("admin");
        when(appFacade.getApps()).thenReturn(List.of(app));

        AuthService.LegacyAppInfo info = service.getLegacyAppInfo("token-1");

        assertEquals("Fool Service", info.getAppName());
        assertEquals("1.0.0", info.getAppVer());
        assertEquals("YFGE", info.getAppPowerBy());
        assertEquals("https://example.com", info.getAppPowerUrl());
        assertEquals("/logo.png", info.getAppLogoUrl());
        assertEquals(100L, info.getDefaultViewId());
        assertEquals("fool-service", info.getAppId());
    }

    @Test
    public void getLegacyAppInfoUsesLoginV2SessionApplication() throws Exception {
        TokenService tokenService = mock(TokenService.class);
        AppFacade appFacade = mock(AppFacade.class);
        AuthService service = new AuthService();
        setField(service, "tokenService", tokenService);
        setField(service, "appFacade", appFacade);
        ApplicationDefinition fallback = new ApplicationDefinition();
        fallback.setAppId("fallback");
        fallback.setName("Fallback");
        fallback.setDefaultView(100L);
        ApplicationDefinition selected = new ApplicationDefinition();
        selected.setAppId("selected");
        selected.setName("Selected");
        selected.setDefaultView(200L);
        when(tokenService.getUidByToken("token-1")).thenReturn("admin");
        when(tokenService.getLegacyAppId("token-1")).thenReturn("selected");
        when(appFacade.getApps()).thenReturn(List.of(fallback, selected));

        AuthService.LegacyAppInfo info = service.getLegacyAppInfo("token-1");

        assertEquals("Selected", info.getAppName());
        assertEquals(200L, info.getDefaultViewId());
        assertEquals("selected", info.getAppId());
    }

    @Test
    public void getLegacyConnectionContextUsesLoginV2SessionAppAndDatabase() throws Exception {
        DaoService daoService = mock(DaoService.class);
        TokenService tokenService = mock(TokenService.class);
        AppFacade appFacade = mock(AppFacade.class);
        AuthService service = new AuthService();
        setField(service, "daoService", daoService);
        setField(service, "tokenService", tokenService);
        setField(service, "appFacade", appFacade);
        ApplicationDefinition app = new ApplicationDefinition();
        app.setAppId("selected");
        app.setSysCon("app-con");
        StoreDatabase db = new StoreDatabase();
        db.setStoreBaseId("car_wash");
        db.setConnection("data-con");
        when(tokenService.getUidByToken("token-1")).thenReturn("admin");
        when(tokenService.getLegacyAppId("token-1")).thenReturn("selected");
        when(tokenService.getLegacyDbId("token-1")).thenReturn("car_wash");
        when(appFacade.getApps()).thenReturn(List.of(app));
        when(daoService.selectList(eq(StoreDatabase.class), anyString(), eq("selected")))
                .thenReturn(List.of(db));

        assertEquals("app-con", service.getLegacyAppConnection("token-1"));
        assertEquals("data-con", service.getLegacyDataConnection("token-1"));
    }

    @Test
    public void getLegacyInitAppInfoMapsApplicationAndDatabases() throws Exception {
        DaoService daoService = mock(DaoService.class);
        AppFacade appFacade = mock(AppFacade.class);
        AuthService service = new AuthService();
        setField(service, "daoService", daoService);
        setField(service, "appFacade", appFacade);
        ApplicationDefinition app = new ApplicationDefinition();
        app.setAppId("fool-service");
        app.setName("Fool Service");
        app.setVersion("1.0.0");
        app.setInitImage("/init.png");
        app.setCompany("YFGE");
        app.setUrl("https://example.com");
        StoreDatabase db = new StoreDatabase();
        db.setStoreBaseId("car_wash");
        db.setName("car_wash");
        when(appFacade.getApp("fool-service", "fool-service")).thenReturn(app);
        when(daoService.selectList(eq(StoreDatabase.class), anyString(), eq("fool-service")))
                .thenReturn(List.of(db));

        AuthService.LegacyInitAppInfo info = service.getLegacyInitAppInfo("fool-service", "fool-service");

        assertEquals("Fool Service", info.getAppTitle());
        assertEquals("Fool Service", info.getAppName());
        assertEquals("1.0.0", info.getAppVersion());
        assertEquals("/init.png", info.getAppImg());
        assertEquals("YFGE", info.getAppPowerBy());
        assertEquals("https://example.com", info.getAppUrl());
        assertEquals("car_wash", info.getDbs().get(0).getDbId());
        assertEquals("car_wash", info.getDbs().get(0).getDbName());
    }

    @Test
    public void hasLegacyStoreDatabaseChecksAppDatabaseRelation() throws Exception {
        DaoService daoService = mock(DaoService.class);
        AuthService service = new AuthService();
        setField(service, "daoService", daoService);
        StoreDatabase db = new StoreDatabase();
        db.setStoreBaseId("car_wash");
        when(daoService.selectList(eq(StoreDatabase.class), anyString(), eq("fool-service")))
                .thenReturn(List.of(db));

        assertEquals(true, service.hasLegacyStoreDatabase("fool-service", "car_wash"));
    }

    private static MenuItem menu(Long id, String text, Long viewId, Integer index) {
        MenuItem item = new MenuItem();
        item.setId(id);
        item.setText(text);
        item.setViewId(viewId);
        item.setIndex(index);
        return item;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
