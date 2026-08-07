package es.caib.comanda.estadistica.logic.helper;

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
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a EstadisticaClientHelper")
class EstadisticaClientHelperTest {

    @Mock private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    @Mock private MonitorServiceClient monitorServiceClient;
    @Mock private EntornAppServiceClient entornAppServiceClient;
    @Mock private EntornServiceClient entornServiceClient;
    @Mock private AppServiceClient appServiceClient;

    @InjectMocks
    private EstadisticaClientHelper estadisticaClientHelper;

    private App app;
    private EntornApp entornApp;
    private Entorn entorn;
    private Monitor monitor;
    private String authHeader;

    @BeforeEach
    void setUp() {
        app = new App();
        ReflectionTestUtils.setField(app, "id", 1L);
        ReflectionTestUtils.setField(app, "nom", "Test App");
        ReflectionTestUtils.setField(app, "activa", true);

        entornApp = EntornApp.builder().id(1L).activa(true).build();

        entorn = Entorn.builder().id(1L).codi("ENT").nom("ENT Name").build();

        monitor = Monitor.builder()
            .entornAppId(1L)
            .modul(ModulEnum.ESTADISTICA)
            .tipus(AccioTipusEnum.SORTIDA)
            .url("http://test.com/estadistica")
            .estat(EstatEnum.OK)
            .build();

        authHeader = "Bearer test-token";
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(authHeader);
    }

    // ========================================================================
    // 1. TESTOS PER A appFindById
    // ========================================================================

