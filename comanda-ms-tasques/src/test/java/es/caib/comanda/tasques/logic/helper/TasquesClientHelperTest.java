package es.caib.comanda.tasques.logic.helper;

import es.caib.comanda.client.AppServiceClient;
import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.EntornServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TasquesClientHelperTest {

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private MonitorServiceClient monitorServiceClient;

    @Mock
    private EntornAppServiceClient entornAppServiceClient;

    @Mock
    private EntornServiceClient entornServiceClient;

    @Mock
    private AppServiceClient appServiceClient;

    @InjectMocks
    private TasquesClientHelper helper;

    private EntornApp entornApp;
    private Monitor monitor;
    private App app;
    private Entorn entorn;
    private static final String AUTH_HEADER = "Bearer test-token";

    @BeforeEach
    void setUp() {
        entornApp = EntornApp.builder()
                .id(1L)
                .activa(true)
                .build();
        monitor = Monitor.builder()
                .entornAppId(1L)
                .modul(ModulEnum.SALUT)
                .tipus(AccioTipusEnum.SORTIDA)
                .url("http://test.com/health")
                .estat(EstatEnum.OK)
                .build();
        app = new App();
        entorn = Entorn.builder()
                .id(1L)
                .codi("ENT")
                .nom("ENT Name")
                .build();
        lenient().when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
    }

    @Test
    void entornAppFindById_quanElClientRetornaEntitat_retornaElContingut() {
        when(entornAppServiceClient.getOne(1L, null, AUTH_HEADER)).thenReturn(EntityModel.of(entornApp));

        EntornApp result = helper.entornAppFindById(1L);

        assertThat(result).isEqualTo(entornApp);
        verify(entornAppServiceClient).getOne(1L, null, AUTH_HEADER);
    }

    @Test
    void entornAppFindById_quanElClientRetornaNull_retornaNull() {
        when(entornAppServiceClient.getOne(1L, null, AUTH_HEADER)).thenReturn(null);

        EntornApp result = helper.entornAppFindById(1L);

        assertThat(result).isNull();
    }

    @Test
    void entornAppFindById_quanElClientLlanzaNotFound_retornaNull() {
        Request request = Request.create(Request.HttpMethod.GET, "/url", Collections.emptyMap(), null, null, null);
        when(entornAppServiceClient.getOne(1L, null, AUTH_HEADER)).thenThrow(new FeignException.NotFound("not found", request, null, null));

        EntornApp result = helper.entornAppFindById(1L);

        assertThat(result).isNull();
    }

    @Test
    void entornAppFindByEntornCodiAndAppCodi_quanTrobaEntitat_laRetorna() {
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(Collections.singletonList(EntityModel.of(entornApp)), new PagedModel.PageMetadata(1, 0, 1));
        when(entornAppServiceClient.find(eq(null), anyString(), eq(null), eq(null), eq("UNPAGED"), eq(null), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        Optional<EntornApp> result = helper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP");

        assertThat(result).isPresent().contains(entornApp);
    }

    @Test
    void entornAppFindByEntornAndApp_quanTrobaEntitat_laRetorna() {
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(Collections.singletonList(EntityModel.of(entornApp)), new PagedModel.PageMetadata(1, 0, 1));
        when(entornAppServiceClient.find(eq(null), contains("entorn.id:1"), eq(null), eq(null), eq("UNPAGED"), eq(null), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        Optional<EntornApp> result = helper.entornAppFindByEntornAndApp(1L, 1L);

        assertThat(result).isPresent().contains(entornApp);
    }

    @Test
    void entornAppFindByApp_quanTrobaEntitat_laRetorna() {
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(Collections.singletonList(EntityModel.of(entornApp)), new PagedModel.PageMetadata(1, 0, 1));
        when(entornAppServiceClient.find(eq(null), contains("app.id:1"), eq(null), eq(null), eq("UNPAGED"), eq(null), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        Optional<EntornApp> result = helper.entornAppFindByApp(1L);

        assertThat(result).isPresent().contains(entornApp);
    }

    @Test
    void entornAppFindByEntorn_quanTrobaEntitat_laRetorna() {
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(Collections.singletonList(EntityModel.of(entornApp)), new PagedModel.PageMetadata(1, 0, 1));
        when(entornAppServiceClient.find(eq(null), contains("entorn.id:1"), eq(null), eq(null), eq("UNPAGED"), eq(null), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        Optional<EntornApp> result = helper.entornAppFindByEntorn(1L);

        assertThat(result).isPresent().contains(entornApp);
    }

    @Test
    void monitorCreate_quanElClientFunciona_delegaLaCridaAlServeiDeMonitor() {
        helper.monitorCreate(monitor);

        verify(monitorServiceClient).create(monitor, AUTH_HEADER);
    }

    @Test
    void monitorCreate_quanElClientLlanzaExcepcio_noLaPropaga() {
        doThrow(new RuntimeException("monitor down")).when(monitorServiceClient).create(monitor, AUTH_HEADER);

        assertThatCode(() -> helper.monitorCreate(monitor))
                .doesNotThrowAnyException();
    }

    @Test
    void appById_quanTroba_laRetorna() {
        when(appServiceClient.getOne(1L, null, AUTH_HEADER)).thenReturn(EntityModel.of(app));

        App result = helper.appById(1L);

        assertThat(result).isEqualTo(app);
    }

    @Test
    void appById_quanNoTroba_retornaNull() {
        when(appServiceClient.getOne(1L, null, AUTH_HEADER)).thenReturn(null);

        App result = helper.appById(1L);

        assertThat(result).isNull();
    }

    @Test
    void entornById_quanTroba_laRetorna() {
        when(entornServiceClient.getOne(1L, null, AUTH_HEADER)).thenReturn(EntityModel.of(entorn));

        Entorn result = helper.entornById(1L);

        assertThat(result).isEqualTo(entorn);
    }

    @Test
    void entornById_quanNoTroba_retornaNull() {
        when(entornServiceClient.getOne(1L, null, AUTH_HEADER)).thenReturn(null);

        Entorn result = helper.entornById(1L);

        assertThat(result).isNull();
    }
}
