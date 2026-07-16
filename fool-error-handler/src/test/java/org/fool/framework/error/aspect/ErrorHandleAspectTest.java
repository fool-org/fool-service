package org.fool.framework.error.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.fool.framework.common.authz.AuthorizationDeniedException;
import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.error.ExceptionDealerService;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class ErrorHandleAspectTest {
    @Test
    public void securityExceptionsReachControllerAdvice() throws Throwable {
        ErrorHandleAspect aspect = new ErrorHandleAspect();
        ExceptionDealerService dealer = mock(ExceptionDealerService.class);
        ReflectionTestUtils.setField(aspect, "dealerService", dealer);

        ProceedingJoinPoint controlled = mock(ProceedingJoinPoint.class);
        ControlledActionException controlledError =
                new ControlledActionException(403, "NO_MATCHING_ALLOW");
        when(controlled.proceed()).thenThrow(controlledError);
        assertSame(controlledError, assertThrows(
                ControlledActionException.class,
                () -> aspect.arroundPointCut(controlled)));

        ProceedingJoinPoint authorization = mock(ProceedingJoinPoint.class);
        AuthorizationDeniedException authorizationError =
                new AuthorizationDeniedException("FIELD_NOT_READABLE");
        when(authorization.proceed()).thenThrow(authorizationError);
        assertSame(authorizationError, assertThrows(
                AuthorizationDeniedException.class,
                () -> aspect.arroundPointCut(authorization)));
        verifyNoInteractions(dealer);
    }

    @Test
    public void legacyErrorsKeepCommonResponseCompatibility() throws Throwable {
        ErrorHandleAspect aspect = new ErrorHandleAspect();
        ExceptionDealerService dealer = mock(ExceptionDealerService.class);
        ReflectionTestUtils.setField(aspect, "dealerService", dealer);
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        IllegalStateException error = new IllegalStateException("legacy");
        CommonResponse<Void> response = CommonResponse.<Void>builder()
                .code(500)
                .message("legacy")
                .build();
        when(joinPoint.proceed()).thenThrow(error);
        when(dealer.handle(error)).thenReturn(response);

        assertSame(response, aspect.arroundPointCut(joinPoint));
    }
}
