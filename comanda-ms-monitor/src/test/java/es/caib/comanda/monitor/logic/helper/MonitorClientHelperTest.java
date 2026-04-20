package es.caib.comanda.monitor.logic.helper;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.model.*;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MonitorClientHelperTest {

    @Mock private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    @Mock private EntornAppServiceClient entornAppServiceClient;

    @InjectMocks private MonitorClientHelper monitorClientHelper;

    private static final String AUTH_HEADER = "Bearer test-token";
    private static final Long APP_ID = 1L;
    private static final Long ENTORN_ID = 2L;
    private static final Long ENTORN_APP_ID = 3L;

    //Objetos
    @Mock private AppRef appRef;
    @Mock private EntornRef entornRef;
    @Mock(answer = Answers.CALLS_REAL_METHODS) private EntornApp entornApp;

    @BeforeEach
    void setUp() {
        monitorClientHelper = new MonitorClientHelper(httpAuthorizationHeaderHelper, entornAppServiceClient);
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        //App
        Mockito.when(appRef.getId()).thenReturn(APP_ID);
        Mockito.when(appRef.getNom()).thenReturn("test-app");
        // Entorn
        Mockito.when(entornRef.getId()).thenReturn(ENTORN_ID);
        Mockito.when(entornRef.getNom()).thenReturn("test-entorn-description");
        //EntornApp
        entornApp.setId(ENTORN_APP_ID);
        entornApp.setApp(appRef);
        entornApp.setEntorn(entornRef);

    }

    @Test
    @DisplayName("entornAppFindById: retorna EntornApp quan el client respon correctament")
    void entornAppFindById_clientRespon_retornaEntornApp() {
        // Arrange
        EntityModel<EntornApp> entityModel = EntityModel.of(entornApp);
        when(entornAppServiceClient.getOne(eq(ENTORN_APP_ID), eq((String[]) null), eq(AUTH_HEADER)))
                .thenReturn(entityModel);

        // Act
        EntornApp result = monitorClientHelper.entornAppFindById(ENTORN_APP_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ENTORN_APP_ID);
        assertThat(result.getApp()).isEqualTo(appRef);
        verify(entornAppServiceClient).getOne(eq(ENTORN_APP_ID), eq((String[]) null), eq(AUTH_HEADER));
    }

    @Test
    @DisplayName("entornAppFindById: retorna null quan el client llança FeignException.NotFound")
    void entornAppFindById_clientNotFound_retornaNull() {
        // Arrange
        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        when(entornAppServiceClient.getOne(eq(ENTORN_APP_ID), eq((String[]) null), eq(AUTH_HEADER)))
                .thenThrow(notFound);

        // Act
        EntornApp result = monitorClientHelper.entornAppFindById(ENTORN_APP_ID);

        // Assert
        assertThat(result).isNull();
        verify(entornAppServiceClient).getOne(eq(ENTORN_APP_ID), eq((String[]) null), eq(AUTH_HEADER));
    }

    @Test
    @DisplayName("entornAppFindById: retorna null quan el client retorna null")
    void entornAppFindById_clientRetornaNull_retornaNull() {
        // Arrange
        when(entornAppServiceClient.getOne(eq(ENTORN_APP_ID), eq((String[]) null), eq(AUTH_HEADER)))
                .thenReturn(null);

        // Act
        EntornApp result = monitorClientHelper.entornAppFindById(ENTORN_APP_ID);

        // Assert
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @MethodSource("proporcionarCasosFindEntornAppIds")
    @DisplayName("findEntornAppIdsBy*: matriu d'escenaris")
    void findEntornAppIds_escenaris(String descripcion, String filter,
                                    PagedModel<EntityModel<EntornApp>> mockResponse,
                                    List<Long> expectedIds) {
        // Arrange
        if (filter.startsWith("app.id:")) {
            long appId = Long.parseLong(filter.split(":")[1]);
            when(entornAppServiceClient.find(eq(null), eq(filter), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                    .thenReturn(mockResponse);
            assertThat(monitorClientHelper.findEntornAppIdsByAppId(appId)).as(descripcion).isEqualTo(expectedIds);
        } else if (filter.startsWith("entorn.id:")) {
            long entornId = Long.parseLong(filter.split(":")[1]);
            when(entornAppServiceClient.find(eq(null), eq(filter), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                    .thenReturn(mockResponse);
            assertThat(monitorClientHelper.findEntornAppIdsByEntornId(entornId)).as(descripcion).isEqualTo(expectedIds);
        }
    }

    private static Stream<Arguments> proporcionarCasosFindEntornAppIds() {
        return Stream.of(
                Arguments.of("app.id amb múltiples entornApps", "app.id:" + APP_ID, crearPagedModelMock(Arrays.asList(10L, 11L, 12L)), Arrays.asList(10L, 11L, 12L)),
                Arguments.of("app.id amb un sol entornApp", "app.id:" + APP_ID, crearPagedModelMock(Collections.singletonList(99L)), Collections.singletonList(99L)),
                Arguments.of("entorn.id amb múltiples entornApps", "entorn.id:" + ENTORN_ID, crearPagedModelMock(Arrays.asList(20L, 21L, 22L)), Arrays.asList(20L, 21L, 22L)), // ← ¡Ahora sí!
                Arguments.of("resposta amb content null", "app.id:" + APP_ID, crearPagedModelConContenidoNull(), Collections.emptyList()),
                Arguments.of("resposta amb content buit", "entorn.id:" + ENTORN_ID, crearPagedModelMock(Collections.emptyList()), Collections.emptyList())
        );
    }

    /** Retorna un mock de PagedModel amb mocks de EntornApp amb ID configurat. */
    private static PagedModel<EntityModel<EntornApp>> crearPagedModelMock(List<Long> ids) {
        List<EntityModel<EntornApp>> content = ids.stream()
                .map(id -> {
                    EntornApp mockApp = mock(EntornApp.class, withSettings().defaultAnswer(Answers.CALLS_REAL_METHODS));
                    mockApp.setId(id);
                    return EntityModel.of(mockApp);
                })
                .collect(java.util.stream.Collectors.toList());

        PagedModel<EntityModel<EntornApp>> mockPage = mock(PagedModel.class);
        when(mockPage.getContent()).thenReturn(content);
        return mockPage;
    }

    private static PagedModel<EntityModel<EntornApp>> crearPagedModelConContenidoNull() {
        PagedModel<EntityModel<EntornApp>> mockPage = mock(PagedModel.class);
        when(mockPage.getContent()).thenReturn(null);
        return mockPage;
    }

    @Test
    @DisplayName("findEntornAppIdsByAppId: crida el client amb el filtre i auth correctes")
    void findEntornAppIdsByAppId_verificaParametresCrida() {
        // Arrange
        PagedModel<EntityModel<EntornApp>> response = crearPagedModelMock(Collections.emptyList());
        when(entornAppServiceClient.find(eq(null), eq("app.id:" + APP_ID), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                .thenReturn(response);

        // Act
        monitorClientHelper.findEntornAppIdsByAppId(APP_ID);

        // Assert
        verify(entornAppServiceClient).find(
                eq(null),
                eq("app.id:" + APP_ID),
                any(),
                any(),
                eq("UNPAGED"),
                any(),
                eq(AUTH_HEADER)
        );
        verify(httpAuthorizationHeaderHelper).getAuthorizationHeader();
    }
}