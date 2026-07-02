package org.fool.framework.auth.foolframework.auth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MenuItemFactory {
    // ponytail: keep legacy connection payloads opaque until DB-backed menu loading needs them.
    private final Object con;
    private final Object conFac;

    public MenuItemFactory(Object con, Object conFac) {
        this.con = con;
        this.conFac = conFac;
    }

    public List<MenuItem> getTopMenus(AuthorizedUser user) {
        List<MenuItem> items = roleItems(user);
        return items.stream()
                .sorted(Comparator.comparing(MenuItem::getIndex))
                .filter(item -> items.stream().noneMatch(parent -> hasSubItem(parent, item.getId())))
                .toList();
    }

    public List<MenuItem> getMenus(AuthorizedUser user, long topId) {
        List<MenuItem> items = roleItems(user);
        return items.stream()
                .sorted(Comparator.comparing(MenuItem::getIndex))
                .filter(item -> items.stream()
                        .anyMatch(parent -> Objects.equals(parent.getId(), topId) && hasSubItem(parent, item.getId())))
                .toList();
    }

    public Object getCon() {
        return con;
    }

    public Object getConFac() {
        return conFac;
    }

    private static List<MenuItem> roleItems(AuthorizedUser user) {
        List<MenuItem> result = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        for (Role role : user.getRoles()) {
            for (MenuItem item : role.getItems()) {
                if (ids.add(item.getId())) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    private static boolean hasSubItem(MenuItem parent, Long itemId) {
        return parent.getSubItems().stream().anyMatch(child -> Objects.equals(child.getId(), itemId));
    }
}
