package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.client.AppServiceClient;
import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.EntornServiceClient;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlarmaClientHelperTest {

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    @Mock
    private EntornAppServiceClient entornAppServiceClient;
    @Mock
    private EntornServiceClient entornServiceClient;
    @Mock
    private AppServiceClient appServiceClient;

    @InjectMocks
    private AlarmaClientHelper alarmaClientHelper;

    private static final String AUTH_HEADER = "Bearer token";
    private static final Long APP_ID = 1L;
    private static final Long ENTORN_APP_ID = 2L;
    private static final Long ENTORN_ID = 3L;

    @BeforeEach
    void setUp() {
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
    }

    @Test
    @DisplayName("appFindById retorna l'aplicació quan existeix")
    void appFindById_quanExisteix_retornaApp() {
        // Arrange
        App app = new App();
        ReflectionTestUtils.setField(app, "id", APP_ID);
        EntityModel<App> entityModel = EntityModel.of(app);
        when(appServiceClient.getOne(eq(APP_ID), any(), eq(AUTH_HEADER))).thenReturn(entityModel);

        // Act
        App result = alarmaClientHelper.appFindById(APP_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(APP_ID);
    }

    @Test
    @DisplayName("appFindById retorna null quan no existeix")
    void appFindById_quanNoExisteix_retornaNull() {
        // Arrange
        when(appServiceClient.getOne(eq(APP_ID), any(), eq(AUTH_HEADER))).thenReturn(null);

        // Act
        App result = alarmaClientHelper.appFindById(APP_ID);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("entornAppFindById retorna l'entornApp quan existeix")
    void entornAppFindById_quanExisteix_retornaEntornApp() {
        // Arrange
        EntornApp entornApp = new EntornApp();
        ReflectionTestUtils.setField(entornApp, "id", ENTORN_APP_ID);
        EntityModel<EntornApp> entityModel = EntityModel.of(entornApp);
        when(entornAppServiceClient.getOne(eq(ENTORN_APP_ID), any(), eq(AUTH_HEADER))).thenReturn(entityModel);

        // Act
        EntornApp result = alarmaClientHelper.entornAppFindById(ENTORN_APP_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ENTORN_APP_ID);
    }

    @Test
    @DisplayName("entornAppFindById retorna null quan no existeix")
    void entornAppFindById_quanNoExisteix_retornaNull() {
        // Arrange
        when(entornAppServiceClient.getOne(eq(ENTORN_APP_ID), any(), eq(AUTH_HEADER))).thenReturn(null);

        // Act
        EntornApp result = alarmaClientHelper.entornAppFindById(ENTORN_APP_ID);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("entornById retorna l'entorn quan existeix")
    void entornById_quanExisteix_retornaEntorn() {
        // Arrange
        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", ENTORN_ID);
        EntityModel<Entorn> entityModel = EntityModel.of(entorn);
        when(entornServiceClient.getOne(eq(ENTORN_ID), any(), eq(AUTH_HEADER))).thenReturn(entityModel);

        // Act
        Entorn result = alarmaClientHelper.entornById(ENTORN_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ENTORN_ID);
    }

    @Test
    @DisplayName("entornById retorna null quan no existeix")
    void entornById_quanNoExisteix_retornaNull() {
        // Arrange
        when(entornServiceClient.getOne(eq(ENTORN_ID), any(), eq(AUTH_HEADER))).thenReturn(null);

        // Act
        Entorn result = alarmaClientHelper.entornById(ENTORN_ID);

        // Assert
        assertThat(result).isNull();
    }
}
