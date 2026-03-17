package es.caib.comanda.api.helper;

import es.caib.comanda.client.AppServiceClient;
import es.caib.comanda.client.AvisServiceClient;
import es.caib.comanda.client.EntornServiceClient;
import es.caib.comanda.client.TascaServiceClient;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Avis;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.Tasca;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiClientHelperTest {

    @Mock
    private AppServiceClient appServiceClient;
    @Mock
    private EntornServiceClient entornServiceClient;
    @Mock
    private TascaServiceClient tascaServiceClient;
    @Mock
    private AvisServiceClient avisServiceClient;
    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @InjectMocks
    private ApiClientHelper apiClientHelper;

    private static final String AUTH_HEADER = "Bearer token";

    @Test
    @DisplayName("getAppByCodi retorna l'aplicació si existeix")
    void getAppByCodi_quanExisteix_retornaApp() {
        // Arrange
        String appCodi = "APP1";
        App app = new App();
        ReflectionTestUtils.setField(app, "codi", appCodi);
        PagedModel<EntityModel<App>> pagedModel = PagedModel.of(Collections.singletonList(EntityModel.of(app)), new PagedModel.PageMetadata(1, 0, 1));

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(appServiceClient.find(any(), eq("codi:'APP1'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        // Act
        Optional<App> result = apiClientHelper.getAppByCodi(appCodi);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCodi()).isEqualTo(appCodi);
    }

    @Test
    @DisplayName("getEntornByCodi retorna l'entorn si existeix")
    void getEntornByCodi_quanExisteix_retornaEntorn() {
        // Arrange
        String entornCodi = "ENT1";
        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "codi", entornCodi);
        PagedModel<EntityModel<Entorn>> pagedModel = PagedModel.of(Collections.singletonList(EntityModel.of(entorn)), new PagedModel.PageMetadata(1, 0, 1));

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(entornServiceClient.find(any(), eq("codi:'ENT1'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        // Act
        Optional<Entorn> result = apiClientHelper.getEntornByCodi(entornCodi);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCodi()).isEqualTo(entornCodi);
    }

    @Test
    @DisplayName("getTasca retorna la tasca si existeix")
    void getTasca_quanExisteix_retornaTasca() {
        // Arrange
        Tasca tasca = new Tasca();
        tasca.setIdentificador("T1");
        PagedModel<EntityModel<Tasca>> pagedModel = PagedModel.of(Collections.singletonList(EntityModel.of(tasca)), new PagedModel.PageMetadata(1, 0, 1));

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(tascaServiceClient.find(any(), eq("appId:'1' and entornId:'1' and identificador:'T1'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        // Act
        Optional<Tasca> result = apiClientHelper.getTasca("T1", 1L, 1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getIdentificador()).isEqualTo("T1");
    }

    @Test
    @DisplayName("getTasques retorna llista de tasques")
    void getTasques_quanExisteixen_retornaLlista() {
        // Arrange
        Tasca t1 = new Tasca(); t1.setIdentificador("T1");
        Tasca t2 = new Tasca(); t2.setIdentificador("T2");
        PagedModel<EntityModel<Tasca>> pagedModel = PagedModel.of(List.of(EntityModel.of(t1), EntityModel.of(t2)), new PagedModel.PageMetadata(2, 0, 2));

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(tascaServiceClient.find(any(), any(), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        // Act
        List<Tasca> result = apiClientHelper.getTasques(Set.of("T1", "T2"), 1L, 1L);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getAvis retorna l'avís si existeix")
    void getAvis_quanExisteix_retornaAvis() {
        // Arrange
        Avis avis = new Avis();
        avis.setIdentificador("A1");
        PagedModel<EntityModel<Avis>> pagedModel = PagedModel.of(Collections.singletonList(EntityModel.of(avis)), new PagedModel.PageMetadata(1, 0, 1));

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(avisServiceClient.find(any(), eq("appId:'1' and entornId:'1' and identificador:'A1'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        // Act
        Optional<Avis> result = apiClientHelper.getAvis("A1", 1L, 1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getIdentificador()).isEqualTo("A1");
    }

    @Test
    @DisplayName("getAvisos retorna llista d'avisos")
    void getAvisos_quanExisteixen_retornaLlista() {
        // Arrange
        Avis a1 = new Avis(); a1.setIdentificador("A1");
        PagedModel<EntityModel<Avis>> pagedModel = PagedModel.of(List.of(EntityModel.of(a1)), new PagedModel.PageMetadata(1, 0, 1));

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(avisServiceClient.find(any(), any(), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        // Act
        List<Avis> result = apiClientHelper.getAvisos(Set.of("A1"), 1L, 1L);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getTasques paginat retorna PagedModel")
    void getTasques_paginat_retornaPagedModel() {
        // Arrange
        PagedModel<EntityModel<Tasca>> pagedModel = PagedModel.empty();
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(tascaServiceClient.find(any(), any(), any(), any(), any(), any(), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        // Act
        PagedModel<EntityModel<Tasca>> result = apiClientHelper.getTasques(null, null, null, null, "0", 10);

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getAvisos paginat retorna PagedModel")
    void getAvisos_paginat_retornaPagedModel() {
        // Arrange
        PagedModel<EntityModel<Avis>> pagedModel = PagedModel.empty();
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(avisServiceClient.find(any(), any(), any(), any(), any(), any(), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        // Act
        PagedModel<EntityModel<Avis>> result = apiClientHelper.getAvisos(null, null, null, null, "0", 10);

        // Assert
        assertThat(result).isNotNull();
    }
}
