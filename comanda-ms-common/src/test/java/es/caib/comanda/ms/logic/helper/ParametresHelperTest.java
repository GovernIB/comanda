package es.caib.comanda.ms.logic.helper;

import es.caib.comanda.client.ParametreServiceClient;
import es.caib.comanda.client.model.ParamTipus;
import es.caib.comanda.client.model.Parametre;
import es.caib.comanda.ms.logic.intf.exception.ParametreTipusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParametresHelperTest {

    @Mock
    private ParametreServiceClient parametreServiceClient;
    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    private ParametresHelper parametresHelper;

    private static final String AUTH_HEADER = "Bearer token";
    private static final String CODI_PARAM = "TEST_PARAM";

    @BeforeEach
    void setUp() {
        parametresHelper = new ParametresHelper(parametreServiceClient, httpAuthorizationHeaderHelper);
        // Injectem self per simular el comportament de Spring (proxy)
        ReflectionTestUtils.setField(parametresHelper, "self", parametresHelper);
    }

    @Test
    @DisplayName("Troba paràmetre per codi")
    void perametreFindByCodi_quanExisteix_retornaParametre() {
        // Arrange
        Parametre parametre = Parametre.builder().codi(CODI_PARAM).valor("valor").build();
        PagedModel<EntityModel<Parametre>> pagedModel = PagedModel.of(
                Collections.singletonList(EntityModel.of(parametre)),
                new PagedModel.PageMetadata(1, 0, 1));

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(parametreServiceClient.find(any(), eq("codi:'" + CODI_PARAM + "'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        // Act
        Parametre result = parametresHelper.perametreFindByCodi(CODI_PARAM);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCodi()).isEqualTo(CODI_PARAM);
        assertThat(result.getValor()).isEqualTo("valor");
    }

    @Test
    @DisplayName("Obté paràmetre de text amb valor per defecte")
    void getParametreText_quanNoExisteix_retornaDefault() {
        // Arrange
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(parametreServiceClient.find(any(), anyString(), any(), any(), anyString(), any(), anyString()))
                .thenReturn(null);

        // Act
        String result = parametresHelper.getParametreText(CODI_PARAM, "default");

        // Assert
        assertThat(result).isEqualTo("default");
    }

    @Test
    @DisplayName("Obté paràmetre booleà correctament")
    void getParametreBoolean_quanExisteix_retornaValor() {
        // Arrange
        Parametre parametre = Parametre.builder().codi(CODI_PARAM).tipus(ParamTipus.BOOLEAN).valor("true").build();
        PagedModel<EntityModel<Parametre>> pagedModel = PagedModel.of(
                Collections.singletonList(EntityModel.of(parametre)),
                new PagedModel.PageMetadata(1, 0, 1));

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(parametreServiceClient.find(any(), anyString(), any(), any(), anyString(), any(), anyString()))
                .thenReturn(pagedModel);

        // Act
        Boolean result = parametresHelper.getParametreBoolean(CODI_PARAM);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Llença excepció quan el tipus no és booleà")
    void getParametreBoolean_quanTipusIncorrecte_llencaExcepcio() {
        // Arrange
        Parametre parametre = Parametre.builder().codi(CODI_PARAM).tipus(ParamTipus.TEXT).valor("true").build();
        PagedModel<EntityModel<Parametre>> pagedModel = PagedModel.of(
                Collections.singletonList(EntityModel.of(parametre)),
                new PagedModel.PageMetadata(1, 0, 1));

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(parametreServiceClient.find(any(), anyString(), any(), any(), anyString(), any(), anyString()))
                .thenReturn(pagedModel);

        // Act & Assert
        assertThatThrownBy(() -> parametresHelper.getParametreBoolean(CODI_PARAM))
                .isInstanceOf(ParametreTipusException.class);
    }

    @Test
    @DisplayName("Obté paràmetre enter correctament")
    void getParametreEnter_quanExisteix_retornaValor() {
        // Arrange
        Parametre parametre = Parametre.builder().codi(CODI_PARAM).tipus(ParamTipus.NUMERIC).valor("123").build();
        PagedModel<EntityModel<Parametre>> pagedModel = PagedModel.of(
                Collections.singletonList(EntityModel.of(parametre)),
                new PagedModel.PageMetadata(1, 0, 1));

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(parametreServiceClient.find(any(), anyString(), any(), any(), anyString(), any(), anyString()))
                .thenReturn(pagedModel);

        // Act
        Integer result = parametresHelper.getParametreEnter(CODI_PARAM);

        // Assert
        assertThat(result).isEqualTo(123);
    }
}
