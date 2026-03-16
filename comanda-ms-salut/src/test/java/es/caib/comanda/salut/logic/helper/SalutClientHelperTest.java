package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalutClientHelperTest {

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private MonitorServiceClient monitorServiceClient;

    @Mock
    private EntornAppServiceClient entornAppServiceClient;

    @InjectMocks
    private SalutClientHelper helper;

    private EntornApp entornApp;
    private Monitor monitor;
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
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
    }

    @Test
    void entornAppFindById_quanElClientRetornaEntitat_retornaElContingut() {
        when(entornAppServiceClient.getOne(1L, null, AUTH_HEADER)).thenReturn(EntityModel.of(entornApp));

        EntornApp result = helper.entornAppFindById(1L);

        assertThat(result).isSameAs(entornApp);
        verify(entornAppServiceClient).getOne(1L, null, AUTH_HEADER);
    }

    @Test
    void entornAppFindById_quanElClientRetornaNull_retornaNull() {
        when(entornAppServiceClient.getOne(1L, null, AUTH_HEADER)).thenReturn(null);

        EntornApp result = helper.entornAppFindById(1L);

        assertThat(result).isNull();
    }

    @Test
    void entornAppFindByActivaTrue_quanNoHiHaFiltreExtra_consultaAmbElFiltreBase() {
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
                Collections.singletonList(EntityModel.of(entornApp)),
                new PagedModel.PageMetadata(1, 0, 1));
        when(entornAppServiceClient.find(
                isNull(),
                eq("activa:true and app.activa:true"),
                isNull(),
                isNull(),
                eq("UNPAGED"),
                isNull(),
                eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        List<EntornApp> result = helper.entornAppFindByActivaTrue();

        assertThat(result).containsExactly(entornApp);
    }

    @Test
    void entornAppFindByActivaTrue_quanHiHaFiltreExtra_concatenaElFiltreCorrectament() {
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
                Collections.singletonList(EntityModel.of(entornApp)),
                new PagedModel.PageMetadata(1, 0, 1));
        when(entornAppServiceClient.find(
                isNull(),
                contains("activa:true and app.activa:true"),
                isNull(),
                isNull(),
                eq("UNPAGED"),
                isNull(),
                eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        List<EntornApp> result = helper.entornAppFindByActivaTrue("entorn.id:5");

        assertThat(result).containsExactly(entornApp);
        verify(entornAppServiceClient).find(
                isNull(),
                eq("entorn.id:5 and activa:true and app.activa:true"),
                isNull(),
                isNull(),
                eq("UNPAGED"),
                isNull(),
                eq(AUTH_HEADER));
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
}
