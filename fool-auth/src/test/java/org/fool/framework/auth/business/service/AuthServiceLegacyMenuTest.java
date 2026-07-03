package org.fool.framework.auth.business.service;

import org.fool.framework.auth.foolframework.auth.MenuItem;
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
