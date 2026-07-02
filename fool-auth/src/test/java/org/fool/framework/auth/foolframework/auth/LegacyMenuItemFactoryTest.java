package org.fool.framework.auth.foolframework.auth;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class LegacyMenuItemFactoryTest {
    @Test
    public void storesConnectionPayloads() {
        Object con = new Object();
        Object conFac = new Object();
        MenuItemFactory factory = new MenuItemFactory(con, conFac);

        assertSame(con, factory.getCon());
        assertSame(conFac, factory.getConFac());
    }

    @Test
    public void getsDedupedTopMenusByIndex() {
        MenuItem root = menu(1L, 20);
        MenuItem child = menu(2L, 10);
        MenuItem orphan = menu(3L, 5);
        root.getSubItems().add(child);

        AuthorizedUser user = user(role(child, root), role(orphan, menu(1L, 99)));

        assertEquals(List.of(3L, 1L), ids(new MenuItemFactory(null, null).getTopMenus(user)));
    }

    @Test
    public void getsDedupedChildMenusByTopIdAndIndex() {
        MenuItem root = menu(1L, 20);
        MenuItem first = menu(2L, 10);
        MenuItem second = menu(3L, 5);
        root.getSubItems().add(first);
        root.getSubItems().add(second);

        AuthorizedUser user = user(role(root, first, second), role(menu(2L, 99)));

        assertEquals(List.of(3L, 2L), ids(new MenuItemFactory(null, null).getMenus(user, 1L)));
    }

    private static AuthorizedUser user(Role... roles) {
        AuthorizedUser user = new AuthorizedUser();
        user.getRoles().addAll(List.of(roles));
        return user;
    }

    private static Role role(MenuItem... items) {
        Role role = new Role();
        role.getItems().addAll(List.of(items));
        return role;
    }

    private static MenuItem menu(Long id, Integer index) {
        MenuItem item = new MenuItem();
        item.setId(id);
        item.setIndex(index);
        return item;
    }

    private static List<Long> ids(List<MenuItem> items) {
        return items.stream().map(MenuItem::getId).toList();
    }
}
