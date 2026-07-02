package org.fool.framework.auth.foolframework.auth;

import org.fool.framework.dao.DaoService;

public class AuthoriezedFactory {
    // ponytail: keep legacy app/context payloads opaque until routed SysCon lookup is needed.
    private final Object app;
    private final Object conFac;
    private final DaoService daoService;

    public AuthoriezedFactory(Object app, Object conFac, DaoService daoService) {
        this.app = app;
        this.conFac = conFac;
        this.daoService = daoService;
    }

    public AuthorizedUser getAuthrizedUser(User user) {
        return daoService.getOneDetailByKey(AuthorizedUser.class, user.getUserId());
    }

    public Object getApp() {
        return app;
    }

    public Object getConFac() {
        return conFac;
    }
}
