package org.fool.framework.view;

import org.fool.framework.common.authz.DataPolicy;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.query.IQueryFilter;
import org.fool.framework.view.api.ReportController;
import org.fool.framework.view.model.View;
import org.fool.framework.view.service.ReadAuthorizationEnforcer;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

/**
 * Keeps legacy unit tests focused on view mapping while authorization behavior
 * is covered by the dedicated authorization tests.
 */
public final class TestReadAuthorization {
    private TestReadAuthorization() {
    }

    public static ReadAuthorizationEnforcer install(Object target) {
        ReadAuthorizationEnforcer enforcer = Mockito.mock(ReadAuthorizationEnforcer.class);
        configure(enforcer);
        ReflectionTestUtils.setField(target, "authorizationEnforcer", enforcer);
        if (target instanceof org.fool.framework.view.service.DataQueryService) {
            ReflectionTestUtils.setField(target, "actionExecutionGuard",
                    Mockito.mock(org.fool.framework.common.authz.ControlledActionExecutionGuard.class));
        }
        if (target instanceof ReportController) {
            ReflectionTestUtils.setField(target, "daoService", reportDao());
        }
        return enforcer;
    }

    public static void configure(ReadAuthorizationEnforcer enforcer) {
        Mockito.when(enforcer.requireView(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(DataPolicy.unrestricted());
        Mockito.when(enforcer.requireModel(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(DataPolicy.unrestricted());
        Mockito.when(enforcer.constrainView(Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(enforcer.constrainWritableView(Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(enforcer.readable(Mockito.any(), Mockito.nullable(org.fool.framework.model.model.Property.class)))
                .thenReturn(true);
        Mockito.when(enforcer.readable(Mockito.any(), Mockito.nullable(org.fool.framework.view.model.ViewItem.class)))
                .thenReturn(true);
        Mockito.when(enforcer.writable(Mockito.any(), Mockito.nullable(org.fool.framework.view.model.ViewItem.class)))
                .thenReturn(true);
        Mockito.when(enforcer.rowFilter(Mockito.any(), Mockito.any()))
                .thenReturn(IQueryFilter.init());
        Mockito.when(enforcer.mask(Mockito.any(org.fool.framework.view.dto.ListViewResult.class), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(enforcer.mask(Mockito.any(org.fool.framework.view.dto.QueryDataDetailResult.class), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static DaoService reportDao() {
        Property symbol = new Property();
        symbol.setName("Symbol");
        symbol.setColumn("order_symbol");
        Property state = new Property();
        state.setName("State");
        state.setColumn("order_state");
        Model model = new Model();
        model.setName("TestReport");
        model.setProperties(List.of(symbol, state));
        View view = new View();
        view.setId(100L);
        view.setViewModel("TestReport");
        DaoService daoService = Mockito.mock(DaoService.class);
        Mockito.when(daoService.getOneDetailByKey(Mockito.eq(View.class), Mockito.anyString())).thenReturn(view);
        Mockito.when(daoService.getOneDetailByKey(Mockito.eq(Model.class), Mockito.eq("TestReport"))).thenReturn(model);
        return daoService;
    }
}
