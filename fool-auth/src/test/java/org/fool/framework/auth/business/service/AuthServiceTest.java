package org.fool.framework.auth.business.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuthServiceTest {

    @Test
    public void passwordHashUsesStableMd5HexForDockerSeedUsers() throws Exception {
        assertEquals(
                "21232f297a57a5a743894a0e4a801fc3",
                new AuthService().passwordHash("admin", ""));
    }
}
