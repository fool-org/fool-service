package org.fool.framework.app;

import org.fool.framework.model.model.ConnectionType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConnectionTypeMigrationTest {
    @Test
    public void connectionTypeKeepsLegacyOrdinals() {
        assertEquals(0, ConnectionType.DEFAULT.code());
        assertEquals(1, ConnectionType.SYSTEM.code());
        assertEquals(2, ConnectionType.APP_SYS.code());
        assertEquals(3, ConnectionType.CURRENT.code());
        assertEquals(4, ConnectionType.MODEL_SYS.code());
    }

    @Test
    public void appInstallConstantsUseLegacyConnectionTypeCodes() {
        assertEquals(ConnectionType.APP_SYS.code(), AppInstalledModel.CONNECTION_TYPE_APP_SYS);
        assertEquals(ConnectionType.CURRENT.code(), AppInstalledModel.CONNECTION_TYPE_CURRENT);
        assertEquals(ConnectionType.APP_SYS.code(), AppSystemView.CONNECTION_TYPE_APP_SYS);
    }
}
