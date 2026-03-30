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
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    private final String AUTH_HEADER = "Bearer token";

    @BeforeEach
    void setUp() {
        lenient().when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
    }

    @Test
    @DisplayName("entornAppFindById ha de retornar el contingut si el client retorna una entitat")
    void entornAppFindById_quanElClientRetornaEntitat_retornaElContingut() {
        // Arrange
        EntornApp entornApp = new EntornApp();
        entornApp.setId(1L);
        when(entornAppServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenReturn(EntityModel.of(entornApp));

        // Act
        EntornApp result = avisClientHelper.entornAppFindById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("entornAppFindById ha de retornar null si el client retorna null")
    void entornAppFindById_quanElClientRetornaNull_retornaNull() {
        // Arrange
        when(entornAppServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenReturn(null);

        // Act
        EntornApp result = avisClientHelper.entornAppFindById(1L);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("entornAppFindByEntornCodiAndAppCodi ha de retornar el primer element si el client troba resultats")
    void entornAppFindByEntornCodiAndAppCodi_quanTrobaEntitat_laRetorna() {
        // Arrange
        EntornApp entornApp = new EntornApp();
        entornApp.setId(1L);
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(Collections.singletonList(EntityModel.of(entornApp)), new PagedModel.PageMetadata(1, 0, 1));
        when(entornAppServiceClient.find(any(), anyString(), any(), any(), anyString(), any(), eq(AUTH_HEADER))).thenReturn(pagedModel);

        // Act
        Optional<EntornApp> result = avisClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("monitorCreate ha de cridar al client sense propagar excepció si falla")
    void monitorCreate_quanElClientLlanzaExcepcio_noLaPropaga() {
        // Arrange
        Monitor monitor = new Monitor();
        doThrow(new RuntimeException("Error")).when(monitorServiceClient).create(eq(monitor), eq(AUTH_HEADER));

        // Act & Assert (no ha de llançar excepció)
        avisClientHelper.monitorCreate(monitor);
        verify(monitorServiceClient).create(monitor, AUTH_HEADER);
    }

    @Test
    @DisplayName("appById ha de retornar l'app si el client la troba")
    void appById_quanTroba_laRetorna() {
        // Arrange
        App app = new App();
        when(appServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenReturn(EntityModel.of(app));

        // Act
        App result = avisClientHelper.appById(1L);

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("entornById ha de retornar l'entorn si el client el troba")
    void entornById_quanTroba_laRetorna() {
        // Arrange
        Entorn entorn = new Entorn();
        when(entornServiceClient.getOne(eq(1L), any(), eq(AUTH_HEADER))).thenReturn(EntityModel.of(entorn));

        // Act
        Entorn result = avisClientHelper.entornById(1L);

        // Assert
        assertThat(result).isNotNull();
    }
}
