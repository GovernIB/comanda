package es.caib.comanda.acl.logic.helper;

import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AclClientHelperTest {

    @Test
    void monitorCreate_absorbeixErrorsDelClientDeMonitoritzacio() {
        // Verifica que el helper de client no propaga errors quan falla el servei de monitorització.
        HttpAuthorizationHeaderHelper authHelper = mock(HttpAuthorizationHeaderHelper.class);
        MonitorServiceClient monitorServiceClient = mock(MonitorServiceClient.class);
        when(authHelper.getAuthorizationHeader()).thenReturn("Bearer token");
        doThrow(new RuntimeException("boom")).when(monitorServiceClient).create(any(Monitor.class), anyString());
        AclClientHelper helper = new AclClientHelper(authHelper, monitorServiceClient);

        helper.monitorCreate(Monitor.builder().operacio("acl").build());

        verify(monitorServiceClient).create(any(Monitor.class), anyString());
    }
}
