package es.caib.comanda.avisos.logic.helper;

import es.caib.comanda.client.AppServiceClient;
import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.EntornServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a AvisClientHelper")
class AvisClientHelperTest {

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
    private AvisClientHelper avisClientHelper;

    private static final String AUTH_HEADER = "Bearer token";

    @BeforeEach
    void setUp() {
        lenient().when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
    }

    // ========================================================================
    // 1. TESTOS PER A entornAppFindById
    // ========================================================================

    @Test
    @DisplayName("entornAppFindById: retorna el contingut quan el client troba l'entitat")
    void entornAppFindById_quanElClientRetornaEntitat_llavorsRetornaElContingut() {
        // Arrange
        EntornApp entornApp = new EntornApp();
        entornApp.setId(1L);
        when(entornAppServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenReturn(EntityModel.of(entornApp));

        // Act
        EntornApp result = avisClientHelper.entornAppFindById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(entornAppServiceClient, times(1)).getOne(eq(1L), any(), eq(AUTH_HEADER));
    }

    @Test
    @DisplayName("entornAppFindById: retorna null quan el client retorna null")
    void entornAppFindById_quanElClientRetornaNull_llavorsRetornaNull() {
        // Arrange
        when(entornAppServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenReturn(null);

        // Act
        EntornApp result = avisClientHelper.entornAppFindById(1L);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("entornAppFindById: retorna null quan es llança FeignException.NotFound")
    void entornAppFindById_quanEsLlancaNotFound_llavorsRetornaNull() {
        // Arrange
        when(entornAppServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER)))
            .thenThrow(FeignException.NotFound.class);

        // Act
        EntornApp result = avisClientHelper.entornAppFindById(1L);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("entornAppFindById: gestiona correctament un ID null")
    void entornAppFindById_quanIdEsNull_llavorsNoLlancaExcepcio() {
        // Arrange
        when(entornAppServiceClient.getOne(eq(null), any(), eq(AUTH_HEADER))).thenReturn(null);

        // Act & Assert
        assertThatCode(() -> avisClientHelper.entornAppFindById(null)).doesNotThrowAnyException();
    }

    // ========================================================================
    // 2. TESTOS PER A entornAppFindByEntornCodiAndAppCodi
    // ========================================================================

    @Test
    @DisplayName("entornAppFindByEntornCodiAndAppCodi: retorna el primer element quan hi ha resultats")
    void entornAppFindByEntornCodiAndAppCodi_quanHiHaResultats_llavorsRetornaElPrimerElement() {
        // Arrange
        EntornApp entornApp = new EntornApp();
        entornApp.setId(1L);
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
            Collections.singletonList(EntityModel.of(entornApp)),
            new PagedModel.PageMetadata(1, 0, 1)
        );
        when(entornAppServiceClient.find(any(), anyString(), any(), any(), anyString(), any(), eq(AUTH_HEADER)))
            .thenReturn(pagedModel);

        // Act
        Optional<EntornApp> result = avisClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("entornAppFindByEntornCodiAndAppCodi: retorna Optional buit quan no hi ha resultats")
    void entornAppFindByEntornCodiAndAppCodi_quanNoHiHaResultats_llavorsRetornaOptionalBuit() {
        // Arrange
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
            Collections.emptyList(),
            new PagedModel.PageMetadata(0, 0, 0)
        );
        when(entornAppServiceClient.find(any(), anyString(), any(), any(), anyString(), any(), eq(AUTH_HEADER)))
            .thenReturn(pagedModel);

        // Act
        Optional<EntornApp> result = avisClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP");

        // Assert
        assertThat(result).isEmpty();
    }

    // ========================================================================
    // 3. TESTOS PER A entornAppFindByEntornAndApp
    // ========================================================================

    @Test
    @DisplayName("entornAppFindByEntornAndApp: retorna el primer element quan hi ha resultats")
    void entornAppFindByEntornAndApp_quanHiHaResultats_llavorsRetornaElPrimerElement() {
        // Arrange
        EntornApp entornApp = new EntornApp();
        entornApp.setId(2L);
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
            Collections.singletonList(EntityModel.of(entornApp)),
            new PagedModel.PageMetadata(1, 0, 1)
        );
        when(entornAppServiceClient.find(any(), anyString(), any(), any(), anyString(), any(), eq(AUTH_HEADER)))
            .thenReturn(pagedModel);

        // Act
        Optional<EntornApp> result = avisClientHelper.entornAppFindByEntornAndApp(10L, 20L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("entornAppFindByEntornAndApp: retorna Optional buit quan no hi ha resultats")
    void entornAppFindByEntornAndApp_quanNoHiHaResultats_llavorsRetornaOptionalBuit() {
        // Arrange
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
            Collections.emptyList(),
            new PagedModel.PageMetadata(0, 0, 0)
        );
        when(entornAppServiceClient.find(any(), anyString(), any(), any(), anyString(), any(), eq(AUTH_HEADER)))
            .thenReturn(pagedModel);

        // Act
        Optional<EntornApp> result = avisClientHelper.entornAppFindByEntornAndApp(10L, 20L);

        // Assert
        assertThat(result).isEmpty();
    }

    // ========================================================================
    // 4. TESTOS PER A monitorCreate
    // ========================================================================

    @Test
    @DisplayName("monitorCreate: crida al client correctament quan no hi ha errors")
    void monitorCreate_quanNoHiHaErrors_llavorsCridaAlClient() {
        // Arrange
        Monitor monitor = new Monitor();

        // Act
        avisClientHelper.monitorCreate(monitor);

        // Assert
        verify(monitorServiceClient, times(1)).create(monitor, AUTH_HEADER);
    }

    @Test
    @DisplayName("monitorCreate: no propaga l'excepció quan el client falla")
    void monitorCreate_quanElClientLlancaExcepcio_llavorsNoLaPropaga() {
        // Arrange
        Monitor monitor = new Monitor();
        doThrow(new RuntimeException("Error de xarxa")).when(monitorServiceClient).create(eq(monitor), eq(AUTH_HEADER));

        // Act & Assert
        assertThatCode(() -> avisClientHelper.monitorCreate(monitor)).doesNotThrowAnyException();
        verify(monitorServiceClient, times(1)).create(monitor, AUTH_HEADER);
    }

    // ========================================================================
    // 5. TESTOS PER A appById
    // ========================================================================

    @Test
    @DisplayName("appById: retorna l'app quan el client la troba")
    void appById_quanElClientTrobaApp_llavorsLaRetorna() {
        // Arrange
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 1L);
        when(appServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenReturn(EntityModel.of(app));

        // Act
        App result = avisClientHelper.appById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("appById: retorna null quan el client retorna null")
    void appById_quanElClientRetornaNull_llavorsRetornaNull() {
        // Arrange
        when(appServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenReturn(null);

        // Act
        App result = avisClientHelper.appById(1L);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("appById: retorna null quan es llança FeignException.NotFound")
    void appById_quanEsLlancaNotFound_llavorsRetornaNull() {
        // Arrange
        when(appServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenThrow(FeignException.NotFound.class);

        // Act
        App result = avisClientHelper.appById(1L);

        // Assert
        assertThat(result).isNull();
    }

    // ========================================================================
    // 6. TESTOS PER A entornById
    // ========================================================================

    @Test
    @DisplayName("entornById: retorna l'entorn quan el client el troba")
    void entornById_quanElClientTrobaEntorn_llavorsElRetorna() {
        // Arrange
        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 1L);
        when(entornServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenReturn(EntityModel.of(entorn));

        // Act
        Entorn result = avisClientHelper.entornById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("entornById: retorna null quan el client retorna null")
    void entornById_quanElClientRetornaNull_llavorsRetornaNull() {
        // Arrange
        when(entornServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenReturn(null);

        // Act
        Entorn result = avisClientHelper.entornById(1L);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("entornById: retorna null quan es llança FeignException.NotFound")
    void entornById_quanEsLlancaNotFound_llavorsRetornaNull() {
        // Arrange
        when(entornServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenThrow(FeignException.NotFound.class);

        // Act
        Entorn result = avisClientHelper.entornById(1L);

        // Assert
        assertThat(result).isNull();
    }
}
