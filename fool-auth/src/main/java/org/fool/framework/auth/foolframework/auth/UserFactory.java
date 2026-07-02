package org.fool.framework.auth.foolframework.auth;

import java.util.ArrayList;
import java.util.List;

public class UserFactory {
    // ponytail: keep the legacy app payload opaque until real auth lookup needs it.
    private final Object app;

    public UserFactory(Object app) {
        this.app = app;
    }

    public List<User> getUsers() {
        return new ArrayList<>();
    }
}
