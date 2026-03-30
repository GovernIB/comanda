package es.caib.comanda.permisos.logic.helper;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermisosClientHelperTest {

    @Test
    void entornAppFindById_retornaElRecursQuanElClientElTroba() {
        // Comprova la consulta d'entorn-app per identificador contra el client remot.
        HttpAuthorizationHeaderHelper authHelper = mock(HttpAuthorizationHeaderHelper.class);
        MonitorServiceClient monitorServiceClient = mock(MonitorServiceClient.class);
        EntornAppServiceClient entornAppServiceClient = mock(EntornAppServiceClient.class);
        when(authHelper.getAuthorizationHeader()).thenReturn("Bearer token");
        PermisosClientHelper helper = new PermisosClientHelper(authHelper, monitorServiceClient, entornAppServiceClient);
        EntornApp entornApp = EntornApp.builder().id(9L).build();
        when(entornAppServiceClient.getOne(9L, null, "Bearer token")).thenReturn(EntityModel.of(entornApp));

        EntornApp byId = helper.entornAppFindById(9L);

        assertThat(byId).isNotNull();
        assertThat(byId.getId()).isEqualTo(9L);
    }

    @Test
    void entornAppFindById_retornaNullQuanElClientNoTrobaCapRecurs() {
        // Exercita el ramal on la cerca remota per id no retorna cap entorn-app.
        HttpAuthorizationHeaderHelper authHelper = mock(HttpAuthorizationHeaderHelper.class);
        MonitorServiceClient monitorServiceClient = mock(MonitorServiceClient.class);
        EntornAppServiceClient entornAppServiceClient = mock(EntornAppServiceClient.class);
        when(authHelper.getAuthorizationHeader()).thenReturn("Bearer token");
        PermisosClientHelper helper = new PermisosClientHelper(authHelper, monitorServiceClient, entornAppServiceClient);
        when(entornAppServiceClient.getOne(99L, null, "Bearer token")).thenReturn(null);

        EntornApp byId = helper.entornAppFindById(99L);

        assertThat(byId).isNull();
    }

    @Test
    void entornAppFindByEntornAndApp_retornaElPrimerResultatQuanLaCercaTrobaCoincidencies() {
        // Verifica la cerca per filtre entorn/app quan el client remot retorna resultats.
        HttpAuthorizationHeaderHelper authHelper = mock(HttpAuthorizationHeaderHelper.class);
        MonitorServiceClient monitorServiceClient = mock(MonitorServiceClient.class);
        EntornAppServiceClient entornAppServiceClient = mock(EntornAppServiceClient.class);
        when(authHelper.getAuthorizationHeader()).thenReturn("Bearer token");
        PermisosClientHelper helper = new PermisosClientHelper(authHelper, monitorServiceClient, entornAppServiceClient);
        EntornApp entornApp = EntornApp.builder().id(9L).build();
        PagedModel<EntityModel<EntornApp>> paged = PagedModel.of(List.of(EntityModel.of(entornApp)), new PagedModel.PageMetadata(1, 0, 1));
        when(entornAppServiceClient.find(null, "entorn.id:2 and app.id:3", null, null, "UNPAGED", null, "Bearer token")).thenReturn(paged);

        Optional<EntornApp> byFilter = helper.entornAppFindByEntornAndApp(2L, 3L);

        assertThat(byFilter).isPresent();
        assertThat(byFilter.get().getId()).isEqualTo(9L);
    }

    @Test
    void entornAppFindByEntornAndApp_retornaOptionalBuitQuanNoHiHaResultats() {
        // Comprova el ramal on la cerca remota queda buida per al filtre entorn/app.
        HttpAuthorizationHeaderHelper authHelper = mock(HttpAuthorizationHeaderHelper.class);
        MonitorServiceClient monitorServiceClient = mock(MonitorServiceClient.class);
        EntornAppServiceClient entornAppServiceClient = mock(EntornAppServiceClient.class);
        when(authHelper.getAuthorizationHeader()).thenReturn("Bearer token");
        PermisosClientHelper helper = new PermisosClientHelper(authHelper, monitorServiceClient, entornAppServiceClient);
        when(entornAppServiceClient.find(null, "entorn.id:7 and app.id:8", null, null, "UNPAGED", null, "Bearer token"))
                .thenReturn(PagedModel.empty());

        Optional<EntornApp> byFilter = helper.entornAppFindByEntornAndApp(7L, 8L);

        assertThat(byFilter).isEmpty();
    }

    @Test
    void monitorCreate_absorbeixErrorsDelClientDeMonitoritzacio() {
        // Verifica que la creació de monitor no propaga excepcions del client remot.
        HttpAuthorizationHeaderHelper authHelper = mock(HttpAuthorizationHeaderHelper.class);
        MonitorServiceClient monitorServiceClient = mock(MonitorServiceClient.class);
        EntornAppServiceClient entornAppServiceClient = mock(EntornAppServiceClient.class);
        when(authHelper.getAuthorizationHeader()).thenReturn("Bearer token");
        doThrow(new RuntimeException("boom")).when(monitorServiceClient).create(any(Monitor.class), eq("Bearer token"));
        PermisosClientHelper helper = new PermisosClientHelper(authHelper, monitorServiceClient, entornAppServiceClient);

        helper.monitorCreate(Monitor.builder().operacio("op").build());

        verify(monitorServiceClient).create(any(Monitor.class), eq("Bearer token"));
    }
}