    @Test
    @DisplayName("appFindById: retorna l'aplicació quan el client la troba")
    void appFindById_quanClientLaTroba_llavorsRetornaApp() {
        // Arrange
        EntityModel<App> entityModel = EntityModel.of(app);
        when(appServiceClient.getOne(eq(1L), isNull(), eq(authHeader))).thenReturn(entityModel);

        // Act
        App result = estadisticaClientHelper.appFindById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNom()).isEqualTo("Test App");
        verify(appServiceClient).getOne(eq(1L), isNull(), eq(authHeader));
    }

    @Test
    @DisplayName("appFindById: retorna null quan el client retorna null")
    void appFindById_quanClientRetornaNull_llavorsRetornaNull() {
        // Arrange
        when(appServiceClient.getOne(eq(1L), isNull(), eq(authHeader))).thenReturn(null);

        // Act
        App result = estadisticaClientHelper.appFindById(1L);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("appFindById: retorna null quan es llança FeignException.NotFound")
    void appFindById_quanLlancaNotFound_llavorsRetornaNull() {
        // Arrange
        when(appServiceClient.getOne(eq(1L), isNull(), eq(authHeader)))
            .thenThrow(mock(FeignException.NotFound.class));

        // Act
        App result = estadisticaClientHelper.appFindById(1L);

        // Assert
        assertThat(result).isNull();
    }

    // ========================================================================
    // 2. TESTOS PER A appFindByCodi
    // ========================================================================

    @Test
    @DisplayName("appFindByCodi: retorna l'aplicació quan el client la troba per codi")
    void appFindByCodi_quanClientLaTrobaPerCodi_llavorsRetornaApp() {
        // Arrange
        EntityModel<App> entityModel = EntityModel.of(app);
        PagedModel<EntityModel<App>> pagedModel = PagedModel.of(
            Collections.singletonList(entityModel),
            new PagedModel.PageMetadata(1, 0, 1));
        when(appServiceClient.find(isNull(), eq("codi:'APP_CODI'"), isNull(), isNull(), eq("0"), eq(1), eq(authHeader)))
            .thenReturn(pagedModel);

        // Act
        App result = estadisticaClientHelper.appFindByCodi("APP_CODI");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("appFindByCodi: retorna null quan el client retorna una llista buida")
    void appFindByCodi_quanLlistaBuida_llavorsRetornaNull() {
        // Arrange
        PagedModel<EntityModel<App>> pagedModel = PagedModel.of(
            Collections.emptyList(),
            new PagedModel.PageMetadata(0, 0, 0));
        when(appServiceClient.find(isNull(), eq("codi:'APP_CODI'"), isNull(), isNull(), eq("0"), eq(1), eq(authHeader)))
            .thenReturn(pagedModel);

        // Act
        App result = estadisticaClientHelper.appFindByCodi("APP_CODI");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("appFindByCodi: retorna null quan es llança FeignException.NotFound")
    void appFindByCodi_quanLlancaNotFound_llavorsRetornaNull() {
        // Arrange
        when(appServiceClient.find(isNull(), eq("codi:'APP_CODI'"), isNull(), isNull(), eq("0"), eq(1), eq(authHeader)))
            .thenThrow(mock(FeignException.NotFound.class));

        // Act
        App result = estadisticaClientHelper.appFindByCodi("APP_CODI");

        // Assert
        assertThat(result).isNull();
    }

    // ========================================================================
    // 3. TESTOS PER A entornAppFindById
    // ========================================================================

    @Test
    @DisplayName("entornAppFindById: retorna l'EntornApp quan el client el troba")
    void entornAppFindById_quanClientElTroba_llavorsRetornaEntornApp() {
        // Arrange
        EntityModel<EntornApp> entityModel = EntityModel.of(entornApp);
        when(entornAppServiceClient.getOne(eq(1L), isNull(), eq(authHeader))).thenReturn(entityModel);

        // Act
        EntornApp result = estadisticaClientHelper.entornAppFindById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("entornAppFindById: retorna null quan el client retorna null")
    void entornAppFindById_quanClientRetornaNull_llavorsRetornaNull() {
        // Arrange
        when(entornAppServiceClient.getOne(eq(1L), isNull(), eq(authHeader))).thenReturn(null);

        // Act
        EntornApp result = estadisticaClientHelper.entornAppFindById(1L);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("entornAppFindById: retorna null quan es llança FeignException.NotFound")
    void entornAppFindById_quanLlancaNotFound_llavorsRetornaNull() {
        // Arrange
        when(entornAppServiceClient.getOne(eq(1L), isNull(), eq(authHeader)))
            .thenThrow(mock(FeignException.NotFound.class));

        // Act
        EntornApp result = estadisticaClientHelper.entornAppFindById(1L);

        // Assert
        assertThat(result).isNull();
    }

    // ========================================================================
    // 4. TESTOS PER A entornAppFindByAppAndEntorn
    // ========================================================================

    @Test
    @DisplayName("entornAppFindByAppAndEntorn: retorna l'EntornApp quan existeix")
    void entornAppFindByAppAndEntorn_quanExisteix_llavorsRetornaEntornApp() {
        // Arrange
        EntityModel<EntornApp> entityModel = EntityModel.of(entornApp);
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
            Collections.singletonList(entityModel),
            new PagedModel.PageMetadata(1, 0, 1));
        when(entornAppServiceClient.find(isNull(), eq("app.id:10 and entorn.id:20"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(authHeader)))
            .thenReturn(pagedModel);

        // Act
        EntornApp result = estadisticaClientHelper.entornAppFindByAppAndEntorn(10L, 20L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("entornAppFindByAppAndEntorn: retorna null quan el client retorna null")
    void entornAppFindByAppAndEntorn_quanClientRetornaNull_llavorsRetornaNull() {
        // Arrange
        when(entornAppServiceClient.find(isNull(), eq("app.id:10 and entorn.id:20"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(authHeader)))
            .thenReturn(null);

        // Act
        EntornApp result = estadisticaClientHelper.entornAppFindByAppAndEntorn(10L, 20L);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("entornAppFindByAppAndEntorn: llança ResourceNotFoundException quan la llista és buida")
    void entornAppFindByAppAndEntorn_quanLlistaBuida_llavorsLlancaResourceNotFoundException() {
        // Arrange
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
            Collections.emptyList(),
            new PagedModel.PageMetadata(0, 0, 0));
        when(entornAppServiceClient.find(isNull(), eq("app.id:10 and entorn.id:20"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(authHeader)))
            .thenReturn(pagedModel);

        // Act & Assert
        assertThatThrownBy(() -> estadisticaClientHelper.entornAppFindByAppAndEntorn(10L, 20L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========================================================================
    // 5. TESTOS PER A entornAppFindByAppAndEntornOrDefaultNull
    // ========================================================================

    @Test
    @DisplayName("entornAppFindByAppAndEntornOrDefaultNull: retorna l'EntornApp quan existeix")
    void entornAppFindByAppAndEntornOrDefaultNull_quanExisteix_llavorsRetornaEntornApp() {
        // Arrange
        EntityModel<EntornApp> entityModel = EntityModel.of(entornApp);
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
            Collections.singletonList(entityModel),
            new PagedModel.PageMetadata(1, 0, 1));
        when(entornAppServiceClient.find(isNull(), eq("app.id:10 and entorn.id:20"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(authHeader)))
            .thenReturn(pagedModel);

        // Act
        EntornApp result = estadisticaClientHelper.entornAppFindByAppAndEntornOrDefaultNull(10L, 20L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("entornAppFindByAppAndEntornOrDefaultNull: retorna null quan no existeix (captura ResourceNotFoundException)")
    void entornAppFindByAppAndEntornOrDefaultNull_quanNoExisteix_llavorsRetornaNull() {
        // Arrange
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
            Collections.emptyList(),
            new PagedModel.PageMetadata(0, 0, 0));
        when(entornAppServiceClient.find(isNull(), eq("app.id:10 and entorn.id:20"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(authHeader)))
            .thenReturn(pagedModel);

        // Act
        EntornApp result = estadisticaClientHelper.entornAppFindByAppAndEntornOrDefaultNull(10L, 20L);

        // Assert
        assertThat(result).isNull();
    }

    // ========================================================================
    // 6. TESTOS PER A entornAppFindByActivaTrue
    // ========================================================================

    @Test
    @DisplayName("entornAppFindByActivaTrue: retorna la llista d'EntornApps actius")
    void entornAppFindByActivaTrue_quanHiHaActius_llavorsRetornaLlista() {
        // Arrange
        EntityModel<EntornApp> entityModel = EntityModel.of(entornApp);
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
            Collections.singletonList(entityModel),
            new PagedModel.PageMetadata(1, 0, 1));
        when(entornAppServiceClient.find(isNull(), eq("activa:true and app.activa:true"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(authHeader)))
            .thenReturn(pagedModel);

        // Act
        List<EntornApp> result = estadisticaClientHelper.entornAppFindByActivaTrue();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("entornAppFindByActivaTrue: retorna llista buida quan el client retorna null")
    void entornAppFindByActivaTrue_quanClientRetornaNull_llavorsRetornaLlistaBuida() {
        // Arrange
        when(entornAppServiceClient.find(isNull(), eq("activa:true and app.activa:true"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(authHeader)))
            .thenReturn(null);

        // Act
        List<EntornApp> result = estadisticaClientHelper.entornAppFindByActivaTrue();

        // Assert
        assertThat(result).isEmpty();
    }

    // ========================================================================
    // 7. TESTOS PER A getEntornAppsIdByAppId
    // ========================================================================

    @Test
    @DisplayName("getEntornAppsIdByAppId: retorna la llista d'IDs quan appId no és null")
    void getEntornAppsIdByAppId_quanAppIdNoEsNull_llavorsRetornaLlistaIds() {
        // Arrange
        EntityModel<EntornApp> entityModel = EntityModel.of(entornApp);
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
            Collections.singletonList(entityModel),
            new PagedModel.PageMetadata(1, 0, 1));
        when(entornAppServiceClient.find(isNull(), eq("app.id:10"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(authHeader)))
            .thenReturn(pagedModel);

        // Act
        List<Long> result = estadisticaClientHelper.getEntornAppsIdByAppId(10L);

        // Assert
        assertThat(result).containsExactly(1L);
    }

    @Test
    @DisplayName("getEntornAppsIdByAppId: utilitza cadena buida com a filtre quan appId és null")
    void getEntornAppsIdByAppId_quanAppIdEsNull_llavorsUsaCadenaBuida() {
        // Arrange
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
            Collections.emptyList(),
            new PagedModel.PageMetadata(0, 0, 0));
        when(entornAppServiceClient.find(isNull(), eq(""), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(authHeader)))
            .thenReturn(pagedModel);

        // Act
        List<Long> result = estadisticaClientHelper.getEntornAppsIdByAppId(null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getEntornAppsIdByAppId: retorna llista buida quan el client retorna null")
    void getEntornAppsIdByAppId_quanClientRetornaNull_llavorsRetornaLlistaBuida() {
        // Arrange
        when(entornAppServiceClient.find(isNull(), eq("app.id:10"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(authHeader)))
            .thenReturn(null);

        // Act
        List<Long> result = estadisticaClientHelper.getEntornAppsIdByAppId(10L);

        // Assert
        assertThat(result).isEmpty();
    }

    // ========================================================================
    // 8. TESTOS PER A entornById
    // ========================================================================

    @Test
    @DisplayName("entornById: retorna l'Entorn quan el client el troba")
    void entornById_quanClientElTroba_llavorsRetornaEntorn() {
        // Arrange
        EntityModel<Entorn> entityModel = EntityModel.of(entorn);
        when(entornServiceClient.getOne(eq(1L), isNull(), eq(authHeader))).thenReturn(entityModel);

        // Act
        Entorn result = estadisticaClientHelper.entornById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("entornById: retorna null quan el client retorna null")
    void entornById_quanClientRetornaNull_llavorsRetornaNull() {
        // Arrange
        when(entornServiceClient.getOne(eq(1L), isNull(), eq(authHeader))).thenReturn(null);

        // Act
        Entorn result = estadisticaClientHelper.entornById(1L);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("entornById: retorna null quan es llança FeignException.NotFound")
    void entornById_quanLlancaNotFound_llavorsRetornaNull() {
        // Arrange
        when(entornServiceClient.getOne(eq(1L), isNull(), eq(authHeader)))
            .thenThrow(mock(FeignException.NotFound.class));

        // Act
        Entorn result = estadisticaClientHelper.entornById(1L);

        // Assert
        assertThat(result).isNull();
    }

    // ========================================================================
    // 9. TESTOS PER A entornByCodi
    // ========================================================================

    @Test
    @DisplayName("entornByCodi: retorna l'Entorn quan el client el troba per codi")
    void entornByCodi_quanClientElTrobaPerCodi_llavorsRetornaEntorn() {
        // Arrange
        EntityModel<Entorn> entityModel = EntityModel.of(entorn);
        PagedModel<EntityModel<Entorn>> pagedModel = PagedModel.of(
            Collections.singletonList(entityModel),
            new PagedModel.PageMetadata(1, 0, 1));
        when(entornServiceClient.find(isNull(), eq("codi:'ENT_CODI'"), isNull(), isNull(), eq("0"), eq(1), eq(authHeader)))
            .thenReturn(pagedModel);

        // Act
        Entorn result = estadisticaClientHelper.entornByCodi("ENT_CODI");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("entornByCodi: retorna null quan el client retorna una llista buida")
    void entornByCodi_quanLlistaBuida_llavorsRetornaNull() {
        // Arrange
        PagedModel<EntityModel<Entorn>> pagedModel = PagedModel.of(
            Collections.emptyList(),
            new PagedModel.PageMetadata(0, 0, 0));
        when(entornServiceClient.find(isNull(), eq("codi:'ENT_CODI'"), isNull(), isNull(), eq("0"), eq(1), eq(authHeader)))
            .thenReturn(pagedModel);

        // Act
        Entorn result = estadisticaClientHelper.entornByCodi("ENT_CODI");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("entornByCodi: retorna null quan es llança FeignException.NotFound")
    void entornByCodi_quanLlancaNotFound_llavorsRetornaNull() {
        // Arrange
        when(entornServiceClient.find(isNull(), eq("codi:'ENT_CODI'"), isNull(), isNull(), eq("0"), eq(1), eq(authHeader)))
            .thenThrow(mock(FeignException.NotFound.class));

        // Act
        Entorn result = estadisticaClientHelper.entornByCodi("ENT_CODI");

        // Assert
        assertThat(result).isNull();
    }

    // ========================================================================
    // 10. TESTOS PER A monitorCreate
    // ========================================================================

    @Test
    @DisplayName("monitorCreate: crida al client correctament quan no hi ha errors")
    void monitorCreate_quanNoHiHaErrors_llavorsCridaAlClient() {
        // Act
        estadisticaClientHelper.monitorCreate(monitor);

        // Assert
        verify(monitorServiceClient).create(eq(monitor), eq(authHeader));
    }

    @Test
    @DisplayName("monitorCreate: no propaga l'excepció quan el client falla")
    void monitorCreate_quanClientFalla_llavorsNoPropagaExcepcio() {
        // Arrange
        doThrow(new RuntimeException("Test exception")).when(monitorServiceClient).create(eq(monitor), eq(authHeader));

        // Act & Assert
        assertDoesNotThrow(() -> estadisticaClientHelper.monitorCreate(monitor));
        verify(monitorServiceClient).create(eq(monitor), eq(authHeader));
    }
}
